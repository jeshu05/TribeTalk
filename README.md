# TribeTalk — Offline Vernacular AI Classroom Platform
### *Bridging the Hindi ↔ Santali (Ol Chiki) Linguistic Divide in Primary Tribal Education*
#### Developed for Jharkhand's **PALASH Mother Tongue-Based Multilingual Education (MTB-MLE)** & **NIPUN Bharat** Programme

[![Android 9.0+ (API 28+)](https://img.shields.io/badge/Platform-Android%209.0%2B%20(API%2028%2B)-brightgreen.svg)](https://developer.android.com)
[![100% Offline](https://img.shields.io/badge/Network-100%25%20Offline%20First-blue.svg)](#-edge-ai-constraints--system-specifications)
[![Target RAM: 2 GB](https://img.shields.io/badge/Memory%20Budget-%3C%20500%20MB%20(2%20GB%20RAM)-orange.svg)](#-edge-ai-constraints--system-specifications)
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-72%2F72%20Passing-success.svg)](#-building-testing--verification)
[![Hindi ASR](https://img.shields.io/badge/ASR-IndicConformer%20INT8%20(15.24%25%20WER)-purple.svg)](#1-hindi-automatic-speech-recognition-asr)
[![Neural NMT](https://img.shields.io/badge/NMT-IndicTrans2%20INT8%20(31.54%20chrF%2B%2B)-teal.svg)](#2-5-tier-hybrid-translation-engine)
[![Santali TTS](https://img.shields.io/badge/TTS-Parler--TTS%20%2F%20SPRING__F5%20(24%20kHz)-red.svg)](#3-santali-speech-synthesis-tts)

---

## 📖 Executive Summary & Real-World Challenge

In primary schools across Jharkhand's tribal heartlands, foundational classroom instruction is predominantly delivered in **Standard Hindi (Devanagari script)**. However, tens of thousands of indigenous primary students enter Grade 1 speaking exclusively **Santali** (an Austroasiatic Munda language with complex agglutinative morphology and unique phonology) written in the **Ol Chiki script** (`ᱚ, ᱛ, ᱜ, ᱝ...`).

This linguistic divide creates immediate foundational comprehension deficits, leading to early school dropouts and alienation. Furthermore, rural schools face severe real-world constraints:
* **Zero Internet Connectivity**: Village classrooms have unreliable or completely non-existent cellular broadband; cloud-dependent AI translation APIs (Google Cloud, OpenAI, Azure) fail completely and risk student data privacy.
* **Low-Cost Hardware Realities**: Government-issued classroom devices are budget Android tablets equipped with only **~2 GB of RAM**, where large multi-gigabyte models cause immediate Out-Of-Memory (OOM) crashes and Android Low Memory Killer (LMK) evictions.
* **Script vs. Language Disconnect**: Teachers who speak only standard Hindi cannot read the Ol Chiki alphabet or pronounce unreleased checked consonants, making text-only translation ineffective.

**TribeTalk** solves this challenge through an **edge-first, end-to-end multimodal AI platform**:
1. **Live Bidirectional Voice Bridge**: Transcribes teacher Hindi speech offline, translates it into Santali Ol Chiki, and synthesizes native 24 kHz Santali speech in real-time.
2. **Pedagogical Classroom Suite**: Provides structured bilingual FLN lessons, NIPUN Bharat aligned visual flashcards with active-recall study modes, auto-generated bilingual worksheets with native A4 vector PDF export, local classroom learning insights, and offline diagnostics.

---

## 🔄 End-to-End System Architecture

```text
                                        TRIBETALK ARCHITECTURE
                                                  │
          ┌───────────────────────────────────────┴───────────────────────────────────────┐
          │                                                                               │
          ▼                                                                               ▼
  🎙️ REAL-TIME VOICE BRIDGE (Phases 12 & 13)                                 📚 PEDAGOGICAL CLASSROOM SUITE
  ┌───────────────────────────────────────────────┐               ┌───────────────────────────────────────────────┐
  │  1. Continuous Audio Record (16 kHz Mono)     │               │  1. FLN Lesson Library (7 Curated Units)      │
  │     └─ AudioVADProcessor (Energy + ZCR)       │               │     └─ 3-Part: Script / Activity / Assessment │
  │                                               │               │                                               │
  │  2. Hindi ASR (IndicConformer INT8 ONNX)      │               │  2. Visual Flashcards (NIPUN Goals 1, 2, 3)   │
  │     └─ LiveUtteranceProcessor (NLP Clean)     │               │     └─ Interactive Study Mode (Recall Toggle) │
  │                                               │               │                                               │
  │  3. 5-Tier Hybrid Translation Engine          │               │  3. Auto-Generated Bilingual Worksheets       │
  │     └─ TM ➔ FLN Corpus ➔ Subword ➔ NMT ➔ G2P  │               │     └─ Native A4 Vector PDF Compiler          │
  │                                               │               │                                               │
  │  4. Sequential Utterance Queue (FIFO)         │               │  4. Local Learning Insights (Zero-Cloud Log)  │
  │     └─ Parallel TTS Background Pre-Synthesis  │               │     └─ NIPUN FLN Domain Progress Bars         │
  │                                               │               │                                               │
  │  5. Santali Audio Playback Queue (24 kHz PCM) │               │  5. Offline Diagnostics & Settings            │
  │     └─ Mic Feedback Prevention Coordination   │               │     └─ Room/SQLite TM & Live Voice Test       │
  └───────────────────────────────────────────────┘               └───────────────────────────────────────────────┘
```

---

## 🚀 The 6 Core Application Modules

The TribeTalk application is structured into 6 interconnected classroom modules managed from a unified dashboard:

```text
                                       TRIBETALK MAIN DASHBOARD
                                                  │
   ┌──────────────────┬───────────────────┼───────────────────┬──────────────────┬──────────────────┐
   │                  │                   │                   │                  │                  │
   ▼                  ▼                   ▼                   ▼                  ▼                  ▼
🎙️ Live Classroom  📚 Lesson Library   🎴 Visual Flashcards 📄 FLN Worksheets  📊 Insights        ⚙️ Settings
Hindi ↔ Santali    Literacy & Math     NIPUN Goals 1-3      6 Question Types   Local Analytics    Diagnostics &
Voice Bridge       Curriculum Units    Study Carousel       A4 PDF Generator   Domain Coverage    TTS Health Check
```

### 1. 🎙️ Live Classroom (Bidirectional Voice Bridge)
* **Dual-Pane Conversational HUD**:
  * **Teacher Channel (Hindi ➔ Santali)**: Continuously records teacher speech, displays live Devanagari transcripts, displays translated Ol Chiki with phonetic guides, and synthesizes native Santali speech.
  * **Student Channel (Santali ➔ Hindi)**: Captures student responses in Santali and translates back to Hindi for the teacher.
* **Continuous Microphone Streaming & VAD**:
  * Employs [AudioVADProcessor.kt](app/src/main/java/com/alchemists/tribetalk/voice/AudioVADProcessor.kt) with sliding 20–30 ms energy and zero-crossing rate analysis.
  * Detects speech pauses (~1.2s silence) to segment utterances naturally without manual button clicks.
* **Sequential Pipelining with Audio Queuing (Phases 12 & 13)**:
  * **[LiveUtteranceQueue.kt](app/src/main/java/com/alchemists/tribetalk/nlp/LiveUtteranceQueue.kt)**: Decouples microphone ingestion from downstream inference.
  * **[SantaliAudioQueue.kt](app/src/main/java/com/alchemists/tribetalk/voice/SantaliAudioQueue.kt)**: While Utterance A is playing aloud, Utterance B is simultaneously translated and pre-synthesized in the background, preventing audio overlap, stutter, or clipped speech.
* **Acoustic Feedback Suppression**:
  * Coordinates microphone listening state during TTS playback, preventing synthesized Santali audio from feeding back into the Hindi ASR recognizer.
* **Live Pipeline Telemetry**:
  * [LatencyTracker.kt](app/src/main/java/com/alchemists/tribetalk/nlp/LatencyTracker.kt) displays real-time execution breakdowns (ASR latency, NMT latency, TTS latency, and total roundtrip).
* **Teacher In-Place Editing & Translation Memory**:
  * Teachers can tap any translation card to edit translated text.
  * Edits can be immediately committed to [TranslationMemory.kt](app/src/main/java/com/alchemists/tribetalk/translation/TranslationMemory.kt) to prioritize local dialectal variations.

### 2. 📚 FLN Lesson Library & Pedagogical Units
* **7 Curated Demonstration Units** (aligned with JCERT & NIPUN Bharat standards):
  * *Literacy & Oral Language*: Greetings and Introductions (`नमस्ते एवं परिचय`), Basic Vocabulary & Daily Objects (`शब्दावली एवं दैनिक वस्तुएं`), Letter & Sound Awareness (`ध्वनि एवं Ol Chiki लिपि समझ`), Simple Sentences (`सरल वाक्य रचना`).
  * *Numeracy & Mathematical Thinking*: Counting 1–10 (`संख्या ज्ञान एवं १-१० गिनती`), Number Recognition & Addition (`संख्या पहचान एवं जोड़`), Shapes Around Us (`हमारे आस-पास के आकार`).
* **3-Part Pedagogical Layout**:
  1. 📖 **Lesson Script**: Parallel Hindi and Santali dialogues with Ol Chiki script and Latin phonetic pronunciation.
  2. 🎯 **Activity Instructions**: Actionable pedagogical exercises for classroom group participation.
  3. 📝 **Assessment Prompts**: Formative evaluation questions for teachers to assess student comprehension.
* **Dynamic Audio Playback**:
  * `▶ PLAY SANTALI` synthesizes lesson scripts using the real Santali TTS engine.
* **Teacher Editor with Dynamic Re-Translation**:
  * Teachers can customize lesson scripts, activities, or questions, and trigger instant re-translation into Santali.
* **1-Tap Module Bridges**:
  * `[ 🎴 FLASHCARDS ]` pre-fills the flashcard generator with the lesson's target vocabulary.
  * `[ 📄 WORKSHEET ]` pre-fills the worksheet generator with the lesson's core learning outcomes.

### 3. 🎴 NIPUN Bharat Visual Flashcard Generator & Study Mode
* **Aligned with NIPUN Bharat's 3 Developmental Goals**:
  * **Goal 1: Health & Well-being** (Living beings, animals, fruits, nature).
  * **Goal 2: Effective Communicators** (Everyday objects, greetings, action words).
  * **Goal 3: Involved Learners** (Numbers 1–10, geometric shapes, spatial counting).
* **Bilingual Card Generation**:
  * Generates 5 curated visual cards containing Hindi text, Santali Ol Chiki text, Latin phonetic pronunciation guide, and NIPUN developmental goal badges.
* **Interactive Single-Card Carousel & Study Mode**:
  * Optimized large-font UI designed for low-cost Android tablet screens.
  * **"SHOW SANTALI" / "HIDE SANTALI" Active Recall Toggle**: Hides the Santali script to test student recall before revealing the correct Ol Chiki glyphs.
* **Local Photo Integration & Editing**:
  * Teachers can capture photos or select gallery images to replace card visuals with locally familiar classroom objects.
  * Full in-place editing with automatic re-translation.

### 4. 📄 Auto-Generated Bilingual FLN Worksheets & Native A4 PDF Export
* **6 Balanced Pedagogical Question Types**:
  1. *Fill in the Blanks (खाली स्थान भरें)*
  2. *Match the Following (सही मिलान करें)*
  3. *True or False (सही या गलत)*
  4. *Multiple Choice Questions (बहुविकल्पीय प्रश्न)*
  5. *Word Meaning (शब्दार्थ लिखें)*
  6. *Sentence Translation (सरल वाक्य अनुवाद)*
* **Dual-Script Presentation**:
  * Every exercise presents standard Hindi side-by-side with Santali (Ol Chiki) and designated student answer lines.
* **Teacher Review & Live Re-Translation**:
  * Teachers can adjust any prompt or option; secondary action buttons re-translate edits immediately.
* **Native Offline A4 PDF Vector Compiler**:
  * [WorksheetPdfExporter.kt](app/src/main/java/com/alchemists/tribetalk/worksheet/WorksheetPdfExporter.kt) uses Android's native `android.graphics.pdf.PdfDocument` API.
  * Directly embeds [NotoSansOlChiki-Regular.ttf](app/src/main/assets/fonts/NotoSansOlChiki-Regular.ttf) into the vector canvas.
  * Generates print-ready A4 PDFs complete with school headers, student roll number blocks, bilingual exercises, and ruled response boxes—with **zero cloud reliance**.

### 5. 📊 Classroom Learning Insights (Local Analytics)
* **Classroom Engagement Metrics**:
  * 📘 Total lessons utilized
  * 🎴 Flashcard sessions conducted
  * 📄 Worksheets generated and exported
  * 🎤 Real-time voice translations executed
* **NIPUN FLN Domain Progress**:
  * Visual progress bars tracking coverage across *Foundational Literacy*, *Foundational Numeracy*, and *Environmental Awareness*.
* **Chronological Session Log**:
  * Real-time activity feed showing pedagogical actions taken during classroom hours.
* **100% Zero-Cloud Privacy Guarantee**:
  * All analytics are computed and persisted locally on the tablet. No telemetry, student metrics, or usage logs leave the device.

### 6. ⚙️ Offline Diagnostics & System Settings
* **Language Configuration**:
  * Source: `Hindi (हिन्दी - Devanagari)` | Target: `Santali (ᱥᱟᱱᱛᱟᱲᱤ - Ol Chiki)`.
* **Offline Readiness Verification**:
  * Translation Memory: `✓ Available (Local SQLite/Room)`
  * FLN Curriculum Corpus: `✓ 440+ Verified Items`
  * Flashcard & Worksheet Engines: `✓ Available Offline`
  * Lesson Library: `✓ 7 Demo Units Available`
  * Network Requirement: `Not Required (100% Offline)`
* **Dual TTS Backend Configuration**:
  * Supports LAN FastAPI endpoints (`http://<LAN-IP>:8000`) for high-fidelity AI4Bharat Indic Parler-TTS with instant fallback to on-device neural TTS.
  * **`[ ▶ TEST SANTALI VOICE (ᱡᱚᱦᱟᱨ) ]`**: Instant acoustic audio test button.

---

## 🧠 Core AI / ML Subsystems & Empirical Performance

```text
                                  AI & SPEECH PROCESSING SUBSYSTEMS
                                                  │
         ┌────────────────────────────────┼────────────────────────────────┐
         │                                │                                │
         ▼                                ▼                                ▼
  1. HINDI SPEECH ASR            2. 5-TIER HYBRID NMT             3. SANTALI TTS SYNTHESIS
  ai4bharat/indicconformer       Hybrid Edge Engine               AI4Bharat Parler-TTS /
  CTC Hybrid (120M Params)       (TM ➔ FLN ➔ Subword ➔ NMT ➔ G2P) SPRING_F5 INT8 / Piper VITS
  INT8: 131.30 MB                IndicTrans2 320M INT8            INT8: 347.04 MB / 24 kHz
  WER: 15.24% (Common Voice)     chrF++: 31.54 (IN22-Conv)        Direct AudioTrack Stream
```

### 1. Hindi Automatic Speech Recognition (ASR)
* **Model Checkpoint**: [`ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large`](https://huggingface.co/ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large) (120M parameters, Conformer CTC).
* **Audio Preprocessor**: 80-band NeMo Log-Mel Spectrogram Filterbank (`n_fft=512`, `hop_length=160`, `win_length=400`, `preemph=0.97`).
* **INT8 Dynamic Quantization**: FP32 (459.73 MB) $\rightarrow$ INT8 (**131.30 MB**, **71.44% size reduction**).
* **Mozilla Common Voice Hindi v26.0 Benchmark (100 Real Human Clips)**:
  * **Corpus WER**: **15.24%** (Word Accuracy: **84.76%**)
  * **Average Latency**: **359.55 ms** ($\text{RTF} = 0.166$)
  * **Peak Memory Footprint**: **597.79 MB**
* **Kotlin Runtime**: [IndicConformerHindiAsr.kt](app/src/main/java/com/alchemists/tribetalk/voice/IndicConformerHindiAsr.kt) & [VoiceInputManager.kt](app/src/main/java/com/alchemists/tribetalk/voice/VoiceInputManager.kt) with Android System Recognizer fallback.

### 2. 5-Tier Hybrid Translation Engine
* **Architecture**: [HybridEdgeAITranslationEngine.kt](app/src/main/java/com/alchemists/tribetalk/translation/HybridEdgeAITranslationEngine.kt) cascades through 5 cascading tiers for optimal speed and linguistic precision:
  * **Priority 1: Teacher Translation Memory** ([TranslationMemory.kt](app/src/main/java/com/alchemists/tribetalk/translation/TranslationMemory.kt)): Instant local SQLite/CSV cache of teacher-approved corrections ($0\text{ ms}$ latency).
  * **Priority 2: Curated NIPUN FLN & JCERT Database** ([FLNCurriculumDatabase.kt](app/src/main/java/com/alchemists/tribetalk/translation/FLNCurriculumDatabase.kt)): 440+ hand-verified foundational vocabulary pairs covering classroom instructions, mathematics (1–100), animals, and stories.
  * **Priority 3: Agglutinative & Subword Morphological Matcher**: Resolves inflected prefixes and suffixes common in Santali grammar.
  * **Priority 4: Neural IndicTrans2 Transformer** ([NeuralNMTTranslationEngine.kt](app/src/main/java/com/alchemists/tribetalk/translation/NeuralNMTTranslationEngine.kt)): ONNX-quantized [`ai4bharat/indictrans2-indic-indic-dist-320M`](https://huggingface.co/ai4bharat/indictrans2-indic-indic-dist-320M).
  * **Priority 5: G2P Dual-Script Transliteration** ([OlChikiTransliterator.kt](app/src/main/java/com/alchemists/tribetalk/translation/OlChikiTransliterator.kt)): Rule-based fallback generating Ol Chiki glyphs, Devanagari phonetic pronunciation, and Latin IPA stress marks.
* **IN22-Conv Parallel Benchmark (1,503 Sentences)**:
  * **chrF++ Score**: **31.54** (Character/subword gold standard for agglutinative Santali).
  * **BLEU Score**: **4.99**.
  * **Average Sentence Latency**: **849.93 ms**.

### 3. Santali Speech Synthesis (TTS)
* **Dual-Engine Strategy**:
  * **Primary (LAN / Microservice)**: AI4Bharat Indic Parler-TTS serviced through a high-performance FastAPI endpoint ([RealSantaliTTSProvider.kt](app/src/main/java/com/alchemists/tribetalk/voice/RealSantaliTTSProvider.kt)) with local audio caching and HTTP health checks.
  * **Offline Edge Option**: Decoupled `SPRING_F5` DiT + Vocos ([OfflineSpringF5Tts.kt](app/src/main/java/com/alchemists/tribetalk/voice/OfflineSpringF5Tts.kt)) and Piper-TTS VITS models running via the ONNX Runtime CPU provider.
* **Quantization & Mobile Portability**:
  * Model Footprint: FP32 (1.35 GB) $\rightarrow$ INT8 (**347.04 MB**, **74.28% footprint reduction**).
  * 1D Convolutional `conv_stft` layer replacing complex `torch.istft` operators to ensure full compatibility with the C++ ONNX Runtime Android runtime.
* **Audio Characteristics**: 24,000 Hz (24 kHz) Mono 16-bit PCM audio stream rendered through Android `AudioTrack` and `MediaPlayer`.

### 4. Real-Time NLP Live Processing Pipeline
* **[HindiNlpProcessor.kt](app/src/main/java/com/alchemists/tribetalk/nlp/HindiNlpProcessor.kt)**:
  * Normalizes spoken Hindi punctuation and Devanagari numbers.
  * Filters speech disfluencies, false starts, and filler sounds (e.g., *umm*, *uh*, *मतलब*, *तो*).
  * Performs semantic sentence boundary segmentation for smooth speech pacing.
* **[LiveUtteranceProcessor.kt](app/src/main/java/com/alchemists/tribetalk/nlp/LiveUtteranceProcessor.kt)**:
  * Manages sliding window context retention to deduplicate overlapping ASR recognition results.

---

## 📊 Comprehensive Subsystem Benchmark Comparison

| Subsystem Component | Primary Model Checkpoint | INT8 Footprint | Accuracy / Benchmark Metric | Average Latency | Target Memory |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Hindi ASR** | `ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large` | **131.30 MB** | **15.24% WER** (Mozilla Common Voice v26.0) | **359.55 ms** ($\text{RTF}=0.166$) | **< 600 MB** |
| **Hybrid NMT** | `ai4bharat/indictrans2-indic-indic-dist-320M` + FLN Base | **~320 MB** | **31.54 chrF++** (IN22-Conv Benchmark) | **849.93 ms** | **< 400 MB** |
| **Santali TTS** | `AI4Bharat Indic Parler-TTS` / `SPRINGLab/SPRING_F5` | **347.04 MB** | **24 kHz Audio Fidelity** | **~1.2 s** | **< 400 MB** |
| **Live Voice Pipeline** | End-to-end ASR ➔ NLP ➔ NMT ➔ TTS Audio Queue | **Integrated** | **Continuous Streaming + VAD** | **Pipelined** | **< 500 MB Budget** |

---

## 📱 Edge AI Constraints & System Specifications

| Parameter | Specification | Design Implementation |
| :--- | :--- | :--- |
| **Target Device Hardware** | Low-Cost Android Tablets (~2 GB RAM) | Quantized INT8 weights with small heap memory allocations |
| **Minimum OS Requirement** | Android 9.0 (API Level 28) | Backwards-compatible Android NDK & Jetpack Compose APIs |
| **Network Dependency** | **0% (100% Offline-First)** | Completely local ONNX model graphs, local SQLite TM, and embedded vector fonts |
| **Resident App Memory** | Strict **< 500 MB RAM** | Prevents Android Low Memory Killer (LMK) foreground eviction |
| **Audio Processing** | 16 kHz Input / 24 kHz Output | Direct low-latency Android `AudioRecord` and `AudioTrack` buffers |
| **Font Rendering** | Native Ol Chiki Unicode (`U+1C50`–`U+1C7F`) | Bundled `NotoSansOlChiki-Regular.ttf` applied to Compose UI and PDF canvases |

---

## 📁 Repository Layout

```text
TribeTalk/
├── app/                                            # Android Application (Jetpack Compose + Kotlin AI Engines)
│   ├── src/main/
│   │   ├── AndroidManifest.xml                     # Audio, Network, & FileProvider Permissions
│   │   ├── assets/
│   │   │   ├── fonts/NotoSansOlChiki-Regular.ttf   # Bundled Ol Chiki TrueType Font
│   │   │   └── asr_test/001.txt                    # Reference ASR test prompts
│   │   └── java/com/alchemists/tribetalk/
│   │       ├── MainActivity.kt                     # Application entry point & 6-module router
│   │       ├── flashcards/                         # NIPUN Bharat Flashcards Generator & Models
│   │       │   ├── Flashcard.kt, FlashcardSet.kt
│   │       │   ├── FlashcardGenerator.kt
│   │       └── NIPUNLearningFramework.kt
│   │       ├── lessons/                            # FLN Lesson Library & Pedagogical Units
│   │       │   ├── Lesson.kt
│   │       │   └── LessonRepository.kt
│   │       ├── nlp/                                # Real-time Voice NLP & Telemetry
│   │       │   ├── HindiNlpProcessor.kt
│   │       │   ├── LiveUtteranceProcessor.kt
│   │       │   ├── LiveUtteranceQueue.kt
│   │       │   └── LatencyTracker.kt
│   │       ├── pedagogy/                           # Legacy pedagogy bridges
│   │       ├── translation/                        # 5-Tier Hybrid Translation Subsystem
│   │       │   ├── HybridEdgeAITranslationEngine.kt
│   │       │   ├── FLNCurriculumDatabase.kt
│   │       │   ├── NeuralNMTTranslationEngine.kt
│   │       │   ├── OnnxTranslationEngine.kt
│   │       │   ├── OlChikiTransliterator.kt
│   │       │   ├── SentencePieceTokenizer.kt
│   │       │   └── TranslationMemory.kt
│   │       ├── ui/
│   │       │   ├── screens/                        # Jetpack Compose UI Screens
│   │       │   │   ├── DashboardScreen.kt
│   │       │   │   ├── LiveClassroomScreen.kt
│   │       │   │   ├── LessonLibraryScreen.kt, LessonDetailScreen.kt
│   │       │   │   ├── FlashcardGeneratorScreen.kt, FlashcardPreviewScreen.kt
│   │       │   │   ├── WorksheetGeneratorScreen.kt, WorksheetPreviewScreen.kt
│   │       │   │   ├── LearningInsightsScreen.kt
│   │       │   │   └── SettingsScreen.kt
│   │       │   └── theme/                          # Material3 Color, Type, and Theme definitions
│   │       ├── voice/                              # Voice Bridge & Audio Synthesis Engines
│   │       │   ├── AudioVADProcessor.kt            # Energy & ZCR Voice Activity Detection
│   │       │   ├── IndicConformerHindiAsr.kt       # AI4Bharat IndicConformer CTC INT8 Engine
│   │       │   ├── NeuralSpeechRecognizer.kt       # On-device PCM AudioRecord ASR
│   │       │   ├── RealSantaliTTSProvider.kt       # FastAPI Parler-TTS Audio Client
│   │       │   ├── SantaliAudioQueue.kt            # Sequential FIFO Playback & Pre-Synthesis Queue
│   │       │   ├── VoiceInputManager.kt            # Continuous Mic Coordinator
│   │       │   ├── VoiceTranslationBridge.kt       # End-to-end Live Voice Orchestrator
│   │       │   └── OfflineSpringF5Tts.kt           # On-device SPRING_F5 TTS engine
│   │       └── worksheet/                          # Bilingual FLN Worksheets & Native PDF
│   │           ├── Worksheet.kt, WorksheetQuestion.kt
│   │           ├── WorksheetGenerator.kt
│   │           └── WorksheetPdfExporter.kt         # Native A4 PDF graphics canvas compiler
│   └── src/test/java/com/alchemists/tribetalk/     # Android Unit Test Suites (72 Passing Tests)
├── asr/                                            # Hindi ASR Python Benchmark & Quantization Pipeline
│   ├── docs/                                       # BASELINE.md, COMMON_VOICE_EVALUATION.md
│   ├── scripts/                                    # export_onnx.py, quantize_int8.py, evaluate_common_voice.py
│   └── tests/common_voice_hi/                      # 100-sample human speech evaluation harness
├── translation/                                    # Neural NMT Subsystem & Benchmark Scripts
│   ├── docs/                                       # INDICTRANS2_HINDI_SANTALI_EVALUATION.md
│   ├── results/                                    # Parallel predictions, metrics.json
│   └── scripts/                                    # translate_hi_sat.py, benchmark_translation.py
├── spring-f5-android/                              # Santali TTS ONNX Export & Python Inference Subsystem
│   ├── docs/                                       # SPRING_F5_ARCHITECTURE.md, ONNX_COMPATIBILITY.md
│   ├── spring_f5_onnx/                             # Standalone ONNX runtime TTS package
│   └── scripts/                                    # reference_inference.py, export_spring_f5_onnx.py
├── training/                                       # Corpus Curation & Dataset Acquisition Pipeline
│   ├── data/                                       # hindi_santali_large_parallel.csv
│   └── download_and_train_large_dataset.py
└── docs/                                           # Extended Architecture Documentation
    └── FEATURES.md                                 # Comprehensive UI & Pedagogical Features Guide
```

---

## 🛠️ Developer Quick Start & Build Guide

### Prerequisites
1. **JDK 21** (e.g. OpenJDK 21 or Eclipse Temurin 21)
2. **Android SDK 34** with Build-Tools 34.0.0
3. **Android Studio** (Ladybug / Koala or newer recommended)
4. (Optional for Python scripts): **Python 3.10+** with `pip`

### 1. Clone & Switch to `jk-branch`
```bash
git clone https://github.com/jeshu05/TribeTalk.git
cd TribeTalk
git checkout jk-branch
```

### 2. Run All Android Unit Tests
Verify all 72 unit tests across the ASR preprocessor, NMT engines, transliteration, VAD, NLP live processor, flashcard generator, and worksheet generator:
```bash
# Windows
.\gradlew.bat test

# Linux / macOS
./gradlew test
```
> **Status**: **72 of 72 unit tests pass cleanly in ~13 seconds**.

### 3. Assemble Android Debug APK
Compile and build the ready-to-deploy debug APK:
```bash
# Windows
.\gradlew.bat assembleDebug

# Linux / macOS
./gradlew assembleDebug
```
Output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### 4. Install onto an Android Device
Ensure USB Debugging is enabled on your Android tablet or phone:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 5. (Optional) Run Local Santali TTS Server
If you wish to test with the high-fidelity AI4Bharat Indic Parler-TTS server on your local LAN:
```bash
cd tts-server
# Activate environment and launch FastAPI service
uvicorn main:app --host 0.0.0.0 --port 8000
```
In the TribeTalk Android App, navigate to **Settings** $\rightarrow$ enter your host machine's IP address (e.g. `http://192.168.1.15:8000`) $\rightarrow$ tap **Test Connection** or tap **`[ ▶ TEST SANTALI VOICE (ᱡᱚᱦᱟᱨ) ]`**.

---

## ⚖️ Differentiation & Comparative Matrix

| Capability | Standard Translators (e.g. Google Translate) | Generic EdTech Apps (e.g. DIKSHA, Khan Academy) | **TribeTalk (PALASH MTB-MLE)** |
| :--- | :---: | :---: | :---: |
| **Network Independence** | ❌ Requires continuous cloud connectivity | ⚠️ Video/resource downloads required | ✅ **100% Offline-First (ONNX INT8)** |
| **Santali (Ol Chiki) Support** | ❌ Unsupported or text-only transliteration | ❌ Standard Hindi / English only | ✅ **Native Speech & Ol Chiki Script** |
| **Low-Cost Hardware Budget** | ❌ Cloud-dependent / Heavy RAM | ❌ Web-wrapper lag on budget tablets | ✅ **Quantized (< 500 MB Resident RAM)** |
| **Integrated Voice Bridge** | ⚠️ Generic disconnected mic input | ❌ Static video/quiz UI | ✅ **Pipelined FIFO Voice Translation** |
| **Pedagogical FLN Tools** | ❌ None | ⚠️ Non-customizable content | ✅ **Curriculum Units, Flashcards, & Worksheets** |
| **Offline Vector PDF Export** | ❌ None | ❌ Cloud print service required | ✅ **Native On-Device A4 Vector PDF** |
| **Teacher Dialect Override** | ❌ Rigid closed models | ❌ Not available | ✅ **Teacher Translation Memory (SQLite)** |

---

## 🧪 Quality Assurance & Verification Summary

The codebase has undergone comprehensive automated and manual verification:

| Test Suite | Class Name | Tests Passed | Covered Areas |
| :--- | :--- | :---: | :--- |
| **Worksheet Generator** | [`WorksheetGeneratorTest`](app/src/test/java/com/alchemists/tribetalk/worksheet/WorksheetGeneratorTest.kt) | **15 / 15** | 6 FLN question types, bilingual generation, answer keys |
| **FLN Translation Engine** | [`TranslationEngineTest`](app/src/test/java/com/alchemists/tribetalk/translation/TranslationEngineTest.kt) | **10 / 10** | Bilingual lexicon lookup, bidirectional Hindi ↔ Santali |
| **Flashcard Generator** | [`FlashcardGeneratorTest`](app/src/test/java/com/alchemists/tribetalk/flashcards/FlashcardGeneratorTest.kt) | **9 / 9** | NIPUN Goals 1–3, study carousel, recall toggle logic |
| **Hybrid Edge AI Engine** | [`HybridEdgeAITranslationEngineTest`](app/src/test/java/com/alchemists/tribetalk/translation/HybridEdgeAITranslationEngineTest.kt) | **9 / 9** | 5-tier fallback prioritization, teacher translation memory |
| **Ol Chiki Transliterator** | [`OlChikiTransliteratorTest`](app/src/test/java/com/alchemists/tribetalk/translation/OlChikiTransliteratorTest.kt) | **6 / 6** | Devanagari ↔ Ol Chiki G2P, IPA stress marks |
| **Phase 11 Dashboard Modules** | [`Phase11ModulesTest`](app/src/test/java/com/alchemists/tribetalk/Phase11ModulesTest.kt) | **6 / 6** | Lessons library, local analytics tracking, settings diagnostics |
| **Neural NMT Engine** | [`NeuralNMTTranslationEngineTest`](app/src/test/java/com/alchemists/tribetalk/translation/NeuralNMTTranslationEngineTest.kt) | **5 / 5** | IndicTrans2 subword tokenization, inference pipeline |
| **Hindi NLP Live Processor** | [`HindiNlpProcessorTest`](app/src/test/java/com/alchemists/tribetalk/nlp/HindiNlpProcessorTest.kt) | **5 / 5** | Filler word suppression, normalization, segmentation |
| **Audio VAD Processor** | [`AudioVADProcessorTest`](app/src/test/java/com/alchemists/tribetalk/voice/AudioVADProcessorTest.kt) | **3 / 3** | Energy thresholds, zero-crossing rate, speech endpointing |
| **Live Voice Pipeline** | [`LiveVoicePipelineTest`](app/src/test/java/com/alchemists/tribetalk/nlp/LiveVoicePipelineTest.kt) | **3 / 3** | Live utterance queue, latency tracker, streaming coordination |
| **ONNX Runtime Engine** | [`OnnxTranslationEngineTest`](app/src/test/java/com/alchemists/tribetalk/translation/OnnxTranslationEngineTest.kt) | **1 / 1** | Model session lifecycle, tensor allocation |
| **Total Automated Tests** | | **72 / 72 Passing (100%)** | |

---

## 📜 Acknowledgments & References

* **Government of Jharkhand**: School Education and Literacy Department & JCERT for foundational pedagogy, curriculum materials, and the **PALASH MTB-MLE** initiative.
* **AI4Bharat (IIT Madras)**: For pioneering open-source Indic AI models:
  * [`ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large`](https://huggingface.co/ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large)
  * [`ai4bharat/indictrans2-indic-indic-dist-320M`](https://huggingface.co/ai4bharat/indictrans2-indic-indic-dist-320M)
* **SPRINGLab**: For [`SPRINGLab/SPRING_F5`](https://huggingface.co/SPRINGLab/SPRING_F5) speech synthesis and DiT research.
* **Google Fonts**: For [`NotoSansOlChiki-Regular.ttf`](https://fonts.google.com/noto/specimen/Noto+Sans+Ol+Chiki) enabling native vector rendering of Ol Chiki glyphs.
* **Team Alchemists**: Engineering, quantizing, and deploying edge AI solutions for indigenous language empowerment.
