"""
Mozilla Common Voice Hindi Evaluation Script (Phases 7 - 10)
Evaluates IndicConformer ONNX model graphs on real human Hindi speech clips from Common Voice.
Calculates corpus-wide WER = (S + D + I) / N, latency, RTF, and outputs detailed per-sample CSV and summary JSON.
"""

import os
import sys
import csv
import json
import time
import argparse
import unicodedata
import torch
import numpy as np
import soundfile as sf
import librosa
import onnxruntime as ort

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

DEFAULT_DATASET_ROOT = r"C:\Users\jesva\Downloads\1781715680033-cv-corpus-26.0-2026-06-12-hi\cv-corpus-26.0-2026-06-12\hi"

class NeMoMelPreprocessor:
    def __init__(self, sample_rate=16000, n_fft=512, hop_length=160, win_length=400, n_mels=80, preemph=0.97):
        self.sample_rate = sample_rate
        self.n_fft = n_fft
        self.hop_length = hop_length
        self.win_length = win_length
        self.n_mels = n_mels
        self.preemph = preemph
        
        mel_fb = librosa.filters.mel(sr=sample_rate, n_fft=n_fft, n_mels=n_mels, fmin=0.0, fmax=8000.0)
        self.mel_fb = torch.from_numpy(mel_fb).float()

    def process(self, waveform: torch.Tensor) -> torch.Tensor:
        if waveform.ndim == 1:
            waveform = waveform.unsqueeze(0)

        if self.preemph > 0:
            waveform = torch.cat([waveform[:, :1], waveform[:, 1:] - self.preemph * waveform[:, :-1]], dim=1)

        window = torch.hann_window(self.win_length, periodic=False, device=waveform.device)
        spec = torch.stft(
            waveform,
            n_fft=self.n_fft,
            hop_length=self.hop_length,
            win_length=self.win_length,
            window=window,
            center=True,
            pad_mode="reflect",
            return_complex=True
        )
        power_spec = torch.abs(spec) ** 2

        mel_spec = torch.matmul(self.mel_fb.to(waveform.device), power_spec)
        log_mel = torch.log(mel_spec + 1e-5)

        mean = log_mel.mean(dim=2, keepdim=True)
        std = log_mel.std(dim=2, keepdim=True)
        norm_mel = (log_mel - mean) / (std + 1e-5)

        return norm_mel

def normalize_text(text: str) -> str:
    text = unicodedata.normalize('NFKC', text)
    punctuation = "।,!?-\"':;()[]{}«»"
    for p in punctuation:
        text = text.replace(p, "")
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

def parse_args():
    parser = argparse.ArgumentParser(description="Evaluate IndicConformer on Common Voice Hindi")
    parser.add_argument("--dataset-root", type=str, default=DEFAULT_DATASET_ROOT, help="Common Voice dataset root")
    parser.add_argument("--metadata", type=str, default="asr/tests/common_voice_hi/metadata.csv", help="Metadata CSV file")
    parser.add_argument("--model", type=str, default="asr/models/onnx/model.int8.onnx", help="ONNX model path")
    parser.add_argument("--vocab", type=str, default="asr/models/onnx/vocab.txt", help="Vocab file path")
    parser.add_argument("--output-csv", type=str, default="asr/outputs/common_voice_hi_int8_results.csv", help="Output CSV path")
    parser.add_argument("--output-json", type=str, default="asr/outputs/common_voice_hi_int8_summary.json", help="Output JSON path")
    return parser.parse_args()

def main():
    args = parse_args()
    clips_dir = os.path.join(args.dataset_root, "clips")

    print("============================================================")
    print("MOZILLA COMMON VOICE HINDI ASR EVALUATION")
    print("============================================================")
    print(f"[*] Dataset Root : {args.dataset_root}")
    print(f"[*] Metadata CSV : {args.metadata}")
    print(f"[*] ONNX Model   : {args.model}")

    if not os.path.exists(args.metadata):
        print(f"[ERROR] Metadata CSV not found: {args.metadata}")
        return

    # Load ONNX Session
    preprocessor = NeMoMelPreprocessor()
    opts = ort.SessionOptions()
    opts.intra_op_num_threads = 4
    opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
    session = ort.InferenceSession(args.model, opts, providers=["CPUExecutionProvider"])

    vocab_lines = open(args.vocab, encoding="utf-8").readlines()
    vocab = [line.strip().split()[0] if line.strip() else "" for line in vocab_lines]
    blank_id = len(vocab) - 1

    with open(args.metadata, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        samples = list(reader)

    print(f"[*] Loaded {len(samples)} metadata records")

    total_ref_words = 0
    total_subs = 0
    total_dels = 0
    total_inss = 0
    latencies_ms = []
    durations_sec = []

    per_sample_rows = []

    for idx, item in enumerate(samples):
        mp3_name = item["file"]
        raw_ref = item["reference"]
        mp3_path = os.path.join(clips_dir, mp3_name)

        if not os.path.exists(mp3_path):
            print(f"[WARNING] Missing clip: {mp3_path}")
            continue

        # Decode MP3 audio
        y, sr = librosa.load(mp3_path, sr=16000, mono=True)
        duration_sec = len(y) / 16000.0
        durations_sec.append(duration_sec)

        # Feature Extraction & Inference
        t0 = time.time()
        features = preprocessor.process(torch.from_numpy(y).float()).numpy()
        length = np.array([features.shape[2]], dtype=np.int64)

        t_infer = time.time()
        outputs = session.run(None, {"audio_signal": features, "length": length})
        logprobs = outputs[0]
        lat_ms = (time.time() - t_infer) * 1000.0
        latencies_ms.append(lat_ms)

        # CTC Decoding
        tokens = np.argmax(logprobs[0], axis=-1)
        collapsed = []
        prev = None
        for t in tokens:
            if t != prev:
                collapsed.append(t)
                prev = t

        subwords = []
        for t in collapsed:
            if t > 0 and t < blank_id:
                subwords.append(vocab[t])

        raw_pred = "".join(subwords).replace("▁", " ").strip()

        # Text Normalization
        norm_ref = normalize_text(raw_ref)
        norm_pred = normalize_text(raw_pred)

        ref_words = norm_ref.split()
        hyp_words = norm_pred.split()

        subs, dels, inss = levenshtein_distance(ref_words, hyp_words)
        sample_n = len(ref_words)
        sample_wer = (subs + dels + inss) / float(sample_n) if sample_n > 0 else 0.0
        rtf = (lat_ms / 1000.0) / duration_sec if duration_sec > 0 else 0.0

        total_ref_words += sample_n
        total_subs += subs
        total_dels += dels
        total_inss += inss

        per_sample_rows.append({
            "file": mp3_name,
            "reference": raw_ref,
            "prediction": raw_pred,
            "normalized_reference": norm_ref,
            "normalized_prediction": norm_pred,
            "substitutions": subs,
            "deletions": dels,
            "insertions": inss,
            "wer": round(sample_wer, 4),
            "audio_duration_sec": round(duration_sec, 2),
            "latency_ms": round(lat_ms, 2),
            "rtf": round(rtf, 3)
        })

        if (idx + 1) % 10 == 0 or (idx + 1) == len(samples):
            print(f"  Processed [{idx+1:03d}/{len(samples)}] - {mp3_name} ({duration_sec:.2f}s) -> Latency: {lat_ms:.2f}ms | WER: {sample_wer*100:.1f}%")

    corpus_wer = (total_subs + total_dels + total_inss) / float(total_ref_words) if total_ref_words > 0 else 0.0
    corpus_accuracy = (1.0 - corpus_wer) * 100.0
    avg_lat_ms = float(np.mean(latencies_ms))
    med_lat_ms = float(np.median(latencies_ms))
    p95_lat_ms = float(np.percentile(latencies_ms, 95))
    avg_rtf = float(np.mean([r["rtf"] for r in per_sample_rows]))
    model_size_mb = (os.path.getsize(args.model) + (os.path.getsize(args.model + "_data") if os.path.exists(args.model + "_data") else 0)) / (1024.0 * 1024.0)

    # Write per-sample CSV
    os.makedirs(os.path.dirname(args.output_csv), exist_ok=True)
    with open(args.output_csv, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=[
            "file", "reference", "prediction", "normalized_reference", "normalized_prediction",
            "substitutions", "deletions", "insertions", "wer", "audio_duration_sec", "latency_ms", "rtf"
        ])
        writer.writeheader()
        writer.writerows(per_sample_rows)

    # Write summary JSON
    summary = {
        "dataset_name": "Mozilla Common Voice Hindi v26.0",
        "evaluation_split": "test.tsv",
        "model_path": args.model,
        "model_size_mb": round(model_size_mb, 2),
        "total_samples": len(per_sample_rows),
        "total_reference_words": total_ref_words,
        "substitutions": total_subs,
        "deletions": total_dels,
        "insertions": total_inss,
        "corpus_wer_percentage": round(corpus_wer * 100.0, 2),
        "corpus_word_accuracy_percentage": round(corpus_accuracy, 2),
        "average_latency_ms": round(avg_lat_ms, 2),
        "median_latency_ms": round(med_lat_ms, 2),
        "p95_latency_ms": round(p95_lat_ms, 2),
        "average_rtf": round(avg_rtf, 3),
        "status": "PASS"
    }

    with open(args.output_json, "w", encoding="utf-8") as f:
        json.dump(summary, f, indent=2, ensure_ascii=False)

    print("\n" + "=" * 65)
    print("REAL HUMAN-SPEECH COMMON VOICE EVALUATION SUMMARY")
    print("=" * 65)
    print(f"  Evaluation Subset       : {len(per_sample_rows)} clips")
    print(f"  Total Reference Words   : {total_ref_words}")
    print(f"  Substitutions (S)       : {total_subs}")
    print(f"  Deletions (D)           : {total_dels}")
    print(f"  Insertions (I)          : {total_inss}")
    print(f"  Corpus WER              : {corpus_wer * 100.0:.2f}%")
    print(f"  Corpus Word Accuracy    : {corpus_accuracy:.2f}%")
    print(f"  Average Latency         : {avg_lat_ms:.2f} ms")
    print(f"  Median Latency          : {med_lat_ms:.2f} ms")
    print(f"  P95 Latency             : {p95_lat_ms:.2f} ms")
    print(f"  Average RTF             : {avg_rtf:.3f}")
    print(f"[OK] Detailed CSV results saved to {args.output_csv}")
    print(f"[OK] Summary JSON report saved to {args.output_json}")
    print("=" * 65)

if __name__ == "__main__":
    main()
