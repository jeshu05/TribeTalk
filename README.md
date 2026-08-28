# TribeTalk (SIH 2026 — Problem Statement: SIH26042)

TribeTalk is an educational classroom translation companion developed for the **Smart India Hackathon (SIH 2026)**. The application is designed to bridge the communication gap between teachers and students in tribal regions by enabling real-time local dialect translation.

The initial prototype platform is **Android (API 28+ / Android 9.0+)**, optimized to run on low-resource devices (down to 2 GB RAM). The MVP language flow bridges **Hindi ↔ Santali**.

---

## 🚀 Project Status: Milestone Progress

### 📍 Phase 1: Shell & User Interface (Complete)
- **Modern Build System**: Created a standard Kotlin + Jetpack Compose Gradle project configured via a central Version Catalog (`libs.versions.toml`).
- **Teacher Dashboard**: A clean, professional, responsive Material 3 layout presenting:
  - Teaching Language: Hindi | Student Language: Santali configuration.
  - A prominent call-to-action button for the **Live Classroom**.
  - Secondary academic slots (Lessons, Worksheets, Learning Insights, Settings).
- **Navigation**: Designed a lightweight, zero-overhead state-based router optimized for 2 GB devices (avoiding heavy external navigation libraries).

### 📍 Phase 2: Text Translation Integration (Complete)
- **Decoupled Translation Layer**: Abstracted the translation logic using a custom interface:
  ```
  TranslationEngine
         ↓
   MockTranslationEngine (Phase 2 Local Dictionary)
         ↓
   OfflineNmtTranslationEngine (Future local NMT model integration)
  ```
- **Local Dictionary**: Mapped FLN-domain test sentences covering greetings, directions, numbers, and doubts in Romanized and Devanagari scripts (Hindi ↔ Santali).
- **Workspace UI**: Added a functional Text Translation Workspace directly below the flow charts on the Live Classroom screen:
  - Source/Target selectors.
  - **Swap** button (exchanges source/target and swaps input/output text).
  - Multiline text input with a built-in clear action.
  - **Copy** button to capture translations directly to the clipboard with visual system toast alerts.

---

## 🔄 Two-Way Translation Flow

```mermaid
graph TD
    subgraph Hindi to Santali Flow (Teacher to Student)
        A[Teacher Speaks in Hindi] --> B[ASR Processes Speech to Hindi Text]
        B --> C[MT Engine Translates Hindi to Santali]
        C --> D[Student Hears/Reads Santali]
    end

    subgraph Santali to Hindi Flow (Student to Teacher)
        E[Student Speaks in Santali] --> F[ASR Processes Speech to Santali Text]
        F --> G[MT Engine Translates Santali to Hindi]
        G --> H[Teacher Hears/Reads Hindi]
    end
    
    style A fill:#e8f1f5,stroke:#1e56a0,stroke-width:2px
    style D fill:#f6f6f6,stroke:#0f2c59,stroke-width:2px
    style E fill:#e8f1f5,stroke:#1e56a0,stroke-width:2px
    style H fill:#f6f6f6,stroke:#0f2c59,stroke-width:2px
```

---

## 🛠️ Build and Execution Instructions

### Prerequisites
- **JDK 21**
- **Android SDK (API 34 / Build Tools 34.0.0)**

### 1. Compile Kotlin Sources
Checks scripts, enums, UI components, and models for compiler errors:
```bash
.\gradlew.bat compileDebugKotlin
```

### 2. Assemble Debug Binary
Packages files and compiles resources into an installable `.apk`:
```bash
.\gradlew.bat assembleDebug
```
*The output binary is located at `app/build/outputs/apk/debug/app-debug.apk`.*

### 3. Deploy to Connected Physical Device
Install the compiled debug binary directly onto an Android device connected via ADB with USB Debugging enabled:
```bash
.\gradlew.bat installDebug
```

---

## 📂 Project Architecture Layout

```text
TribeTalk/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/alchemists/tribetalk/
│   │       │       ├── MainActivity.kt        # Entry point & navigation state router
│   │       │       ├── translation/
│   │       │       │   ├── Language.kt          # Hindi and Santali Enums
│   │       │       │   ├── TranslationEngine.kt # Translation contract interface
│   │       │       │   └── MockTranslationEngine.kt # Dictionary matching logic
│   │       │       └── ui/
│   │       │           ├── screens/
│   │       │           │   ├── DashboardScreen.kt     # Dashboard layout
│   │       │           │   ├── LiveClassroomScreen.kt # Visual diagram + Translation workspace
│   │       │           │   └── PlaceholderScreens.kt  # Shell page template
│   │       │           └── theme/
│   │       │               ├── Color.kt
│   │       │               ├── Theme.kt
│   │       │               └── Type.kt
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                          # Central library Version Catalog
├── build.gradle.kts                                # Project level script
├── settings.gradle.kts                             # Module registrations
└── gradle.properties                               # Parallel compilation configurations
```
