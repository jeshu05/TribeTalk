"""Download and Stage SmolLM2-135M INT8 ONNX Model for TribeTalk.

Downloads the quantized SmolLM2-135M-Instruct ONNX model from Hugging Face:
- Model: onnx-community/SmolLM2-135M-Instruct-ONNX (onnx/model_int8.onnx, ~135 MB)
- Tokenizer: tokenizer.json
- Config: config.json

Stages into `staged_models/slm/` and automatically pushes to connected Android devices via ADB:
`/sdcard/Android/data/org.tribetalk/files/models/slm/`
"""

import os
import shutil
import subprocess
from pathlib import Path
from huggingface_hub import hf_hub_download

REPO_ID = "onnx-community/SmolLM2-135M-Instruct-ONNX"
STAGING_DIR = Path("staged_models/slm")
ADB = r"C:\Users\jesva\AppData\Local\Android\Sdk\platform-tools\adb.exe"
DEVICE_DEST = "/sdcard/Android/data/org.tribetalk/files/models/slm"


def download_slm_model():
    print("=== Step 1: Downloading SmolLM2-135M-Instruct INT8 ONNX from Hugging Face ===")
    STAGING_DIR.mkdir(parents=True, exist_ok=True)

    # 1. Model weights
    print(f"Fetching onnx/model_int8.onnx from {REPO_ID}...")
    model_cached_path = hf_hub_download(
        repo_id=REPO_ID,
        filename="onnx/model_int8.onnx"
    )
    dest_model = STAGING_DIR / "model.onnx"
    shutil.copyfile(model_cached_path, dest_model)
    print(f"  -> Model staged to {dest_model} ({dest_model.stat().st_size / 1e6:.1f} MB)")

    # 2. Tokenizer
    print("Fetching tokenizer.json...")
    tok_cached_path = hf_hub_download(
        repo_id=REPO_ID,
        filename="tokenizer.json"
    )
    dest_tok = STAGING_DIR / "tokenizer.json"
    shutil.copyfile(tok_cached_path, dest_tok)
    print(f"  -> Tokenizer staged to {dest_tok} ({dest_tok.stat().st_size / 1e6:.1f} MB)")

    # 3. Config
    print("Fetching config.json...")
    cfg_cached_path = hf_hub_download(
        repo_id=REPO_ID,
        filename="config.json"
    )
    dest_cfg = STAGING_DIR / "config.json"
    shutil.copyfile(cfg_cached_path, dest_cfg)
    print(f"  -> Config staged to {dest_cfg}")

    print("\nAll SLM files downloaded and staged successfully!")


def push_to_android():
    print(f"\n=== Step 2: Checking Connected Android Device and Pushing to {DEVICE_DEST} ===")
    if not os.path.exists(ADB):
        print(f"Warning: ADB not found at {ADB}. Skipping device push.")
        return

    # Check connected devices
    res = subprocess.run([ADB, "devices"], capture_output=True, text=True)
    lines = [line.strip() for line in res.stdout.splitlines() if line.strip() and not line.startswith("List")]
    devices = [line.split()[0] for line in lines if "device" in line]

    if not devices:
        print("No Android device currently attached via ADB. Staged models are ready for manual transfer.")
        return

    device_id = devices[0]
    print(f"Detected device: {device_id}")

    # Ensure remote directory exists
    subprocess.run([ADB, "-s", device_id, "shell", f"mkdir -p {DEVICE_DEST}"], capture_output=True)

    # Push files
    print(f"Pushing {STAGING_DIR} to {DEVICE_DEST}...")
    push_cmd = [ADB, "-s", device_id, "push", str(STAGING_DIR) + "/.", DEVICE_DEST]
    push_res = subprocess.run(push_cmd, capture_output=True, text=True)
    print(push_res.stdout)
    if push_res.stderr:
        print(push_res.stderr)

    if push_res.returncode == 0:
        print(f"\nSUCCESS! SmolLM2-135M INT8 model successfully deployed to {device_id}:{DEVICE_DEST}")
        # Verify
        verify_res = subprocess.run([ADB, "-s", device_id, "shell", f"ls -lh {DEVICE_DEST}"], capture_output=True, text=True)
        print("Remote files:\n" + verify_res.stdout)
    else:
        print(f"ADB push failed with exit code {push_res.returncode}")


if __name__ == "__main__":
    download_slm_model()
    push_to_android()
