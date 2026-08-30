# AI4Bharat IndicConformer Hindi ASR Subsystem

An offline, reproducible speech recognition (ASR) subsystem that uses the official [`ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large`](https://huggingface.co/ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large) model — **without fine-tuning** — converted to dynamic 8-bit quantized ONNX for network-free deployment on low-cost Android 9+ hardware (~2 GB RAM).

---

## 📊 Performance Benchmarks (Real Human Speech vs Synthetic Regression)

| Benchmark Dataset | Evaluation Type | Total Clips | INT8 Model Footprint | Corpus WER (%) | Word Accuracy (%) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Mozilla Common Voice Hindi v26.0** | **Real Human Speech** | **100 Clips** | **131.30 MB** | **15.24%** (FP32: 12.62%) | **84.76%** (FP32: 87.38%) |
| **Classroom Hindi Sanity Suite** | **Synthetic gTTS Regression** | **30 Clips** | **131.30 MB** | **18.80%** | **81.20%** |

---

## 1. Installation
```bash
git clone https://github.com/jeshu05/TribeTalk.git
cd TribeTalk/asr

# Install Python dependencies
pip install -r requirements.txt
```

---

## 2. Model Download
Download official PyTorch weights and pre-exported ONNX models:
```bash
python scripts/download_model.py
```

---

## 3. Real Human Speech Evaluation (Mozilla Common Voice)
Create evaluation subset and run real human speech benchmark:
```bash
# 1. Create 100-sample metadata index from local Common Voice dataset
python scripts/create_common_voice_subset.py "C:\Users\jesva\Downloads\1781715680033-cv-corpus-26.0-2026-06-12-hi\cv-corpus-26.0-2026-06-12\hi"

# 2. Evaluate FP32 ONNX Model
python scripts/evaluate_common_voice.py --model models/onnx/indicconformer_hi_ctc_fp32.onnx --output-csv outputs/common_voice_hi_fp32_results.csv --output-json outputs/common_voice_hi_fp32_summary.json

# 3. Evaluate INT8 ONNX Model
python scripts/evaluate_common_voice.py --model models/onnx/model.int8.onnx --output-csv outputs/common_voice_hi_int8_results.csv --output-json outputs/common_voice_hi_int8_summary.json
```

---

## 4. Synthetic Regression Testing (gTTS Pipeline Check)
Run synthetic regression testing across 30 gTTS classroom Hindi utterances:
```bash
python scripts/evaluate_wer.py
```
Outputs: `outputs/wer_results.json` (Synthetic Regression WER = **18.80%**).

---

## 5. Single Audio File Transcription
Transcribe any Hindi audio sample to Devanagari Hindi text:
```bash
python scripts/transcribe.py --audio tests/audio/001.wav --output outputs/test01.txt
```

---

## 6. Baseline Benchmark
Benchmark model loading time, latency (avg, median, P95), RTF, peak RAM, and model disk size:
```bash
python scripts/benchmark.py
```
Outputs: `outputs/baseline_benchmark.json`.

---

## 7. Export Standalone ONNX Graph
Validate and export standalone CTC FP32 ONNX graph:
```bash
python scripts/export_onnx.py
```
Outputs: `models/onnx/indicconformer_hi_ctc_fp32.onnx` (459.73 MB).

---

## 8. INT8 Dynamic Quantization
Quantize model weights to 8-bit dynamic integers:
```bash
python scripts/quantize_int8.py
```
Outputs: `models/onnx/indicconformer_hi_ctc_int8.onnx` (131.30 MB, **71.44% size reduction**).

---

## 9. Android Integration & Build Verification
```bash
# Test Android application build with embedded IndicConformer ASR engine
cd ..
.\gradlew.bat test
.\gradlew.bat assembleDebug
```
All model binaries and tokenizers are bundled in `app/src/main/assets/models/int8/indicconformer_hi_ctc_int8.onnx` for **100% offline operation**.
