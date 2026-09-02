# Content Pipeline Specification

## Bilingual Hindi -> Santali Translation & Audio Synthesis

### 1. Translation Pipeline
- **Source**: `hin_Deva` (Hindi Devanagari)
- **Target**: `sat_Olck` (Santali Ol Chiki)
- **Engine**: AI4Bharat IndicTrans2 320M ONNX model + fallback mapping dictionary.

### 2. Audio Synthesis Pipeline
- **Engine**: SPRINGLab / SPRING_F5 Neural TTS
- **Sample Rate**: 24,000 Hz
- **Channels**: 1 (Mono)
- **Sample Width**: 16-bit PCM
- **Storage**: `content_pack/lessons/{lesson_id}/audio/`
