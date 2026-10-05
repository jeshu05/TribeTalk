"""Phase 3 (memory): fuse the two IndicTrans2 decoders into ONE ONNX session.

    pip install "optimum[onnxruntime]"
    python scripts/merge_nmt_decoders.py

Measured problem
----------------
The staged stack (hari31416/indictrans2-indic-indic-dist-320M-ONNX-int8) is
ALREADY weight-only INT8 - every projection is a MatMulInteger initializer, so
quantize_dynamic() is a no-op (verified: 312.6 MB -> 497.3 MB, i.e. +59%, purely
because external data got inlined).

The real cost is DUPLICATION. optimum's `use_past` export splits the decoder:

    decoder_model.onnx            cpu-inits 193.8 MB   (first step)
    decoder_with_past_model.onnx  cpu-inits 184.6 MB   (steps 2..N)

Both graphs carry the SAME 122672-token embedding Gather (60 MB) and the same
tied lm_head (60 MB int8) plus 6 identical transformer layers, and ORT
instantiates initializers PER SESSION. Keeping both resident therefore costs
~378 MB of RAM for ~193 MB of unique weights - on a 2 GB tablet that alone
breaks the budget.

Additionally the shipped NmtEngine.kt feed map for the past session is invalid:
it sends "attention_mask" + "encoder_hidden_states", but that graph requires
"encoder_attention_mask" and has no encoder_hidden_states input at all
(reproduced: "Required inputs (['encoder_attention_mask']) are missing from
input feed"), so on-device NMT could never actually run.

Fix
---
Fuse both graphs into a single `decoder_model_merged.onnx` with an
`use_cache_branch` boolean input (optimum's merge_decoders). One session then
serves step 0 and steps 2..N, halving decoder RAM.

Validation
----------
Both paths are run on the same staged sentences and their greedy token id
sequences must match exactly:
    A) reference two-session loop (scripts/nmt_pipeline.py, what ships today)
    B) merged single-session loop (what NmtEngine.kt will do)
"""

from __future__ import annotations

import json
import sys
import time
from pathlib import Path

import numpy as np
import onnxruntime as ort

sys.path.insert(0, str(Path(__file__).resolve().parent))

from nmt_pipeline import (  # noqa: E402
    BOS_ID,
    EOS_ID,
    NmtPipeline,
    Vocab,
    detokenize,
    enable_utf8_console,
    tokenize,
)
from run_phase3_nmt import HINDI_TO_SANTALI, SANTALI_TO_HINDI  # noqa: E402

REPO = Path(__file__).resolve().parent.parent
NMT = REPO / "staged_models" / "nmt"
MERGED = NMT / "decoder_model_merged.onnx"
REPORT = REPO / "scripts" / "decoder_merge_report.json"
MAX_NEW_TOKENS = 24


def input_mb(path: Path) -> float:
    return path.stat().st_size / 1048576


class MergedPipeline:
    """Encoder + ONE merged decoder session (use_cache_branch)."""

    def __init__(self, enc: Path, merged: Path, threads: int = 2):
        opts = ort.SessionOptions()
        opts.intra_op_num_threads = threads
        opts.inter_op_num_threads = 1
        opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        prov = ["CPUExecutionProvider"]
        self.enc = ort.InferenceSession(str(enc), opts, providers=prov)
        self.dec = ort.InferenceSession(str(merged), opts, providers=prov)
        self.past_names = [i.name for i in self.dec.get_inputs() if i.name.startswith("past_key_values")]
        self.present_names = [o.name for o in self.dec.get_outputs() if o.name.startswith("present")]
        self.needs_use_cache_branch = any(i.name == "use_cache_branch" for i in self.dec.get_inputs())

    def translate(self, src_ids: list[int], max_new_tokens: int = MAX_NEW_TOKENS):
        inp = np.array([src_ids], dtype=np.int64)
        attn = np.ones_like(inp)
        hidden = self.enc.run(None, {"input_ids": inp, "attention_mask": attn})[0]

        generated: list[int] = []
        past: dict[str, np.ndarray] = {}
        cur = np.array([[BOS_ID]], dtype=np.int64)

        for step in range(max_new_tokens):
            feed: dict[str, np.ndarray] = {
                "input_ids": cur,
                "encoder_attention_mask": attn,
                "encoder_hidden_states": hidden,
            }
            for name in self.past_names:
                if name in past:
                    feed[name] = past[name]
                else:
                    # zero-length KV on the first step (past_sequence_length = 0)
                    info = next(i for i in self.dec.get_inputs() if i.name == name)
                    shape = [d if isinstance(d, int) else 1 for d in info.shape]
                    shape[2] = 0
                    feed[name] = np.zeros(shape, dtype=np.float32)
            if self.needs_use_cache_branch:
                feed["use_cache_branch"] = np.array([step > 0], dtype=bool)

            outputs = self.dec.run(None, feed)
            next_id = int(np.argmax(outputs[0][0, -1, :]))
            if next_id == EOS_ID:
                break
            generated.append(next_id)

            past = {}
            for name, value in zip(self.present_names, outputs[1:]):
                past[name.replace("present", "past_key_values", 1)] = value
            cur = np.array([[next_id]], dtype=np.int64)

        return generated


def value_field_count(init) -> int:
    """How many mutually-exclusive TensorProto value fields are populated."""
    return sum(
        1
        for field in (
            init.raw_data, init.float_data, init.int32_data, init.int64_data,
            init.string_data, init.double_data, init.uint64_data, init.external_data,
        )
        if len(field)
    )


def to_raw_bytes(init) -> bytes:
    """Re-materialise an initializer's data as raw little-endian bytes."""
    if init.raw_data:
        return bytes(init.raw_data)
    np_dtype = {
        1: np.float32, 2: np.uint8, 3: np.int8, 4: np.uint16, 5: np.int16,
        6: np.int32, 7: np.int64, 9: np.bool_, 10: np.float16, 11: np.float64,
        12: np.uint32, 13: np.uint64,
    }.get(init.data_type)
    for field_name in ("float_data", "int32_data", "int64_data", "double_data", "uint64_data"):
        values = getattr(init, field_name)
        if len(values):
            if np_dtype is None:
                np_dtype = np.float32 if field_name == "float_data" else np.int64
            return np.asarray(list(values), dtype=np_dtype).tobytes()
    return b""


def repair_tensor(tensor) -> bool:
    """Normalise one TensorProto to a single value field. Returns True if fixed."""
    if value_field_count(tensor) <= 1:
        return False
    tensor.raw_data = to_raw_bytes(tensor)
    del tensor.float_data[:]
    del tensor.int32_data[:]
    del tensor.int64_data[:]
    del tensor.string_data[:]
    del tensor.double_data[:]
    del tensor.uint64_data[:]
    del tensor.external_data[:]
    return True


def repair_initializers(model) -> dict:
    """Make EVERY TensorProto satisfy ONNX's "exactly one value field" rule.

    merge_decoders fuses the per-tensor INT8 scale/zero_point tensors that both
    decoder branches share, and the fused copies end up with SEVERAL populated
    value fields at once (raw_data plus float_data, or raw_data plus a stale
    external_data entry pointing at decoder_shared.onnx.data). They are not all
    in graph.initializer: the merge materialises some as Constant node
    attributes inside the `optimum::if` subgraphs, so the walk has to recurse
    through attributes and subgraphs.
    """
    import onnx

    fixed: list[dict] = []

    def walk(graph):
        for init in graph.initializer:
            if repair_tensor(init):
                fixed.append({"name": init.name, "where": "initializer"})
        for node in graph.node:
            for attr in node.attribute:
                if attr.type == onnx.AttributeProto.TENSOR:
                    if repair_tensor(attr.t):
                        fixed.append({"name": attr.t.name, "where": node.op_type})
                elif attr.type == onnx.AttributeProto.TENSORS:
                    for t in attr.tensors:
                        if repair_tensor(t):
                            fixed.append({"name": t.name, "where": node.op_type})
                elif attr.type == onnx.AttributeProto.GRAPH:
                    walk(attr.g)
                elif attr.type == onnx.AttributeProto.GRAPHS:
                    for g in attr.graphs:
                        walk(g)

    walk(model.graph)
    return {"offenders": fixed, "count": len(fixed)}


def fuse_decoders(dec: Path, dec_past: Path, cache: Path):
    """Run optimum's merge_decoders once, caching the raw protobuf for reruns."""
    import onnx
    from optimum.onnx.graph_transformations import merge_decoders

    if cache.exists():
        merged = onnx.ModelProto()
        merged.ParseFromString(cache.read_bytes())
        return merged, True

    a = onnx.load(str(dec), load_external_data=True)
    b = onnx.load(str(dec_past), load_external_data=True)

    # optimum's check_and_save_model() always runs onnx.checker for models under
    # 2 GB, and the fused scale tensors fail that lint before the repair below
    # can run. Suppress it for the fuse, then apply the same lint to the repaired
    # artifact that actually ships.
    tmp = cache.with_name("_merge_tmp.onnx")
    real_check = onnx.checker.check_model
    onnx.checker.check_model = lambda *args, **kwargs: None
    try:
        merged = merge_decoders(a, b, save_path=str(tmp))
    finally:
        onnx.checker.check_model = real_check
    for stale in tmp.parent.glob("_merge_tmp.onnx*"):
        if stale.is_file():
            try:
                stale.unlink()
            except OSError:
                pass
    cache.write_bytes(merged.SerializeToString())
    return merged, False


def main() -> int:
    import onnx

    enable_utf8_console()

    dec = NMT / "decoder_model.onnx"
    dec_past = NMT / "decoder_with_past_model.onnx"
    enc = NMT / "encoder_model.onnx"
    for p in (dec, dec_past, enc):
        if not p.exists():
            print(f"FAIL: {p} missing", file=sys.stderr)
            return 1

    print("[1/4] fusing decoder_model + decoder_with_past_model -> merged", flush=True)
    t0 = time.time()
    merged, cached = fuse_decoders(dec, dec_past, REPO / "scripts" / "_merged_raw.bin")
    if cached:
        print("      reusing cached fuse", flush=True)

    fix = repair_initializers(merged)
    print(f"      repaired {fix['count']} malformed tensors", flush=True)
    for o in fix["offenders"][:4]:
        print(f"        {o['name']}  in {o['where']}", flush=True)
    onnx.checker.check_model(merged)  # real lint on the shippable artifact
    onnx.save_model(merged, str(MERGED))
    print(
        f"      merged graph: {len(merged.graph.node)} nodes, "
        f"{input_mb(MERGED):.1f} MB on disk, onnx.checker PASSED "
        f"in {time.time() - t0:.1f}s",
        flush=True,
    )

    two_session_mb = 193.8 + 184.6
    print(
        f"      decoder RAM: {two_session_mb:.1f} MB (2 sessions) -> "
        f"~{input_mb(MERGED):.1f} MB (1 session), "
        f"saves ~{two_session_mb - input_mb(MERGED):.0f} MB",
        flush=True,
    )

    print("[2/4] loading reference + merged pipelines", flush=True)
    ref = NmtPipeline(enc, dec, dec_past)
    mrg = MergedPipeline(enc, MERGED)
    print(f"      merged inputs: {len(mrg.past_names)} past + use_cache_branch"
          f"={mrg.needs_use_cache_branch}", flush=True)

    print("[3/4] verifying merged session serves BOTH step 0 and steps 2..N", flush=True)
    src_vocab = Vocab.load(NMT / "vocab.src.tsv")
    tgt_vocab = Vocab.load(NMT / "vocab.tgt.tsv")
    cases = [(t, True) for t in HINDI_TO_SANTALI] + [(t, False) for t in SANTALI_TO_HINDI]

    per_case = []
    exact = 0
    n_tok = 0
    merged_ms = 0.0
    for text, h2s in cases:
        ids = tokenize(text, src_vocab, h2s)
        a, _, _ = ref.translate(ids)
        t0 = time.time()
        b = mrg.translate(ids)
        merged_ms += (time.time() - t0) * 1000
        n_tok += max(len(b), 1)
        same = a == b
        exact += int(same)
        per_case.append(
            {
                "direction": "hi->sat" if h2s else "sat->hi",
                "src": text,
                "ref_tokens": len(a),
                "merged_tokens": len(b),
                "identical": same,
                "merged_text": detokenize(b, tgt_vocab),
            }
        )
        print(
            f"      [{'hi->sat' if h2s else 'sat->hi'}] {text!r} -> "
            f"{len(b)} tok {'IDENTICAL' if same else 'DIVERGED'}",
            flush=True,
        )
        if not same:
            print(f"        ref   ={a}\n        merged={b}", flush=True)

    rate = exact / len(cases)
    report = {
        "merged_file": MERGED.name,
        "merged_mb": round(input_mb(MERGED), 1),
        "encoder_mb": round(input_mb(enc) + input_mb(NMT / "encoder_model.onnx.data"), 1),
        "nodes": len(merged.graph.node),
        "decoder_ram_before_mb": round(two_session_mb, 1),
        "decoder_ram_after_mb": round(input_mb(MERGED), 1),
        "decoder_ram_saved_mb": round(two_session_mb - input_mb(MERGED), 1),
        "equivalence": {
            "cases": len(cases),
            "identical_token_sequences": exact,
            "exact_match_rate": round(rate, 4),
        },
        "speed": {
            "merged_total_ms": round(merged_ms),
            "merged_ms_per_token": round(merged_ms / max(n_tok, 1), 1),
        },
        "cases": per_case,
        "validated_at": time.strftime("%Y-%m-%d %H:%M:%S"),
    }
    REPORT.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"[4/4] report -> {REPORT}", flush=True)

    if rate < 1.0:
        print(
            f"FAIL: merged decoder diverged on {len(cases) - exact}/{len(cases)} cases",
            file=sys.stderr,
        )
        return 1
    print(
        f"VALIDATION PASSED: merged decoder is token-identical on {exact}/{len(cases)} "
        f"sentences and cuts decoder RAM by {report['decoder_ram_saved_mb']} MB "
        f"(translate-mode total ~"
        f"{report['encoder_mb'] + report['decoder_ram_after_mb']:.0f} MB).",
        flush=True,
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())