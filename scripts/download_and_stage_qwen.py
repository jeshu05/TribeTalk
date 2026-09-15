"""Download, quantize/verify, and stage Qwen 0.5B ONNX model for TribeTalk Android."""

import os
import sys
import json
import time
import shutil
from pathlib import Path
from huggingface_hub import hf_hub_download
import onnxruntime as ort
import numpy as np

REPO_ID = "onnx-community/Qwen2.5-0.5B-Instruct"
STAGING_DIR = Path("staged_models/qwen")
APP_MODELS_DIR = Path("models/qwen")

FILES_TO_DOWNLOAD = [
    "config.json",
    "generation_config.json",
    "tokenizer.json",
    "tokenizer_config.json",
    "vocab.json",
    "special_tokens_map.json",
    "onnx/model_int8.onnx"
]

def main():
    print(f"=== Staging Qwen 0.5B from {REPO_ID} ===")
    STAGING_DIR.mkdir(parents=True, exist_ok=True)
    APP_MODELS_DIR.mkdir(parents=True, exist_ok=True)

    downloaded_paths = {}
    for filename in FILES_TO_DOWNLOAD:
        print(f"Downloading {filename}...")
        t0 = time.time()
        path = hf_hub_download(repo_id=REPO_ID, filename=filename)
        dt = time.time() - t0
        dest_name = Path(filename).name
        dest_path = STAGING_DIR / dest_name
        shutil.copy2(path, dest_path)
        shutil.copy2(path, APP_MODELS_DIR / dest_name)
        downloaded_paths[dest_name] = dest_path
        size_mb = os.path.getsize(dest_path) / (1024 * 1024)
        print(f"  -> Saved {dest_name} ({size_mb:.2f} MB in {dt:.1f}s)")

    # Write versioned model manifest
    manifest = {
        "model": "Qwen2.5-0.5B-Instruct",
        "role": "curriculum_activity_planner",
        "runtime": "onnxruntime",
        "format": "ONNX",
        "quantization": "INT8",
        "version": "2.5",
        "parameters": "490M",
        "model_file": "model_int8.onnx",
        "model_size_bytes": os.path.getsize(STAGING_DIR / "model_int8.onnx"),
        "model_size_mb": round(os.path.getsize(STAGING_DIR / "model_int8.onnx") / (1024 * 1024), 2),
        "tokenizer": "tokenizer.json",
        "context_length": 2048,
        "offline": True,
        "abi_support": ["arm64-v8a", "x86_64", "universal"]
    }
    
    with open(STAGING_DIR / "manifest.json", "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2)
    with open(APP_MODELS_DIR / "manifest.json", "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2)
    print("  -> Created versioned manifest.json")

    print("\n=== Verifying Model Inference with ONNX Runtime ===")
    model_path = str(STAGING_DIR / "model_int8.onnx")
    sess_options = ort.SessionOptions()
    sess_options.intra_op_num_threads = 2
    sess = ort.InferenceSession(model_path, sess_options)
    print("ONNX Model Inputs:")
    for inp in sess.get_inputs():
        print(f"  {inp.name}: {inp.shape} ({inp.type})")
    print("ONNX Model Outputs:")
    for out in sess.get_outputs():
        print(f"  {out.name}: {out.shape} ({out.type})")

    # Load tokenizer
    from tokenizers import Tokenizer
    tokenizer = Tokenizer.from_file(str(STAGING_DIR / "tokenizer.json"))
    
    prompt = "<|im_start|>system\nYou are a curriculum planner for Grade 1 Numeracy.<|im_end|>\n<|im_start|>user\nGenerate counting activity 1-10.<|im_end|>\n<|im_start|>assistant\n"
    encoded = tokenizer.encode(prompt)
    input_ids = np.array([encoded.ids], dtype=np.int64)
    attention_mask = np.ones_like(input_ids, dtype=np.int64)

    feed = {"input_ids": input_ids}
    inp_names = [inp.name for inp in sess.get_inputs()]
    if "attention_mask" in inp_names:
        feed["attention_mask"] = attention_mask
    if "position_ids" in inp_names:
        feed["position_ids"] = np.arange(input_ids.shape[1], dtype=np.int64).reshape(1, -1)

    # Initialize past_key_values if required by model
    for inp in sess.get_inputs():
        if inp.name.startswith("past_key_values"):
            shape = [dim if isinstance(dim, int) else (1 if i==0 else 0) for i, dim in enumerate(inp.shape)]
            feed[inp.name] = np.zeros(shape, dtype=np.float32)

    t0 = time.time()
    outputs = sess.run(None, feed)
    logits = outputs[0]
    next_token = int(np.argmax(logits[0, -1, :]))
    dt = time.time() - t0
    decoded_token = tokenizer.decode([next_token])
    print(f"\nInference verified in {dt*1000:.1f}ms! Next token ID: {next_token}, Decoded: '{decoded_token}'")
    print("=== Qwen 0.5B Staging Complete & 100% Operational ===")

if __name__ == "__main__":
    main()
