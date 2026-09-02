# STAGE 3 — Interactive Student Learning & Assessment Subsystem

This directory documents the **Interactive Student Learning + Assessment Module** for Jharkhand's PALASH Mother Tongue-Based Multilingual Education (MTB-MLE) system.

## 📌 Architecture & System Flow

```text
               Lesson Module (NIPUN Outcome)
                             │
                             ▼
         [AssessmentQuestionGenerator.kt]
         (Generates 4 Data-Driven Question Types)
                             │
                             ▼
              [StudentAssessmentScreen.kt]
        (Child-Friendly UI + Santali Audio Trigger)
                             │
                             ▼
        [Immediate Feedback: ✓ Correct / 🙂 Try Again]
                             │
                             ▼
              [AssessmentResultsScreen.kt]
      (Score %, Mastery Level, Teacher Recommendation)
                             │
                             ▼
         [AssessmentPersistenceRepository.kt]
        (100% Offline Local SharedPreferences Storage)
                             │
                             ▼
              [TeacherProgressScreen.kt]
      (Concept-Level Mastery Analytics Dashboard)
```

---

## 🎨 Supported Question Types (4 Types)

1. **`VISUAL_MULTIPLE_CHOICE`**: Visual emoji stimulus (e.g. 🚗 🚗 🚗 🚗) + Santali prompt (*"ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱛᱤᱱᱟᱹᱜ ᱜᱟᱹᱰᱤ ᱢᱮᱱᱟᱜᱼᱟ?"*) + Touch option choices (`[3] [4] [5]`).
2. **`IMAGE_MATCHING`**: Target number/concept (e.g. `2 (ᱵᱟᱨ)`) + Option visual groups (`🍎🍎`, `🍎🍎🍎`, `🍎🍎🍎🍎`).
3. **`IDENTIFY_SELECT`**: Target shape/colour (e.g. Circle ⭕) + Option shape cards (`⭕`, `⏹️`, `🔺`).
4. **`ORDERING`**: Sequence arrangement (e.g. `1 ➜ 2 ➜ 3`).

---

## 📊 Scoring & Mastery Methodology

- **Accuracy Formula**: $\text{accuracy} = \frac{\text{correct\_answers}}{\text{total\_questions}} \times 100$.
- **Mastery Classification Rules**:
  - $\ge 80\%$: `MASTERED` (🟢)
  - $60\% - 79\%$: `DEVELOPING` (🟡)
  - $< 60\%$: `NEEDS_PRACTICE` (🔴)

---

## 📱 Polished 5-Question Demo Scenario Procedure

1. Open **Lessons** $\rightarrow$ Select **"Counting 1–10" (`math_counting_01`)**.
2. Scroll to bottom and tap **"Start Interactive Assessment"**.
3. **Question 1**: Count 3 Cars 🚗🚗🚗 $\rightarrow$ Select `3` $\rightarrow$ Tap **Submit** $\rightarrow$ See positive feedback `✓ ᱴᱷᱤᱠ ᱜᱮᱭᱟ!`.
4. **Question 2**: Count 5 Apples 🍎🍎🍎🍎🍎 $\rightarrow$ Select `5` $\rightarrow$ Tap **Submit** $\rightarrow$ Next.
5. **Question 3**: Match Image for 2 Books 📖📖 $\rightarrow$ Select `2 ᱡᱤᱱᱤᱥ` $\rightarrow$ Next.
6. **Question 4**: Identify Shape ⭕ $\rightarrow$ Select `वृत्त (गोल)` $\rightarrow$ Next.
7. **Question 5**: Ordering 1, 2, 3 $\rightarrow$ Select `1 ➜ 2 ➜ 3` $\rightarrow$ Tap **View Results**.
8. **Results Screen**: Shows **80% Score**, **🟢 Mastered** status, and recommendation.
9. Tap **"Teacher Progress"** to view concept analytics dashboard.
