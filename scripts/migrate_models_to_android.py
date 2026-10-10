#!/usr/bin/env python3
"""TribeTalk Android Model Staging and Device Deployment Utility.

Automates deploying offline neural AI models (ASR, NMT, TTS, Qwen 0.5B SLM)
to connected Android devices via ADB.

Usage:
    python scripts/migrate_models_to_android.py [OPTIONS]

Options:
    --adb PATH          Custom path to adb executable
    --device ID         Specific Android device serial ID (auto-detected if single device)
    --dest PATH         Destination path on device (default: /sdcard/Android/data/org.tribetalk/files/models)
    --source DIR        Local source directory (default: staged_models)
    --all               Push all staged model variants (including experimental INT4/unpruned)
    --verify-only       Only verify models installed on the connected Android device
    --dry-run           Print operations without performing ADB push
    --download          Attempt to download missing models from Hugging Face Hub
"""

from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path
from typing import Dict, List, Optional, Tuple

REPO_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_STAGING_DIR = REPO_ROOT / "staged_models"
DEFAULT_DEVICE_DEST = "/sdcard/Android/data/org.tribetalk/files/models"

# Production Minimal Required Model Inventory
# (Total disk footprint ~1.19 GB, peak RAM <= 350 MB)
PRODUCTION_MODELS = {
    "asr": [
        "hindi_conformer.onnx",
        "hindi_vocab.txt",
        "santali_conformer.onnx",
        "santali_vocab.txt",
    ],
    "nmt": [
        "encoder_model_int8_pruned.onnx",
        "decoder_model_merged_pruned.onnx",
        "vocab.src.pruned.tsv",
        "vocab.tgt.pruned.tsv",
        "config.json",
        "generation_config.json",
    ],
    "qwen": [
        "model_int8.onnx",
        "tokenizer.json",
        "vocab.json",
        "config.json",
        "generation_config.json",
        "tokenizer_config.json",
        "special_tokens_map.json",
        "manifest.json",
    ],
    "tts": [
        "hindi_tts.onnx",
        "hindi_tts_vocab.json",
        "sat_piper_model.onnx",
        "sat_piper_model.onnx.json",
    ],
}


def find_adb(custom_path: Optional[str] = None) -> Optional[str]:
    """Locate ADB executable across PATH, environment variables, local.properties, and standard SDK locations."""
    if custom_path:
        p = Path(custom_path)
        if p.is_file() and os.access(p, os.X_OK):
            return str(p.resolve())
        # On Windows, try adding .exe if omitted
        if os.name == "nt" and (p.with_suffix(".exe")).is_file():
            return str(p.with_suffix(".exe").resolve())

    # 1. System PATH
    found = shutil.which("adb")
    if found:
        return found

    candidates: List[Path] = []

    # 2. Inspect local.properties in repo
    local_props = REPO_ROOT / "local.properties"
    if local_props.is_file():
        try:
            for line in local_props.read_text(encoding="utf-8", errors="ignore").splitlines():
                if line.startswith("sdk.dir="):
                    sdk_dir = line.split("=", 1)[1].strip().replace("\\:", ":").replace("\\\\", "/")
                    candidates.append(Path(sdk_dir) / "platform-tools" / ("adb.exe" if os.name == "nt" else "adb"))
        except Exception:
            pass

    # 3. Environment variables
    for env_var in ["ANDROID_HOME", "ANDROID_SDK_ROOT"]:
        val = os.environ.get(env_var)
        if val:
            candidates.append(Path(val) / "platform-tools" / ("adb.exe" if os.name == "nt" else "adb"))

    # 4. Standard OS install paths
    home = Path.home()
    if os.name == "nt":
        local_app_data = os.environ.get("LOCALAPPDATA")
        if local_app_data:
            candidates.append(Path(local_app_data) / "Android" / "Sdk" / "platform-tools" / "adb.exe")
        candidates.append(home / "AppData" / "Local" / "Android" / "Sdk" / "platform-tools" / "adb.exe")
    elif sys.platform == "darwin":
        candidates.append(home / "Library" / "Android" / "sdk" / "platform-tools" / "adb")
    else:  # Linux
        candidates.append(home / "Android" / "Sdk" / "platform-tools" / "adb")
        candidates.append(home / ".android" / "platform-tools" / "adb")
        candidates.append(Path("/usr/lib/android-sdk/platform-tools/adb"))

    for c in candidates:
        if c.is_file():
            return str(c.resolve())

    return None


def get_connected_devices(adb_bin: str) -> List[Tuple[str, str]]:
    """Return list of (device_id, status) tuples from `adb devices`."""
    try:
        res = subprocess.run([adb_bin, "devices"], capture_output=True, text=True, check=True)
    except Exception as e:
        print(f"Error running adb: {e}")
        return []

    devices = []
    lines = res.stdout.strip().splitlines()
    for line in lines[1:]:  # skip header
        parts = line.strip().split()
        if len(parts) >= 2:
            devices.append((parts[0], parts[1]))
    return devices


def verify_device_model_files(adb_bin: str, device_id: str, dest_dir: str) -> Dict[str, Dict[str, int]]:
    """Inspect remote model directories on device and return file sizes in bytes."""
    results: Dict[str, Dict[str, int]] = {}
    for sub in ["asr", "nmt", "qwen", "tts"]:
        results[sub] = {}
        remote_sub = f"{dest_dir}/{sub}"
        cmd = [adb_bin, "-s", device_id, "shell", f"ls -l {remote_sub}"]
        res = subprocess.run(cmd, capture_output=True, text=True)
        if res.returncode == 0:
            for line in res.stdout.strip().splitlines():
                parts = line.split()
                # Typical ls -l output: -rw-rw---- 1 u0_a... u0_a... 137775104 2026-10-05 22:30 hindi_conformer.onnx
                if len(parts) >= 5 and not parts[0].startswith("total") and not parts[0].startswith("d"):
                    fname = parts[-1]
                    # size is usually part 4 or 3
                    for p in parts[3:5]:
                        if p.isdigit():
                            results[sub][fname] = int(p)
                            break
    return results


def check_local_staging(source_dir: Path, minimal: bool) -> Tuple[bool, List[str], List[str]]:
    """Check if required models exist in local staging directory."""
    missing = []
    available = []

    categories = PRODUCTION_MODELS if minimal else PRODUCTION_MODELS

    for cat, files in categories.items():
        cat_dir = source_dir / cat
        for f in files:
            path = cat_dir / f
            # Also allow fallback/alternative names if present
            alt_path = None
            if f == "hindi_conformer.onnx":
                alt_path = cat_dir / "hindi_conformer_int8.onnx"
            elif f == "santali_conformer.onnx":
                alt_path = cat_dir / "santali_conformer_int8.onnx"
            elif f == "model_int8.onnx":
                alt_path = cat_dir / "model_int4.onnx"

            if path.exists() and path.stat().st_size > 0:
                available.append(f"{cat}/{f}")
            elif alt_path and alt_path.exists() and alt_path.stat().st_size > 0:
                available.append(f"{cat}/{alt_path.name}")
            else:
                missing.append(f"{cat}/{f}")

    return len(missing) == 0, available, missing


def extract_from_zip_if_needed(source_dir: Path) -> None:
    """If models.zip exists in the workspace, unpack missing models into source_dir."""
    zip_path = REPO_ROOT / "models.zip"
    if not zip_path.is_file():
        return

    print(f"Checking {zip_path.name} ({zip_path.stat().st_size / 1e6:.1f} MB) for missing assets...")
    try:
        with zipfile.ZipFile(zip_path, "r") as zf:
            for member in zf.infolist():
                if member.is_dir():
                    continue
                target = source_dir / member.filename
                if not target.exists() or target.stat().st_size == 0:
                    print(f"  Extracting {member.filename}...")
                    target.parent.mkdir(parents=True, exist_ok=True)
                    zf.extract(member, source_dir)
    except Exception as e:
        print(f"Warning: Failed to extract from models.zip: {e}")


def main() -> int:
    parser = argparse.ArgumentParser(
        description="TribeTalk: Deploy offline AI models to Android device via ADB."
    )
    parser.add_argument("--adb", type=str, default=None, help="Path to adb executable")
    parser.add_argument("--device", type=str, default=None, help="Target Android device ID")
    parser.add_argument("--dest", type=str, default=DEFAULT_DEVICE_DEST, help="Destination directory on device")
    parser.add_argument("--source", type=str, default=str(DEFAULT_STAGING_DIR), help="Source staging directory")
    parser.add_argument("--all", action="store_true", help="Push all staged files including experimental models")
    parser.add_argument("--verify-only", action="store_true", help="Only verify model installation on device")
    parser.add_argument("--dry-run", action="store_true", help="Dry-run without copying files")
    parser.add_argument("--download", action="store_true", help="Attempt Hugging Face download for missing models")

    args = parser.parse_args()

    source_dir = Path(args.source).resolve()

    print("=" * 65)
    print("      TribeTalk: Android Neural AI Model Deployment Tool      ")
    print("=" * 65)

    # 1. Locate ADB
    adb_bin = find_adb(args.adb)
    if not adb_bin:
        print("\n[ERROR] ADB executable could not be found.")
        print("Please ensure Android Studio / Platform Tools are installed, or specify:")
        print("    python scripts/migrate_models_to_android.py --adb C:\\path\\to\\adb.exe")
        return 1

    print(f"[OK] ADB detected: {adb_bin}")

    # 2. Identify Target Device
    devices = get_connected_devices(adb_bin)
    if not devices:
        print("\n[ERROR] No Android devices or emulators attached.")
        print("\nTroubleshooting checklist:")
        print("  1. Connect your Android device using a good USB data cable.")
        print("  2. In Android Settings > Developer Options, ensure 'USB Debugging' is ON.")
        print("  3. Unlock your phone and accept the 'Allow USB debugging?' dialog.")
        print("  4. Verify connection by running: adb devices\n")
        return 1

    device_id = args.device
    if not device_id:
        if len(devices) == 1:
            device_id = devices[0][0]
            print(f"[OK] Auto-selected connected device: {device_id} ({devices[0][1]})")
        else:
            print("\nMultiple devices attached:")
            for idx, (dev, state) in enumerate(devices, 1):
                print(f"  [{idx}] {dev} ({state})")
            print("\nPlease specify a device using: --device <device_id>")
            return 1
    else:
        matched = [d for d in devices if d[0] == device_id]
        if not matched:
            print(f"\n[ERROR] Specified device '{device_id}' not found in attached devices:")
            for dev, state in devices:
                print(f"  - {dev} ({state})")
            return 1
        print(f"[OK] Using specified device: {device_id} ({matched[0][1]})")

    # If --verify-only is requested
    if args.verify_only:
        print(f"\nVerifying models on device at: {args.dest} ...")
        device_models = verify_device_model_files(adb_bin, device_id, args.dest)
        total_found = 0
        total_bytes = 0
        for cat, req_files in PRODUCTION_MODELS.items():
            print(f"\n[{cat.upper()}]")
            for req in req_files:
                sz = device_models.get(cat, {}).get(req)
                if sz is not None:
                    print(f"  ✓ {req} ({sz / 1e6:.2f} MB)")
                    total_found += 1
                    total_bytes += sz
                else:
                    # check alt
                    alt_found = False
                    for existing_name, esz in device_models.get(cat, {}).items():
                        if req.split(".")[0] in existing_name:
                            print(f"  ✓ {existing_name} (alt for {req}) ({esz / 1e6:.2f} MB)")
                            total_found += 1
                            total_bytes += esz
                            alt_found = True
                            break
                    if not alt_found:
                        print(f"  ✗ MISSING: {req}")
        print(f"\nSummary: {total_found} model files verified on device ({total_bytes / 1e6:.1f} MB total).")
        return 0

    # 3. Check Local Models
    extract_from_zip_if_needed(source_dir)

    all_ok, available, missing = check_local_staging(source_dir, minimal=not args.all)

    if missing:
        print(f"\n[NOTICE] {len(missing)} expected model files are missing from {source_dir}:")
        for m in missing[:10]:
            print(f"  - {m}")
        if len(missing) > 10:
            print(f"  ... and {len(missing) - 10} more.")

        if args.download:
            print("\nAttempting to download missing models via Hugging Face Hub...")
            try:
                from huggingface_hub import hf_hub_download
            except ImportError:
                print("[ERROR] 'huggingface_hub' is required to download models. Run: pip install huggingface_hub")
                return 1

            # Download Qwen if missing
            if any("qwen" in m for m in missing):
                print("Downloading Qwen 0.5B ONNX files...")
                qwen_files = [
                    "config.json", "generation_config.json", "tokenizer.json",
                    "tokenizer_config.json", "vocab.json", "special_tokens_map.json",
                    "onnx/model_int8.onnx"
                ]
                qwen_dir = source_dir / "qwen"
                qwen_dir.mkdir(parents=True, exist_ok=True)
                for f in qwen_files:
                    print(f"  Fetching {f}...")
                    p = hf_hub_download(repo_id="onnx-community/Qwen2.5-0.5B-Instruct", filename=f)
                    dest_file = qwen_dir / (Path(f).name)
                    shutil.copyfile(p, dest_file)

            # Re-check
            all_ok, available, missing = check_local_staging(source_dir, minimal=not args.all)
        else:
            print("\n[TIP] If you need to download missing models, run with '--download'")
            print("or place your model files into 'staged_models/' as documented in SETUP_GUIDE.md.")

    # 4. Prepare Destination on Device
    print(f"\nPreparing destination directory on Android device:")
    print(f"  Target: {args.dest}")

    if not args.dry_run:
        for sub in ["asr", "nmt", "qwen", "tts"]:
            cmd = [adb_bin, "-s", device_id, "shell", f"mkdir -p {args.dest}/{sub}"]
            res = subprocess.run(cmd, capture_output=True, text=True)
            if "Permission denied" in res.stderr:
                print("\n[CRITICAL ERROR] 'Permission denied' when creating directory on device.")
                print("This happens if TribeTalk has NEVER been installed or launched on this phone.")
                print("Android Scoped Storage only initializes when the app is installed.")
                print("\nFIX:")
                print("  1. Build & Run TribeTalk once from Android Studio (or run: ./gradlew assembleDebug)")
                print("  2. Open TribeTalk on your device once.")
                print("  3. Re-run this migration script.")
                return 1

    # 5. Push Files
    print(f"\nStarting model deployment to device {device_id}...")
    files_to_push: List[Tuple[Path, str]] = []

    if args.all:
        for root, _, files in os.walk(source_dir):
            for file in files:
                p = Path(root) / file
                rel = p.relative_to(source_dir)
                rem = f"{args.dest}/{rel.as_posix()}"
                files_to_push.append((p, rem))
    else:
        # Push minimal production set
        for cat, fnames in PRODUCTION_MODELS.items():
            for fname in fnames:
                lp = source_dir / cat / fname
                if not lp.is_file():
                    # check alt
                    if fname == "hindi_conformer.onnx":
                        alt = source_dir / cat / "hindi_conformer_int8.onnx"
                        if alt.is_file():
                            lp = alt
                    elif fname == "santali_conformer.onnx":
                        alt = source_dir / cat / "santali_conformer_int8.onnx"
                        if alt.is_file():
                            lp = alt
                    elif fname == "model_int8.onnx":
                        alt = source_dir / cat / "model_int4.onnx"
                        if alt.is_file():
                            lp = alt
                if lp.is_file():
                    rem = f"{args.dest}/{cat}/{lp.name}"
                    files_to_push.append((lp, rem))

    total_bytes = sum(p[0].stat().st_size for p in files_to_push)
    print(f"Total payload: {len(files_to_push)} files ({total_bytes / 1e6:.1f} MB)")

    for idx, (local_file, remote_target) in enumerate(files_to_push, 1):
        sz_mb = local_file.stat().st_size / 1e6
        print(f"[{idx}/{len(files_to_push)}] Pushing {local_file.parent.name}/{local_file.name} ({sz_mb:.1f} MB)...")
        if args.dry_run:
            continue

        cmd = [adb_bin, "-s", device_id, "push", str(local_file), remote_target]
        res = subprocess.run(cmd, capture_output=True, text=True)
        if res.returncode != 0:
            print(f"  [FAIL] Error pushing {local_file.name}: {res.stderr.strip()}")
        else:
            print(f"  ✓ Completed")

    if args.dry_run:
        print("\n[DRY RUN COMPLETE] No files were pushed.")
        return 0

    # 6. Post-deployment Verification
    print("\n" + "=" * 65)
    print("                 Post-Deployment Verification                 ")
    print("=" * 65)
    dev_files = verify_device_model_files(adb_bin, device_id, args.dest)

    success_count = 0
    total_expected = 0
    for cat, reqs in PRODUCTION_MODELS.items():
        print(f"\n{cat.upper()}:")
        for req in reqs:
            total_expected += 1
            sz = dev_files.get(cat, {}).get(req)
            if sz is not None and sz > 0:
                print(f"  ✓ {req} ({sz / 1e6:.2f} MB)")
                success_count += 1
            else:
                # check alt
                alt_ok = False
                for ex_name, ex_sz in dev_files.get(cat, {}).items():
                    if req.split(".")[0] in ex_name:
                        print(f"  ✓ {ex_name} (alt) ({ex_sz / 1e6:.2f} MB)")
                        success_count += 1
                        alt_ok = True
                        break
                if not alt_ok:
                    print(f"  ✗ {req} NOT FOUND on device")

    print("\n" + "-" * 65)
    if success_count == total_expected:
        print("SUCCESS: 100% of required neural models are installed on device!")
        print("TribeTalk is ready for completely offline inference.")
    else:
        print(f"Deployment finished with {success_count}/{total_expected} models verified.")
        print("You can verify installed models anytime with:")
        print("    python scripts/migrate_models_to_android.py --verify-only")
    print("-" * 65)

    return 0


if __name__ == "__main__":
    sys.exit(main())
