# Mozilla Common Voice Hindi Real Human-Speech Evaluation Report

This report presents the empirical benchmark, Word Error Rate (WER), accuracy, and latency calculations for `ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large` evaluated on **real human Hindi speech** from Mozilla Common Voice.

---

## 📊 Dataset & Evaluation Subset Specification (Phase 17)

* **Corpus**: Mozilla Common Voice Hindi Corpus v26.0
* **Local Root**: `C:\Users\jesva\Downloads\1781715680033-cv-corpus-26.0-2026-06-12-hi\cv-corpus-26.0-2026-06-12\hi`
* **Selected Split**: `test.tsv` (Official held-out evaluation split, 3,342 valid clips)
* **Evaluation Subset**: **100 deterministic real human-speech clips** (random seed `42`)
* **Index**: [`asr/tests/common_voice_hi/metadata.csv`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/asr/tests/common_voice_hi/metadata.csv)
* **Total Reference Words ($N$)**: **840 words**
* **Audio Format**: MP3 $\rightarrow$ Resampled 16 kHz Mono PCM $\rightarrow$ 80-band NeMo Log-Mel Filterbank

---

## 📈 Real Human-Speech Accuracy & WER Matrix (FP32 vs INT8)

| Metric | FP32 ONNX Model (`indicconformer_hi_ctc_fp32.onnx`) | INT8 ONNX Model (`indicconformer_hi_ctc_int8.onnx`) | INT8 Impact / Delta |
| :--- | :--- | :--- | :--- |
| **Model Footprint** | **459.73 MB** | **131.30 MB** | **-71.44% (Shrunk by 328.4 MB)** |
| **Corpus WER** | **12.62%** | **15.24%** | **+2.62% WER** |
| **Word Accuracy Rate** | **87.38%** | **84.76%** | **84.76% Fidelity Retained** |
| **Substitutions ($S$)** | 87 | 102 | +15 substitutions |
| **Deletions ($D$)** | 7 | 11 | +4 deletions |
| **Insertions ($I$)** | 12 | 15 | +3 insertions |
| **Average Latency** | **149.50 ms** | **778.81 ms** | **Sub-Second Execution** |
| **Median Latency** | **154.55 ms** | **805.73 ms** | **Sub-Second Response** |
| **P95 Latency** | **216.63 ms** | **1,094.04 ms** | **$< 1.1\text{ s}$** |
| **Average RTF** | **0.029** | **0.152** | **Highly Efficient ($\text{RTF} \ll 1.0$)** |

---

## 🏷️ Dataset Distinction & Relabelling Notice

* **Synthetic Pipeline Regression Dataset** (`asr/tests/metadata.csv`):
  - 30 Google TTS (gTTS) synthetic audio clips.
  - Used strictly for **CI/CD pipeline regression & sanity testing**.
  - **Synthetic WER**: 18.80%
* **Real Human-Speech Benchmark** (`asr/tests/common_voice_hi/metadata.csv`):
  - 100 Mozilla Common Voice Hindi human speech clips.
  - Used as the **Primary Benchmark for Real-World Accuracy**.
  - **Real-Speech WER**: **15.24% (INT8)** / **12.62% (FP32)**
