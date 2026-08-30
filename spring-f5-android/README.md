# SPRING_F5 Offline Android ONNX TTS Pipeline

An offline, reproducible deployment pipeline that converts the pretrained [`SPRINGLab/SPRING_F5`](https://huggingface.co/SPRINGLab/SPRING_F5) text-to-speech model weights — **without fine-tuning** — into an optimized ONNX Runtime inference engine for Android devices.

---

## 1. What This Project Does
This project converts `SPRINGLab/SPRING_F5` (337.54M parameter DiT + Vocos vocoder) into a modular ONNX pipeline (`spring_f5_transformer.onnx` + `spring_f5_decoder.onnx`) optimized with **FP16** and **INT8 dynamic quantization** for 100% offline, on-device Santali / Ol Chiki speech synthesis on Android 9+.

---

## 2. Installation
```bash
git clone https://github.com/jeshu05/TribeTalk.git
cd TribeTalk/spring-f5-android

# Install Python dependencies
pip install torch torchaudio onnx onnxruntime onnxconverter-common f5-tts vocos soundfile librosa psutil
```

---

## 3. Model Download
Download the pretrained `SPRINGLab/SPRING_F5` weights directly from Hugging Face:
```bash
python -c "
from huggingface_hub import hf_hub_download
hf_hub_download('SPRINGLab/SPRING_F5', 'checkpoints/model_170000.pt', local_dir='checkpoints')
hf_hub_download('SPRINGLab/SPRING_F5', 'checkpoints/vocab.txt', local_dir='checkpoints')
"
```

---

## 4. PyTorch Reference Inference (Phase 3)
Run PyTorch reference synthesis on Santali / Ol Chiki test sentences:
```bash
python scripts/reference_inference.py --text "ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ ᱫᱚ ᱵᱤᱨᱥᱟ ᱠᱟᱱᱟ" --output outputs/reference.wav
```

---

## 5. ONNX Modular Export (Phase 4)
Export `SPRING_F5` DiT backbone and Vocos vocoder into dynamic ONNX graphs:
```bash
python scripts/export_onnx.py --ckpt_path checkpoints/checkpoints/model_170000.pt --output_dir models/fp32
```

---

## 6. ONNX Runtime Validation (Phase 5)
Validate ONNX Runtime FP32 inference against PyTorch reference outputs:
```bash
python scripts/validate_onnx.py
```
Outputs: `outputs/onnx_fp32.wav` and `outputs/validation.json`.

---

## 7. FP16 Optimization (Phase 6)
Convert FP32 model graphs to FP16 precision:
```bash
python scripts/convert_fp16.py
```
Outputs: `models/fp16/spring_f5_transformer.onnx` (646 MB) and `outputs/fp16_validation.json`.

---

## 8. INT8 Dynamic Quantization (Phase 7)
Quantize model weights to 8-bit dynamic integers:
```bash
python scripts/quantize_int8.py
```
Outputs: `models/int8/spring_f5_transformer.onnx` (325 MB) and `outputs/quantization_comparison.json`.

---

## 9. Android Build & APK Integration (Phase 8 & 9)
```bash
# Compile and test Android app with embedded ONNX runtime engine
cd ..
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

---

## 10. 100% Offline Operation & Asset Bundling
All required ONNX binaries, tokenizers, and vocabulary files are bundled locally inside `app/src/main/assets/models/`:
* `models/int8/spring_f5_transformer.onnx` (325.93 MB)
* `models/int8/spring_f5_decoder.onnx` (21.11 MB)
* `models/vocab.txt`

The app makes **zero network requests** during TTS inference.
