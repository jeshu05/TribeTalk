"""Prune the IndicTrans2 vocab tables down to the scripts TribeTalk needs.

    python scripts/prune_nmt_vocab.py

IndicTrans2-indic-indic-dist-320M ships a 122,706-piece source vocab covering
all 22 scheduled Indic scripts. TribeTalk only ever sees Devanagari (Hindi)
and Ol Chiki (Santali), so ~38k pieces of Tamil/Telugu/Bengali/Gurmukhi/...
are dead weight inside three 59.9 MB int8 tables:

    encoder embed   [122706, 512]
    decoder embed   [122672, 512]
    decoder lm_head [512, 122672]

Slicing the three tables to the kept pieces and renumbering ids 0..N-1 cuts
NMT RAM by the same fraction, and behaviour is unchanged because every dropped
piece was unreachable from Hindi/Ol Chiki input text (asserted by the
token-identical equivalence test in scripts/run_phase3_nmt.py).

The rewritten `vocab.src.tsv` / `vocab.tgt.tsv` carry the NEW ids, so the
on-device engine needs no change beyond re-reading the TSVs.
"""

from __future__ import annotations

import json
import re
import sys
import time
from pathlib import Path

import numpy as np

REPO = Path(__file__).resolve().parent.parent
NMT = REPO / "staged_models" / "nmt"
REPORT = REPO / "scripts" / "prune_report.json"

SPACE_MARKER = "\u2581"

# Script coverage that actually reaches this app, plus ASCII for numerals,
# punctuation and Latin abbreviations. Anything outside is pruned.
ALLOWED_RANGES = [
    (0x000A, 0x000A),  # newline piece
    (0x0020, 0x007E),  # ASCII printable (digits, punctuation, Latin)
    (0x00A0, 0x00BF),  # NBSP / inverted punctuation
    (0x2000, 0x206F),  # general punctuation, ZWJ/ZWNJ, currency
    (0x0900, 0x097F),  # Devanagari (Hindi)
    (0xA8E0, 0xA8FF),  # Devanagari Extended (Vedic accents)
    (0x1CD0, 0x1CFF),  # Vedic Extensions
    (0x1C50, 0x1C7F),  # Ol Chiki (Santali)
]

TAG_RE = re.compile(r"__(?:hi|sat|dnt|eng|hin|snt)__|<[a-z_]+>")


def piece_is_kept(piece: str) -> bool:
    if piece in {"<s>", "<pad>", "</s>", "<unk>"}:
        return True
    if TAG_RE.search(piece):
        return True
    core = piece.replace(SPACE_MARKER, "")
    if not core:
        return True
    for ch in core:
        if not any(lo <= ord(ch) <= hi for lo, hi in ALLOWED_RANGES):
            return False
    return True


def keep_ids(dict_path: Path) -> tuple[list[int], list[str]]:
    """Returns (old ids to keep, ascending) and their pieces."""
    data = json.loads(dict_path.read_text(encoding="utf-8"))
    pairs = sorted(((int(idx), piece) for piece, idx in data.items()))
    kept = [(old, piece) for old, piece in pairs if piece_is_kept(piece)]
    return [old for old, _ in kept], [piece for _, piece in kept]


def find_vocab_tables(model, vocab_size: int) -> dict:
    """Locate vocab-sized weights: the embedding table and the lm_head."""
    found: dict[str, tuple[str, str]] = {}
    for init in model.graph.initializer:
        dims = [int(d) for d in init.dims]
        if len(dims) != 2:
            continue
        if dims[0] == vocab_size:
            found["embed"] = (init.name, "rows")
        elif dims[1] == vocab_size:
            found["lm_head"] = (init.name, "cols")
    return found


def prune_table(model, name: str, axis: str, keep: np.ndarray) -> dict:
    from onnx.numpy_helper import from_array, to_array

    for idx, init in enumerate(model.graph.initializer):
        if init.name != name:
            continue
        arr = to_array(init)
        before = list(arr.shape)
        sliced = arr[keep] if axis == "rows" else arr[:, keep]
        model.graph.initializer[idx].CopyFrom(
            from_array(np.ascontiguousarray(sliced), name=init.name)
        )
        return {"name": name, "axis": axis, "before": before, "after": list(sliced.shape)}
    raise KeyError(f"initializer {name!r} not found")


def fix_merged_vocab_metadata(model, old_vocab: int, new_vocab: int, keep: np.ndarray) -> dict:
    """Rewrite every lingering 122672 vocab dim in the optimum-merged decoder.

    prune_table slices the two 2-D vocab tables (embed rows, lm_head cols),
    but the INT8 lm_head also scales by 1-D [vocab] vectors and the two
    If-subgraphs carry value_info / output shapes stamped with the OLD vocab
    dim. ORT type-checks the subgraphs on load, so a half-pruned graph fails
    with "Incompatible dimensions" at the lm_head Mul. This walks the top
    graph AND both If branches and rewrites:

      * 1-D initializers of length old_vocab -> keep-selected slice
      * every value_info / output / subgraph-output dim equal to old_vocab
    """
    from onnx.numpy_helper import from_array, to_array

    fixed_inits: list[str] = []
    fixed_shapes = 0

    def fix_dims(dims) -> bool:
        changed = False
        for d in dims:
            if d.dim_value == old_vocab:
                d.dim_value = new_vocab
                nonlocal fixed_shapes
                fixed_shapes += 1
                changed = True
        return changed

    def fix_value_infos(infos) -> None:
        for vi in infos:
            tt = vi.type.tensor_type
            if tt.HasField("shape"):
                fix_dims(tt.shape.dim)

    def fix_graph(graph) -> None:
        for idx, init in enumerate(graph.initializer):
            dims = [int(d) for d in init.dims]
            if len(dims) == 1 and dims[0] == old_vocab:
                arr = to_array(init)[keep]
                graph.initializer[idx].CopyFrom(
                    from_array(np.ascontiguousarray(arr), name=init.name)
                )
                fixed_inits.append(init.name)
        fix_value_infos(graph.value_info)
        fix_value_infos(graph.output)
        for node in graph.node:
            for attr in node.attribute:
                if attr.type == 5 and attr.g is not None:  # GRAPH
                    fix_graph(attr.g)
                elif attr.type == 6:  # GRAPHS
                    for g in attr.graphs:
                        fix_graph(g)

    fix_graph(model.graph)
    return {"fixed_1d_initializers": fixed_inits, "fixed_shape_dims": fixed_shapes}


def prune_graph(graph: Path, out: Path, keep: np.ndarray, vocab_size: int, expect: str) -> list[dict]:
    import onnx

    t0 = time.time()
    model = onnx.load(str(graph), load_external_data=True)
    new_vocab = int(keep.shape[0])
    tables = find_vocab_tables(model, vocab_size)
    if expect == "embed" and "embed" not in tables:
        raise RuntimeError(f"{graph.name}: no embedding table of size {vocab_size}")
    if expect == "both" and not {"embed", "lm_head"} <= set(tables):
        raise RuntimeError(f"{graph.name}: need embed+lm_head, found {sorted(tables)}")
    results = []
    for role in (["embed"] if expect == "embed" else ["embed", "lm_head"]):
        name, axis = tables[role]
        info = prune_table(model, name, axis, keep)
        info["role"] = role
        info["graph"] = graph.name
        results.append(info)
    if expect == "both":
        # The optimum-merged decoder carries the 122672 vocab dim in three more
        # places prune_table cannot see: the lm_head 1-D scale/zero-point vectors
        # and the shape metadata the If-subgraphs type-check against. Rewrite all
        # of them so shape inference agrees with the sliced tables.
        fix_merged_vocab_metadata(model, old_vocab=vocab_size, new_vocab=new_vocab, keep=keep)
    onnx.save_model(model, str(out))
    for r in results:
        r["seconds"] = round(time.time() - t0, 1)
        r["out_mb"] = round(out.stat().st_size / 1048576, 1)
    return results


def write_vocab_tsv(path: Path, pieces: list[str]) -> int:
    """Escape exactly the three characters the Kotlin reader un-escapes."""
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        for new_id, piece in enumerate(pieces):
            safe = piece.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t")
            fh.write(f"{safe}\t{new_id}\n")
    return len(pieces)


def main() -> int:
    src_keep, src_pieces = keep_ids(NMT / "dict.SRC.json")
    tgt_keep, tgt_pieces = keep_ids(NMT / "dict.TGT.json")
    src_idx = np.array(src_keep, dtype=np.int64)
    tgt_idx = np.array(tgt_keep, dtype=np.int64)

    print(f"[1/3] keep sets: src {len(src_keep)}/122706, tgt {len(tgt_keep)}/122672", flush=True)

    print("[2/3] slicing vocab tables", flush=True)
    tables = []
    for graph, out, keep, vsize, expect in [
        ("encoder_model_int8.onnx", "encoder_model_int8_pruned.onnx", src_idx, 122706, "embed"),
        ("decoder_model_merged.onnx", "decoder_model_merged_pruned.onnx", tgt_idx, 122672, "both"),
    ]:
        g, o = NMT / graph, NMT / out
        if not g.exists():
            print(f"FAIL: {g} missing", file=sys.stderr)
            return 1
        info = prune_graph(g, o, keep, vsize, expect)
        tables.extend(info)
        for r in info:
            print(
                f"      {r['graph']} {r['role']}: {r['before']} -> {r['after']} "
                f"({r['out_mb']} MB, {r['seconds']}s)",
                flush=True,
            )

    print("[3/3] writing vocab TSVs", flush=True)
    n_src = write_vocab_tsv(NMT / "vocab.src.pruned.tsv", src_pieces)
    n_tgt = write_vocab_tsv(NMT / "vocab.tgt.pruned.tsv", tgt_pieces)
    print(f"      vocab.src.pruned.tsv {n_src} rows, vocab.tgt.pruned.tsv {n_tgt} rows", flush=True)

    enc_before = (NMT / "encoder_model_int8.onnx").stat().st_size
    enc_after = (NMT / "encoder_model_int8_pruned.onnx").stat().st_size
    tsv_before = sum(
        (NMT / n).stat().st_size
        for n in ("vocab.src.tsv", "vocab.tgt.tsv")
        if (NMT / n).exists()
    )
    tsv_after = (NMT / "vocab.src.pruned.tsv").stat().st_size + (
        NMT / "vocab.tgt.pruned.tsv"
    ).stat().st_size
    report = {
        "keep": {
            "src": {"total": 122706, "kept": len(src_keep)},
            "tgt": {"total": 122672, "kept": len(tgt_keep)},
        },
        "tables": tables,
        "encoder_mb": {
            "before": round(enc_before / 1048576, 1),
            "after": round(enc_after / 1048576, 1),
        },
        "vocab_tsv_mb": {
            "before": round(tsv_before / 1048576, 1),
            "after": round(tsv_after / 1048576, 1),
        },
        "validated_at": time.strftime("%Y-%m-%d %H:%M:%S"),
    }
    REPORT.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8")
    print(
        f"DONE: encoder {report['encoder_mb']['before']} -> {report['encoder_mb']['after']} MB, "
        f"vocab tsv {report['vocab_tsv_mb']['before']} -> {report['vocab_tsv_mb']['after']} MB, "
        f"report -> {REPORT}",
        flush=True,
    )
    print("NEXT: python scripts/run_phase3_nmt.py --verify-pruned", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
