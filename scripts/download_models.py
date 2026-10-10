#!/usr/bin/env python3
"""
TribeTalk — Automated Model Downloader

Downloads and unpacks the pre-quantized production ONNX models (~1.34 GB)
from Hugging Face (jeshu05/tribetalk-models) into staged_models/.

Usage:
    python scripts/download_models.py
    python scripts/download_models.py --url <CUSTOM_URL>
"""

import argparse
import sys
import urllib.request
import zipfile
from pathlib import Path

# Safe terminal encoding on Windows
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

HUGGINGFACE_MODELS_URL = "https://huggingface.co/jeshu05/tribetalk-models/resolve/main/models.zip"


class DownloadProgressBar:
    def __init__(self):
        self.last_percent = -1

    def __call__(self, block_num, block_size, total_size):
        downloaded = block_num * block_size
        if total_size > 0:
            percent = min(100, int(downloaded * 100 / total_size))
            if percent != self.last_percent:
                self.last_percent = percent
                mb_down = downloaded / (1024 * 1024)
                mb_total = total_size / (1024 * 1024)
                bar_len = 35
                filled = int(bar_len * percent / 100)
                bar = "=" * filled + "-" * (bar_len - filled)
                print(
                    f"\r[{bar}] {percent:3d}% ({mb_down:6.1f} / {mb_total:6.1f} MB)",
                    end="",
                    flush=True,
                )


def download_models(download_url: str, root_dir: Path) -> int:
    zip_path = root_dir / "models.zip"
    staged_dir = root_dir / "staged_models"

    print("=" * 65)
    print("        TribeTalk Neural Model Downloader (Hugging Face)       ")
    print("=" * 65)
    print(f"Source Repository: https://huggingface.co/jeshu05/tribetalk-models\n")

    # 1. Check if already staged
    if staged_dir.exists():
        onnx_files = list(staged_dir.glob("*/*.onnx"))
        if len(onnx_files) >= 5:
            print(f"[INFO] Verified models already staged in '{staged_dir}':")
            for f in onnx_files:
                print(f"  [OK] {f.parent.name}/{f.name} ({f.stat().st_size / 1e6:.1f} MB)")
            print("\nYou can deploy them directly to your phone by running:")
            print("    python scripts/migrate_models_to_android.py")
            return 0

    # 2. Check if local models.zip exists
    if not zip_path.exists():
        print(f"Downloading pre-quantized production models (~1.34 GB)...")
        print(f"URL: {download_url}\n")
        try:
            progress = DownloadProgressBar()
            # User-Agent header for Hugging Face
            req = urllib.request.Request(
                download_url,
                headers={"User-Agent": "TribeTalk-Downloader/1.0"}
            )
            with urllib.request.urlopen(req) as response, open(zip_path, "wb") as out_file:
                total_size = int(response.info().get("Content-Length", -1))
                block_size = 1024 * 1024  # 1 MB blocks
                block_num = 0
                while True:
                    buffer = response.read(block_size)
                    if not buffer:
                        break
                    out_file.write(buffer)
                    block_num += 1
                    progress(block_num, block_size, total_size)
            print("\n\n[OK] Download complete!")
        except Exception as e:
            if zip_path.exists():
                zip_path.unlink()  # remove partial download
            print(f"\n\n[ERROR] Failed to download models.zip: {e}")
            print("\nManual Alternative:")
            print("  1. Download 'models.zip' directly from Hugging Face:")
            print("     https://huggingface.co/jeshu05/tribetalk-models/blob/main/models.zip")
            print("  2. Place 'models.zip' in the project root folder.")
            print("  3. Re-run: python scripts/download_models.py")
            return 1
    else:
        print(f"[INFO] Found local 'models.zip' ({zip_path.stat().st_size / 1e6:.1f} MB). Skipping download.")

    # 3. Extract models.zip
    print(f"\nExtracting models.zip into '{staged_dir}'...")
    staged_dir.mkdir(parents=True, exist_ok=True)
    try:
        with zipfile.ZipFile(zip_path, "r") as zf:
            zf.extractall(root_dir)
        print("[OK] Extraction complete!")
    except Exception as e:
        print(f"[ERROR] Failed to extract zip: {e}")
        return 1

    print("\n" + "=" * 65)
    print("SUCCESS: All models are unpacked and ready in staged_models/!")
    print("To deploy them to your connected Android phone or tablet, run:")
    print("    python scripts/migrate_models_to_android.py")
    print("=" * 65)
    return 0


def main():
    parser = argparse.ArgumentParser(description="Download pre-quantized ONNX models from Hugging Face for TribeTalk")
    parser.add_argument(
        "--url",
        default=HUGGINGFACE_MODELS_URL,
        help="Direct URL to models.zip on Hugging Face",
    )
    args = parser.parse_args()
    return download_models(args.url, Path.cwd())


if __name__ == "__main__":
    sys.exit(main())
