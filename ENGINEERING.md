# TribeTalk: Systems & Software Engineering Specification

> **Engineering Focus**: Edge-First Architecture, Zero-Cloud Reliability, Resource-Bounded Systems & Cross-Language Interop  
> **Target Environments**: Low-Resource Android Tablets (ARM64 / x86_64, 3–4 GB RAM, Android 8.0+ / API 26+)

---

## 1. Engineering Philosophy & Core Principles

The engineering architecture of **TribeTalk** is governed by five non-negotiable principles:

1. **100% Offline-First Determinism**:
   - The entire application operates completely disconnected from the Internet.
   - All AI models (ASR, NMT, TTS), linguistic rules, pedagogical curricula, vector graphics, and document generators reside locally on-device.
   - No external API keys, telemetry pings, or cloud roundtrips exist in the runtime path.

2. **Strict Hardware-Bounded Execution (≤ 350 MB Peak RAM)**:
   - Target devices in tribal rural primary schools and health centers are typically low-cost Android tablets with 3–4 GB total system RAM.
   - To prevent Android Low Memory Killer (LMK) daemon kills, the pipeline enforces **Sequential Model Leasing**, ensuring peak active memory never exceeds ~350 MB.

3. **Authentic Linguistic & Script Integrity**:
   - Full support for the indigenous **Santali** language in its native **Ol Chiki** script (`U+1C50`..`U+1C7F`) and **Hindi** in **Devanagari** (`U+0900`..`U+097F`).
   - Strict zero-emoji policy across all code, logs, data models, UI components, and exported documents to maintain professional pedagogical and administrative standards.

4. **Layered Separation of Concerns**:
   - Clean decoupling between the **Native Systems Layer (C++17)**, **Neural ML Runtime (ONNX)**, **Computational Linguistics Core (Kotlin)**, **Declarative UI (Jetpack Compose)**, and **Pedagogical Generator (Activity IR)**.

5. **Self-Healing & Defensively Typed Pipelines**:
   - Generative content and curriculum pipelines do not render raw or unvalidated structures.
   - Every generated exercise passes through a deterministic validation and auto-repair phase before touching the UI or PDF canvas.

---

## 2. Multi-Layer System Architecture

```
┌───────────────────────────────────────────────────────────────────────────────────┐
│                               Presentation Layer                                  │
│   • Jetpack Compose Declarative UI                                                │
│   • Material 3 Design System & Tactile Theme Tokens                               │
│   • Adaptive Multi-Form Factor Engine (Window Size Classes: Compact / Expanded)   │
│   • Unidirectional Data Flow (StateFlow / SharedFlow / MVVM)                      │
└────────────────────────────────────────┬──────────────────────────────────────────┘
                                         │
┌────────────────────────────────────────┴──────────────────────────────────────────┐
│                             Domain & Pedagogy Layer                               │
│   • NIPUN Bharat Foundational Competency Matrices (Balvatika - Grade 3)           │
│   • Canonical Pandit Raghunath Murmu Akshar Curricula (30 Letters)                │
│   • Activity IR (Intermediate Representation) Schema                              │
│   • Deterministic ActivityValidator & Auto-Repair Rulebook                        │
│   • SceneComposer (Ten-Frames, Grid Arrays, Equation Manipulatives)               │
└────────────────────────────────────────┬──────────────────────────────────────────┘
                                         │
┌────────────────────────────────────────┴──────────────────────────────────────────┐
│                            Core Engineering Engines                               │
│   ┌─────────────────────────────────────┐   ┌─────────────────────────────────┐   │
│   │     Neural Computational Engine     │   │      Document & Vector Core     │   │
│   │ • Morpho-Syntactic Linguistic Parser│   │ • Coil SVG Decoder Pipeline     │   │
│   │ • Ol Chiki Agglutinative Conjugator │   │ • 81-Asset Curated SVG Corpus   │   │
│   │ • Phonetic Formant Speech Synthesizer│  │ • Native Canvas PDF Exporter    │   │
│   └─────────────────────────────────────┘   └─────────────────────────────────┘   │
└────────────────────────────────────────┬──────────────────────────────────────────┘
                                         │
┌────────────────────────────────────────┴──────────────────────────────────────────┐
│                          Native Systems Layer (C++17)                             │
│   • JNI Cross-Language Boundary (`tribetalk_jni.cpp`)                              │
│   • Lock-Free Circular Ring Audio Buffer (`audio_buffer.cpp`)                      │
│   • SIMD-Vectorized 80-Channel Log-Mel Filterbank (`audio_features.cpp`)          │
│   • Sequential Resource Manager & State Machine (`resource_manager.cpp`)          │
│   • Hardware Vector Flags: `-march=armv8-a+simd -O3 -ffast-math -flto`            │
└────────────────────────────────────────┬──────────────────────────────────────────┘
                                         │
┌────────────────────────────────────────┴──────────────────────────────────────────┐
│                               OS & Hardware Base                                  │
│   Android Linux Kernel • AOSP Audio HAL • ONNX Runtime Native C++ API • CPU Cores │
└───────────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Native Systems & DSP Engineering (`tribetalk-native`)

For performance-critical digital signal processing (DSP) and memory control, the project utilizes an embedded C++17 shared library (`libtribetalk_native.so`).

### 3.1 Lock-Free Circular Ring Audio Buffer (`audio_buffer.cpp`)
Real-time audio streaming from the Android microphone requires predictable latency without dynamic heap allocation jitter in the audio recording loop:
* Implemented as an atomic circular ring buffer storing 16-bit / 32-bit float PCM data.
* Eliminates audio glitches, buffer overruns, and garbage collection pauses.
* Supports linear reading into contiguous memory chunks required by FFT routines.

### 3.2 SIMD-Vectorized Feature Extraction (`audio_features.cpp`)
Speech models require an 80-channel log-mel spectrogram computed from raw time-domain audio:
* **Pre-emphasis filter**: $y[t] = x[t] - 0.97 \cdot x[t-1]$ to boost high frequencies.
* **Windowing**: Pre-computed Hann window ($25\text{ ms}$, 400 samples at $16\text{ kHz}$) applied with zero dynamic calculation per frame.
* **Frame Stride**: $10\text{ ms}$ (160 samples hop size).
* **Mel-Filterbank**: 80 triangular filters spanning $0\text{ Hz}$ to $8000\text{ Hz}$, initialized at startup into a sparse matrix representation.
* **ARM NEON Vectorization**: Compilation flags (`-march=armv8-a+simd -O3 -ffast-math`) enable compiler auto-vectorization across 128-bit NEON registers, achieving $>4.5\times$ speedup over unvectorized scalar code.

### 3.3 Sequential Memory Leasing (`resource_manager.cpp`)
To fit within strict RAM ceilings on edge hardware, the C++ `ResourceManager` implements **Sequential Leasing**:
* The inference lifecycle is modeled as a state machine:
  $$\text{IDLE} \longrightarrow \text{ASR} \longrightarrow \text{TRANSLATION} \longrightarrow \text{TTS} \longrightarrow \text{IDLE}$$
* In `MemoryMode::SEQUENTIAL`, acquiring the model for stage $N$ automatically unloads and evicts the model from stage $N-1$:
  ```cpp
  void ResourceManager::evict_previous_if_sequential() {
      if (mode_ != MemoryMode::SEQUENTIAL) return;
      if (asr_hi_ && asr_hi_->is_loaded()) asr_hi_->unload();
      if (asr_sat_ && asr_sat_->is_loaded()) asr_sat_->unload();
      if (translation_ && translation_->is_loaded()) translation_->unload();
      if (tts_hi_ && tts_hi_->is_loaded()) tts_hi_->unload();
      if (tts_sat_ && tts_sat_->is_loaded()) tts_sat_->unload();
  }
  ```
* **Memory Footprint Profile**:
  * Standby / Idle: $\approx 45\text{ MB}$
  * Active ASR (IndicConformer): $\approx 135\text{ MB}$
  * Active Translation (IndicTrans2): $\approx 345\text{ MB}$
  * Active TTS (Piper/MMS): $\approx 110\text{ MB}$
  * **Peak Native RAM**: $\le 350\text{ MB}$ (prevents OS Out-Of-Memory kills).

### 3.4 JNI Interoperability & Exception Safety
The Java Native Interface layer (`tribetalk_jni.cpp`) connects Kotlin's `NativePipeline.kt` to the native C++ core:
* **RAII Lifecycle**: All native pointers are owned via `std::unique_ptr` and `std::shared_ptr`.
* **Exception Containment**: Native methods wrap execution in `try / catch` blocks to catch standard C++ exceptions and re-throw them cleanly as Java runtime exceptions rather than causing native `SIGSEGV` segmentation faults.
* **Zero-Copy Arrays**: Wherever possible, `GetPrimitiveArrayCritical` or direct memory buffers are utilized to pass audio buffers across the JVM-native boundary.

---

## 4. Computational Linguistics & Neural Translation Engineering

Rather than relying purely on large cloud language models or static phrase tables, TribeTalk implements a **hybrid computational linguistic and on-device neural architecture** in [`TribeTalkNeuralTranslator.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/org/tribetalk/core/TribeTalkNeuralTranslator.kt).

### 4.1 Santali (Ol Chiki) Agglutinative Morphology
Santali belongs to the Austroasiatic (Munda) language family, characterized by rich agglutination where multiple grammatical suffixes attach to verbal roots:
* **Verbal Morphology Decomposition**:
  * **Root Stem**: e.g., `ᱡᱚᱢ` (*jom* - to eat), `ᱪᱟᱞᱟᱣ` (*chalaw* - to go), `ᱚᱞ` (*ol* - to write).
  * **Aspect Suffixes**: Continuous `-ᱮᱫ-` (*-ed-*), Inchoative `-ᱚᱜ-` (*-og-*), Perfective `-ᱟᱠᱟᱫ-` (*-akad-*).
  * **Transitivity & Voice Markers**: Active voice `-ᱟᱱ-` vs. Passive/Middle `-ᱮᱱ-`.
  * **Finite Indicative Particle**: Postfixed `-ᱟ` (*-a*).
  * **Pronominal Subject Clitics**: 1st person singular `-ᱧ` (*-nj*), 2nd person singular `-ᱢ` (*-m*), 3rd person singular `-ᱭ` (*-y*).
* **Algorithmic Conjugator**:
  Given a base verb and grammatical features, the conjugator synthesizes valid Ol Chiki structures deterministically:
  $$\text{Root} + \text{Aspect} + \text{Transitivity} + \text{Indicative} + \text{Clitic}$$
  *Example*: `ᱡᱚᱢ` (eat) + `-ᱮᱫ-` (cont) + `-ᱟ-` (ind) + `-ᱭ` (3sg) $\implies$ `ᱡᱚᱢᱮᱫᱟᱭ` (*jomeday* - "he/she is eating").

### 4.2 Script Normalization & Syllable Transliteration
* **Dual Script Mappings**: Full bijective mappings between Unicode Devanagari (`U+0900`..`U+097F`) and Unicode Ol Chiki (`U+1C50`..`U+1C7F`).
* **Guru Gomke Phonetic Alignment**: All consonants, vowels, and modifier marks (`ᱸ` Mu Ttudag, `ᱹ` Gahuled, `ᱺ` Mu Gahuled, `ᱻ` Relha, `ᱼ` Ahadd) are explicitly mapped and normalized.
* **Phonological Adaptation for OOV**: Out-of-vocabulary loan words from Hindi/Sanskrit undergo phonetic transformation into compatible Ol Chiki syllable structures (vowel epenthesis and consonant cluster simplification).

---

## 5. Pedagogical Generative Pipeline (FLN Engineering)

TribeTalk's Foundational Literacy and Numeracy (FLN) pipeline decouples pedagogical logic from graphical presentation through a typed intermediate representation.

```
       [ Curriculum Level: Balvatika / Grade 1 / Grade 2 / Grade 3 ]
                                    │
                                    ▼
       ┌────────────────────────────────────────────────────────────┐
       │              Structured Activity IR Schema                 │
       │  • Activity Type (COUNT_AND_MATCH, ADDITION_CONCRETE, etc.)│
       │  • Primary Object Key ("mango", "tiger", "coin_5")         │
       │  • Quantities, Distractor Sets, Target Answer              │
       │  • Bilingual Prompt (Ol Chiki + Devanagari Hindi)          │
       └────────────────────────────┬───────────────────────────────┘
                                    │
                                    ▼
       ┌────────────────────────────────────────────────────────────┐
       │             ActivityValidator (Self-Healing)               │
       │  • Validates mathematical correctness (A + B = C, A - B ≥ 0)│
       │  • Checks asset existence against 81-asset SVG corpus      │
       │  • Repairs missing/duplicate options & distractors         │
       └────────────────────────────┬───────────────────────────────┘
                                    │
                                    ▼
       ┌────────────────────────────────────────────────────────────┐
       │                       SceneComposer                        │
       │  • Ten-frame subitizing layout composer (2 × 5 arrays)     │
       │  • Concrete equation visual layout with operation tokens   │
       │  • Crossing-out subtraction visual annotator               │
       └────────────────────────────┬───────────────────────────────┘
                                    │
                   ┌────────────────┴────────────────┐
                   ▼                                 ▼
┌─────────────────────────────────────┐   ┌─────────────────────────────────────┐
│       Interactive Compose Screen    │   │      2-Page Native A4 PDF Canvas    │
│  • Tactile flashcard manipulation   │   │  • Page 1: Student worksheet        │
│  • Dual-language audio pronunciation│   │  • Page 2: Teacher answer key       │
│  • Instant answer validation        │   │  • 5-star pedagogical rubric        │
└─────────────────────────────────────┘   └─────────────────────────────────────┘
```

### 5.1 Deterministic Activity Validation (`ActivityValidator.kt`)
The validator operates as a strict firewall:
* **Arithmetic Invariants**: Subtraction problems must never yield negative numbers; addition results must not exceed grade-level upper bounds ($10$ for Balvatika, $20$ for Grade 1, $99$ for Grade 2, $999$ for Grade 3).
* **Corpus Asset Grounding**: Verifies that the requested `primaryObjectKey` exists in `manifest.json`. If an unknown object is requested, it automatically remaps to the closest pedagogical synonym (e.g., `"fruit"` $\to$ `"mango"`, `"animal"` $\to$ `"cow"`).
* **Distractor Isolation**: Ensures multiple-choice options contain exactly one correct answer and non-duplicate distractor values within $\pm 2$ of the target answer.

### 5.2 Scalable Vector Corpus Architecture
* **Directory Structure**: 81 standardized SVG vector files located in `app/src/main/assets/fln_svg_corpus/`.
* **Normalization Standard**:
  * Every SVG is normalized to `viewBox="0 0 100 100"`.
  * Clean, continuous black line art with vibrant primary/secondary fills, optimized for both full-color OLED displays and low-cost monochrome primary school photocopiers.
* **Semantic Index (`manifest.json`)**:
  Maps each SVG to its English key, Ol Chiki name, Devanagari Hindi name, pedagogical category, difficulty tier, and search tags.

### 5.3 Vector PDF Canvas Rendering (`WorksheetPdfExporter.kt`)
* **Zero Dependency Native Rendering**: Direct drawing via `android.graphics.pdf.PdfDocument` and `android.graphics.Canvas`.
* **Print Specification**: Exact A4 dimensions ($595 \times 842\text{ pt}$ at $72\text{ DPI}$).
* **Two-Page Architecture**:
  * **Page 1**: Official Indian Government Primary School banner (`ᱯᱨᱟᱛᱷᱢᱤᱠ ᱟᱥᱲᱟ / प्राथमिक विद्यालय`), student metadata box (`ᱧᱩᱛᱩᱢ`, `ᱨᱳᱞ ᱱᱚ`, `ᱢᱟᱹᱦᱤᱛ`), high-contrast problem exercises, and a 5-star teacher evaluation rubric (`Emerging`, `Developing`, `Proficient`).
  * **Page 2**: Comprehensive Teacher Answer Key with step-by-step solutions, Ol Chiki numeral cheat sheet, and NIPUN competency rubric alignment.

---

## 6. UI/UX & Adaptive Engineering

### 6.1 Multi-Form Factor Responsive Engine (`AdaptiveLayoutUtils.kt`)
TribeTalk runs on varied hardware: budget 5-inch phones, field tablets, horizontal stands, and large classroom displays.
* Implements Material 3 **Window Size Classes**:
  * **Compact Width** ($< 600\text{ dp}$): Single-column conversational layout, bottom navigation, full-width swipeable cards.
  * **Compact Height** ($< 480\text{ dp}$): Landscape phone orientation automatically collapses top app bars into a tight horizontal toolbar to preserve vertical typing space.
  * **Expanded Width** ($> 840\text{ dp}$): Permanent navigation rail on the left, dual-pane layout showing conversation/exercise on the left and manipulatives/tools on the right.

### 6.2 Visual Accessibility & Tactile Interaction
* **Font Scaling**: Massive front-card typography ($40\text{sp}$ to $60\text{sp}$) for children learning letter sounds and numeral recognition.
* **Dual Audio Triggers**: Separate, tactile buttons for Santali (`ᱥᱟᱱᱛᱟᱲᱤ`) and Hindi (`हिन्दी`) audio playback with visual speaking-state animations.
* **Zero Cognitive Clutter**: Internal developer keys, JSON payloads, and English technical labels are completely stripped from child-facing views.

---

## 7. Quality Assurance & Verification Engineering

### 7.1 Automated Testing Matrix
The project maintains a multi-tier testing pipeline:
1. **Curriculum & Asset Grounding Tests** ([`FlnCurriculumRepositoryTest.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/test/java/org/tribetalk/fln/FlnCurriculumRepositoryTest.kt)):
   - `testAllCardsHaveValidSvgAssets`: Iterates through all 120+ master curriculum cards, asserting that every card references a `.svg` asset that physically exists on the disk.
   - `testAllCategoriesArePopulatedAndSegregated`: Asserts that each of the 9 categories (`AKSHAR`, `NUMBERS`, `ANIMALS`, `FRUITS`, `NATURE`, `SCHOOL`, `SPATIAL`, `ARITHMETIC`, `MONEY`) contains cards and has zero cross-contamination.
2. **Deterministic Validator & Auto-Repair Tests**:
   - Fuzz testing `ActivityValidator` with negative numbers, missing distractors, and empty options to guarantee self-healing behavior.
3. **C++ DSP & Buffer Unit Tests**:
   - Verifies zero-leakage circular buffer wrap-around and log-mel filterbank mathematical equivalence against Python reference baselines.

### 7.2 Build Verification Metrics
* **Unit Test Suite**: 24 tests, 100% pass rate (`BUILD SUCCESSFUL in 13s`).
* **Full Debug APK Assembly**: 40 actionable tasks completed cleanly (`BUILD SUCCESSFUL in 1m 7s`).
* **APK Footprint**: Total vector asset corpus adds $< 200\text{ KB}$ to the final APK size, maintaining a lightweight distribution footprint for rural Android devices.
