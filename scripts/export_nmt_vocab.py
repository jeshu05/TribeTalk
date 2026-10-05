"""Export compact vocab TSVs for the on-device NMT engine.

Source of truth is dict.SRC.json / dict.TGT.json (122706 / 122672 entries), the
dictionaries IndicTransTokenizer actually builds from. An earlier version of this
script read model.SRC / model.TGT through the sentencepiece pip package, which
produced a subtly different 128000-entry table: sentencepiece adds trailing
empty pieces, and the resulting mapping shifted every id after the first gap
(measured: dict.SRC.json id 40 == "▁" vs model.SRC id 219 == "▁"). Ids must
match the graphs' embedding rows exactly, so the JSON dictionaries are used.

Writes vocab.src.tsv / vocab.tgt.tsv as "piece<TAB>id" lines, which is what
NmtEngine.loadVocab() parses on device.
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

STAGED = Path(__file__).resolve().parent.parent / "staged_models" / "nmt"
MAX_ENTRIES = 200_000

# FLORES-200 language tags the IndicTrans2 graphs were exported with. They live
# in the SOURCE dictionary (hin_Deva=8, sat_Olck=29925) and must be prefixed to
# the source text, or the decoder cannot tell which language it is reading.
REQUIRED_SRC_PIECES = ["hin_Deva", "sat_Olck"]


def export(dict_path: Path, dst_path: Path) -> dict:
    data = json.loads(dict_path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise TypeError(f"{dict_path.name} is not a piece->id mapping")

    with open(dst_path, "w", encoding="utf-8", newline="\n") as fh:
        for idx, (piece, pid) in enumerate(
            sorted(data.items(), key=lambda kv: kv[1])
        ):
            if idx >= MAX_ENTRIES:
                break
            # \n inside a piece would break the line-oriented TSV format;
            # NmtEngine.loadVocab reverses this escaping.
            safe = piece.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t")
            fh.write(f"{safe}\t{pid}\n")

    missing = (
        [p for p in REQUIRED_SRC_PIECES if p not in data]
        if dict_path.name.startswith("dict.SRC")
        else []
    )
    return {
        "entries": len(data),
        "path": dst_path.name,
        "bytes": dst_path.stat().st_size,
        "missing_tags": missing,
        "checked_tags": dict_path.name.startswith("dict.SRC"),
    }


def main() -> int:
    failed = False
    for dict_name, dst_name in [("dict.SRC.json", "vocab.src.tsv"), ("dict.TGT.json", "vocab.tgt.tsv")]:
        src = STAGED / dict_name
        if not src.exists():
            print(f"FAIL {dict_name}: not found under {STAGED}")
            failed = True
            continue
        info = export(src, STAGED / dst_name)
        print(
            f"exported {dict_name} -> {info['path']} "
            f"({info['entries']} entries, {info['bytes'] / 1048576:.2f} MB)"
        )
        if not info["checked_tags"]:
            print("  (target dictionary holds no source language tags)")
            continue
        if info["missing_tags"]:
            print(f"  FAIL missing language tags: {info['missing_tags']}")
            failed = True
        else:
            print(f"  language tags present: {', '.join(REQUIRED_SRC_PIECES)}")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
