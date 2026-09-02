# Stage 2 Architecture Analysis & Integration Plan

## 1. 🔍 Repository & Existing Infrastructure Inspection

### A. Android Architecture & Data Models
- **Package**: `com.alchemists.tribetalk`
- **Data Models**: Defined in [`FLNModels.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/curriculum/models/FLNModels.kt) (`Lesson`, `LearningOutcome`, `LessonInstruction`, `Activity`, `AssessmentQuestion`, `GeneratedActivityItem`, `VisualStimulus`).
- **Repository Store**: Defined in [`FLNCurriculumRepository.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/curriculum/repository/FLNCurriculumRepository.kt).
- **Navigation Architecture**: Two-screen navigation in [`MainActivity.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/MainActivity.kt) (`LessonSelectionScreen` $\rightarrow$ `LessonDetailScreen`).

### B. Existing AI Models & Pretrained Infrastructures
1. **Hindi ASR**: AI4Bharat IndicConformer CTC INT8 ONNX (`indicconformer_hi_ctc_int8.onnx`, 131.30 MB) executed via [`IndicConformerHindiAsr.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/voice/IndicConformerHindiAsr.kt).
2. **Hindi $\rightarrow$ Santali NMT**: AI4Bharat IndicTrans2 320M (`hin_Deva` $\rightarrow$ `sat_Olck`) executed via Python [`IndicTransONNX`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/translation/models/translate.py) & Kotlin [`OnnxTranslationEngine.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/translation/OnnxTranslationEngine.kt).
3. **Santali Speech Synthesis (TTS)**: SPRINGLab/SPRING_F5 DiT + Vocos Neural Vocoder executed via Python `f5_tts` & Kotlin [`OfflineSpringF5Tts.kt`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/app/src/main/java/com/alchemists/tribetalk/voice/OfflineSpringF5Tts.kt).

---

## 2. 💡 Key Architectural Rule: Build-Time vs. Runtime Separation

To ensure $100\%$ zero-lag classroom operation on low-cost $2\text{ GB}$ RAM Android tablets (Android 9+):

- **Build Machine (Python `build_content_pack.py`)**:
  - Consumes structured Hindi curriculum templates.
  - Translates Hindi fields to Santali Ol Chiki using `IndicTrans2`.
  - Synthesizes 24 kHz mono 16-bit PCM WAV audio for Santali prompts using `SPRING_F5`.
  - Bundles output into `app/src/main/assets/content_pack/` (`manifest.json`, `curriculum.json`, `lessons/...`).
- **Android App Runtime**:
  - Reads `manifest.json` and `lesson.json` dynamically from assets via `ContentPackRepository.kt`.
  - Displays text and visual stimuli with 0 ONNX translation/TTS execution delay.
  - Streams pre-rendered 24 kHz WAV audio directly via Android's native audio engine.

---

## 3. 🎯 Integration Strategy

1. **No Duplicate Classes**: Reuse `FLNModels.kt`, `OlChikiTransliterator.kt`, `IndicTransONNX`, and `SPRING_F5` interfaces.
2. **Zero Hardcoded Lesson Text in Kotlin**: UI components dynamically consume loaded JSON objects (`lesson.learning_outcome.santali`, `lesson.teacher_instruction.hindi`, etc.).
3. **Full Offline Integrity**: All assets, JSON files, and WAV audio are packaged locally. Network access is disabled and unnecessary.
