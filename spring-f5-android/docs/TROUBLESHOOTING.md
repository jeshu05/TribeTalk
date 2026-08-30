# SPRING_F5 Deployment & Troubleshooting Guide

This guide documents common operational edge cases, ONNX Runtime runtime exceptions, and resolution strategies.

---

## 🚨 Common Edge Cases & Fixes

### 1. `RuntimeError: Unknown number type: complex` during Vocoder Export
* **Symptom**: `torch.onnx.export` fails on `torch.istft` in `Vocos` vocoder head.
* **Root Cause**: PyTorch ONNX exporter cannot serialize complex-valued tensors natively in opset 17.
* **Fix Applied**: Replaced `torch.istft` with the `conv_stft` 1D convolution replacement (`STFT(fft_len=1024, win_hop=256, win_len=1024)`).

### 2. `UnicodeEncodeError: 'charmap'` on Windows Console
* **Symptom**: `scripts/reference_inference.py` fails when printing Ol Chiki glyphs (`ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ`).
* **Fix Applied**: Configured stdout encoding at script entry: `sys.stdout.reconfigure(encoding='utf-8')`.

### 3. `PermissionError: WinError 32` during Dynamic Quantization
* **Symptom**: `quantize_dynamic()` fails to remove `*-inferred.onnx` temporary file on Windows.
* **Fix Applied**: Loaded `onnx.ModelProto` into memory before passing to `quantize_dynamic(model_input=model, ...)` avoiding temporary disk file locking.

### 4. `UnsatisfiedLinkError` during Desktop JVM Unit Tests
* **Symptom**: Unit tests fail when calling `OrtEnvironment.getEnvironment()` on Windows host.
* **Fix Applied**: Wrapped ONNX environment initialization in `catch (e: Throwable)` to handle native `.so` library link boundaries gracefully during desktop testing.
