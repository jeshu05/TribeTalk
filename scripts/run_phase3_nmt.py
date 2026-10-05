"""Phase 3: verify the 2-session merged + vocab-pruned NMT stack on device budget.

    python scripts/run_phase3_nmt.py

What was wrong (measured):

  * NmtEngine.kt feeds `encoder_hidden_states` to decoder_with_past_model, but
    that graph has no such input (inputs: input_ids, encoder_attention_mask,
    72 past KV tensors). On device this raises "Invalid input name" and every
    translation silently fell back to the lexicon. The neural path never ran.
  * decoder_model AND decoder_with_past_model were both staged, so a correct
    implementation would hold 194 MB + 186 MB of decoder sessions.
  * 3 x 59.9 MB int8 vocab tables carried all 22 Indic scripts.

This runner proves the replacement stack is behaviour-preserving:

  reference : encoder_model_int8 + decoder_model_int8 + decoder_with_past_model_int8,
              full 122k vocab (3 sessions)
  candidate : encoder_model_int8_pruned + decoder_model_merged_pruned,
              pruned 93k vocab (2 sessions)

Acceptance = identical generated *piece* sequences (hence identical text) on
every test sentence in both directions. Greedy decoding is deterministic, so
piece equality is the correct criterion; a divergence is a real regression.
"""

from __future__ import annotations

import gc
import json
import sys
import time
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from nmt_pipeline import (  # noqa: E402
    BOS_ID,
    EOS_ID,
    MergedNmtPipeline,
    NmtPipeline,
    Vocab,
    detokenize,
    enable_utf8_console,
    tokenize,
)

REPO = Path(__file__).resolve().parent.parent
NMT = REPO / "staged_models" / "nmt"
REPORT = REPO / "scripts" / "phase3_nmt_report.json"

HINDI_TO_SANTALI = [
    "बच्चे स्कूल जाते हैं",
    "यह किताब मेरी है",
    "माँ ने खाना बनाया",
    "पानी ठंडा है",
    "तुम्हारा नाम क्या है",
    "आज मौसम अच्छा है",
]
SANTALI_TO_HINDI = [
    "ᱜᱤᱫᱨᱟᱹ ᱠᱚ ᱤᱥᱠᱩᱞ ᱛᱮ ᱪᱟᱞᱟᱜ ᱠᱟᱱᱟ",
    "ᱱᱚᱶᱟ ᱯᱩᱛᱷᱤ ᱤᱧᱟᱜ ᱠᱟᱱᱟ",
    "ᱫᱟᱜ ᱨᱮᱭᱟᱲ ᱜᱮᱭᱟ",
    "ᱟᱢᱟᱜ ᱧᱩᱛᱩᱢ ᱪᱤ ᱠᱟᱱᱟ",
]


def rss_mb() -> float:
    try:
        import psutil  # type: ignore

        return round(psutil.Process().memory_info().rss / 1048576, 1)
    except Exception:
        return -1.0


def three_session_translate(pipeline: NmtPipeline, src_ids: list[int], max_new_tokens: int = 24):
    """Step the two-decoder reference exactly like MergedNmtPipeline does."""
    input_ids = np.array([src_ids], dtype=np.int64)
    attn = np.ones_like(input_ids)
    hidden = pipeline.enc.run(None, {"input_ids": input_ids, "attention_mask": attn})[0]
    generated: list[int] = []
    past: dict[str, np.ndarray] = {}
    decoder_input = np.array([[BOS_ID]], dtype=np.int64)
    for step in range(max_new_tokens):
        if step == 0:
            outputs = pipeline.dec.run(
                None,
                {
                    "input_ids": decoder_input,
                    "encoder_attention_mask": attn,
                    "encoder_hidden_states": hidden,
                },
            )
            sess = pipeline.dec
        else:
            feed = {"input_ids": decoder_input, "encoder_attention_mask": attn}
            feed.update(past)
            outputs = pipeline.dec_past.run(None, feed)
            sess = pipeline.dec_past
        next_id = int(np.argmax(outputs[0][0, -1, :]))
        if next_id == EOS_ID:
            break
        generated.append(next_id)
        past = {
            name.replace("present.", "past_key_values.", 1): value
            for name, value in zip([o.name for o in sess.get_outputs()][1:], outputs[1:])
            if name.startswith("present.")
        }
        decoder_input = np.array([[next_id]], dtype=np.int64)
    return generated


def main() -> int:
    enable_utf8_console()
    need = [
        "encoder_model_int8.onnx",
        "decoder_model_int8.onnx",
        "decoder_with_past_model_int8.onnx",
        "encoder_model_int8_pruned.onnx",
        "decoder_model_merged_pruned.onnx",
        "vocab.src.tsv",
        "vocab.tgt.tsv",
        "vocab.src.pruned.tsv",
        "vocab.tgt.pruned.tsv",
    ]
    missing = [n for n in need if not (NMT / n).exists()]
    if missing:
        print(f"FAIL: missing {missing} under {NMT}", file=sys.stderr)
        print("Run scripts/prune_nmt_vocab.py first.", file=sys.stderr)
        return 1

    full_src = Vocab.load(NMT / "vocab.src.tsv")
    full_tgt = Vocab.load(NMT / "vocab.tgt.tsv")
    pr_src = Vocab.load(NMT / "vocab.src.pruned.tsv")
    pr_tgt = Vocab.load(NMT / "vocab.tgt.pruned.tsv")

    # The pruned tokenizers must agree with the full ones on surviving pieces:
    # that is what makes renumbering safe. The Kotlin reader un-escapes \n too.
    drift = [
        piece
        for piece in pr_src.piece_to_id
        if full_src.piece_to_id.get(piece) is None
    ]
    print(
        f"[1/4] vocab src {len(pr_src.piece_to_id)}/{len(full_src.piece_to_id)}, "
        f"tgt {len(pr_tgt.piece_to_id)}/{len(full_tgt.piece_to_id)}, drift {len(drift)}",
        flush=True,
    )
    if drift:
        print(f"FAIL: pruned vocab drifted on e.g. {drift[:5]}", file=sys.stderr)
        return 1

    cases = [(t, True) for t in HINDI_TO_SANTALI] + [(t, False) for t in SANTALI_TO_HINDI]

    base = rss_mb()
    print("[2/4] reference stack: 3 sessions, full vocab", flush=True)
    ref = NmtPipeline(
        NMT / "encoder_model_int8.onnx",
        NMT / "decoder_model_int8.onnx",
        NMT / "decoder_with_past_model_int8.onnx",
    )
    ref_rss = rss_mb()

    ref_out = []
    ref_ms = 0.0
    for text, h2s in cases:
        ids = tokenize(text, full_src, h2s)
        t0 = time.time()
        ref_out.append(three_session_translate(ref, ids))
        ref_ms += (time.time() - t0) * 1000
    print(f"      rss delta +{ref_rss - base:.0f} MB, {ref_ms:.0f} ms total", flush=True)

    del ref
    gc.collect()
    mid = rss_mb()

    print("[3/4] candidate stack: 2 sessions, merged decoder, pruned vocab", flush=True)
    cand = MergedNmtPipeline(
        NMT / "encoder_model_int8_pruned.onnx", NMT / "decoder_model_merged_pruned.onnx"
    )
    cand_rss = rss_mb()

    rows = []
    identical = 0
    cand_ms = 0.0
    tokens_total = 0
    print("[4/4] greedy decode equivalence", flush=True)
    for (text, h2s), gen_ref in zip(cases, ref_out):
        ids = tokenize(text, pr_src, h2s)
        t0 = time.time()
        gen, _, _ = cand.translate(ids)
        cand_ms += (time.time() - t0) * 1000
        pieces_ref = [full_tgt.id_to_piece.get(i, "") for i in gen_ref]
        pieces_cand = [pr_tgt.id_to_piece.get(i, "") for i in gen]
        same = pieces_cand == pieces_ref
        identical += int(same)
        tokens_total += max(len(gen_ref), 1)
        direction = "hi->sat" if h2s else "sat->hi"
        rows.append(
            {
                "direction": direction,
                "src": text,
                "pieces_reference": pieces_ref,
                "pieces_candidate": pieces_cand,
                "identical": same,
                "reference_text": detokenize(gen_ref, full_tgt),
                "candidate_text": detokenize(gen, pr_tgt),
            }
        )
        print(
            f"      [{direction}] {text!r} -> {len(gen_ref)} pieces "
            f"{'IDENTICAL' if same else 'DIVERGED'}",
            flush=True,
        )
        if not same:
            print(f"        ref ={pieces_ref}", flush=True)
            print(f"        cand={pieces_cand}", flush=True)

    rate = identical / len(cases)
    report = {
        "vocab": {
            "src_total": len(full_src.piece_to_id),
            "src_kept": len(pr_src.piece_to_id),
            "tgt_total": len(full_tgt.piece_to_id),
            "tgt_kept": len(pr_tgt.piece_to_id),
        },
        "rss_mb": {
            "reference_3_sessions": round(ref_rss - base, 1),
            "candidate_2_sessions_pruned": round(cand_rss - mid, 1),
            "saved": round((ref_rss - base) - (cand_rss - mid), 1),
        },
        "equivalence": {"cases": len(cases), "identical": identical, "rate": round(rate, 4)},
        "speed": {
            "reference_total_ms": round(ref_ms),
            "candidate_total_ms": round(cand_ms),
            "generated_tokens": tokens_total,
            "reference_ms_per_token": round(ref_ms / max(tokens_total, 1), 1),
            "candidate_ms_per_token": round(cand_ms / max(tokens_total, 1), 1),
        },
        "cases": rows,
        "validated_at": time.strftime("%Y-%m-%d %H:%M:%S"),
    }
    REPORT.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"      report -> {REPORT}", flush=True)

    if rate < 1.0:
        print(
            f"FAIL: {len(cases) - identical}/{len(cases)} sentences diverged after merge+prune",
            file=sys.stderr,
        )
        return 1
    print(
        f"VALIDATION PASSED: piece-identical on {identical}/{len(cases)} sentences; "
        f"RSS {report['rss_mb']['reference_3_sessions']} MB -> "
        f"{report['rss_mb']['candidate_2_sessions_pruned']} MB "
        f"(saved {report['rss_mb']['saved']} MB); "
        f"{report['speed']['candidate_ms_per_token']} ms/token.",
        flush=True,
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
