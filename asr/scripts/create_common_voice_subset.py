"""
Common Voice Hindi Evaluation Subset Creator (Phase 4)
Deterministically selects 100 real human-speech samples from Mozilla Common Voice Hindi test.tsv (seed=42).
Creates asr/tests/common_voice_hi/metadata.csv.
"""

import os
import sys
import csv
import random

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

DEFAULT_DATASET_ROOT = r"C:\Users\jesva\Downloads\1781715680033-cv-corpus-26.0-2026-06-12-hi\cv-corpus-26.0-2026-06-12\hi"

def main():
    dataset_root = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_DATASET_ROOT
    test_tsv = os.path.join(dataset_root, "test.tsv")
    clips_dir = os.path.join(dataset_root, "clips")
    out_dir = "asr/tests/common_voice_hi"
    out_csv = os.path.join(out_dir, "metadata.csv")
    out_readme = os.path.join(out_dir, "README.md")

    os.makedirs(out_dir, exist_ok=True)

    print("============================================================")
    print("CREATING COMMON VOICE HINDI 100-SAMPLE EVALUATION SUBSET")
    print("============================================================")

    if not os.path.exists(test_tsv):
        print(f"[ERROR] TSV file not found: {test_tsv}")
        return

    with open(test_tsv, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f, delimiter="\t")
        all_rows = list(reader)

    print(f"[*] Total rows in test.tsv: {len(all_rows)}")

    # Filter for existing, non-empty MP3 files and reference texts
    valid_rows = []
    for r in all_rows:
        mp3_name = r["path"]
        mp3_path = os.path.join(clips_dir, mp3_name)
        sentence = r["sentence"].strip()
        if os.path.exists(mp3_path) and os.path.getsize(mp3_path) > 0 and sentence:
            valid_rows.append({
                "file": mp3_name,
                "reference": sentence,
                "client_id": r.get("client_id", "")
            })

    print(f"[*] Verified valid matching audio clips in clips/: {len(valid_rows)}")

    # Select 100 samples deterministically using seed=42
    random.seed(42)
    subset = random.sample(valid_rows, min(100, len(valid_rows)))
    # Sort by filename for reproducibility
    subset.sort(key=lambda x: x["file"])

    with open(out_csv, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=["file", "reference", "client_id"])
        writer.writeheader()
        writer.writerows(subset)

    with open(out_readme, "w", encoding="utf-8") as f:
        f.write("# Mozilla Common Voice Hindi Evaluation Subset\n\n")
        f.write("* **Dataset Source**: Mozilla Common Voice Hindi Corpus v26.0 (`test.tsv` split)\n")
        f.write("* **Samples**: 100 real human Hindi speech clips\n")
        f.write("* **Selection Method**: Random sampling with fixed random seed `42`\n")
        f.write("* **Metadata Index**: [`metadata.csv`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/asr/tests/common_voice_hi/metadata.csv)\n")

    print("\n" + "=" * 60)
    print(f"[OK] Selected {len(subset)} real human-speech clips")
    print(f"[OK] Saved metadata to {out_csv}")
    print(f"[OK] Saved documentation to {out_readme}")
    print("=" * 60)

if __name__ == "__main__":
    main()
