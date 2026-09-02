# Stage 2 Content Pack Validation Specifications

Every generated content pack undergoes strict automated verification:

1. **Manifest Integrity**: `manifest.json` exists and lists valid lesson directory IDs.
2. **Schema Integrity**: All `lesson.json` files contain required fields (`title`, `learning_outcome`, `teacher_instruction`, `activity`, `assessment`).
3. **Language Integrity**: Hindi text is non-empty; Santali text is non-empty and contains valid Ol Chiki Unicode characters (`U+1C50`..`U+1C7F`).
4. **Audio Integrity**: Referenced WAV files exist, have a sample rate of $24,000\text{ Hz}$, 16-bit PCM sample width, and mono channel layout.
5. **Asset Integrity**: Referenced visual icon emojis / assets exist locally.
