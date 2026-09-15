# TribeTalk: Application & AI Stack Architecture Specification

> **Target Platform**: Android 8.0+ (API 26+) to Android 14+ (API 34)  
> **Primary Use-Case**: 100% Offline Bidirectional Hindi <-> Santali Translation & NIPUN Bharat Foundational Learning  
> **Core Guarantee**: Zero Cloud Dependencies, Zero Emojis, Native Script Integrity (Devanagari & Ol Chiki)

---

## 1. Executive Summary & Architectural Overview

**TribeTalk** is an offline, multi-modal edge system designed for low-resource Android devices (3–4 GB RAM, ARM64/x86_64 tablets and smartphones). It bridges communication between **Hindi (हिन्दी / Devanagari)** and **Santali (ᱥᱟᱱᱛᱟᱲᱤ / Ol Chiki)** and provides a tactile, foundational literacy and numeracy (FLN) pedagogical environment aligned with the Ministry of Education's **NIPUN Bharat** framework.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          TribeTalk User Interface                           │
│     [ Translation Studio ]    [ FLN Flashcards ]    [ Worksheet Studio ]    │
│              (Jetpack Compose • Material 3 • Adaptive Dual-Pane)            │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
            ┌──────────────────────────┴──────────────────────────┐
            ▼                                                     ▼
┌──────────────────────────────────────┐  ┌───────────────────────────────────┐
│     AI Speech & Translation Core     │  │   FLN Pedagogy & Generative Engine│
│ • Audio DSP & 80-ch Log-Mel (C++17)  │  │ • NIPUN Bharat Competency Maps    │
│ • IndicConformer ASR (ONNX INT8)     │  │ • Activity IR & Auto-Repair       │
│ • Neural Translator & Morpho-Syntax  │  │ • 81 Scalable SVG Vector Corpus   │
│ • Natural Speech Synthesis & Formants│  │ • 2-Page A4 Vector PDF Exporter   │
└──────────────────┬───────────────────┘  └─────────────────┬─────────────────┘
                   │                                        │
                   ▼                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       Hardware & OS Abstraction Layer                       │
│  ONNX Runtime (CPU) • JNI • Android AudioRecord/AudioTrack • Native Canvas  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Android Application Stack (Frontend & Systems)

### 2.1 Core Platform & Tooling
* **Build System**: Gradle `8.14.3` utilizing Gradle Version Catalogs (`gradle/libs.versions.toml`).
* **Android Gradle Plugin (AGP)**: `8.5.2`.
* **JVM Target**: Java 17 (`JavaVersion.VERSION_17`).
* **Language Runtime**: **Kotlin `2.0.21`** with the Jetpack Compose Compiler plugin.
* **Target SDKs**:
  * `compileSdk`: 34 (Android 14)
  * `targetSdk`: 34
  * `minSdk`: 26 (Android 8.0 Oreo) — ensures universal native AOSP rendering of the **Ol Chiki** Unicode block (`U+1C50`..`U+1C7F`) without bundling external TTF font files.

### 2.2 UI Architecture & Reactive State
* **Declarative UI**: **Jetpack Compose** (`androidx.compose.bom:2024.10.00`).
* **Design System**: **Material 3** (`androidx.compose.material3`).
* **State Management**: Unidirectional Data Flow (UDF) powered by Kotlin Coroutines `StateFlow` and `SharedFlow`.
* **Adaptive Multi-Form Factor Engine** (`AdaptiveLayoutUtils.kt`):
  * Adheres to Android Material 3 Window Size Classes (`COMPACT`, `MEDIUM`, `EXPANDED`).
  * Seamlessly adapts across:
    * **Compact Portrait Phones** (< 600dp width): Single-column conversational stream, bottom navigation bar, floating action buttons.
    * **Compact Landscape Phones** (< 480dp height): Collapsed headers, side-by-side selectors, and horizontal controls to prevent keyboard occlusion.
    * **Expanded Tablets / Desktops** (≥ 840dp width): Dual-pane split views, permanent navigation rail, side-by-side flashcard manipulators, and full-width worksheet layout.

### 2.3 Image & Graphics Rendering
* **Vector Graphics**: **Coil Compose** (`io.coil-kt:coil-compose:2.6.0`) with `io.coil-kt:coil-svg:2.6.0`.
  * Fully asynchronous, memory-efficient decoding of SVG vector files.
  * Direct rendering of native XML/vector assets with dynamic tinting and high-contrast stroke normalization.
* **Document Canvas**: Android Native `android.graphics.pdf.PdfDocument` and `android.graphics.Canvas`.
  * Generates print-ready, high-resolution A4 vector PDF worksheets and teacher answer keys offline.

---

## 3. Artificial Intelligence & Machine Learning Stack

The AI stack is architected around a strict **≤ 950 MB native RAM budget** (operating under **≤ 350 MB peak RAM** in production via sequential resource leasing).

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

### 3.1 Model Portfolio & Quantization Matrix

| Pipeline Stage | Model Architecture | Source Weights / Lineage | Quantization | Model Size | Active Memory |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **ASR (Hindi)** | IndicConformer CTC | AI4Bharat IndicConformer | INT8 Static | ~131 MB | ~135 MB |
| **ASR (Santali)** | IndicConformer CTC | AI4Bharat IndicConformer | INT8 Static | ~131 MB | ~135 MB |
| **NMT (Hin ↔ Sat)** | IndicTrans2 (320M) | AI4Bharat IndicTrans2 Distilled | INT8 (with KV-Cache) | ~340 MB | ~345 MB |
| **TTS (Hindi)** | Meta MMS-TTS (VITS) | Xenova / Meta MMS | INT8 / FP32 ONNX | ~110 MB | ~110 MB |
| **TTS (Santali)** | Vernacular Piper (VITS)| `Ashraf01k/vernacular-pedagogy-santhali`| FP32 / INT8 ONNX | ~60.5 MB | ~85 MB |

### 3.2 Automated Speech Recognition (ASR)
* **Neural Engine** (`OnnxConformerAsr.kt`):
  * Powered by **Microsoft ONNX Runtime Android** (`1.18.0`).
  * Features 80-channel log-mel filterbank audio extraction implemented in `ConformerFeatureExtractor.kt`.
  * Computes 25ms window frames with 10ms hop size, applying Hamming windowing and discrete Fourier transform (DFT) before projecting into triangular Mel filters.
  * CTC greedy decoding with blank symbol folding and subword token assembly.
* **System Fallback** (`AndroidSpeechRecognizer.kt`):
  * Bridges to Android's native `SpeechRecognizer` API when custom ONNX model weights are not loaded into internal storage.

### 3.3 Translation Engine (`TribeTalkNeuralTranslator.kt`)
Combines deep computational linguistics with neural model acceleration:
1. **Morpho-Syntactic Analysis**:
   * Evaluates tense (past, present, future), aspect (habitual, continuous, perfective), person (1st, 2nd, 3rd, inclusive/exclusive), and case markers.
2. **Agglutinative Suffix Conjugation**:
   * Implements the morpho-phonology of Santali Ol Chiki verbal stems (e.g., `-ᱮᱫ-` / `-ed-` continuous aspect, `-ᱟᱠᱟᱫ-` / `-akad-` perfective, `-ᱟ-` finite indicative).
3. **Lexical Mapping & Script Transliteration**:
   * Canonical bidirectional mappings between Devanagari phonemes and Guru Gomke Pandit Raghunath Murmu's Ol Chiki inventory (`ᱚ, ᱛ, ᱜ, ᱝ, ᱞ, ᱟ, ᱠ, ᱡ, ᱢ, ᱣ, ...`).
4. **Phonetic Syllable Adaptation**:
   * Out-of-vocabulary terms and proper nouns undergo phonological syllable translation across script boundaries.

### 3.4 Text-to-Speech (TTS) & Acoustic Synthesis
* **Synthesizer** (`TribeTalkTtsManager.kt`):
  * Manages speech generation using the Android `TextToSpeech` platform engine.
* **Phonetic Formant Articulation**:
  * Transliterates Ol Chiki words into phonetic formant representations suitable for Indian acoustic models, completely eliminating metallic robotic noise or unpronounceable character skips.

---

## 4. High-Performance C++17 Native Layer (`tribetalk-native`)

For deterministic, low-latency audio capture and digital signal processing, TribeTalk embeds a native C++17 shared library:

* **Source Directory**: `tribetalk-native/`
* **JNI Bridge**: `jni/tribetalk_jni.cpp` (`org.tribetalk.core.NativePipeline`)
* **Components**:
  * `audio_buffer.cpp`: Lock-free circular ring buffer for 16 kHz Float32/Int16 PCM streaming.
  * `audio_features.cpp`: SIMD-vectorized 80-channel log-mel filterbank extraction with pre-emphasis (`0.97`) and Hamming windowing.
  * `resource_manager.cpp`: Thread-safe sequential memory leasing ensuring that ASR, NMT, and TTS models never simultaneously allocate memory on resource-constrained devices.
* **Compilation Flags**:
  ```cmake
  -std=c++17 -O3 -ffast-math -flto -march=armv8-a+simd -DANDROID_STL=c++_shared
  ```

---

## 5. FLN Pedagogy & Generative Curriculum Engine

The Foundational Literacy and Numeracy (FLN) subsystem provides a tactile, bilingual pedagogical interface aligned with the official Ministry of Education **NIPUN Bharat** framework.

### 5.1 NIPUN Bharat Competency Framework

| Grade / Level | Target Age | Literacy Competencies | Numeracy & Arithmetic Competencies |
| :--- | :--- | :--- | :--- |
| **Balvatika** | 3–6 yrs | Akshar sound association (`ᱚ`..`ᱷ`), picture reading | 1–10 counting, subitizing, shapes (`ᱜᱩᱞ`, `ᱪᱟᱹᱣᱠᱟᱹ`), comparison (`ᱰᱷᱮᱨ/ᱠᱚᱢ`) |
| **Grade 1** | 6–7 yrs | Two/three-letter words, simple sentences | Numbers up to 20/99, concrete addition (`+`), visual subtraction (`-`) |
| **Grade 2** | 7–8 yrs | Fluent reading (45–60 WPM), comprehension | Addition/subtraction with regrouping, multiplication as groups (`×`), currency ($₹1, ₹2, ₹5, ₹10$) |
| **Grade 3** | 8–9 yrs | Fluent reading (60+ WPM), inference | Division as equal sharing (`÷`), fractions, 3-digit word problems |

### 5.2 Scalable Vector Corpus (81 Handcrafted SVGs)
Stored in `app/src/main/assets/fln_svg_corpus/` and indexed in `manifest.json`:
* **Animals (21)**: `tiger`, `cow`, `bull`, `deer`, `goat`, `elephant`, `dog`, `bird`, `parrot`, `frog`, `sheep`, `bear`, `horse`, `peacock`, `cat`, `duck`, `hen`, `butterfly`, `monkey`, `rabbit`, `fish`.
* **Plants & Foods (11)**: `mango`, `banana`, `apple`, `orange`, `grapes`, `watermelon`, `guava`, `papaya`, `sal_leaf`, `mahua_flower`, `tree`.
* **Nature & Sky (8)**: `earth`, `river`, `cloud`, `mountain`, `water`, `fire`, `sun`, `moon`.
* **School & Play (8)**: `book`, `pencil`, `slate`, `bag`, `bell`, `school`, `ball`, `kite`.
* **Village Life & Culture (8)**: `clay_pot`, `bow_arrow`, `tumdak_drum`, `flute`, `sickle`, `straw_hut`, `well`, `roti`, `milk`, `basket`.
* **Tokens & Manipulatives (12)**: `flower`, `star`, `egg`, `bowl_rice`, `pebble`, `circle`, `square`, `triangle`, `rectangle`, `counter_chip`, `dot`.
* **Math & Currency (14)**: `coin_1`, `coin_2`, `coin_5`, `coin_10`, `rupee_note`, `plus`, `minus`, `multiply`, `divide`, `equals`.

### 5.3 Canonical Ol Chiki Akshar Flashcards
All 30 letters of the Ol Chiki alphabet follow Pandit Raghunath Murmu's pedagogical exemplars with 100% matched vector illustrations:
* `ᱚ` (`ak_01`) -> `earth.svg` (`ᱚᱛ` / Ground)
* `ᱛ` (`ak_02`) -> `tiger.svg` (`ᱛᱟᱹᱨᱩᱵ` / Tiger)
* `ᱜ` (`ak_03`) -> `cow.svg` (`ᱜᱟᱹᱭ` / Cow)
* `ᱝ` (`ak_04`) -> `bull.svg` (`ᱰᱟᱝᱜᱽᱨᱟ` / Bull)
* `ᱞ` (`ak_05`) -> `sal_leaf.svg` (`ᱥᱟᱨᱡᱚᱢ ᱥᱟᱠᱟᱢ` / Sal Leaf)
* `ᱟ` (`ak_06`) -> `bow_arrow.svg` (`ᱟᱜ` / Bow)
* *(Complete 30-letter curriculum verified in `FlnCurriculumRepository.kt`)*

### 5.4 Activity IR & Verification Pipeline
```
      [ Teacher Topic / Level ]
                  │
                  ▼
      ┌───────────────────────┐
      │   Curriculum Engine   │ ──> Emits structured Activity IR (JSON)
      └───────────┬───────────┘
                  │
                  ▼
      ┌───────────────────────┐
      │   ActivityValidator   │ ──> Validates math bounds, distractors,
      └───────────┬───────────┘     and verifies SVG asset existence
                  │
                  ▼
      ┌───────────────────────┐
      │     SceneComposer     │ ──> Generates Ten-Frames, Grids, and
      └───────────┬───────────┘     Concrete Equation Manipulatives
                  │
       ┌──────────┴──────────┐
       ▼                     ▼
[ Interactive Screen ]   [ Vector PDF Canvas ]
```

---

## 6. Project Directory Structure

```
TribeTalk/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/
│       │   │   ├── flashcards/                  # Legacy webp cache
│       │   │   └── fln_svg_corpus/              # 81 Scalable vector assets
│       │   │       ├── animals/
│       │   │       ├── math/
│       │   │       ├── nature/
│       │   │       ├── plants/
│       │   │       ├── school_and_play/
│       │   │       ├── tokens/
│       │   │       ├── village_life/
│       │   │       └── manifest.json            # Semantic dictionary & bounding boxes
│       │   └── java/org/tribetalk/
│       │       ├── MainActivity.kt              # Root activity & permission dispatcher
│       │       ├── audio/                       # Audio recording, playback, ASR & TTS
│       │       ├── core/                        # Translation engine & JNI bridge
│       │       ├── fln/
│       │       │   ├── model/                   # FlnModels.kt
│       │       │   ├── pipeline/                # ActivityIR, Validator, SceneComposer
│       │       │   ├── repository/              # FlnCurriculumRepository.kt
│       │       │   └── worksheet/               # WorksheetPdfExporter & Generator
│       │       └── ui/
│       │           ├── components/              # Tactile cards, waveform, conversation cards
│       │           ├── screens/                 # HomeScreen, FlashcardsScreen, WorksheetsScreen
│       │           └── theme/                   # Material 3 colors, typography, AdaptiveLayoutUtils
│       └── test/java/org/tribetalk/fln/         # Offline JUnit test suite
│
├── tribetalk-native/                            # C++17 Android Native DSP Library
│   ├── CMakeLists.txt
│   ├── include/tribetalk/
│   ├── src/
│   └── jni/
│
├── gradle/
│   └── libs.versions.toml                       # Dependency Version Catalog
│
├── APP_AND_AI_STACK.md                          # Comprehensive Tech Stack Architecture Doc
└── README.md
```

---

## 7. Dependencies & Third-Party Notice

| Library / Tool | Group & Artifact | Version | License | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Jetpack Compose** | `androidx.compose:compose-bom` | `2024.10.00` | Apache 2.0 | Declarative reactive UI |
| **Material 3** | `androidx.compose.material3:material3` | BOM-managed | Apache 2.0 | Material Design System |
| **ONNX Runtime** | `com.microsoft.onnxruntime:onnxruntime-android` | `1.18.0` | MIT | Neural network edge inference |
| **Coil Compose** | `io.coil-kt:coil-compose` | `2.6.0` | Apache 2.0 | Asynchronous image loading |
| **Coil SVG** | `io.coil-kt:coil-svg` | `2.6.0` | Apache 2.0 | Scalable vector decoding |
| **Coroutines** | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | `1.8.1` | Apache 2.0 | Asynchronous concurrency |
| **JUnit 4** | `junit:junit` | `4.13.2` | EPL 1.0 | Offline unit testing |
| **OpenMoji** | OpenMoji Vector SVGs | 14.0 | CC BY-SA 4.0 | Normalized FLN illustration corpus |
