"""Phase 3 (final lever): prune the IndicTrans2 vocabulary to the scripts the
device actually speaks, and PROVE the pruned graphs compute the same thing.

    python scripts/run_phase3_prune.py            # measure + prune + validate
    python scripts/run_phase3_prune.py --measure  # report only, no writes

Measured problem (scripts/nmt_weight_layout.json):

    encoder_model            114.9 MB of weights, 60.0 MB (52%) is the
                             [122706, 512] INT8 embedding Gather
    decoder (merged)         193.8 MB of weights, 119.8 MB (62%) is
                             decoder.embed_tokens [122672, 512] + lm_head [512, 122672]

`quantize_dynamic` cannot touch any of it: the graphs are already weight-only
INT8 (re-running it changed the stack by 2%, see scripts/q3.log), and ORT's
dynamic quantizer deliberately leaves Gather/embedding tables alone.  The rows
exist only because one checkpoint serves all 22 scheduled Indian languages.
Hindi (Devanagari) and Santali (Ol Chiki) plus ASCII code-switching need a
small fraction of them, and every dead row costs RAM on a 2 GB tablet.

Pruning keeps special tokens, the FLORES-200 tags, ASCII, Devanagari(+Extended),
Ol Chiki, combining marks and general punctuation.  Row contents are unchanged;
only the row set changes, so the pruned graph is the same computation with a
smaller matrix.  Acceptance is therefore exact greedy-decode equality on both
translation directions, evaluated through the SAME merged-decoder protocol the
Kotlin engine uses (`use_cache_branch` False on the first step, True after).
"""

from __future__ import annotations

import argparse
import json
import sys
import time
from pathlib import Path

import numpy as np
import onnxruntime as ort

sys.path.insert(0, str(Path(__file__).resolve().parent))

from nmt_pipeline import (  # noqa: E402
    EOS_ID,
    Vocab,
    enable_utf8_console,
    tokenize,
)
from prune_nmt_vocab import keep_ids, piece_is_kept  # noqa: E402

REPO = Path(__file__).resolve().parent.parent
NMT = REPO / "staged_models" / "nmt"
REPORT = REPO / "scripts" / "quantization_report_nmt_prune.json"

BOS_ID = 2  # decoder_start_token_id
DECODER_START = BOS_ID

CASES = [
    ("hi->sat", "बच्चे स्कूल जाते हैं", True),
    ("hi->sat", "यह किताब मेरी है", True),
    ("hi->sat", "माँ ने खाना बनाया", True),
    ("hi->sat", "पानी ठंडा है", True),
    ("hi->sat", "तुम्हारा नाम क्या है", True),
    ("hi->sat", "सूरज पूरब से निकलता है", True),
    ("sat->hi", "ᱜᱤᱫᱨᱟᱹ ᱠᱚ ᱤᱥᱠᱩᱞ ᱛᱮ ᱪᱟᱞᱟᱜ ᱠᱟᱱᱟ", False),
    ("sat->hi", "ᱱᱚᱶᱟ ᱯᱩᱛᱷᱤ ᱤᱧᱟᱜ ᱠᱟᱱᱟ", False),
    ("sat->hi", "ᱫᱟᱜ ᱨᱮᱭᱟᱲ ᱜᱮᱭᱟ", False),
class MergedPipeline:
    """Encoder + optimum merged decoder driven by `use_cache_branch`."""

    def __init__(self, enc_path: Path, dec_path: Path, threads: int = 2):
        opts = ort.SessionOptions()
        opts.intra_op_num_threads = threads
        opts.inter_op_num_threads = 1
        opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        prov = ["CPUExecutionProvider"]
        self.enc = ort.InferenceSession(str(enc_path), opts, providers=prov)
        self.dec = ort.InferenceSession(str(dec_path), opts, providers=prov)
        dtypes = {
            "tensor(float)": np.float32,
            "tensor(float16)": np.float16,
            "tensor(double)": np.float64,
        }
        self.past_spec = []
        for i in self.dec.get_inputs():
            if not i.name.startswith("past_key_values."):
                continue
            shape = [d if isinstance(d, int) else 1 for d in i.shape]
            shape[2] = 0
            self.past_spec.append((i.name, np.zeros(shape, dtype=dtypes.get(i.type, np.float32))))
        if not self.past_spec:
            raise RuntimeError("merged decoder exposes no past_key_values inputs")

    def translate(self, src_ids: list[int], max_new_tokens: int = 24) -> tuple[list[int], float]:
        attn = np.ones((1, len(src_ids)), dtype=np.int64)
        t0 = time.time()
        hidden = self.enc.run(
            None, {"input_ids": np.array([src_ids], dtype=np.int64), "attention_mask": attn}
        )[0]

        generated: list[int] = []
        past = {name: buf for name, buf in self.past_spec}
        decoder_input = np.array([[DECODER_START]], dtype=np.int64)
        for step in range(max_new_tokens):
            feed = {
                "input_ids": decoder_input,
                "encoder_attention_mask": attn,
                "encoder_hidden_states": hidden,
                "use_cache_branch": np.array([step > 0], dtype=bool),
            }
            feed.update(past)
            outputs = self.dec.run(None, feed)
            next_id = int(np.argmax(outputs[0][0, -1, :]))
            if next_id == EOS_ID:
                break
            generated.append(next_id)
            past = {
                name.replace("present.", "past_key_values.", 1): value
                for name, value in zip([o.name for o in self.dec.get_outputs()], outputs)
                if name.startswith("present.")
            }
            decoder_input = np.array([[next_id]], dtype=np.int64)
        return generated, time.time() - t0


def walk_initializers(graph, onnx, out: list):
    """Yield every 2-D initializer sized by the vocabulary, subgraphs included."""
    for init in graph.initializer:
        dims = [int(d) for d in init.dims]
        if len(dims) == 2 and (dims[0] == init_vocab_hint() or dims[1] == init_vocab_hint()):
            out.append((init, "rows" if dims[0] == init_vocab_hint() else "cols"))
        elif len(dims) == 1 and dims[0] == init_vocab_hint():
            out.append((init, "WARN_1D"))
    for node in graph.node:
        for attr in node.attribute:
            if attr.type == onnx.AttributeProto.GRAPH:
                walk_initializers(attr.g, onnx, out)
            elif attr.type == onnx.AttributeProto.GRAPHS:
                for sub in attr.graphs:
                    walk_initializers(sub, onnx, out)


_VOCAB_HINTS = [122706, 122672]


def init_vocab_hint() -> int:
    """Sentinel that makes the walker match either vocabulary size."""
    return _VOCAB_HINTS[0] if len(_VOCAB_HINTS) == 1 else _VOCAB_HINTS[_hint_idx()]


def _hint_idx() -> int:
    return _VOCAB_HINTS_CURRENT[0]


_VOCAB_HINTS_CURRENT = [0]

