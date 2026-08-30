"""
Word Error Rate (WER) Evaluation Script (Phase 4)
Calculates WER = (Substitutions + Deletions + Insertions) / Reference Words across classroom Hindi dataset.
Normalizes Devanagari Unicode, punctuation, and whitespace.
"""

import os
import sys
import csv
import json
import unicodedata
from transcribe import IndicConformerASR

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

def normalize_text(text: str) -> str:
    # Apply Unicode NFKC normalization
    text = unicodedata.normalize('NFKC', text)
    # Strip punctuation
    punctuation = "।,!?-\"':;()[]{}"
    for p in punctuation:
        text = text.replace(p, "")
    # Normalize whitespace
    words = text.strip().split()
    return " ".join(words)

def levenshtein_distance(ref_words: list[str], hyp_words: list[str]):
    r_len = len(ref_words)
    h_len = len(hyp_words)
    dp = [[0] * (h_len + 1) for _ in range(r_len + 1)]

    for i in range(r_len + 1):
        dp[i][0] = i
    for j in range(h_len + 1):
        dp[0][j] = j

    for i in range(1, r_len + 1):
        for j in range(1, h_len + 1):
            if ref_words[i - 1] == hyp_words[j - 1]:
                dp[i][j] = dp[i - 1][j - 1]
            else:
                sub = dp[i - 1][j - 1] + 1
                ins = dp[i][j - 1] + 1
                delf = dp[i - 1][j] + 1
                dp[i][j] = min(sub, ins, delf)

    # Backtrack to count S, D, I
    i, j = r_len, h_len
    subs, dels, inss = 0, 0, 0
    while i > 0 or j > 0:
        if i > 0 and j > 0 and ref_words[i - 1] == hyp_words[j - 1]:
            i -= 1
            j -= 1
        elif i > 0 and j > 0 and dp[i][j] == dp[i - 1][j - 1] + 1:
            subs += 1
            i -= 1
            j -= 1
        elif j > 0 and dp[i][j] == dp[i][j - 1] + 1:
            inss += 1
            j -= 1
        elif i > 0 and dp[i][j] == dp[i - 1][j] + 1:
            dels += 1
            i -= 1

    return subs, dels, inss

def main():
    metadata_path = "asr/tests/metadata.csv"
    audio_dir = "asr/tests/audio"
    output_report = "asr/outputs/wer_results.json"
    os.makedirs("asr/outputs", exist_ok=True)

    if not os.path.exists(metadata_path):
        print(f"[ERROR] Metadata CSV not found: {metadata_path}")
        return

    print("============================================================")
    print("INDICCONFORMER WORD ERROR RATE (WER) EVALUATION (Phase 4)")
    print("============================================================")

    asr = IndicConformerASR()
    
    with open(metadata_path, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        samples = list(reader)

    total_ref_words = 0
    total_subs = 0
    total_dels = 0
    total_inss = 0
    detailed_results = []

    for item in samples:
        filename = item["file"]
        raw_ref = item["reference"]
        audio_path = os.path.join(audio_dir, filename)

        if not os.path.exists(audio_path):
            continue

        hyp_raw, latency_ms, rtf = asr.transcribe(audio_path)

        norm_ref = normalize_text(raw_ref)
        norm_hyp = normalize_text(hyp_raw)

        ref_words = norm_ref.split()
        hyp_words = norm_hyp.split()

        subs, dels, inss = levenshtein_distance(ref_words, hyp_words)

        total_ref_words += len(ref_words)
        total_subs += subs
        total_dels += dels
        total_inss += inss

        detailed_results.append({
            "file": filename,
            "reference": norm_ref,
            "hypothesis": norm_hyp,
            "substitutions": subs,
            "deletions": dels,
            "insertions": inss,
            "sample_words": len(ref_words),
            "match": (norm_ref == norm_hyp)
        })
        print(f"[{filename}] Ref: '{norm_ref}' | Hyp: '{norm_hyp}' | Match: {norm_ref == norm_hyp}")

    wer = (total_subs + total_dels + total_inss) / float(total_ref_words) if total_ref_words > 0 else 0.0
    wer_pct = wer * 100.0

    report = {
        "samples_evaluated": len(detailed_results),
        "total_reference_words": total_ref_words,
        "substitutions": total_subs,
        "deletions": total_dels,
        "insertions": total_inss,
        "word_error_rate_percentage": round(wer_pct, 2),
        "accuracy_percentage": round(100.0 - wer_pct, 2),
        "normalization_applied": "NFKC Unicode + Punctuation Removal + Whitespace Normalization",
        "detailed_samples": detailed_results
    }

    with open(output_report, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2, ensure_ascii=False)

    print("\n" + "=" * 60)
    print("WER EVALUATION REPORT SUMMARY")
    print("=" * 60)
    print(f"  Samples Evaluated   : {len(detailed_results)}")
    print(f"  Total Reference Words: {total_ref_words}")
    print(f"  Substitutions (S)   : {total_subs}")
    print(f"  Deletions (D)       : {total_dels}")
    print(f"  Insertions (I)      : {total_inss}")
    print(f"  Word Error Rate (WER): {wer_pct:.2f}%")
    print(f"  Accuracy Rate       : {100.0 - wer_pct:.2f}%")
    print(f"[OK] Detailed results saved to {output_report}")
    print("=" * 60)

if __name__ == "__main__":
    main()
