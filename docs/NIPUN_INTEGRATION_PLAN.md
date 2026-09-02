# NIPUN/FLN Curriculum & Local Room Database Integration Plan

## 1. Discovered Architecture & Reused Components

- **Architecture**: MVVM with Jetpack Compose UI, StateFlow viewmodels, local persistence repository layers, and ONNX Runtime offline models.
- **Speech-to-Speech Subsystems**:
  - **Hindi ASR**: AI4Bharat IndicConformer CTC ONNX INT8 (`IndicConformerHindiAsr.kt`).
  - **Hindi ➔ Santali NMT**: AI4Bharat IndicTrans2 ONNX INT8 (`OnnxTranslationEngine.kt`).
  - **Santali TTS**: SPRINGLab SPRING_F5 DiT + Vocos ONNX (`SpringF5AudioSynthesizer.kt`).
- **Curriculum & Activity Engines**:
  - `FLNCurriculumRepository.kt`: NIPUN Bharat learning outcomes dataset.
  - `FLNActivityGenerator.kt` & `FLNWorksheetGenerator.kt`: Offline visual content packs and activity templates.
  - `ResponsePersistenceRepository.kt`: Persisted assessment attempts and analytics tracking.

---

## 2. Reused & Added Components

### Reused:
- `VoiceTranslationBridge.kt` for live classroom speech-to-speech.
- `ContentPackRepository.kt` streaming offline pre-rendered WAV audio.
- Compose design tokens & UI components (`TribeTalkTheme`).

### Added:
- **Reproducible Data Pipeline**: `data/curriculum/demo/curriculum_demo.json` + `scripts/build_nipun_db.py` generating `app/src/main/assets/database/nipun.db`.
- **Validation Script**: `scripts/validate_nipun_db.py`.
- **Room Database Layer**:
  - `NipunDatabase.kt`: Room pre-packaged database loading `assets/database/nipun.db`.
  - Entities: `TeacherEntity`, `ClassroomEntity`, `DomainEntity`, `CompetencyEntity`, `LearningOutcomeEntity`, `LessonEntity`, `ActivityEntity`, `AssessmentEntity`, `FlashcardEntity`, `WorksheetEntity`, `StudentEntity`, `StudentOutcomeProgressEntity`, `TeachingResourceEntity`, `ContentTranslationEntity`.
  - DAOs: `NipunDao.kt` querying class-specific curriculum, local search, dynamic analytics, and resource uploads.
- **Teacher Setup & Managed Classes**: Offline local teacher setup selecting managed classes (`Class 1`, `Class 2`, `Class 3`).
- **Two-Screen Navigation**: Dedicated `LessonDetailScreen` separated from selection panels.
- **Local PDF/PPT Resource Manager**: Teacher upload of local documents stored in app storage.
- **Local Search Engine**: Room SQLite search over lessons, outcomes, competencies, and domains.

---

## 3. Database & Migration Strategy

- Development workflow: `curriculum_demo.json` ➔ `build_nipun_db.py` ➔ `nipun.db` ➔ `app/src/main/assets/database/nipun.db` ➔ `Room.databaseBuilder(...).createFromAsset("database/nipun.db")`.
- 100% offline, zero network, zero cloud API dependencies.

---

## 4. Documentation Artifacts

- `docs/NIPUN_DATABASE.md`
- `docs/NIPUN_INTEGRATION.md`
- `docs/OFFLINE_ARCHITECTURE.md`
