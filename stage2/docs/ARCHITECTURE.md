# Stage 2 Architectural Documentation

## System Architecture

```text
               NIPUN Bharat Learning Outcome
                             │
                             ▼
                 [curriculum_templates.json]
                             │
                             ▼
             [stage2/scripts/build_content_pack.py]
              ├── IndicTrans2 (hin_Deva -> sat_Olck)
              └── SPRING_F5 (24 kHz PCM WAV)
                             │
                             ▼
           [app/src/main/assets/content_pack/]
              ├── manifest.json
              ├── curriculum.json
              └── lessons/
                  └── {lesson_id}/
                      ├── lesson.json
                      └── audio/
                          ├── introduction.wav
                          ├── instruction.wav
                          └── question.wav
                             │
                             ▼
                 [ContentPackRepository.kt]
                             │
                             ▼
                   [LessonDetailScreen.kt]
```

### Key Components
1. **`build_content_pack.py`**: Automated build script orchestrating translation and WAV synthesis.
2. **`ContentPackRepository.kt`**: Android Kotlin repository parsing asset JSONs and streaming pre-rendered WAV audio.
3. **`FLNViewModel.kt`**: UI StateFlow manager connecting screens to the content pack store.
