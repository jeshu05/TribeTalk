"""Utility script to extract, stage, and push all neural AI models to the Android app.

Extracts genuine ONNX models from HuggingFace cache:
1. IndicConformer Hindi ASR ONNX (137 MB)
2. IndicConformer Santali ASR ONNX (137 MB)
3. IndicTrans2 INT8 Bidirectional NMT (encoder + decoder + vocab)
4. MMS Hindi TTS ONNX (38 MB)
"""

import os
import shutil
import subprocess
from pathlib import Path

HF_HUB = Path(os.path.expanduser("~/.cache/huggingface/hub"))
STAGING_DIR = Path("staged_models")
ADB = r"C:\Users\jesva\AppData\Local\Android\Sdk\platform-tools\adb.exe"
DEVICE_ID = "624758d2"
DEVICE_DEST = "/sdcard/Android/data/org.tribetalk/files/models"


def resolve_pointer(file_path: Path) -> Path:
    """Resolve Windows HuggingFace pointer or symlink to actual blob file."""
    if not file_path.exists():
        raise FileNotFoundError(f"Missing file {file_path}")

    # If it's a non-empty regular file, return it
    if file_path.stat().st_size > 500:
        return file_path

    # Read pointer text if small
    try:
        content = file_path.read_text(encoding="utf-8", errors="ignore").strip()
        lines = content.splitlines()
        # Look for git-lfs oid sha256:xxx or relative path
        for line in lines:
            if line.startswith("oid sha256:"):
                hash_val = line.split(":", 1)[1].strip()
                # Find in blobs
                blob = file_path.parents[2] / "blobs" / hash_val
                if blob.exists():
                    return blob
            elif "blobs" in line:
                target = (file_path.parent / line).resolve()
                if target.exists():
                    return target
    except Exception:
        pass

    # Check os.readlink
    try:
        target = Path(os.readlink(file_path))
        if not target.is_absolute():
            target = (file_path.parent / target).resolve()
        if target.exists() and target.stat().st_size > 0:
            return target
    except Exception:
        pass

    return file_path


def stage_models():
    print("=== Step 1: Locating and Staging Neural Models ===")
    STAGING_DIR.mkdir(exist_ok=True)
    (STAGING_DIR / "asr").mkdir(exist_ok=True)
    (STAGING_DIR / "nmt").mkdir(exist_ok=True)
    (STAGING_DIR / "tts").mkdir(exist_ok=True)

    # 1. Hindi ASR
    hi_asr_snap = list((HF_HUB / "models--OpenVoiceOS--ai4bharat-indicconformer-hi-onnx" / "snapshots").glob("*"))[0]
    print(f"Staging Hindi ASR from {hi_asr_snap.name}...")
    hi_model = resolve_pointer(hi_asr_snap / "model.int8.onnx")
    hi_vocab = resolve_pointer(hi_asr_snap / "vocab.txt")
    shutil.copyfile(hi_model, STAGING_DIR / "asr" / "hindi_conformer.onnx")
    shutil.copyfile(hi_vocab, STAGING_DIR / "asr" / "hindi_vocab.txt")
    print(f"  -> Hindi ASR staged: {hi_model.stat().st_size / 1e6:.1f} MB")

    # 2. Santali ASR
    sat_asr_snap = list((HF_HUB / "models--OpenVoiceOS--ai4bharat-indicconformer-sat-onnx" / "snapshots").glob("*"))[0]
    print(f"Staging Santali ASR from {sat_asr_snap.name}...")
    sat_model = resolve_pointer(sat_asr_snap / "model.int8.onnx")
    sat_vocab = resolve_pointer(sat_asr_snap / "vocab.txt")
    shutil.copyfile(sat_model, STAGING_DIR / "asr" / "santali_conformer.onnx")
    shutil.copyfile(sat_vocab, STAGING_DIR / "asr" / "santali_vocab.txt")
    print(f"  -> Santali ASR staged: {sat_model.stat().st_size / 1e6:.1f} MB")

    # 3. IndicTrans2 NMT
    nmt_snap = list((HF_HUB / "models--hari31416--indictrans2-indic-indic-dist-320M-ONNX-int8" / "snapshots").glob("*"))[0]
    print(f"Staging IndicTrans2 NMT from {nmt_snap.name}...")
    nmt_files = [
        ("encoder_model.onnx", "encoder_model.onnx"),
        ("encoder_model.onnx.data", "encoder_model.onnx.data"),
        ("decoder_model.onnx", "decoder_model.onnx"),
        ("decoder_with_past_model.onnx", "decoder_with_past_model.onnx"),
        ("decoder_shared.onnx.data", "decoder_shared.onnx.data"),
        ("dict.SRC.json", "dict.SRC.json"),
        ("dict.TGT.json", "dict.TGT.json"),
        ("model.SRC", "model.SRC"),
        ("model.TGT", "model.TGT"),
        ("tokenizer_src.json", "tokenizer_src.json"),
        ("tokenizer_tgt.json", "tokenizer_tgt.json"),
        ("tokenizer_meta.json", "tokenizer_meta.json"),
        ("generation_config.json", "generation_config.json"),
        ("config.json", "config.json"),
    ]
    for src_name, dst_name in nmt_files:
        src_path = resolve_pointer(nmt_snap / src_name)
        dst_path = STAGING_DIR / "nmt" / dst_name
        shutil.copyfile(src_path, dst_path)
        print(f"  -> Staged {dst_name}: {src_path.stat().st_size / 1e6:.1f} MB")

    # 4. Hindi TTS
    tts_snap = list((HF_HUB / "models--Xenova--mms-tts-hin" / "snapshots").glob("*"))[0]
    print(f"Staging MMS Hindi TTS from {tts_snap.name}...")
    onnx_candidates = list(tts_snap.glob("**/*model*.onnx"))
    tts_model_file = onnx_candidates[0] if onnx_candidates else list(tts_snap.glob("**/*.onnx"))[0]
    tts_model = resolve_pointer(tts_model_file)
    vocab_candidates = list(tts_snap.glob("**/vocab.json"))
    tts_vocab = resolve_pointer(vocab_candidates[0])
    shutil.copyfile(tts_model, STAGING_DIR / "tts" / "hindi_tts.onnx")
    shutil.copyfile(tts_vocab, STAGING_DIR / "tts" / "hindi_tts_vocab.json")
    print(f"  -> Hindi TTS staged: {tts_model.stat().st_size / 1e6:.1f} MB")

    # 5. SmolLM2 SLM
    (STAGING_DIR / "slm").mkdir(exist_ok=True)
    slm_cached_model = STAGING_DIR / "slm" / "model.onnx"
    if slm_cached_model.exists():
        print(f"  -> SmolLM2 SLM already staged: {slm_cached_model.stat().st_size / 1e6:.1f} MB")
    else:
        print("  -> Note: Run scripts/download_and_stage_slm.py to fetch SmolLM2-135M INT8 ONNX.")

    print("All models staged successfully.")


def push_to_device():
    print(f"=== Step 2: Pushing Models to Device {DEVICE_ID} ===")
    cmd = [ADB, "-s", DEVICE_ID, "push", str(STAGING_DIR) + "/.", DEVICE_DEST]
    print(f"Running: {' '.join(cmd)}")
    res = subprocess.run(cmd, capture_output=True, text=True)
    print("STDOUT:", res.stdout)
    if res.stderr:
        print("STDERR:", res.stderr)
    if res.returncode == 0:
        print(f"SUCCESS: Models pushed to {DEVICE_DEST} on device!")
    else:
        print(f"FAILED with exit code {res.returncode}")


if __name__ == "__main__":
    stage_models()
    push_to_device()
