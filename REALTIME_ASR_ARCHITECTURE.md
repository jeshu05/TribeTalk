# Real-Time Offline Speech-to-Speech Architectural Design (`REALTIME_ASR_ARCHITECTURE.md`)

## 1. Discovered Existing Architecture & Bottlenecks

### Existing Architecture:
- `IndicConformerHindiAsr.kt`: Offline AI4Bharat IndicConformer CTC ONNX INT8 model (131 MB) expecting a complete 16 kHz PCM short array.
- `NeuralSpeechRecognizer.kt`: Batch microphone recorder accumulating PCM samples into a `pcmAccumulator` list until `stopListening()` is called, then invoking ASR on the whole clip.
- `VoiceTranslationBridge.kt`: Sequential bridge translating recorded text via IndicTrans2 NMT and passing to TTS.

### Primary Bottleneck:
- The user had to manually press **Record**, record a complete sentence, and press **Stop**.
- ASR, NMT, and TTS were executed sequentially after recording stopped, locking UI transitions and introducing unnecessary delay.

---

## 2. Proposed Continuous Streaming / Chunked Architecture

```text
    Continuous Microphone (RealtimeAudioRecorder.kt)
          │
          ▼ 16kHz PCM Frames (480 samples / 30ms)
    Voice Activity Detector (VoiceActivityDetector.kt)
          │
          ├─► Speech Start
          ├─► Speech Continuing (Accumulates PCM in rolling buffer)
          └─► Endpoint Detected (Silence > 500ms)
          │
          ▼ Finalized Utterance Audio
    IndicConformer ASR (RealtimeHindiAsr.kt)
          │
          ▼ Finalized Hindi Phrase
    Asynchronous Translation Pipeline (RealtimeTranslationPipeline.kt)
          │
          ├── IndicTrans2 NMT (hin_Deva ➔ sat_Olck)
          ├── SPRING_F5 Santali TTS Synthesis
          └── Bounded Audio/Translation Cache
          │
          ▼
    AudioTrack Speaker Playback (Non-blocking Mic Capture)
```

---

## 3. Modified & Added Classes

### Newly Added Subsystems:
1. `RealtimeAudioRecorder.kt`: Continuous 16 kHz Mono 16-bit PCM AudioRecord wrapper returning a Coroutine `Flow<ShortArray>`.
2. `VoiceActivityDetector.kt`: Real-time local RMS energy VAD with adaptive noise floor and endpoint detection (minimum speech: 300 ms, silence timeout: 500–700 ms).
3. `RealtimeHindiAsr.kt`: Streaming orchestration layer running sliding-window/chunked ASR and sentence deduplication over `IndicConformerHindiAsr`.
4. `RealtimeTranslationPipeline.kt`: Asynchronous producer/consumer pipeline processing Hindi utterances, calling IndicTrans2 and SPRING_F5 TTS concurrently without pausing microphone audio capture.
5. `TranslationCache.kt`: Bounded in-memory cache for repeated Hindi ➔ Santali translations & Santali TTS audio frames.

### Modified Files:
- `LiveClassroomScreen.kt`: Continuous real-time translation UI showing live states (`LISTENING`, `PROCESSING ASR`, `TRANSLATING`, `SPEAKING`) and end-to-end latency metrics.
- `VoiceTranslationBridge.kt`: Updated bridge connecting the continuous real-time pipeline to Compose UI state.

---

## 4. Latency Target & Monotonic Instrumentation
- **Instrumentation**: Monotonic system clock (`SystemClock.elapsedRealtime()`).
- **End-to-End Target**: Endpoint detection ➔ Santali audio playback `< 3.0` seconds (Target: ~1.5–2.2 s).
