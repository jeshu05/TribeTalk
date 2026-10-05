# TribeTalk (ट्राइबटॉक / ᱴᱨᱟᱭᱤᱵᱽᱴᱚᱠ)

**TribeTalk** is a production-grade, fully offline, bidirectional speech-to-speech translation system bridging **Hindi (हिन्दी - Devanagari)** and **Santali (ᱥᱟᱱᱛᱟᱲᱤ - Ol Chiki)**.

Engineered specifically for low-resource edge deployments (3–4 GB RAM, ARM64 Android tablets, CPU-first inference), TribeTalk ensures indigenous communities and healthcare/administrative workers can converse fluidly without requiring internet connectivity or cloud APIs.

---

## 🏛️ System Architecture

```
                      [ Audio Input (16 kHz Float32 PCM) ]
                                       │
                                       ▼
                       ┌───────────────────────────────┐
                       │       ASR Engine (ONNX)       │
                       │   IndicConformer CTC INT8     │
                       │   • 80-ch Log-Mel Filterbank  │
                       │   • Greedy CTC Blank Folding  │
                       └───────────────┬───────────────┘
                                       │ Source Text (Devanagari / Ol Chiki)
                                       ▼
                       ┌───────────────────────────────┐
                       │      IndicTrans2 NMT INT8     │
                       │   Encoder-Decoder + KV Cache  │
                       │   • Script Normalization      │
                       │   • Subword Tokenization      │
                       └───────────────┬───────────────┘
                                       │ Target Text (Ol Chiki / Devanagari)
                                       ▼
                       ┌───────────────────────────────┐
                       │       TTS Engine (ONNX)       │
                       │   • Santali: Vernacular Piper │
                       │   • Hindi: MMS-TTS (VITS)     │
                       └───────────────┬───────────────┘
                                       │
                                       ▼
                      [ Audio Output (16 kHz Float32 PCM) ]
```

---

## 💾 Model Portfolio (100% Offline ONNX)

To satisfy the strict **≤ 950 MB native RAM budget** on edge tablets, all models have been consolidated into pure ONNX Runtime INT8 architectures:

| Pipeline Stage | Language | Architecture | Source Model | Size | Quantization |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **ASR** | Hindi | IndicConformer CTC | `ai4bharat-indicconformer-hindi-onnx` | ~131 MB | INT8 Static |
| **ASR** | Santali | IndicConformer CTC | `ai4bharat-indicconformer-santali-onnx` | ~131 MB | INT8 Static |
| **NMT** | Hin ↔ Sat | IndicTrans2 Distilled (320M) | `indictrans2-indic-indic-dist-320M-ONNX-int8` | ~340 MB | INT8 (with past KV-cache) |
| **TTS** | Santali | Vernacular Piper (VITS) | `Ashraf01k/vernacular-pedagogy-santhali` | ~60.5 MB | FP32 / INT8 ONNX |
| **TTS** | Hindi | Meta MMS-TTS (VITS) | `Xenova/mms-tts-hin` | ~110 MB | INT8 / FP32 ONNX |

### ⚡ Memory Bounding: Sequential Leasing
With the **Global Model Resource Manager** (`tribetalk/resource_manager.py` / `tribetalk-native/src/resource/resource_manager.cpp`), execution stages are strictly isolated:
- **ASR active**: ~135 MB
- **NMT active**: ~345 MB
- **TTS active**: ~110 MB
- **Peak RAM footprint**: **≤ 350 MB** (well below the 950 MB ceiling).

---

## 📂 Codebase Layout

```
TribeTalk/
├── tribetalk/                  # Pure Python Reference & Verification Layer
│   ├── asr/
│   │   ├── common/             # Audio preprocessing, interfaces, types
│   │   ├── hindi/              # Hindi NeMo baseline
│   │   ├── santali/            # Santali Whisper baseline
│   │   └── onnx/               # IndicConformer ONNX pure runtime (production)
│   ├── translation/
│   │   ├── common/             # Translation interfaces & types
│   │   ├── indictrans/         # IndicTrans2 ONNX INT8 engine
│   │   └── normalization.py    # Indic script & Ol Chiki normalizer
│   ├── tts/
│   │   ├── common/             # Speech synthesis interfaces & types
│   │   ├── hindi/              # Meta MMS-TTS ONNX engine
│   │   └── santali/            # Vernacular Piper Santali ONNX engine
│   ├── pipeline/
│   │   ├── orchestrator.py     # End-to-end bidirectional state machine
│   │   └── types.py            # Pipeline data classes & lifecycle events
│   └── resource_manager.py     # Thread-safe sequential memory manager
│
├── tribetalk-native/           # High-Performance C++17 Android Native Core
│   ├── CMakeLists.txt          # Modern CMake build configuration
│   ├── include/tribetalk/
│   │   ├── audio/              # Audio buffer & 80-ch log-mel filterbank
│   │   ├── asr/                # C++ ASR inference engine
│   │   ├── translation/        # C++ IndicTrans2 inference engine
│   │   ├── tts/                # C++ TTS synthesis engine
│   │   ├── resource/           # C++ thread-safe resource manager
│   │   └── pipeline/           # C++ pipeline orchestrator & state machine
│   ├── src/                    # Implementation files
│   └── jni/
│       └── tribetalk_jni.cpp   # JNI bridge for Android app integration
│
├── app/                        # Android Jetpack Compose Application
│   ├── src/main/java/org/tribetalk/
│   │   ├── audio/              # OnnxConformer ASR, AudioRecorder & TTS Manager
│   │   ├── core/               # NativePipeline, NmtEngine, MemoryGovernor
│   │   │   └── memory/         # Sequential single-model leasing for 2GB tablets
│   │   ├── curriculum/         # FLN curriculum generator & Qwen 0.5B ONNX SLM
│   │   ├── fln/                # Flashcards, image loader & PDF worksheet exporter
│   │   └── ui/                 # Modern Jetpack Compose UI (Screens, Theme, Components)
│   └── build.gradle.kts        # Modern Android Gradle build configuration
│
├── tests/                      # 121 Comprehensive Unit & Integration Tests
│   ├── asr/                    # 48 tests (audio, CTC, ONNX, normalization)
│   ├── translation/            # 28 tests (IndicTrans2, KV-cache, scripts)
│   ├── tts/                    # 27 tests (MMS, Piper, character mapping)
│   └── pipeline/               # 18 tests (E2E speech-to-speech, memory manager)
│
├── scripts/                    # Quantization, staging, and model evaluation utilities
│   ├── run_phase2_asr.py       # ASR quantization and evaluation harness
│   ├── run_phase2_qwen.py      # Qwen 0.5B INT8 SLM export and benchmark
│   ├── run_phase3_nmt.py       # IndicTrans2 INT8 quantization and testing
│   ├── quantize_models.py      # Automated INT8 dynamic quantization
│   └── migrate_models_to_android.py # Automated ADB model deployment to device
│
├── SETUP_GUIDE.md              # Complete Android Studio & on-device model setup guide
├── ENGINEERING.md              # Detailed low-level engineering specification
├── APP_AND_AI_STACK.md         # Full architecture and model stack documentation
├── requirements.txt            # Python PC staging dependencies
└── README.md                   # System documentation
```

---

## 📱 Android Application & NIPUN Bharat FLN Suite

TribeTalk features a production-ready Android application built with **Jetpack Compose** and **Material 3**:

1. **Bidirectional Voice Bridge (Hindi ↔ Santali)**:
   - On-device speech recognition via **IndicConformer CTC INT8**.
   - Offline neural machine translation via **IndicTrans2 INT8**.
   - Natural voice speech synthesis with authentic Ol Chiki phonetic pronunciation and Hindi TTS.
2. **NIPUN Bharat Foundational Literacy & Numeracy (FLN)**:
   - Authentic NCERT CNCL bilingual worksheets covering Balvatika to Grade 3.
   - Dual-sheet PDF export with dedicated Answer Keys for educators.
   - 8 worksheet modalities: Letter Tracing, Sound Matching, Word Search, Fill in the Blanks, Math Numeracy, and Story Comprehension.
3. **Interactive 3D Flashcards**:
   - Kid-friendly cartoon vector illustrations with touch-flip animations.
   - Bilingual display (Hindi Devanagari + Santali Ol Chiki) with one-tap phonetic audio pronunciation.
4. **On-Device SLM Generative Overhaul**:
   - Local on-device Small Language Model (**Qwen 0.5B INT8 ONNX**) for generating contextual rural classroom exercises without internet connectivity.
5. **MemoryGovernor for 2GB Edge Tablets**:
   - Enforces sequential single-stage residency (`ASR` → `NMT` → `SLM` → `TTS`).
   - Evicts inactive model sessions automatically, maintaining heap footprint well within budget on budget rural classroom devices.

For step-by-step instructions on setting up Android Studio, building the APK, and deploying neural model assets to your physical device or emulator, refer to **[SETUP_GUIDE.md](file:///SETUP_GUIDE.md)**.

---

## 🚀 Quick Start

### 1. Android Development Setup
Refer to the dedicated **[SETUP_GUIDE.md](file:///SETUP_GUIDE.md)** for Android Studio configuration, Gradle build instructions, and automated model deployment via ADB.

To compile the Android debug APK from the command line:
```bash
./gradlew assembleDebug
```

To run Android unit tests:
```bash
./gradlew testDebugUnitTest
```

### 2. Python Environment Setup
Ensure Python 3.10+ is installed:
```bash
python -m venv .venv
# On Windows:
.venv\Scripts\activate
# On Linux/macOS:
source .venv/bin/activate

pip install -r requirements.txt
```

### 3. Running Test Suite
Execute the full 121-test verification suite:
```bash
pytest tests/ -v
```

### 4. Building the Native C++ Engine (Desktop)
```bash
mkdir tribetalk-native/build
cmake -B tribetalk-native/build -S tribetalk-native
cmake --build tribetalk-native/build --config Release
```

---

## 🔬 Benchmark Verification
Evaluation runs verify bidirectional conversion across gold audio benchmarks from `tribe-evaluation/`:
- **IndicVoices Hindi Test Audio** $\to$ Hindi ASR $\to$ IndicTrans2 $\to$ Santali Piper TTS.
- **IndicVoices Santali Test Audio** $\to$ Santali ASR $\to$ IndicTrans2 $\to$ Hindi MMS-TTS.

---

## 📄 License
TribeTalk is distributed under the Apache-2.0 License.
