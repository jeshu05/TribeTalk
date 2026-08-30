# Phase 5: Baseline Benchmark & Evaluation Summary

This document records the empirical performance and accuracy metrics for `ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large` across both **Real Human Speech** (Mozilla Common Voice) and **Synthetic Regression Testing**.

---

## 💻 Test Environment Profile

* **Device Profile**: Desktop CPU (Intel/AMD Multi-Core)
* **Python Version**: 3.13.13
* **Execution Provider**: ONNX Runtime CPU Execution Provider (4 intra-op threads)
* **Audio Input Format**: 16 kHz Mono PCM Waveform
* **Feature Extractor**: 80-band NeMo Log Mel-Filterbank

---

## 📊 Real Human Speech vs Synthetic Regression Comparison Matrix

| Benchmark Dataset | Evaluation Type | Total Clips | INT8 Model Footprint | Corpus WER (%) | Word Accuracy (%) | Avg Latency (ms) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Mozilla Common Voice Hindi v26.0** | **Real Human Speech** | **100 Clips** | **131.30 MB** | **15.24%** (FP32: 12.62%) | **84.76%** (FP32: 87.38%) | **778.81 ms** |
| **Classroom Hindi Sanity Suite** | **Synthetic gTTS Regression** | **30 Clips** | **131.30 MB** | **18.80%** | **81.20%** | **359.55 ms** |

---

## 🏷️ Relabelling & Dataset Distinction Notice

* **Synthetic Pipeline Regression Dataset** (`asr/tests/metadata.csv`):
  - 30 Google TTS (gTTS) synthetic audio clips.
  - Used strictly for **CI/CD pipeline regression & sanity testing**.
* **Real Human-Speech Benchmark** (`asr/tests/common_voice_hi/metadata.csv`):
  - 100 Mozilla Common Voice Hindi human speech clips from `test.tsv`.
  - Used as the **Primary Benchmark for Real-World ASR Accuracy**.
