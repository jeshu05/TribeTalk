# STAGE 5 — Adaptive Learning Loop, Personalised Practice & Teacher Insights Subsystem

This directory documents the **Adaptive Learning Loop, Personalised Practice & Teacher Insights Subsystem** for Jharkhand's PALASH Mother Tongue-Based Multilingual Education (MTB-MLE) system.

## 📌 Architecture & Closed Educational Loop

```text
               NIPUN Curriculum Outcome
                          │
                          ▼
                    Lesson Content
                          │
                          ▼
                 Student Activity
                          │
                          ▼
               Interactive Assessment
                          │
                          ▼
                 AssessmentResult
                          │
                          ▼
           [LearningRecommendationEngine.kt]
             ├── Calculates Mastery State
             ├── Computes Improvement Points (+40%)
             └── Generates Personalised Practice
                          │
         ┌────────────────┴────────────────┐
         ▼                                 ▼
[StudentHomeScreen.kt]         [TeacherDashboardScreen.kt]
(Child-Friendly Visuals)       (Analytics & Insights)
         │                                 │
         └────────────────┬────────────────┘
                          ▼
                  Practice Activity
                          │
                          ▼
                    Re-Assessment
                          │
                          ▼
               Updated Progress Analytics
```

---

## ⚙️ Deterministic Recommendation Engine Rules

- **`NEEDS_PRACTICE` ($< 60\%$)**:
  - Priority: `HIGH`
  - Action: Generates foundational practice activity for weak concept.
- **`DEVELOPING` ($60\% - 79\%$)**:
  - Priority: `MEDIUM`
  - Action: Generates reinforcement practice activity.
- **`MASTERED` ($\ge 80\%$)**:
  - Priority: `LOW`
  - Action: Recommends advancing to the next NIPUN Bharat learning outcome.

---

## 📱 Closed Loop Demonstration Scenario Procedure

1. **Initial Assessment**:
   - Student completes initial assessment on **Counting 1–10 (`math_counting_01`)**.
   - Scores **2 / 5 (40%)** $\rightarrow$ Status set to **🔴 NEEDS PRACTICE**.
2. **Personalised Recommendation**:
   - System automatically generates recommendation: *"NEEDS PRACTICE: Counting. 1 ᱠᱷᱚᱱ 10 ᱦᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟ ᱥᱮᱪᱮᱫ ᱮᱛᱚᱦᱚᱵ ᱯᱮ᱾"*
3. **Personalised Practice Launch**:
   - Student taps **"Start Recommended Practice"** from `StudentHomeScreen`.
   - Completes practice questions, scoring **4 / 5 (80%)**.
4. **Re-Assessment & Mastery Update**:
   - System recalculates score: **$40\% \rightarrow 80\%$** with **$+40$ percentage points gain**!
   - Mastery status updated to **🟢 MASTERED**.
5. **Teacher Insights Dashboard**:
   - Open **Teacher Insights** $\rightarrow$ Dashboard displays updated $80\%$ score, $+40\%$ improvement points, assessment history log, and actionable learning insights (*"12 students mastered counting"*).
