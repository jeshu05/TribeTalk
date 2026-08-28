# TribeTalk (SIH 2026 — Problem Statement: SIH26042)

TribeTalk is an offline-first classroom translation companion developed for the **Smart India Hackathon (SIH 2026)**. The application is designed to bridge the communication gap between teachers and students in tribal regions by enabling real-time local dialect translation.

The prototype is built for **Android (API 28+ / Android 9.0+)** and is highly optimized to run on low-resource devices (down to 2 GB RAM). The MVP language flow bridges **Hindi ↔ Santali** entirely offline.

---

## 🔄 Two-Way Voice-to-Voice Loop

### Teacher to Student (Hindi ➜ Santali)
```text
  [Teacher Speaks Hindi] 
            ↓
  [SpeechRecognizer (Local ASR)] ➜ Hindi Text
            ↓
  [OfflineFLNTranslationEngine] ➜ Santali Text (Confidence Check)
            ↓
  [SpeechOutputManager (TTS)] ➜ Santali Audio Output
            ↓
  [Student hears Santali]
```

### Student to Teacher (Santali ➜ Hindi)
```text
  [Student Speaks Santali] 
            ↓
  [SpeechRecognizer (Local ASR)] ➜ Santali Text
            ↓
  [OfflineFLNTranslationEngine] ➜ Hindi Text (Confidence Check)
            ↓
  [SpeechOutputManager (TTS)] ➜ Hindi Audio Output
            ↓
  [Teacher hears Hindi]
```

---

## 📂 Project Architecture Layout

The following diagram maps the offline execution pipeline of TribeTalk:

```text
    UI (LiveClassroomScreen)
               │
               ▼
       VoiceInputManager (Wraps Android SpeechRecognizer)
               │
               ▼
     VoiceTranslationBridge (Coordinator state machine)
               │
               ▼
       TranslationEngine (Contract interface)
               │
               ▼
  OfflineFLNTranslationEngine (Word-level fallbacks & normalization)
        ├── Curated FLN Dictionary (Local asset database)
        └── TranslationMemory (translation_memory.csv storage writer)
               │
               ▼
       TranslationResult (Exposes text & confidence status)
               │
               ▼
      SpeechOutputManager (Wraps TextToSpeech engine)
               │
               ▼
      Host System Audio Output
```

### Key Local Files
*   [MainActivity.kt](file:///C:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/app/src/main/java/com/alchemists/tribetalk/MainActivity.kt) — Entry point, manager instantiations, and navigation router.
*   [OfflineFLNTranslationEngine.kt](file:///C:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/app/src/main/java/com/alchemists/tribetalk/translation/OfflineFLNTranslationEngine.kt) — Core phrase-matching translation engine.
*   [TranslationMemory.kt](file:///C:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/app/src/main/java/com/alchemists/tribetalk/translation/TranslationMemory.kt) — Local CSV file reader/writer for teacher validations.
*   [VoiceTranslationBridge.kt](file:///C:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/app/src/main/java/com/alchemists/tribetalk/voice/VoiceTranslationBridge.kt) — State coordinator bridging voice input, translation, and TTS output.
*   [SpeechOutputManager.kt](file:///C:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/app/src/main/java/com/alchemists/tribetalk/voice/SpeechOutputManager.kt) — Text-to-speech locale safety wrapper.
*   [VoiceInputManager.kt](file:///C:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/app/src/main/java/com/alchemists/tribetalk/voice/VoiceInputManager.kt) — Speech recognition audio listener wrapper.

---

## 🛠️ Complete Development Phases

### ✅ PHASE 1 — Android Prototype Foundation
*   Established a modern Gradle project with Jetpack Compose configured using a central Version Catalog (`libs.versions.toml`).
*   Developed the Teacher Dashboard screen with custom navigation shortcuts.
*   Designed a lightweight, state-based navigation router in `MainActivity.kt` optimized for low-resource constraints (2 GB RAM).

### ✅ PHASE 2 — Text Translation
*   Defined the decoupled `TranslationEngine` interface contract.
*   Implemented `MockTranslationEngine` containing an initial FLN vocabulary database covering greetings, numbers, and basic instructions.
*   Added swap buttons, manual multi-line text input fields, and copy-to-clipboard functionality.

### ✅ PHASE 3B — Offline FLN Translation & Translation Memory
*   Created the `OfflineFLNTranslationEngine` to handle:
    *   **Text Normalization**: Strips excess whitespace, trailing characters, and punctuation marks.
    *   **Alternative Phrase Matching**: Resolves nearby phrase combinations.
    *   **Word-Level Fallback**: Splits multi-word query sentences, translates each component, and joins them with low confidence.
*   Developed `TranslationMemory` saving teacher overrides to a local CSV file (`translation_memory.csv`) under app-specific private storage.
*   Implemented a 4-tier confidence system:
    1.  `High (Teacher Validated)`: Verified corrections.
    2.  `High (Offline Match)`: Exact dictionary lookup hits.
    3.  `Low (Word-level fallback)`: Joined dictionary components requiring verification.
    4.  `No match`: Unmapped phrases showing warning dialogs.
*   Established matching priority logic:
    $$\text{Teacher Translation Memory} \rightarrow \text{Exact Curated Phrase} \rightarrow \text{Word-level fallback} \rightarrow \text{No-match response}$$

### ✅ PHASE 4 — Offline Voice Input & Speech Output
*   Integrated `VoiceInputManager` wrapping Android's native `SpeechRecognizer` API with runtime `RECORD_AUDIO` permission checks.
*   Checked target language audio synthesizers before outputting speech.
*   Enforced the **Anti-Fabrication Rule**: Santali voice synthesis is *never* fabricated. If a device lacks Santali voice files, the application explicitly alerts: `"Santali voice unavailable on this device"`.

### ✅ PHASE 4A — UI/UX Redesign
*   Transitioned the interface to a professional, clean educational layout using a restrained color system:
    *   **Canvas Background**: `#F8F9FA` off-white.
    *   **Card Surfaces**: `#FFFFFF` white cards with subtle border shadows.
    *   **Primary Elements**: `#0F2C59` deep indigo (AppBar headers, primary headings, primary CTAs).
    *   **Secondary/Active States**: `#008080` teal (listening status indicators, successful translations).
    *   **Warning Indicators**: `#FF9F29` amber (teacher reviews, corrections).
    *   **Text Charcoal**: `#222222` for high readability.
*   Restored prominent outlines on "Clear Card" action buttons (44dp target) which clear card states without resetting saved CSV memory files.
*   Equipped buttons with relative weights (`Modifier.weight(1f)`) to prevent text wrapping/clipping on portrait mobile screens.

### ✅ PHASE 5 — Voice-to-Voice Bridge (Current Completed Milestone)
*   Coded `VoiceTranslationBridge` and `SpeechOutputManager` to coordinate voice recognition inputs, translation retrieval, and speech synthesis.
*   Exposes 7 distinct voice translation states: `Idle`, `Listening`, `Recognized`, `Translating`, `TranslationComplete`, `Speaking`, and `Error`.
*   Added a visual card-level **Voice Bridge Mode** toggle switch:
    *   **OFF (Default)**: Speaking a phrase translates the input text on-screen but requires manually clicking "Play" to output audio.
    *   **ON**: Capturing voice input translates and automatically triggers TTS speech synthesis once translation is complete.

---

## 🔄 Translation Memory Override Loop

```text
    Receive Translation
            │
            ▼
    Select "Correct" ➜ Opens input override text field
            │
            ▼
    Enter Correction ➜ Click "Save"
            │
            ▼
    Writes to local "translation_memory.csv"
            │
            ▼
    Subsequent matching returns "High (Teacher Validated)"
```
*Note: All Translation Memory overrides reside locally in private storage. No cloud databases are used.*

---

## 🧠 Phase 3 Feasibility Investigation & Architectural Decision

During Phase 3 development, we evaluated running neural machine translation (NMT) models locally:
*   **Candidates Evaluated**: ONNX Runtime implementations of **NLLB-200 (distilled 600M parameters)** and **IndicTrans2**.
*   **OOM Constraints**: Running INT8 quantized models requires `~600 MB` storage space and `~1.0 GB` active RAM. Because target low-resource Android devices limit free active RAM below `~700 MB`, loading NMT models consistently triggers Android Out-Of-Memory (OOM) background task terminations.
*   **Latency Constraints**: Seq2Seq transformer operations on standard mobile CPUs resulted in unacceptable latencies of **10 to 30+ seconds** per phrase.
*   **Resulting Architecture**: Based on these findings, we chose a lightweight phrase-matching engine coupled with persistent teacher translation memory. This is a deliberate engineering decision designed to guarantee sub-millisecond offline lookup latencies on devices with 2 GB RAM.

---

## ⚖️ Solution Comparison & Differentiation

| Capability | Adi Vaani | BHASHINI | NIPUN Bharat | TribeTalk |
| :--- | :---: | :---: | :---: | :---: |
| **Multilingual infrastructure** | ❌ | ✅ | ⚠️ (State-specific) | ✅ |
| **Tribal language support** | ✅ | ❌ | ❌ | ✅ (Santali) |
| **FLN classroom workflow focus** | ⚠️ | ❌ | ✅ | ✅ |
| **Offline-first operation** | ✅ | ❌ | ⚠️ | ✅ |
| **Teacher validation memory** | ❌ | ❌ | ❌ | ✅ |
| **Integrated Voice-to-Voice** | ⚠️ | ✅ | ❌ | ✅ |

*TribeTalk's differentiation is the combination of these capabilities inside a single, classroom workflow.*

---

## ⚠️ Current System Limitations

1.  **Santali Speech Synthesis**: Synthesizing Santali speech is dependent on the host device's TTS engine containing a Santali voice pack. If unsupported, the app displays a clear error warning instead of faking audio.
2.  **Offline Speech Recognition**: Offline voice recognition is dependent on the host device having the required offline languages (Hindi/Santali) pre-downloaded via Google Keyboard settings.
3.  **Phrase Database Matching**: Translation relies on FLN phrase and vocabulary lookups rather than a fully generalized generative neural translator. Out-of-vocabulary queries return word-level fallbacks or no-match warnings.

---

## 📊 Verification Log & Status

### Automated Checks
*   `.\gradlew.bat test` ➜ **PASSED** (11 unit tests covering normalizing, dictionary lookups, and memory overrides).
*   `.\gradlew.bat compileDebugKotlin` ➜ **PASSED**
*   `.\gradlew.bat assembleDebug` ➜ **PASSED** (Apk packaged at `app/build/outputs/apk/debug/app-debug.apk`).
*   `.\gradlew.bat installDebug` ➜ **PASSED** (Deployed to device `RMX3870`).

### Verified Runtime Flows
*   **Hindi ➜ Santali**: Speaking or typing `"नमस्ते"` resolves to `"Johar (जोहार)"` with `High (Offline Match)` confidence.
*   **Santali ➜ Hindi**: Speaking or typing `"Ceka menama?"` resolves to `"आप कैसे हैं?"` with `High (Offline Match)` confidence.
*   **Teacher Validation**: Correcting a phrase translates it correctly, writes the update to `translation_memory.csv`, and uses the correction for subsequent matching.
*   **Voice Bridge ON**: Speaking auto-plays Hindi audio output instantly.

---

## 🗺️ Future Roadmap

*   **Custom Santali TTS Integration**: Incorporating a compact, quantized neural TTS engine (e.g. VITS) optimized for Santali to run locally without system voice dependence.
*   **Quantized NMT Engine**: Researching highly distilled, target-quantized Hindi-Santali NMT models (e.g., specialized MobileNMT) for generalized out-of-vocabulary translations under 150 MB RAM limits.
*   **Expanded FLN Curated Content**: Adding pre-configured classroom worksheets, learning insights tables, and student analytics modules.
*   **Broad Tribal Dialect Support**: Replicating this offline translation memory workflow for other tribal dialects (Gondi, Bhili, Kurukh).

