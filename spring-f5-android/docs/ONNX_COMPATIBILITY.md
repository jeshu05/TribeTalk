# ONNX Compatibility & Exportability Inspection (Phase 2)

This document details the ONNX exportability of `SPRINGLab/SPRING_F5` based on existing open-source exporters (`DakeQQ/F5-TTS-ONNX` and `SWivid/F5-TTS`).

---

## 🔍 Exporter Analysis Summary

* **Existing Exporters Evaluated**: `DakeQQ/F5-TTS-ONNX`, `SWivid/F5-TTS` runtime exporter.
* **Compatibility Status**: **COMPATIBLE (Modular Graph Split Required)**

### Reasons & Technical Justification:
1. `SPRINGLab/SPRING_F5` shares the `DiT` backbone specification (`dim=1024`, `depth=22`, `heads=16`, `text_dim=512`, `conv_layers=4`) with standard F5-TTS Base.
2. Forcing the iterative Flow Matching (CFM) loop into a single monolithic ONNX graph introduces dynamic loop unrolling overhead that impairs ONNX Runtime execution on mobile devices.
3. Decoupling into **Modular Sub-Graphs** (`spring_f5_transformer.onnx` and `spring_f5_decoder.onnx`) enables 100% standard operator tracing with zero custom PyTorch C++ extensions.

---

## 🧩 Modular Graph Architecture

```
models/
├── fp32/
│   ├── spring_f5_transformer.onnx   (337M DiT Backbone Velocity Predictor)
│   └── spring_f5_decoder.onnx       (Vocos Mel-Spectrogram 24kHz Waveform Decoder)
├── fp16/
│   ├── spring_f5_transformer.onnx
│   └── spring_f5_decoder.onnx
└── int8/
    ├── spring_f5_transformer.onnx   (Quantized Dynamic INT8 Transformer)
    └── spring_f5_decoder.onnx       (Quantized Vocoder)
```

---

## ⚙️ Operator Compatibility & Rewriting Rules

1. **DiT Time Step Embedding**:
   - `time_mlp` uses standard Linear and SiLU layers $\rightarrow$ **100% Standard ONNX Opset 14+**.
2. **Text Embedding ConvNeXt Blocks**:
   - `dwconv` + `norm` + `pwconv` $\rightarrow$ **100% Standard ONNX Operators**.
3. **Multi-Head Self-Attention (MHSA)**:
   - Scaled Dot-Product Attention (SDPA) traced using standard matrix multiplication (`MatMul`) and `Softmax` for cross-platform execution.
4. **Vocos Vocoder Decoder**:
   - Transposed 1D Convolutions (`ConvTranspose1d`) and ResNet blocks $\rightarrow$ **100% ONNX Opset 14+ compatible**.
