# SPRING_F5 Technical Architecture Specification (Phase 1)

This document provides the complete empirical breakdown of the `SPRINGLab/SPRING_F5` multilingual text-to-speech model.

---

## 🏗️ End-to-End Pipeline Execution Flow

```
[ Santali / Ol Chiki Text ]
             │
             ▼
 ┌───────────────────────┐
 │ 1. Character Tokenizer│ ➔ Maps Ol Chiki / Devanagari characters to numerical token IDs (Vocab: 3,413)
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │ 2. Reference Mel Ext. │ ➔ 100-band Mel-Spectrogram from Vocos Feature Extractor
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │ 3. DiT Transformer    │ ➔ 337.54 Million Parameter Flow-Matching Backbone
 │    Backbone (CFM)     │    (dim=1024, depth=22, heads=16, text_dim=512, conv_layers=4)
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │ 4. Vocos Vocoder      │ ➔ Decodes 100-band Mel-Spectrogram to 24 kHz PCM Waveform
 └───────────┬───────────┘
             │
             ▼
     [ 24 kHz Audio ]
```

---

## 📊 Detailed Component Specifications

| Component | Python Class | Source File | Model Weights / Checkpoint | Input Tensors | Output Tensors | Shape / Dtype | ONNX Exportable? |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Character Tokenizer** | Custom Vocab Parser | `f5_tts/infer/utils_infer.py` | `checkpoints/vocab.txt` | Raw String | Token ID Sequence | `[batch, seq_len]` (int64) | ✅ Yes (Builtin Lookup) |
| **Mel Feature Extractor** | `Vocos` Mel Spec | `vocos/pretrained.py` | `charactr/vocos-mel-24khz` | 24 kHz PCM Waveform | Mel-Spectrogram | `[batch, 100, mel_len]` (float32) | ✅ Yes |
| **DiT Transformer Backbone** | `DiT` | `f5_tts/model/backbones/dit.py` | `checkpoints/model_170000.pt` (337.54M params) | `x` (mel noisy), `t` (step), `cond` (text embedding) | Velocity Vector `v` | `[batch, mel_len, 100]` (float32) | ✅ Yes (Sub-graph Export) |
| **CFM ODE Iterative Solver** | `CFM` | `f5_tts/model/cfm.py` | Euler ODE Solver | Initial Gaussian Noise `x0` | Generated Mel-Spectrogram | `[batch, 100, mel_len]` (float32) | ✅ Yes (Loop / Unrolled ONNX) |
| **Vocos Vocoder Decoder** | `Vocos` | `vocos/pretrained.py` | `charactr/vocos-mel-24khz` | Generated Mel-Spectrogram | 24 kHz Audio PCM Waveform | `[batch, 1, samples]` (float32) | ✅ Yes |

---

## ⚡ Ground-Truth Baseline Metrics (RTX 3050 GPU)

* **Pretrained Checkpoint**: `SPRINGLab/SPRING_F5` (`model_170000.pt`, 5,151 MB file)
* **Model Parameters**: 337.54 Million Parameters
* **Sample Rate**: 24,000 Hz
* **Model Load Time**: 5,932.1 ms (5.93 s)
* **Inference Time (PyTorch)**: 3,021.2 ms (3.02 s) for 7.66s audio
* **Real-Time Factor (RTF)**: **0.394**
* **Peak Memory Usage (RSS)**: 2,192.54 MB
