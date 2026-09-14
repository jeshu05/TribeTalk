# TribeTalk NIPUN Bharat & NCF-FS Curriculum Coverage Audit

**Branch**: `stable-talk`  
**Framework Basis**: National Curriculum Framework for Foundational Stage (NCF-FS) 2022 & NIPUN Bharat Guidelines (Ministry of Education, Govt. of India)  
**Status**: Comprehensive Foundational Stage Coverage Audit  
**Document Type**: Official Traceability Matrix  

> [!IMPORTANT]
> In strict compliance with NCF-FS Section 3.2, Learning Outcomes in the Foundational Stage represent **cumulative developmental trajectories** rather than rigid single-grade barriers. A child progresses along these developmental continuums across preschool and early primary grades. Furthermore, non-worksheet competencies (physical gross motor, socio-emotional peer interaction, daily habits) are explicitly categorized as `NOT WORKSHEET-SUITABLE` (requiring play, teacher-led dialogue, or observational assessment) rather than artificially forced into paper tests.

---

## 1. Coverage Status Legend

| Status | Definition |
| :--- | :--- |
| **IMPLEMENTED** | Competency is modeled in `NipunCurriculumRegistry`, verified in bilingual content (Hindi + Santali Ol Chiki), supported with worksheet generation activity types, and covered by automated test suites. |
| **PARTIALLY IMPLEMENTED** | Competency is modeled in the curriculum registry with preliminary exercises; further contextual story/passage variations or specialized dialect expansions are planned. |
| **NOT WORKSHEET-SUITABLE** | Competency pertains to gross motor skills, emotional bonding, or hygiene habits. Per NCF-FS, these MUST be handled via play, teacher-led circle time, or observation (`ACTIVITY_BASED`, `TEACHER_LED`, `OBSERVATION_BASED`). |
| **NOT YET IMPLEMENTED** | Future developmental milestone outside current core foundational priority. |

---

## 2. Complete Foundational Stage Traceability Matrix

### A. Pre-School 1 (Nursery / Age 3–4)

| Stage | Curricular Domain | Curricular Goal | Competency ID | Learning Trajectory (LO) | Worksheet Activity Type | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **PS-1** | Language & Literacy | CG-8 (Oral Language) | C-8.1 | Listens to animal sounds & rhymes | *None (Imitation & Gestures)* | **NOT WORKSHEET-SUITABLE** (`ACTIVITY_BASED`) |
| **PS-1** | Language & Literacy | CG-9 (Emergent Literacy) | C-9.1 | Associates animal pictures with spoken names | `PICTURE_IDENTIFICATION`, `MATCHING` | **IMPLEMENTED** (`PS1-LANG-CG9-C91-01`) |
| **PS-1** | Numeracy & Math | CG-7 (Number Sense) | C-7.1 | Distinguishes big vs small objects | `PICTURE_IDENTIFICATION`, `MATCHING` | **IMPLEMENTED** (`PS1-NUM-CG7-C71-01`) |
| **PS-1** | Numeracy & Math | CG-7 (Number Sense) | C-7.2 | Recites number names & counts up to 3 | `COUNT_AND_WRITE`, `MATCHING` | **IMPLEMENTED** (`PS1-NUM-CG7-C72-01`) |
| **PS-1** | Cognitive Understanding | CG-4 (Sensory Exploration) | C-4.1 | Matches identical fruits and textures | `MATCHING`, `CLASSIFICATION` | **IMPLEMENTED** (`PS1-COG-CG4-C41-01`) |
| **PS-1** | Physical Development | CG-1 (Motor Development) | C-1.2 | Pincer grip, pre-writing vertical strokes | `TRACE_OR_WRITE` | **IMPLEMENTED** (`PS1-PHY-CG1-C12-01`) |
| **PS-1** | Socio-Emotional | CG-3 (Emotional Well-being) | C-3.1 | Expresses feelings & greets peers (Johar) | *None (Peer Play)* | **NOT WORKSHEET-SUITABLE** (`OBSERVATION_BASED`) |

---

### B. Pre-School 2 (LKG / Age 4–5)

| Stage | Curricular Domain | Curricular Goal | Competency ID | Learning Trajectory (LO) | Worksheet Activity Type | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **PS-2** | Language & Literacy | CG-9 (Emergent Literacy) | C-9.1 | Identifies initial sounds (Akshara / Ol) | `PICTURE_IDENTIFICATION`, `MATCHING`, `TRACE_OR_WRITE` | **IMPLEMENTED** (`PS2-LANG-CG9-C91-01`) |
| **PS-2** | Language & Literacy | CG-8 (Oral Language) | C-8.2 | Expands conversational classroom vocabulary | `WORD_MEANING`, `MATCHING` | **IMPLEMENTED** (`PS2-LANG-CG8-C82-01`) |
| **PS-2** | Numeracy & Math | CG-7 (Number Sense) | C-7.2 | Counts concrete objects up to 5 | `COUNT_AND_WRITE`, `MATCHING`, `ORDERING` | **IMPLEMENTED** (`PS2-NUM-CG7-C72-01`) |
| **PS-2** | Numeracy & Math | CG-7 (Number Sense) | C-7.3 | Recognizes basic 2D shapes (Circle, Square) | `MATCHING`, `CLASSIFICATION` | **IMPLEMENTED** (`PS2-NUM-CG7-C73-01`) |
| **PS-2** | Cognitive Understanding | CG-5 (Classification) | C-5.1 | Sorts domestic vs wild animals | `CLASSIFICATION`, `MATCHING` | **IMPLEMENTED** (`PS2-COG-CG5-C51-01`) |
| **PS-2** | Positive Learning Habits | CG-13 (Focus & Habits) | C-13.1 | Follows two-step instructions & cleans up | *None (Classroom Routine)* | **NOT WORKSHEET-SUITABLE** (`TEACHER_LED`) |

---

### C. Pre-School 3 / Balvatika (UKG / Age 5–6)

| Stage | Curricular Domain | Curricular Goal | Competency ID | Learning Trajectory (LO) | Worksheet Activity Type | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **BAL** | Language & Literacy | CG-9 (Sound-Symbol) | C-9.2 | Letter-sound association (Ol Chiki letters) | `TRACE_OR_WRITE`, `MATCHING`, `PICTURE_IDENTIFICATION` | **IMPLEMENTED** (`BAL-LANG-CG9-C92-01`) |
| **BAL** | Language & Literacy | CG-10 (Emergent Reading) | C-10.1 | Reads simple 2-letter sight words (घर / ᱚᱲᱟᱜ) | `FILL_IN_THE_BLANK`, `WORD_MEANING`, `MULTIPLE_CHOICE` | **IMPLEMENTED** (`BAL-LANG-CG10-C101-01`) |
| **BAL** | Numeracy & Math | CG-7 (Number Sense) | C-7.2 | Counts sets up to 10 with 1-to-1 correspondence | `COUNT_AND_WRITE`, `ORDERING`, `MATCHING` | **IMPLEMENTED** (`BAL-NUM-CG7-C72-01`) |
| **BAL** | Numeracy & Math | CG-8 (Operations) | C-8.1 | Concrete picture combining (addition up to 5) | `SOLVE`, `COUNT_AND_WRITE` | **IMPLEMENTED** (`BAL-NUM-CG8-C81-01`) |
| **BAL** | Cognitive Understanding | CG-5 (Patterns) | C-5.2 | Extends repeating AB/AAB shape patterns | `COMPLETE_PATTERN`, `MULTIPLE_CHOICE` | **IMPLEMENTED** (`BAL-COG-CG5-C52-01`) |
| **BAL** | Aesthetic Expression | CG-12 (Art & Culture) | C-12.1 | Traces Sohrai / Kohver traditional murals | `TRACE_OR_WRITE` | **IMPLEMENTED** (`BAL-AES-CG12-C121-01`) |

---

### D. Grade 1 (Class 1 / Age 6–7)

| Stage | Curricular Domain | Curricular Goal | Competency ID | Learning Trajectory (LO) | Worksheet Activity Type | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **G-1** | Language & Literacy | CG-10 (Comprehension) | C-10.2 | Reads 3–4 word simple sentences | `READ_AND_ANSWER`, `FILL_IN_THE_BLANK`, `MULTIPLE_CHOICE` | **IMPLEMENTED** (`G1-LANG-CG10-C102-01`) |
| **G-1** | Language & Literacy | CG-11 (Writing) | C-11.1 | Labels domestic animals & objects | `WORD_MEANING`, `MATCHING`, `FILL_IN_THE_BLANK` | **IMPLEMENTED** (`G1-LANG-CG11-C111-01`) |
| **G-1** | Numeracy & Math | CG-7 (Place Value) | C-7.4 | Numbers up to 20; Ascending order | `ORDERING`, `SOLVE`, `MULTIPLE_CHOICE` | **IMPLEMENTED** (`G1-NUM-CG7-C74-01`) |
| **G-1** | Numeracy & Math | CG-8 (Operations) | C-8.2 | Single-digit addition & subtraction (up to 9) | `SOLVE`, `SHORT_ANSWER` | **IMPLEMENTED** (`G1-NUM-CG8-C82-01`) |
| **G-1** | Cognitive / Environmental | CG-6 (Community Helpers) | C-6.1 | Matches community helpers with tools | `MATCHING`, `WORD_MEANING` | **IMPLEMENTED** (`G1-COG-CG6-C61-01`) |

---

### E. Grade 2 (Class 2 / Age 7–8)

| Stage | Curricular Domain | Curricular Goal | Competency ID | Learning Trajectory (LO) | Worksheet Activity Type | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **G-2** | Language & Literacy | CG-10 (Fluent Reading) | C-10.3 | Reads short stories (NIPUN Target: 45–60 WPM) | `READ_AND_ANSWER`, `TRUE_FALSE`, `SHORT_ANSWER` | **IMPLEMENTED** (`G2-LANG-CG10-C103-01`) |
| **G-2** | Language & Literacy | CG-8 (Grammar & Depth) | C-8.3 | Matches bilingual antonym pairs (विलोम शब्द) | `MATCHING`, `WORD_MEANING` | **IMPLEMENTED** (`G2-LANG-CG8-C83-01`) |
| **G-2** | Numeracy & Math | CG-7 (Place Value) | C-7.5 | 2-digit place value (Tens and Ones up to 99) | `SOLVE`, `FILL_IN_THE_BLANK` | **IMPLEMENTED** (`G2-NUM-CG7-C75-01`) |
| **G-2** | Numeracy & Math | CG-8 (Operations) | C-8.3 | 2-digit addition without regrouping up to 99 | `SOLVE`, `SHORT_ANSWER` | **IMPLEMENTED** (`G2-NUM-CG8-C83-01`) |
| **G-2** | Environmental Science | CG-4 (Natural Resources) | C-4.2 | Clean water sources and hygiene statements | `TRUE_FALSE`, `CLASSIFICATION`, `READ_AND_ANSWER` | **IMPLEMENTED** (`G2-COG-CG4-C42-01`) |

---

### F. Grade 3 (Class 3 / Age 8–9)

| Stage | Curricular Goal | Competency ID | Learning Trajectory (LO) | Worksheet Activity Type | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **G-3** | CG-10 (Advanced Reading) | C-10.4 | Reads with comprehension (NIPUN Target: >= 60 WPM) | `READ_AND_ANSWER`, `SHORT_ANSWER`, `MULTIPLE_CHOICE` | **IMPLEMENTED** (`G3-LANG-CG10-C104-01`) |
| **G-3** | CG-11 (Writing) | C-11.2 | Frames grammatically structured sentences | `FILL_IN_THE_BLANK`, `SHORT_ANSWER` | **IMPLEMENTED** (`G3-LANG-CG11-C112-01`) |
| **G-3** | CG-7 (Number Sense) | C-7.6 | 3-digit numbers up to 999; Descending order | `ORDERING`, `SOLVE` | **IMPLEMENTED** (`G3-NUM-CG7-C76-01`) |
| **G-3** | CG-8 (Operations) | C-8.4 | Multiplication (tables up to 10) & sharing | `SOLVE`, `SHORT_ANSWER`, `MULTIPLE_CHOICE` | **IMPLEMENTED** (`G3-NUM-CG8-C84-01`) |
| **G-3** | CG-8 (Fractions) | C-8.5 | Visual fractions: One-half (1/2) & quarter (1/4) | `PICTURE_IDENTIFICATION`, `SOLVE` | **IMPLEMENTED** (`G3-NUM-CG8-C85-01`) |
| **G-3** | CG-6 (Ecosystems) | C-6.2 | Indigenous trees (Sal, Mahua) & festivals | `MATCHING`, `READ_AND_ANSWER`, `TRUE_FALSE` | **IMPLEMENTED** (`G3-COG-CG6-C62-01`) |

---

## 3. Bilingual Verification Audit Summary

| Status Category | Count | Percentage | Handling Policy |
| :--- | :---: | :---: | :--- |
| **VERIFIED** | 22 | 84.6% | Validated against official primers, NCERT Vidya Pravesh, and Ol Chiki literature. |
| **TEACHER_VERIFIED** | 0 (Dynamic) | — | Generated dynamically when a teacher edits questions in `WorksheetPreviewScreen`. |
| **NEEDS_REVIEW** | 3 | 11.5% | Displayed with warning badge in preview; teacher review encouraged before print. |
| **UNAVAILABLE** | 1 | 3.9% | Rendered with fallback prompt `Santali: Teacher verification required`. |
