# Teacher-Centric Dynamic Assessment & Real-Time Analytics Subsystem

This directory documents the **Teacher-Centric Classroom Assessment & Dynamic Analytics Subsystem** for Jharkhand's PALASH Mother Tongue-Based Multilingual Education (MTB-MLE) system.

---

## 🛑 REASONING FOR REMOVING SEPARATE STUDENT PORTAL

In real-world primary school classrooms across rural Jharkhand:
- Primary school children do NOT login to separate student accounts.
- The teacher operates the single tablet application during classroom sessions.
- Students are identified using lightweight classroom identifiers (`Class 2A` $\rightarrow$ `Student 01`, `Student 02`, `Student 03` ... `Student 20`).

---

## 📌 Architecture & Teacher Workflow

```text
TEACHER HOME
    │
    ├─► Select Class (e.g. Class 2A) & Student (e.g. Student 01)
    │
    ├─► Select Lesson & NIPUN Learning Outcome
    │
    ├─► Launch Classroom Assessment Activity
    │      ├─► 9 Extensible Activity Types (Visual Counting, Addition, Matching, Oral...)
    │      └─► Offline SPRING_F5 Santali TTS Audio Prompt
    │
    ├─► Record Student Answer (✓ Correct / ✗ Incorrect)
    │      └─► Auto-Advance Roster to Next Student (Student 02)
    │
    ├─► Persist AssessmentAttempt Event Locally (ResponsePersistenceRepository.kt)
    │
    ├─► Calculate Dynamic Metrics (DynamicMetricsEngine.kt)
    │      ├─► Student Accuracy % & Class Accuracy %
    │      ├─► Learning Outcome Accuracy & Before/After Progress Gain (+35 pts)
    │      └─► Map Weak Outcomes (< 60%) to Follow-Up Recommended Activities
    │
    └─► Real-Time Teacher Analytics Dashboard (RealtimeTeacherDashboardScreen.kt)
```

---

## 🎯 Extensible 9 Activity Types

1. **`VISUAL_COUNTING`**: Visual emoji stimulus (e.g. 🚗 🚗 🚗) + Santali prompt (*"ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱛᱤᱱᱟᱹᱜ ᱜᱟᱹᱰᱤ ᱢᱮᱱᱟᱜᱼᱟ?"*) + Touch choices (`[3] [4] [5]`).
2. **`NUMBER_RECOGNITION`**: Target number display `7` + Santali prompt (*"7 ᱮᱞ ᱪᱤᱱᱦᱟᱹᱣ ᱯᱮ"*).
3. **`MULTIPLE_CHOICE`**: Visual stimulus + multi-choice options.
4. **`MATCHING`**: Object to symbol/number sequence matching.
5. **`SIMPLE_ADDITION`**: Visual addition stimulus (🍎🍎 + 🍎🍎🍎 = `5`).
6. **`SIMPLE_SUBTRACTION`**: Visual subtraction stimulus.
7. **`PICTURE_IDENTIFICATION`**: Shape/Colour visual identification.
8. **`ORAL_RESPONSE`**: Teacher-evaluated spoken student response (`✓ Correct` / `✗ Incorrect`).
9. **`TRUE_FALSE`**: True / False prompt evaluation.

---

## 📊 Configurable Mastery Classification Thresholds

- **`>= 80%`**: `MASTERED / STRONG` (🟢)
- **`60% - 79%`**: `DEVELOPING` (🟡)
- **`< 60%`**: `NEEDS REINFORCEMENT` (🔴)

---

## 🧪 Verification & Live Demo Procedure

1. **Classroom Assessment Launch**:
   - Open **Lessons** $\rightarrow$ Select **"Counting 1–10"** $\rightarrow$ Tap **"Start Classroom Assessment"**.
   - Roster displays **`Student 01`**.
2. **Conduct Rapid Assessment**:
   - Q1 for `Student 01` $\rightarrow$ Tap **✓ Correct** $\rightarrow$ Auto-advances to **`Student 02`**.
   - Q2 for `Student 02` $\rightarrow$ Tap **✓ Correct** $\rightarrow$ Auto-advances to **`Student 03`**.
   - Q3 for `Student 03` $\rightarrow$ Tap **✗ Incorrect**.
3. **Dynamic Analytics Verification**:
   - Finish activity $\rightarrow$ Opens **Real-Time Analytics Dashboard**.
   - Displays **Total Attempts: 3**, **Correct: 2**, **Incorrect: 1**, **Class Accuracy: 66.7%**.
4. **Weak Area Detection & Follow-up Activity**:
   - If Addition outcome accuracy is $40\%$, dashboard alerts: *"🔴 Needs Reinforcement: Addition (40%). Recommended Follow-up: Visual Addition Using Objects. [START ACTIVITY]"*.
   - Tap **"Start Follow-up Activity"** $\rightarrow$ Re-assess student $\rightarrow$ Score improves $40\% \rightarrow 75\%$ ($+35$ percentage points gain!).
