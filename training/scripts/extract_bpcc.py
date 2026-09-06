"""
AI4Bharat BPCC Extraction & English-Pivot Script for Hindi <-> Santali
Part of TribeTalk V2 Data Acquisition Pipeline
"""
import os
import sys
import csv
import urllib.request
import unicodedata

sys.stdout.reconfigure(encoding='utf-8')

raw_dir = "training/data/v2/raw/bpcc"
inter_dir = "training/data/v2/intermediate"
os.makedirs(raw_dir, exist_ok=True)
os.makedirs(inter_dir, exist_ok=True)

sat_tsv_path = os.path.join(raw_dir, "sat_Olck.tsv")
hin_tsv_path = os.path.join(raw_dir, "hin_Deva.tsv")

token = os.environ.get("HF_TOKEN") or os.environ.get("HUGGING_FACE_HUB_TOKEN")

def download_file(url, target_path):
    print(f"[*] Downloading {url} to {target_path}...")
    headers = {"User-Agent": "Mozilla/5.0"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req) as resp, open(target_path, "wb") as out_f:
            total_read = 0
            while True:
                chunk = resp.read(1024 * 1024)
                if not chunk:
                    break
                out_f.write(chunk)
                total_read += len(chunk)
                print(f"  Downloaded {round(total_read / (1024 * 1024), 2)} MB...", end="\r")
        print(f"\n[+] Successfully downloaded {target_path} ({round(total_read / (1024 * 1024), 2)} MB)")
        return True
    except urllib.error.HTTPError as e:
        if e.code == 401:
            print(f"[-] HTTP Error 401: Unauthorized. Authentication required. Please set HF_TOKEN or run huggingface-cli login.")
        else:
            print(f"[-] HTTP Error {e.code}: {e.reason}")
        return False
    except Exception as e:
        print(f"[-] Download error: {e}")
        return False

# Download if not present
base_url = "https://huggingface.co/datasets/ai4bharat/BPCC/resolve/main/bpcc-seed-latest"
if not os.path.exists(sat_tsv_path):
    if not download_file(f"{base_url}/sat_Olck.tsv", sat_tsv_path):
        sys.exit(1)

if not os.path.exists(hin_tsv_path):
    if not download_file(f"{base_url}/hin_Deva.tsv", hin_tsv_path):
        sys.exit(1)

print("[*] Processing downloaded BPCC TSVs...")

# Load Santali TSV: columns [src (eng), tgt (sat)]
eng_to_sat = {}
with open(sat_tsv_path, "r", encoding="utf-8") as f:
    reader = csv.reader(f, delimiter="\t")
    for row_idx, row in enumerate(reader):
        if len(row) >= 2:
            eng = unicodedata.normalize("NFC", row[0].strip())
            sat = unicodedata.normalize("NFC", row[1].strip())
            if eng and sat:
                eng_to_sat.setdefault(eng, []).append((row_idx, sat))

print(f"[*] Loaded {len(eng_to_sat)} unique English sentences with Santali translations.")

# Load Hindi TSV: columns [src (eng), tgt (hin)]
pivoted_pairs = []
seen_pairs = set()
with open(hin_tsv_path, "r", encoding="utf-8") as f:
    reader = csv.reader(f, delimiter="\t")
    for hin_row_idx, row in enumerate(reader):
        if len(row) >= 2:
            eng = unicodedata.normalize("NFC", row[0].strip())
            hin = unicodedata.normalize("NFC", row[1].strip())
            if eng in eng_to_sat and hin:
                for sat_row_idx, sat in eng_to_sat[eng]:
                    pair_key = (hin, sat)
                    if pair_key not in seen_pairs:
                        seen_pairs.add(pair_key)
                        pivoted_pairs.append({
                            "src_lang": "hin_Deva",
                            "tgt_lang": "sat_Olck",
                            "src_text": hin,
                            "tgt_text": sat,
                            "source": "AI4BHARAT_BPCC",
                            "source_subset": "bpcc-seed-latest",
                            "source_id": f"bpcc_h_{hin_row_idx}_s_{sat_row_idx}",
                            "license": "CC0-1.0"
                        })

out_csv = os.path.join(inter_dir, "bpcc_pivoted_hi_sat.csv")
fieldnames = ["src_lang", "tgt_lang", "src_text", "tgt_text", "source", "source_subset", "source_id", "license"]
with open(out_csv, "w", encoding="utf-8", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=fieldnames)
    writer.writeheader()
    writer.writerows(pivoted_pairs)

print(f"[+] Wrote {len(pivoted_pairs)} genuine English-pivoted parallel pairs to {out_csv}.")
