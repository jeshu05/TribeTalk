# NIPUN/FLN Integration & Navigation Guide

This document describes the teacher-centric navigation and Room integration architecture.

## 📱 Navigation & Screen Flow

```text
Teacher Setup / Home
          │
          ▼
   Class Selection (Class 1, Class 2, Class 3)
          │
          ▼
   FLN Domain Selection (Literacy / Numeracy)
          │
          ▼
   Lesson List Screen
          │
          ▼  [Select Lesson] ──► NAVIGATE TO NEW SCREEN
   Lesson Detail Screen (Dedicated View)
          ├── Domain & NIPUN Outcome
          ├── Lesson Content & Instructions
          ├── Flashcards & Worksheets
          └── Classroom Assessment Trigger
```

---

## 🛑 Navigation Rule
- Lesson content is **NEVER** displayed stacked below selection panels.
- Selecting a lesson opens a **dedicated `LessonDetailScreen`** with back navigation.
