"""
IndicConformer Model Downloader Script (Phase 1)
Downloads PyTorch checkpoints and pre-exported ONNX models from Hugging Face repositories.
"""

import os
import sys
from huggingface_hub import hf_hub_download

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

def main():
    onnx_dir = "asr/models/onnx"
    pytorch_dir = "asr/models/pytorch"
    os.makedirs(onnx_dir, exist_ok=True)
    os.makedirs(pytorch_dir, exist_ok=True)

    print("============================================================")
    print("DOWNLOADING AI4BHARAT INDICCONFORMER HINDI MODEL ASSETS")
    print("============================================================")

    # 1. Download ONNX assets from OpenVoiceOS mirror
    print("\n[1/2] Downloading ONNX model assets from OpenVoiceOS/ai4bharat-indicconformer-hi-onnx...")
    files_onnx = ["model.onnx", "model.onnx_data", "model.int8.onnx", "vocab.txt", "config.json"]
    for f in files_onnx:
        print(f" -> Downloading {f}...")
        hf_hub_download(
            repo_id="OpenVoiceOS/ai4bharat-indicconformer-hi-onnx",
            filename=f,
            local_dir=onnx_dir
        )
    print(f"[OK] ONNX assets downloaded to {onnx_dir}")

    # 2. Download PyTorch assets from Tejxl un-gated mirror
    print("\n[2/2] Downloading PyTorch model weights from Tejxl/IndicConformer-Hindi...")
    files_pytorch = ["hindi_encoder.pt", "hindi_ctc_decoder.pt", "hindi_config.yaml", "tokenizer.json"]
    for f in files_pytorch:
        print(f" -> Downloading {f}...")
        hf_hub_download(
            repo_id="Tejxl/IndicConformer-Hindi",
            filename=f,
            local_dir=pytorch_dir
        )
    print(f"[OK] PyTorch assets downloaded to {pytorch_dir}")

    print("\n" + "=" * 60)
    print("ALL INDICCONFORMER ASSETS DOWNLOADED SUCCESSFULLY")
    print("=" * 60)

if __name__ == "__main__":
    main()
