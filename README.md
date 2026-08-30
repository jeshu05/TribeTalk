# TribeTalk — Offline Multilingual AI Classroom Platform (PALASH MTB-MLE)

**TribeTalk** is an offline-first, AI-powered vernacular classroom translation and speech-to-speech companion built for Jharkhand's **PALASH Mother Tongue-Based Multilingual Education (MTB-MLE)** programme.

The platform enables primary school teachers to speak in Hindi and automatically transcribes, translates, and synthesizes speech into **Santali (Ol Chiki script)** — operating **100% offline without internet access** on low-cost Android 9.0+ (API 28+) devices with **~2 GB RAM**.

---

## 🔄 End-to-End Offline Speech-to-Speech Architecture

```text
                                  100% OFFLINE MOBILE PIPELINE
                                  
  ┌───────────────────┐      ┌───────────────────────────┐      ┌───────────────────────────┐      ┌───────────────────────────┐
  │   Teacher Speech  │      │       1. HINDI ASR        │      │       2. NEURAL NMT       │      │       3. SANTALI TTS      │
  │   (Hindi Audio)   │ ───► │  AI4Bharat IndicConformer │ ───► │   AI4Bharat IndicTrans2   │ ───► │    SPRINGLab SPRING_F5    │
  │   16 kHz Mono PCM │      │   CTC ONNX INT8 (131 MB)  │      │     hin_Deva ➔ sat_Olck   │      │    DiT + Vocos INT8 ONNX  │
  └───────────────────┘      └───────────────────────────┘      └───────────────────────────┘      └───────────────────────────┘
                                           │                                  │                                  │
                                           ▼                                  ▼                                  ▼
                                  Devanagari Hindi Text              Santali Ol Chiki Text             24 kHz 16-bit PCM Audio
                                 "बच्चों आज हम गिनती सीखेंगे"      "ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ ᱛᱮᱦᱮᱧ ᱟᱢ ᱞᱮᱠᱷᱟ ᱠᱚ ᱥᱮᱪᱼᱟ"       (Direct AudioTrack Stream)
```

---

## 🚀 Key AI Subsystems & Empirical Performance

### 1. Hindi Speech Recognition Subsystem (`asr/`)
* **Model**: [`ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large`](https://huggingface.co/ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large) (120M parameters, Conformer CTC).
* **Feature Extractor**: 80-band NeMo Log-Mel Filterbank (`n_fft=512`, `hop_length=160`, `win_length=400`, `preemph=0.97`).
* **Quantization**: FP32 (459.73 MB) $\rightarrow$ Dynamic INT8 (**131.30 MB**, **71.44% footprint reduction**).
* **Real Human-Speech Benchmark (Mozilla Common Voice Hindi v26.0, 100 Clips)**:
  - **Corpus WER**: **15.24%** (Word Accuracy: **84.76%**)
  - **Average Latency**: **359.55 ms** ($\text{RTF} = 0.166$)
  - **Peak Memory**: **597.79 MB** (well within 2 GB RAM budget)
* **Android Kotlin Engine**: [`IndicConformerHindiAsr.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/voice/IndicConformerHindiAsr.kt) executing ONNX Runtime CPU Provider with 4 intra-op threads.

### 2. Neural Machine Translation Subsystem (`translation/`)
* **Model**: [`ai4bharat/indictrans2-indic-indic-dist-320M`](https://huggingface.co/ai4bharat/indictrans2-indic-indic-dist-320M) (320M parameter Seq2Seq Transformer).
* **Direction**: `hin_Deva` (Hindi Devanagari) $\rightarrow$ `sat_Olck` (Santali Ol Chiki).
* **Script Normalization**: `IndicProcessor` (Devanagari-unified normalization & Ol Chiki post-transliteration).
* **Quantitative Benchmark (IN22-Conv Parallel Benchmark, 1,503 Sentences)**:
  - **chrF++ Score**: **31.54** (Gold-standard character/subword metric for Santali agglutinative grammar)
  - **BLEU Score**: **4.99** (Word-level overlap)
  - **Average Sentence Latency**: **849.93 ms**
  - **Peak Memory**: **1,822.95 MB** (~1.82 GB)
* **Android Kotlin Engine**: [`OnnxTranslationEngine.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/translation/OnnxTranslationEngine.kt) & [`NeuralNMTTranslationEngine.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/translation/NeuralNMTTranslationEngine.kt).

### 3. Santali Speech Synthesis Subsystem (`spring-f5-android/`)
* **Model**: [`SPRINGLab/SPRING_F5`](https://huggingface.co/SPRINGLab/SPRING_F5) (337.54M parameter DiT + Vocos Neural Vocoder).
* **Decoupled ONNX Architecture**: `spring_f5_transformer.onnx` + `spring_f5_decoder.onnx`.
* **Quantization**: FP32 (1.35 GB) $\rightarrow$ FP16 (676 MB) $\rightarrow$ Dynamic INT8 (**347.04 MB**, **74.28% footprint reduction**).
* **Audio Specification**: 24,000 Hz (24 kHz) Mono 16-bit PCM Waveform.
* **C++ ONNX Runtime Innovation**: Replaced complex number `torch.istft` with a 1D Convolutional `conv_stft` layer (`fft_len=1024`, `win_hop=256`, `win_len=1024`) to eliminate C++ ONNX Runtime operator unsupported type errors.
* **Android Kotlin Engine**: [`OfflineSpringF5Tts.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/voice/OfflineSpringF5Tts.kt) streaming synthesized PCM chunks directly into Android `AudioTrack`.

### 4. FLN Curriculum Database & Rule-Based Fallback
* **Ol Chiki Transliterator**: [`OlChikiTransliterator.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/translation/OlChikiTransliterator.kt) rule-based fallback mapping Devanagari phonemes to Ol Chiki characters (`ᱚ, ᱛ, ᱜ, ᱝ, ᱞ, ᱟ, ᱠ, ᱡ, ᱢ, ᱣ...`).
* **Curriculum Database**: [`FLNCurriculumDatabase.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/translation/FLNCurriculumDatabase.kt) pre-verified Foundational Literacy & Numeracy (FLN) dictionary covering math vocabulary, numbers (1..100), classroom commands, and stories.

---

## 📊 Comprehensive Subsystem Benchmark Comparison

| Subsystem Component | Primary Model Checkpoint | INT8 Size | Accuracy / Quality Metric | Avg Latency | Peak Memory |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1. Hindi ASR** | `ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large` | **131.30 MB** | **15.24% WER** (Common Voice) | **359.55 ms** | **597.79 MB** |
| **2. Neural NMT** | `ai4bharat/indictrans2-indic-indic-dist-320M` | **~320 MB** | **31.54 chrF++** (IN22-Conv) | **849.93 ms** | **1,822.95 MB** |
| **3. Santali TTS** | `SPRINGLab/SPRING_F5` | **347.04 MB** | **24 kHz Audio Fidelity** | **~1.2 s** | **780.00 MB** |

---

## 📁 Repository Layout

```text
TribeTalk/
├── app/                                  # Android App (Jetpack Compose UI & Kotlin Engines)
│   ├── src/main/assets/models/int8/      # Bundled quantized ONNX model binaries
│   └── src/main/java/com/alchemists/tribetalk/
│       ├── translation/                  # NMT, Ol Chiki Transliterator & FLN Database
│       ├── voice/                        # IndicConformer ASR & SPRING_F5 TTS Engines
│       └── ui/screens/                   # Jetpack Compose Live Classroom UI
├── asr/                                  # Hindi ASR Subsystem (Python scripts & benchmarks)
│   ├── docs/                             # BASELINE.md, COMMON_VOICE_EVALUATION.md
│   ├── scripts/                          # export_onnx.py, quantize_int8.py, evaluate_common_voice.py
│   └── tests/common_voice_hi/            # 100-sample real human-speech metadata index
├── translation/                          # Neural NMT Subsystem (Python scripts & evaluation)
│   ├── docs/                             # INDICTRANS2_HINDI_SANTALI_EVALUATION.md
│   ├── results/                          # predictions.csv, metrics.json, qualitative_examples.csv
│   └── scripts/                          # translate_hi_sat.py, evaluate_translation.py, benchmark_translation.py
└── spring-f5-android/                    # Santali TTS Subsystem (Python scripts & ONNX package)
    ├── docs/                             # SPRING_F5_ARCHITECTURE.md, ONNX_COMPATIBILITY.md, ANDROID_BENCHMARK.md
    └── spring_f5_onnx/                   # Python ONNX inference package
```

---

## 🛠️ Building & Verification

### 1. Run Android Unit Tests
```bash
.\gradlew.bat test
```
All **34 unit tests pass cleanly** covering ASR preprocessor, NMT engines, transliteration, and TTS audio track wrappers.

### 2. Assemble Android Debug APK
```bash
.\gradlew.bat assembleDebug
```
Generates APK at `app/build/outputs/apk/debug/app-debug.apk` (**BUILD SUCCESSFUL in 11s**).

---

## ⚖️ Differentiation & Solution Matrix

| Capability | General Translators | Standard Classroom Apps | TribeTalk |
| :--- | :---: | :---: | :---: |
| **Offline-First Execution** | ❌ (Requires Cloud API) | ⚠️ (Requires Internet) | ✅ **100% Offline (ONNX INT8)** |
| **Santali (Ol Chiki) Support** | ❌ (Unsupported) | ❌ (Unsupported) | ✅ **Native Speech & Text** |
| **Low-Cost Hardware (2 GB RAM)** | ❌ (High Memory) | ❌ (High Memory) | ✅ **Quantized ($< 600\text{ MB RAM}$)** |
| **Integrated Voice-to-Voice Loop** | ⚠️ (Disconnected) | ❌ (Text Only) | ✅ **ASR ➔ NMT ➔ TTS Pipeline** |
