"""
Hindi -> Santali Translation Evaluation Script (Phase 4)
Calculates BLEU score, chrF++ score, exact match rate, average length, and metrics JSON.
"""

import os
import sys
import csv
import json
import sacrebleu

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

DEFAULT_PREDICTIONS_CSV = "translation/results/predictions.csv"
DEFAULT_METRICS_JSON = "translation/results/metrics.json"

def main():
    pred_path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_PREDICTIONS_CSV
    out_json = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_METRICS_JSON

    if not os.path.exists(pred_path):
        print(f"[ERROR] Predictions CSV not found: {pred_path}")
        return

    print("============================================================")
    print("EVALUATING INDIC TRANS2 HINDI -> SANTALI TRANSLATION METRICS")
    print("============================================================")
    print(f"[*] Input Predictions CSV : {pred_path}")

    with open(pred_path, "r", encoding="utf-8") as f:
        reader = list(csv.DictReader(f))

    references = [r["reference_santhali"].strip() for r in reader]
    hypotheses = [r["predicted_santhali"].strip() for r in reader]

    total_samples = len(reader)
    print(f"[*] Evaluating across {total_samples} sentence pairs")

    # 1. SacreBLEU (Corpus BLEU)
    bleu_result = sacrebleu.corpus_bleu(hypotheses, [references])
    bleu_score = float(bleu_result.score)

    # 2. chrF++ (word_order=2 for chrF++)
    chrf_result = sacrebleu.corpus_chrf(hypotheses, [references], word_order=2)
    chrf_score = float(chrf_result.score)

    # 3. Exact Match Count
    exact_matches = sum(1 for ref, hyp in zip(references, hypotheses) if ref == hyp)
    exact_match_rate = (exact_matches / float(total_samples)) * 100.0

    # 4. Length Analysis
    avg_ref_len_chars = sum(len(r) for r in references) / float(total_samples)
    avg_hyp_len_chars = sum(len(h) for h in hypotheses) / float(total_samples)
    avg_ref_len_words = sum(len(r.split()) for r in references) / float(total_samples)
    avg_hyp_len_words = sum(len(h.split()) for h in hypotheses) / float(total_samples)

    metrics = {
        "model": "ai4bharat/indictrans2-indic-indic-dist-320M",
        "source_language": "hin_Deva",
        "target_language": "sat_Olck",
        "dataset": "IN22-Conv (hi_sat_in22.csv)",
        "samples": total_samples,
        "bleu": round(bleu_score, 2),
        "chrf_plus_plus": round(chrf_score, 2),
        "exact_match_count": exact_matches,
        "exact_match_rate_percentage": round(exact_match_rate, 2),
        "average_reference_length_chars": round(avg_ref_len_chars, 2),
        "average_hypothesis_length_chars": round(avg_hyp_len_chars, 2),
        "average_reference_length_words": round(avg_ref_len_words, 2),
        "average_hypothesis_length_words": round(avg_hyp_len_words, 2),
        "sacrebleu_signature": str(bleu_result),
        "status": "PASS"
    }

    os.makedirs(os.path.dirname(out_json), exist_ok=True)
    with open(out_json, "w", encoding="utf-8") as f:
        json.dump(metrics, f, indent=2, ensure_ascii=False)

    print("\n" + "=" * 65)
    print("INDIC TRANS2 HINDI -> SANTALI EVALUATION SUMMARY")
    print("=" * 65)
    print(f"  Total Samples           : {total_samples}")
    print(f"  BLEU Score              : {bleu_score:.2f}")
    print(f"  chrF++ Score            : {chrf_score:.2f}")
    print(f"  Exact Matches           : {exact_matches} / {total_samples} ({exact_match_rate:.2f}%)")
    print(f"  Avg Ref Length (Words)  : {avg_ref_len_words:.2f} words ({avg_ref_len_chars:.2f} chars)")
    print(f"  Avg Hyp Length (Words)  : {avg_hyp_len_words:.2f} words ({avg_hyp_len_chars:.2f} chars)")
    print(f"[OK] Saved metrics to {out_json}")
    print("=" * 65)

if __name__ == "__main__":
    main()
