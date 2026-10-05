"""Reference implementation of the on-device IndicTrans2 decode loop.

This module mirrors, step for step, what ``app/.../core/NmtEngine.kt`` must do
on the tablet, so quantization can be validated against the *shipping*
algorithm rather than against a convenient Python substitute.

Graph contract (measured on staged_models/nmt, optimum "use_past" export):

    encoder_model.onnx
        in : input_ids, attention_mask
        out: last_hidden_state                     [1, S, 512]

    decoder_model.onnx                             <- FIRST step only
        in : input_ids, encoder_attention_mask, encoder_hidden_states
        out: logits, present.{L}.decoder.{key,value}, present.{L}.encoder.{key,value}

    decoder_with_past_model.onnx                   <- steps 2..N
        in : input_ids, encoder_attention_mask,
             past_key_values.{L}.{decoder,encoder}.{key,value}
        out: logits, present.{L}.decoder.{key,value}, present.{L}.encoder.{key,value}
        (encoder KV outputs are pass-through copies of the inputs)

L is the decoder layer index (0..17); each head group is [1, 8, T, 64].

Feeding ``encoder_hidden_states`` to ``decoder_with_past`` raises
"Invalid input name" at runtime, which is why both decoder graphs must stay
resident: the first step seeds the encoder cross-attention KV cache.
"""

from __future__ import annotations

import sys
from dataclasses import dataclass
from pathlib import Path

import numpy as np
import onnxruntime as ort

BOS_ID = 2  # config.json decoder_start_token_id (NOT pad=1)
EOS_ID = 2  # config.json eos_token_id
MAX_PIECE = 20


def enable_utf8_console() -> None:
    """Windows consoles default to cp1252 and raise on Devanagari/Ol Chiki text."""
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8", errors="replace")
        except (AttributeError, ValueError):
            pass


@dataclass
class Vocab:
    piece_to_id: dict[str, int]
    id_to_piece: dict[int, str]

    @classmethod
    def load(cls, path: Path) -> "Vocab":
        piece_to_id: dict[str, int] = {}
        id_to_piece: dict[int, str] = {}
        with open(path, encoding="utf-8") as fh:
            for line in fh:
                line = line.rstrip("\n")
                if not line:
                    continue
                idx = line.rfind("\t")
                if idx <= 0:
                    continue
                piece = line[:idx]
                try:
                    pid = int(line[idx + 1 :])
                except ValueError:
                    continue
                piece_to_id[piece] = pid
                id_to_piece[pid] = piece
        return cls(piece_to_id, id_to_piece)


def tokenize(text: str, vocab: Vocab, hindi_to_santali: bool) -> list[int]:
    """Longest-piece-first tokenization identical to NmtEngine.tokenize().

    IndicTrans2 is conditioned on FLORES-200 tags and the REAL tokenizer input is
    "<src_tag> <tgt_tag> <sentence>" — measured with IndicProcessor.preprocess_batch
    plus tokenizer_src.json in scripts/probe_nmt_tokenization.py:

        "hin_Deva sat_Olck बच्चे स्कूल जाते हैं" -> [8, 29925, 2662, 2493, 1526, 43, 2]

    Both tags are therefore prepended (they are plain vocabulary entries, so the
    on-device engine can add the two ids directly instead of tokenizing the text).
    """
    ids: list[int] = []
    src_tag = "hin_Deva" if hindi_to_santali else "sat_Olck"
    tgt_tag = "sat_Olck" if hindi_to_santali else "hin_Deva"
    for tag in (src_tag, tgt_tag):
        tag_id = vocab.piece_to_id.get(tag)
        if tag_id is None:
            tag_id = vocab.piece_to_id.get(SPACE_MARKER + tag)
        if tag_id is None:
            raise KeyError(f"language tag {tag!r} missing from NMT vocab")
        ids.append(tag_id)
    norm = text
    for word in norm.split():
        if not word:
            continue
        marked = chr(0x2581) + word
        pos = 0
        while pos < len(marked):
            match_id = None
            match_len = 1
            for end in range(min(len(marked), pos + MAX_PIECE), pos, -1):
                pid = vocab.piece_to_id.get(marked[pos:end])
                if pid is not None:
                    match_id = pid
                    match_len = end - pos
                    break
            if match_id is not None:
                ids.append(match_id)
            pos += match_len
    eos = vocab.piece_to_id.get("</s>")
    if eos is not None:
        ids.append(eos)
    return ids


def detokenize(ids: list[int], vocab: Vocab) -> str:
    if not ids:
        return ""
    pieces = [vocab.id_to_piece.get(i, "") for i in ids]
    return "".join(pieces).replace("\u2581", " ").strip()


import time


class NmtPipeline:
    """Encoder + first-step decoder + KV-cached decoder (three ONNX sessions)."""

    def __init__(self, enc_path: Path, dec_path: Path, dec_past_path: Path, threads: int = 2):
        opts = ort.SessionOptions()
        opts.intra_op_num_threads = threads
        opts.inter_op_num_threads = 1
        opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        prov = ["CPUExecutionProvider"]
        self.enc = ort.InferenceSession(str(enc_path), opts, providers=prov)
        self.dec = ort.InferenceSession(str(dec_path), opts, providers=prov)
        self.dec_past = ort.InferenceSession(str(dec_past_path), opts, providers=prov)

    def translate(
        self, src_ids: list[int], max_new_tokens: int = 24
    ) -> tuple[list[int], float, float]:
        """Returns (generated_ids, encoder_seconds, decoder_seconds)."""
        input_ids = np.array([src_ids], dtype=np.int64)
        attn = np.ones_like(input_ids)

        t0 = time.time()
        hidden = self.enc.run(None, {"input_ids": input_ids, "attention_mask": attn})[0]
        t_enc = time.time() - t0

        generated: list[int] = []
        past: dict[str, np.ndarray] = {}
        decoder_input = np.array([[BOS_ID]], dtype=np.int64)
        t_dec = 0.0



class MergedNmtPipeline:
    """SHIPPING design: encoder + ONE merged decoder session.

    optimum's merge_decoders fuses decoder_model and decoder_with_past_model
    into a single graph whose `use_cache_branch` bool input selects the branch:
      * False -> first step, consumes `encoder_hidden_states`
      * True  -> later steps, consume the past KV inputs

    That halves decoder RAM versus two resident decoder sessions (194 MB
    instead of 194 + 186 MB), which is exactly what the 2 GB budget needs. All
    KV inputs are required in both branches (the unused branch ignores them),
    so the first step feeds zero-length past tensors.
    """

    def __init__(self, enc_path: Path, merged_dec_path: Path, threads: int = 2):
        opts = ort.SessionOptions()
        opts.intra_op_num_threads = threads
        opts.inter_op_num_threads = 1
        opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        prov = ["CPUExecutionProvider"]
        self.enc = ort.InferenceSession(str(enc_path), opts, providers=prov)
        self.dec = ort.InferenceSession(str(merged_dec_path), opts, providers=prov)
        inputs = self.dec.get_inputs()
        self.past_names = [i.name for i in inputs if i.name.startswith("past_key_values")]
        self.past_dtypes = {
            i.name: (np.float16 if "float16" in i.type else np.float32)
            for i in inputs
            if i.name.startswith("past_key_values")
        }
        self.past_shapes = {
            i.name: [d if isinstance(d, int) else 1 for d in i.shape]
            for i in inputs
            if i.name.startswith("past_key_values")
        }
        self.present_names = [o.name for o in self.dec.get_outputs()][1:]

    def empty_past(self) -> dict:
        feed = {}
        for name in self.past_names:
            shape = list(self.past_shapes[name])
            shape[2] = 0  # past_sequence_length
            feed[name] = np.zeros(shape, dtype=self.past_dtypes[name])
        return feed

    def translate(self, src_ids: list[int], max_new_tokens: int = 24):
        """Returns (generated_ids, encoder_seconds, decoder_seconds)."""
        input_ids = np.array([src_ids], dtype=np.int64)
        attn = np.ones_like(input_ids)

        t0 = time.time()
        hidden = self.enc.run(None, {"input_ids": input_ids, "attention_mask": attn})[0]
        t_enc = time.time() - t0

        generated: list[int] = []
        past = self.empty_past()
        decoder_input = np.array([[BOS_ID]], dtype=np.int64)
        use_cache = np.zeros((1,), dtype=bool)
        t_dec = 0.0

        for _ in range(max_new_tokens):
            feed = {
                "input_ids": decoder_input,
                "encoder_attention_mask": attn,
                "encoder_hidden_states": hidden,
                "use_cache_branch": use_cache,
            }
            feed.update(past)
            t1 = time.time()
            outputs = self.dec.run(None, feed)
            t_dec += time.time() - t1

            next_id = int(np.argmax(outputs[0][0, -1, :]))
            if next_id == EOS_ID:
                break
            generated.append(next_id)

            past = {}
            for name, value in zip(self.present_names, outputs[1:]):
                past[name.replace("present.", "past_key_values.", 1)] = value
            decoder_input = np.array([[next_id]], dtype=np.int64)
            use_cache = np.ones((1,), dtype=bool)

        return generated, t_enc, t_dec
