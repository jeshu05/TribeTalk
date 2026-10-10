# TribeTalk: Android Studio Setup & Offline Neural Model Installation Guide

A comprehensive, end-to-end guide to compiling, deploying, and configuring **TribeTalk** on physical Android devices using **Android Studio**, complete with instructions for installing the on-device AI neural models (ASR, NMT, TTS, and Qwen 0.5B SLM).

---

## Table of Contents
1. [Prerequisites & System Requirements](#1-prerequisites--system-requirements)
2. [Android Device Preparation](#2-android-device-preparation)
3. [Android Studio Configuration](#3-android-studio-configuration)
4. [Building & Installing the App](#4-building--installing-the-app)
5. [Neural AI Model Installation (Detailed)](#5-neural-ai-model-installation-detailed)
   - [5.1 Model Architecture & Inventory](#51-model-architecture--inventory)
   - [5.2 Target Storage Location on Device](#52-target-storage-location-on-device)
   - [5.3 Method A: Automated Python / ADB Deployment (Fastest & Recommended)](#53-method-a-automated-python--adb-deployment-fastest--recommended)
   - [5.4 Method B: Android Studio Device File Explorer (GUI)](#54-method-b-android-studio-device-file-explorer-gui)
   - [5.5 Method C: Direct Windows MTP / USB Transfer](#55-method-c-direct-windows-mtp--usb-transfer)
6. [First Run, Permissions & Verification](#6-first-run-permissions--verification)
   - [6.1 Runtime Microphone Permission](#61-runtime-microphone-permission)
   - [6.2 Verifying Model Initialization in Logcat](#62-verifying-model-initialization-in-logcat)
   - [6.3 Fallback Modes (Zero-Crash Safety)](#63-fallback-modes-zero-crash-safety)
7. [Troubleshooting & FAQs](#7-troubleshooting--faqs)

---

## 1. Prerequisites & System Requirements

### Workstation Requirements
* **Operating System**: Windows 10/11, macOS (Apple Silicon or Intel), or Ubuntu Linux 20.04+.
* **Android Studio**: Android Studio Hedgehog (2023.1.1), Jellyfish (2023.3.1), Koala (2024.1.1), or Ladybug (2024.2.1+).
* **Java Development Kit (JDK)**: JDK 17 (bundled automatically with modern Android Studio).
* **Android SDK**:
  * SDK Platform 34 (Android 14.0 "UpsideDownCake").
  * SDK Build-Tools 34.0.0.
  * CMake: Version `3.22.1` (installed via SDK Manager).
  * NDK: Version `28.2.13676358` (or side-by-side NDK 26.x/27.x/28.x).
* **Python (Optional for staging scripts)**: Python 3.9+ with `huggingface_hub`, `onnxruntime`, and `numpy`.

### Target Android Device Requirements
* **Operating System**: Android 8.0 (API level 26) or higher (Recommended: Android 11.0 to Android 14.0 / API 30–34).
* **Processor Architecture**: 64-bit ARM (`arm64-v8a`) or x86_64 (for emulators).
* **RAM**: Minimum 3 GB system RAM (app peak heap consumption is dynamically throttled under 350 MB).
* **Storage Space**: Minimum 2.5 GB free internal storage space (to host the 1.4 GB model suite and generated worksheets).
* **Hardware**: Working microphone and audio loudspeaker.

---

## 2. Android Device Preparation

Before connecting your mobile phone or tablet to Android Studio, you must enable **Developer Options** and **USB Debugging**:

1. **Enable Developer Options**:
   * Open **Settings** on your phone.
   * Navigate to **About Phone** (or **System** > **About Phone**).
   * Locate **Build Number** (on Xiaomi/MIUI/HyperOS tap *OS version*, on Samsung tap *Software information* > *Build number*).
   * Tap **Build Number** rapidly **7 times** until you see the toast: *"You are now a developer!"*.

2. **Enable USB Debugging**:
   * Return to **Settings** > **System** (or **Additional Settings**) > **Developer Options**.
   * Toggle **Developer Options** to **ON**.
   * Scroll down and enable **USB Debugging**.
   * *(Manufacturer-specific)*:
     * **Xiaomi / Poco / Redmi**: Also enable **Install via USB** and **USB Debugging (Security settings)**.
     * **Realme / Oppo / Vivo**: Enable **Disable Permission Monitoring** if installing fails.

3. **Connect to PC & Authorize**:
   * Plug your mobile device into the computer using a high-quality USB data cable.
   * On your phone, change USB connection mode from *Charging Only* to **File Transfer (MTP)**.
   * A dialog will appear on your phone: **"Allow USB debugging?"**.
   * Check **"Always allow from this computer"** and tap **Allow**.

4. **Verify ADB Connection**:
   Open a terminal (PowerShell, Command Prompt, or Bash) and run:
   ```bash
   adb devices
   ```
   **Expected output**:
   ```text
   List of devices attached
   624758d2    device
   ```
   *(If it says `unauthorized`, unlock your phone and accept the prompt. If it is empty, reinstall the OEM USB driver).*

---

## 3. Android Studio Configuration

1. **Open Project**:
   * Launch **Android Studio**.
   * Select **Open** and browse to the cloned `TribeTalk` project root directory:
     ```text
     c:\path\to\TribeTalk
     ```

2. **Verify JDK Version (Must be JDK 17)**:
   * Go to **File** > **Settings** (Windows/Linux) or **Android Studio** > **Settings** (macOS).
   * Navigate to **Build, Execution, Deployment** > **Build Tools** > **Gradle**.
   * Under **Gradle JDK**, select **Embedded JDK version 17** or **jbr-17**.
   * Click **Apply** and **OK**.

3. **Verify SDK, NDK, and CMake Installation**:
   * Open **Tools** > **SDK Manager**.
   * Under **SDK Platforms**, confirm that **Android 14.0 ("UpsideDownCake") / API Level 34** is installed.
   * Under **SDK Tools**:
     * Check **CMake** (version `3.22.1`).
     * Check **NDK (Side by side)** (version `28.2.13676358` or newest 26+).
     * Check **Android SDK Platform-Tools**.
   * Click **Apply** to download missing components if needed.

4. **Sync Gradle**:
   * Click the **Sync Project with Gradle Files** icon (the elephant with a blue arrow) in the top-right toolbar.
   * Wait for Gradle sync to complete. Ensure the build log shows:
     ```text
     BUILD SUCCESSFUL
     ```

---

## 4. Building & Installing the App

1. **Select Target Device**:
   * In the top toolbar device selector dropdown, select your connected physical phone (e.g., `Xiaomi 2201117TI` or your device ID).

2. **Select Build Variant**:
   * In the bottom-left corner, click **Build Variants**.
   * Verify that the active build variant for module `:app` is set to **`debug`**.

3. **Run the App**:
   * Click the green **Run** button (or press `Shift + F10`).
   * Android Studio will:
     1. Compile the C++17 DSP and audio library (`libtribetalk_native.so`) using the Android NDK.
     2. Compile the Kotlin Jetpack Compose code.
     3. Package and assemble the debug APK.
     4. Install the APK to your phone and launch the `MainActivity`.

> [!IMPORTANT]
> **Launch the app at least once before installing models.**
> Running the app for the first time creates the app-specific scoped storage folder on your phone:
> `/sdcard/Android/data/org.tribetalk/files/`

---

## 5. Neural AI Model Installation (Detailed)

TribeTalk uses an entirely offline, on-device AI stack. Because the complete suite of quantized INT8 ONNX models totals **~1.4 GB**, they are managed via the app's external scoped storage rather than being baked directly into the APK. This prevents APK bloating and allows individual models to be upgraded independently.

### 5.1 Model Architecture & Inventory

The models are organized into four dedicated functional directories:

```text
models/
├── asr/                      (Automated Speech Recognition)
│   ├── hindi_conformer.onnx      # AI4Bharat IndicConformer ASR for Hindi (~131 MB INT8 / ~193 MB FP32)
│   ├── hindi_vocab.txt           # Hindi CTC character vocabulary (~3 KB)
│   ├── santali_conformer.onnx    # AI4Bharat IndicConformer ASR for Santali (~131 MB INT8 / ~193 MB FP32)
│   └── santali_vocab.txt         # Santali Ol Chiki character vocabulary (~1 KB)
│
├── nmt/                      (Neural Machine Translation - Phase 3 Shipping Stack)
│   ├── encoder_model_int8_pruned.onnx    # IndicTrans2 320M INT8 Pruned Encoder (~101 MB)
│   ├── decoder_model_merged_pruned.onnx  # Single fused step-1 & autoregressive KV decoder (~166 MB)
│   ├── vocab.src.pruned.tsv              # 93,478-token pruned source vocabulary TSV (~2.1 MB)
│   ├── vocab.tgt.pruned.tsv              # 93,436-token pruned target vocabulary TSV (~2.1 MB)
│   ├── config.json & generation_config.json # Transformer architecture parameters
│   └── [Legacy Fallback: unpruned encoder_model_int8.onnx + decoder_model_merged.onnx also supported]
│
├── tts/                      (Text-To-Speech Synthesis)
│   ├── hindi_tts.onnx            # Meta MMS-TTS Hindi VITS neural voice (~109 MB)
│   ├── hindi_tts_vocab.json      # Hindi phonetic phoneme table (~4 KB)
│   ├── sat_piper_model.onnx      # Vernacular Piper Santali VITS neural voice (~61 MB)
│   └── sat_piper_model.onnx.json # Piper audio sampling and phoneme configuration (~5 KB)
│
└── qwen/                     (Curriculum & Pedagogical Generator SLM)
    ├── model_int8.onnx           # Qwen2.5-0.5B-Instruct INT8 ONNX (~488 MB, or INT4 ~260 MB)
    ├── tokenizer.json            # Fast BPE Byte-Pair Tokenizer (~6.7 MB)
    ├── vocab.json                # Vocabulary index (~2.65 MB)
    ├── tokenizer_config.json     # ChatML special token templates (~7.3 KB)
    ├── special_tokens_map.json   # <|im_start|>, <|im_end|> mappings
    ├── config.json               # Transformer architecture dimensions
    ├── generation_config.json    # Greedy decoding temperature & repetition penalty
    └── manifest.json             # Model version & checksum manifest
```

### 5.2 Target Storage Location on Device

All models must be placed inside the app's scoped external files directory:

```text
/sdcard/Android/data/org.tribetalk/files/models/
```
*(On Android, this resolves to `context.getExternalFilesDir(null)/models`).*

> [!NOTE]
> Android 11+ enforces strict Scoped Storage. Applications always have unrestricted read/write access to their own `Android/data/org.tribetalk/files/` directory **without requiring root or special system storage permissions**.

---

### 5.3 Method A: Automated Python / ADB Deployment (Fastest & Recommended)

All pre-quantized production ONNX models (~1.34 GB) are publicly hosted on Hugging Face:  
🔗 **[jeshu05/tribetalk-models](https://huggingface.co/jeshu05/tribetalk-models/blob/main/models.zip)**

The project includes intelligent deployment tools that auto-detect your Android SDK, ADB path, and attached physical device or emulator.

1. **Option 1: Automated Download & Push in 1 Command**:
   If models are not yet on your PC, you can download from Hugging Face and deploy in a single step:
   ```bash
   python scripts/migrate_models_to_android.py --download
   ```
   Or download to PC first:
   ```bash
   python scripts/download_models.py
   python scripts/migrate_models_to_android.py
   ```

2. **Option 2: Manual Download from Hugging Face**:
   * Download `models.zip` directly from [Hugging Face (models.zip)](https://huggingface.co/jeshu05/tribetalk-models/blob/main/models.zip).
   * Place `models.zip` into the project root folder.
   * Run the deployment script:
     ```bash
     python scripts/migrate_models_to_android.py
     ```
     *(The script auto-detects `models.zip`, extracts missing models, and pushes them to your phone).*

   **What it does automatically:**
   * Auto-locates `adb` from PATH, `ANDROID_HOME`, `local.properties`, or default SDK directories.
   * Auto-detects your connected Android device or emulator.
   * Creates required directories on the device (`/sdcard/Android/data/org.tribetalk/files/models/{asr,nmt,qwen,tts}`).
   * Pushes the production model suite (~1.19 GB) with per-file progress.
   * Runs post-deployment verification and reports confirmation for all models.

3. **Useful Flags**:
   * Test without copying: `python scripts/migrate_models_to_android.py --dry-run`
   * Verify what is already on your phone: `python scripts/migrate_models_to_android.py --verify-only`
   * Target a specific device if multiple are plugged in: `python scripts/migrate_models_to_android.py --device <DEVICE_ID>`
   * Download missing files from Hugging Face: `python scripts/migrate_models_to_android.py --download`

4. **Manual Single ADB Push Alternative**:
   If you prefer raw ADB commands:
   ```bash
   adb push staged_models/. /sdcard/Android/data/org.tribetalk/files/models/
   ```

5. **Verify the Push**:
   ```bash
   adb shell ls -la /sdcard/Android/data/org.tribetalk/files/models/
   ```
   You should see:
   ```text
   drwxrwx--x asr
   drwxrwx--x nmt
   drwxrwx--x qwen
   drwxrwx--x tts
   ```

---

### 5.4 Method B: Android Studio Device File Explorer (GUI)

If you prefer a visual interface without using terminal commands:

1. In Android Studio, open the **Device File Explorer** tool window:
   * Click **View** > **Tool Windows** > **Device File Explorer** (usually located on the right-hand panel).
2. Select your connected device from the dropdown menu at the top.
3. In the tree view, navigate to:
   ```text
   /sdcard/Android/data/org.tribetalk/files/
   ```
   *(Or `/storage/emulated/0/Android/data/org.tribetalk/files/`)*.
4. If the `models` folder does not exist:
   * Right-click on `files` > select **New** > **Directory** > enter `models`.
5. Upload the staged folders:
   * Right-click on the newly created `models` directory.
   * Select **Upload...**.
   * In your local file manager, select all folders inside `staged_models/` (`asr`, `nmt`, `qwen`, `tts`).
   * Click **OK**.
6. Wait for the file transfer progress bar at the bottom of Android Studio to complete.

---

### 5.5 Method C: Direct Windows MTP / USB Transfer

If ADB is unavailable:

1. Connect your phone via USB and ensure **File Transfer (MTP)** mode is enabled on the device.
2. Open **Windows File Explorer** and navigate to:
   ```text
   This PC \ [Your Phone Name] \ Internal shared storage \ Android \ data \ org.tribetalk \ files \
   ```
3. Create a folder named `models`.
4. Copy the `asr`, `nmt`, `tts`, and `qwen` directories from `staged_models/` on your PC into `models`.

> [!TIP]
> On Android 11, 12, 13, and 14, Android locks down direct access to `Android/data` through Windows File Explorer for security reasons. If the `org.tribetalk` folder is invisible in Windows File Explorer, use **Method A (ADB)** or **Method B (Device File Explorer)** which bypass this restriction.

---

## 6. First Run, Permissions & Verification

### 6.1 Runtime Microphone Permission
* When TribeTalk launches, tap the **Microphone** icon in the translator.
* Android will display the permission dialog: *"Allow TribeTalk to record audio?"*.
* Select **"While using the app"**.

### 6.2 Verifying Model Initialization in Logcat
To confirm that all ONNX neural models are correctly discovered and loaded:

1. In Android Studio, open the **Logcat** tab at the bottom.
2. In the filter bar, type:
   ```text
   tag:TribeTalk | tag:QwenLocalModel | tag:NativePipeline | tag:OnnxConformerAsr
   ```
3. Test each component in the app:
   * **Speech-to-Text (ASR)**: Tap the microphone button in the Translator tab and speak in Hindi.
     * *Log confirmation*: `OnnxConformerAsr: Initialized conformer ASR session with model hindi_conformer.onnx`
   * **Translation (NMT)**: Type or speak a sentence and click Translate.
     * *Log confirmation*: `TribeTalkNeuralTranslator: Running IndicTrans2 neural session`
   * **AI Activity Planner (Qwen)**: Go to **Worksheets** > click **AI Generate** > select a topic and generate.
     * *Log confirmation*: `QwenLocalModel: ONNX model loaded successfully from /sdcard/Android/data/org.tribetalk/files/models/qwen/model_int8.onnx`
     * *Log confirmation*: `QwenContentPlanner: Generated valid ActivitySpec JSON in ... ms`

### 6.3 Fallback Modes (Zero-Crash Safety)
TribeTalk is engineered with a **defense-in-depth architecture**:
* **Linguistic Fallback**: If the neural translation weights (`nmt/`) are absent, the app seamlessly falls back to the deterministic computational linguistic engine (`TribeTalkNeuralTranslator.kt`), preserving agglutinative Santali grammar and Ol Chiki transliteration.
* **Curriculum Fallback**: If the Qwen SLM weights (`qwen/model_int8.onnx`) are absent, the app automatically switches to the deterministic pedagogical rulebook (`DeterministicActivityPlanner.kt`), generating authentic NCERT bilingual exercises without any network or model dependency.
* **Audio Fallback**: If the neural TTS models are not yet loaded, audio gracefully falls back to the native Android TextToSpeech engine.

---

## 7. Troubleshooting & FAQs

### Q1: Gradle build fails with `NDK at ... did not have a source.properties file` or version mismatch
* **Fix**: Open `app/build.gradle.kts`. Locate `ndkVersion = "28.2.13676358"`.
* If your Android Studio has a different NDK version installed (e.g., `26.1.10909125` or `27.0.12077973`), update the `ndkVersion` string to match the exact folder name inside `C:\Users\<username>\AppData\Local\Android\Sdk\ndk\<version>`.

### Q2: Build fails with `INSTALL_FAILED_NO_MATCHING_ABIS`
* **Fix**: TribeTalk targets 64-bit systems (`arm64-v8a` for physical phones and `x86_64` for emulators). If you are testing on an ancient 32-bit ARM phone (`armeabi-v7a`), add `"armeabi-v7a"` to `ndk.abiFilters` in `app/build.gradle.kts`.

### Q3: `adb push` returns `remote secure_mkdirs failed: Permission denied`
* **Fix**: This happens if the app has never been installed or run. The OS only creates the app's directory when the app is installed.
  1. Build and run the app from Android Studio once.
  2. Retry the `adb push` command.

### Q4: Worksheets fail to open or share
* **Fix**: TribeTalk uses AndroidX `FileProvider` to safely share generated A4 vector PDFs with PDF viewer apps (Google Drive PDF Viewer, Adobe Acrobat, etc.). Make sure your device has at least one PDF reader installed.

### Q5: Out of Memory (OOM) on budget 2GB/3GB RAM phones
* **Fix**: TribeTalk uses **Sequential Model Leasing**: models are dynamically loaded on-demand and evicted immediately after inference completes. In `app/src/main/AndroidManifest.xml`, `android:largeHeap="true"` is already enabled to provide extra memory headroom.
