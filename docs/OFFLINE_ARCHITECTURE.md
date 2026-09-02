# 100% Offline Architecture & System Integrity

This document outlines the offline guarantees for TribeTalk.

## 🔒 100% Offline System Guarantees

The entire TribeTalk application operates with:
- Wi-Fi = OFF
- Mobile Data = OFF
- Airplane Mode = ON

---

## 🛠️ Offline Subsystem Checklist

1. **Teacher Local Setup**: Saved in local Room database.
2. **Curriculum Browsing**: Queries pre-packaged `nipun.db` asset.
3. **Hindi Speech ASR**: AI4Bharat IndicConformer CTC ONNX INT8 local model.
4. **Hindi ➔ Santali NMT**: AI4Bharat IndicTrans2 ONNX INT8 local model.
5. **Santali Speech TTS**: SPRINGLab SPRING_F5 DiT + Vocos ONNX local model.
6. **Classroom Assessments & Analytics**: Evaluated dynamically from persisted SQLite response events.
7. **Local Teacher PDF/PPT Uploads**: Persisted in local app-specific storage with SQLite paths.
