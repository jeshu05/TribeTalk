# Stage 2: Hindi -> Santali Learning Content Pipeline & Offline Content Pack Subsystem

This subsystem builds an **Offline Content Pack Generator** for Jharkhand's PALASH MTB-MLE programme.

## 🚀 How to Run and Test

### 1. Build the Offline Content Pack
Run the build script to translate structured Hindi templates via `IndicTrans2`, synthesize $24\text{ kHz}$ mono 16-bit PCM Santali audio via `SPRING_F5`, and bundle assets into `app/src/main/assets/content_pack/`:

```bash
python stage2/scripts/build_content_pack.py
```

### 2. Run Python Tests
```bash
python stage2/tests/test_content_pipeline.py
```

### 3. Run Android Unit Tests & Verify APK Build
```bash
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

---

## 🏗️ Build-Time vs Runtime Separation

- **BUILD TIME (Python Pipeline)**: Generates Hindi content, translates Hindi $\rightarrow$ Santali, synthesizes 24 kHz Santali WAV audio, and creates JSON manifests.
- **RUNTIME (Android Tablet App)**: Dynamically loads `manifest.json` and `lesson.json` from `assets/content_pack/`, rendering text and playing pre-rendered WAV audio with **0 ONNX model overhead** during classroom operation!
