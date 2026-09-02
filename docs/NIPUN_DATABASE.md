# Room Pre-Packaged Database Documentation (`nipun.db`)

This document describes the reproducible SQLite database pipeline and Room integration for the offline NIPUN/FLN curriculum layer.

## 📌 Reproducible Data Pipeline

```text
Approved Curriculum Source (JSON)
          │
          ▼
   data/curriculum/demo/curriculum_demo.json
          │
          ▼
   scripts/build_nipun_db.py
          │
          ▼
   app/src/main/assets/database/nipun.db
          │
          ▼
   Room Database (.createFromAsset("database/nipun.db"))
```

---

## 🛠️ Database Re-generation & Validation Steps

1. **Edit Curriculum Source**: Update `data/curriculum/demo/curriculum_demo.json`.
2. **Build Database**:
   ```bash
   python scripts/build_nipun_db.py
   ```
3. **Validate Database**:
   ```bash
   python scripts/validate_nipun_db.py
   ```

---

## 🏷️ Official vs Demo Data Policy

- Every curriculum record includes an `isOfficial` boolean flag.
- Demo records are tagged with `isOfficial = false` and labeled:
  ```text
  DEMO — NOT OFFICIAL CURRICULUM
  ```
- Official curriculum materials will update `isOfficial = true` without breaking schema or DAOs.
