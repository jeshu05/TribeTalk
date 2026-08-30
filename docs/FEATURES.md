# TribeTalk — Complete Features & Capabilities Documentation

**TribeTalk** is an offline-first, edge AI vernacular classroom translation and pedagogical companion designed for Jharkhand's **PALASH Mother Tongue-Based Multilingual Education (MTB-MLE)** program. It enables primary school teachers to bridge the linguistic gap between standard Hindi and **Santali (Ol Chiki script)** in foundational literacy and numeracy (FLN / NIPUN Bharat) classrooms.

---

## 🏗️ System Architecture & Dashboard Overview

```
                                  TRIBETALK DASHBOARD
                                           │
  ┌─────────────────┬──────────────────────┼─────────────────────┬─────────────────┐
  │                 │                      │                     │                 │
🎙️ LIVE CLASSROOM  📚 LESSONS           🎴 FLASHCARDS         📄 WORKSHEETS    📊 INSIGHTS & ⚙️ SETTINGS
  │                 │                      │                     │                 │
  ▼                 ▼                      ▼                     ▼                 ▼
Speech-to-Speech  FLN Lesson Units    NIPUN Outcomes       Bilingual Generator  Local Analytics &
Hindi ↔ Santali   Script/Activity/Q   Visual Cards & Study Native A4 PDF Export System Diagnostics
```

---

## 🎙️ 1. Live Classroom (Real-Time Teacher ↔ Student Voice Bridge)

### Key Capabilities
- **Dual-Panel Conversational HUD**:
  - **Teacher Side (Hindi ➔ Santali)**: Captures teacher's Hindi speech, displays Devanagari text, translates to Santali Ol Chiki with phonetic guides, and synthesizes native Santali speech.
  - **Student Side (Santali ➔ Hindi)**: Allows students to speak/input in Santali and translates back to Hindi for the teacher.
- **Microphone Input & Voice Activity Detection (VAD)**:
  - Supports Android System Speech Recognition (with on-device offline language packs).
  - Integrates `AudioVADProcessor` (WebRTC-style energy and zero-crossing detection) to automatically detect speech endpoints after 1.2 seconds of silence.
- **Voice Translation Bridge (Auto-Play Control)**:
  - **Bridge ON**: Automatically translates spoken sentences and triggers Santali text-to-speech playback without touching the screen.
  - **Bridge OFF**: Performs translation and displays the bilingual text on the HUD for reading without audio playback.
- **Manual Play & Controls**:
  - `▶ Play` button on all translation cards for on-demand pronunciation.
  - `📋 Copy` and `🗑️ Clear` buttons for immediate classroom management.
- **Teacher In-Place Editing & Translation Memory Overrides**:
  - Teachers can edit translated text directly on the HUD.
  - Changes can be saved to local `TranslationMemory` (Room/SQLite) to override future translations for localized dialectal terms.

---

## 📚 2. FLN Lesson Library & Pedagogical Units

### Key Capabilities
- **Curated Demonstration FLN Units**:
  - **Literacy & Oral Language**:
    1. *Greetings and Introductions (नमस्ते एवं परिचय)* — Oral Vocabulary & Social Interaction (Grade 1)
    2. *Basic Vocabulary & Daily Objects (शब्दावली एवं दैनिक वस्तुएं)* — Word-Picture Association (Grade 1)
    3. *Letter & Sound Awareness (ध्वनि एवं Ol Chiki लिपि समझ)* — Phonological Awareness (Grade 2)
    4. *Simple Sentences (सरल वाक्य रचना)* — Classroom Dialogue & Comprehension (Grade 2)
  - **Numeracy & Mathematical Thinking**:
    5. *Counting 1–10 (संख्या ज्ञान एवं १-१० गिनती)* — Number Recognition & Counting (Grade 1)
    6. *Number Recognition & Addition (संख्या पहचान एवं जोड़)* — Basic Quantities & Addition (Grade 2)
    7. *Shapes Around Us (हमारे आस-पास के आकार)* — Shapes & Geometric Concepts (Grade 1)
- **Structured 3-Part Bilingual Lesson Format**:
  - 📖 **Lesson Script**: Parallel Hindi and Santali classroom dialogues with Ol Chiki and Latin phonetic transliteration.
  - 🎯 **Activity Instructions**: Concrete pedagogical steps for classroom group activities.
  - 📝 **Assessment Prompts**: Formative evaluation questions for teachers to check student comprehension.
- **Audio Output**:
  - **`▶ PLAY SANTALI`** button synthesizes full lesson scripts using the real Santali TTS engine.
- **Teacher Lesson Editor with Re-Translation**:
  - Teachers can customize any lesson script, activity instruction, or assessment question.
  - **`[ TRANSLATE TO SANTALI ]`** buttons re-translate modified Hindi text dynamically.
- **Seamless Cross-Module Shortcuts**:
  - **`[ 🎴 FLASHCARDS ]`**: Automatically launches the Flashcard generator pre-filled with the lesson's topic and target vocabulary.
  - **`[ 📄 WORKSHEET ]`**: Automatically launches the Worksheet generator pre-filled with the lesson's script and learning outcome.

---

## 🎴 3. Visual Flashcard Sets (NIPUN Bharat Aligned)

### Key Capabilities
- **NIPUN Bharat Learning Framework Alignment**:
  - **Goal 1 (Health & Well-being)**: Living Beings & Animals, Nature & Environment.
  - **Goal 2 (Effective Communicators)**: Basic Vocabulary & Daily Objects, Greetings & Classroom Commands.
  - **Goal 3 (Involved Learners)**: Numbers & Counting (1–10), Shapes & Spatial Awareness.
- **Automated Bilingual Generation**:
  - Generates 5 curated visual cards per topic containing:
    - Hindi text (e.g. `मछली`)
    - Santali text in Ol Chiki script (e.g. `ᱦᱟᱹᱠᱩ`)
    - Latin phonetic pronunciation (e.g. `Haku`)
    - NIPUN Skill badge and card numbering (e.g. `Card 1 of 5 • Oral Vocabulary`)
- **Single-Card Carousel & Interactive Study Mode**:
  - Large, readable typography optimized for low-cost Android tablet screens.
  - **"SHOW SANTALI" / "HIDE SANTALI" Toggle**: Enables active student recall practice by hiding the Santali script until the teacher taps reveal.
  - Smooth card-to-card navigation via `Previous` / `Next` controls.
- **Local Photo Picker**:
  - Allows teachers to attach local photos or camera captures to any flashcard for visual reinforcement.
- **In-Place Flashcard Editor**:
  - Teachers can edit Hindi words, re-translate into Santali, update phonetic spellings, or change the associated image.

---

## 📄 4. Auto-Generated Bilingual FLN Worksheets

### Key Capabilities
- **One-Tap Worksheet Generation**:
  - Teachers enter or select a topic and enter a short Hindi lesson script.
  - The generator automatically produces 6 balanced FLN questions:
    1. *Fill in the Blanks (खाली स्थान भरें)*
    2. *Match the Following (सही मिलान करें)*
    3. *True or False (सही या गलत)*
    4. *Multiple Choice Question (बहुविकल्पीय प्रश्न)*
    5. *Word Meaning (शब्दार्थ लिखें)*
    6. *Sentence Translation (सरल वाक्य अनुवाद)*
- **Dual-Script Presentation**:
  - Every question is presented in standard Hindi alongside its Santali (Ol Chiki) translation and student answer lines.
- **Teacher Question Editor with Live Re-Translation**:
  - Teachers can modify any Hindi question prompt or answer key in the preview screen.
  - **`[ TRANSLATE TO SANTALI ]`** secondary action translates the updated question text immediately via the translation engine.
- **Native Android A4 PDF Export**:
  - Generates a print-ready A4 PDF document using Android's native `PrintDocumentAdapter` and `PdfDocument` graphics canvas.
  - Formatted with header metadata (School Name, Student Name, Roll No., Date, Learning Outcome), bilingual question blocks, and response lines.
  - Works 100% offline without third-party cloud printing services.

---

## 📊 5. Learning Insights (Local Classroom Monitoring)

### Key Capabilities
- **Classroom Engagement Metrics**:
  - 📘 **Lessons Used Count**
  - 🎴 **Flashcard Sessions Count**
  - 📄 **Worksheets Created Count**
  - 🎤 **Voice Translations Count**
- **NIPUN FLN Domain Progress Bars**:
  - Visual indicators displaying classroom coverage across *Foundational Literacy*, *Foundational Numeracy*, and *Environmental Awareness*.
- **Recent Activity Feed**:
  - Chronological activity log showing recent teaching events (e.g. `"Greetings lesson opened"`, `"Animals flashcards used"`, `"Numbers worksheet created"`).
- **100% Zero-Cloud Execution**:
  - All tracking is stored locally on the tablet, labeled clearly as *Prototype Learning Insights* with zero network telemetry or cloud leakage.

---

## ⚙️ 6. Settings & Offline System Diagnostics

### Key Capabilities
- **Language Profile**:
  - Primary Interface: `Hindi (हिन्दी)`
  - Target Tribal Language: `Santali (ᱥᱟᱱᱛᱟᱲᱤ - Ol Chiki)`
- **Offline Content Status Verification**:
  - Translation Database: `✓ Available (Room / SQLite)`
  - FLN Curriculum Corpus: `✓ 440+ Curated Items`
  - Flashcard Engine: `✓ Available Offline`
  - Worksheet & PDF Generator: `✓ Available Offline`
  - Lesson Library: `✓ 7 Demo Units Available`
  - Network Requirement: `Not Required (100% Offline-First)`
- **Voice & Speech Synthesis (TTS) Engine**:
  - Dual Support: Local FastAPI endpoint (`http://<LAN-IP>:8000`) with AI4Bharat Indic Parler-TTS, with seamless fallback to on-device VITS / Piper-TTS.
  - **`[ ▶ TEST SANTALI VOICE (ᱡᱚᱦᱟᱨ) ]`** button for instant acoustic audio testing.
- **Classroom Defaults & System Info**:
  - Configures default target grades (Grade 1–3) and displays app memory/architecture specifications (< 500 MB footprint on 2 GB RAM tablets).

---

## 🧩 Summary Feature Matrix

| Module | Core Purpose | Offline Ready | Pedagogical Alignment |
| :--- | :--- | :---: | :--- |
| **🎙️ Live Classroom** | Real-time Hindi ↔ Santali speech translation | ✅ Yes | Teacher-Student vernacular dialogue |
| **📚 Lessons** | 3-part bilingual scripts, activities & assessment | ✅ Yes | JCERT / FLN foundational curriculum |
| **🎴 Flashcards** | Visual vocabulary cards with study mode | ✅ Yes | NIPUN Bharat Goals 1, 2, and 3 |
| **📄 Worksheets** | Auto-generated 6-question worksheets & PDF | ✅ Yes | Formative assessment & pen-paper practice |
| **📊 Insights** | Local classroom usage analytics & progress | ✅ Yes | Teacher monitoring & session logging |
| **⚙️ Settings** | System configuration & live Santali voice test | ✅ Yes | Offline database & engine diagnostics |
