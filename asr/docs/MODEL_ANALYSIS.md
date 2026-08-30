# Phase 0: AI4Bharat IndicConformer Model Analysis

This document details the architectural specification, parameter counts, tensor shapes, and input/output contracts for `ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large`.

---

## 📊 Model Overview Specification

* **Model Name**: `ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large`
* **Architecture**: Conformer (Convolutional-augmented Transformer) Hybrid CTC/RNNT
* **Parameter Count**: ~120 Million Parameters (Conformer CTC Encoder + Decoder)
* **Supported Languages**: Hindi (`hi`)
* **Framework**: PyTorch / NeMo / ONNX Runtime
* **Input Audio Format**: 16,000 Hz (16 kHz), Mono PCM Waveform
* **Feature Extractor**: 80-band Log Mel-Spectrogram Filterbank
* **Subsampling**: 4x Subsampling (Depthwise 1D Convolution)
* **Tokenizer / Vocabulary**: 257 Google SentencePiece BPE Tokens (Devanagari Hindi subwords)

---

## 🔌 Tensor Input / Output Contracts

```
[ 16 kHz Mono PCM Waveform ]
             │
             ▼
  80-Band Mel-Filterbank Feature Extractor
             │
             ▼
  [ Tensor: audio_signal (batch, 80, num_frames) ]
  [ Tensor: length       (batch,)                ]
             │
             ▼
 ┌───────────────────────────────────────────────┐
 │ IndicConformer CTC Encoder + Joint Head       │
 └───────────────────────┬───────────────────────┘
                         │
                         ▼
  [ Tensor: logprobs (batch, time_steps, 257) ]
                         │
                         ▼
  CTC Greedy Token Decoder + SentencePiece Unigram
                         │
                         ▼
  [ Devanagari Hindi Text Output ]
```

### Tensor Details:
1. **`audio_signal`**: `[batch_size, 80, num_frames]` (float32, 80-band mel-spectrogram)
2. **`length`**: `[batch_size]` (int64, total frame count)
3. **`logprobs`**: `[batch_size, time_steps, 257]` (float32, log probabilities across 257 subwords)

---

## 📦 Required Dependencies

* `torch` ($\ge 2.0.0$)
* `onnx` ($\ge 1.14.0$)
* `onnxruntime` ($\ge 1.15.0$)
* `torchaudio`
* `librosa`
* `soundfile`
* `numpy`
* `huggingface_hub`
