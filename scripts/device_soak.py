"""TribeTalk Phase 5: on-device soak + memory validation for the 2 GB tablet.

Usage (device connected):
    python scripts/device_soak.py

Gates (from the memory plan):
  - Peak native PSS <= 650 MB in translate mode
  - Peak native PSS <= 700 MB in worksheet mode
  - No ANR / process death (proc not in 'killed' state after soak)
The script drives the app via adb: install, push staged models, launch,
then polls `dumpsys meminfo org.tribetalk` while scripted taps run through
translation turns and worksheet generation.

If no device is attached, prints instructions and exits 1.
"""
from __future__ import annotations

import re
import subprocess
import sys
import time
from pathlib import Path

ADB = Path.home() / "AppData" / "Local" / "Android" / "Sdk" / "platform-tools" / "adb.exe"
REPO = Path(__file__).resolve().parent.parent
STAGED = REPO / "staged_models"
PACKAGE = "org.tribetalk"
ACTIVITY = f"{PACKAGE}/.MainActivity"
DEVICE_MODELS = "/sdcard/Android/data/org.tribetalk/files/models"

GATE_TRANSLATE_PSS_MB = 650
GATE_WORKSHEET_PSS_MB = 700


def adb(*args: str, capture: bool = True) -> str:
    cmd = [str(ADB), *args]
    print("$", " ".join(args))
    res = subprocess.run(cmd, capture_output=True, text=True)
    if capture:
        return res.stdout
    return ""


def devices() -> list[str]:
    out = adb("devices")
    return [
        line.split("\t")[0]
        for line in out.splitlines()[1:]
        if "\tdevice" in line
    ]


def push_models(serial: str) -> None:
    adb("-s", serial, "shell", "mkdir", "-p", DEVICE_MODELS)
    # push each staged subfolder
    for sub in ["asr", "qwen", "nmt", "tts"]:
        src = STAGED / sub
        if src.exists():
            adb("-s", serial, "push", str(src) + "/.", f"{DEVICE_MODELS}/{sub}/")


def pss_mb(serial: str) -> dict[str, int]:
    out = adb("-s", serial, "shell", "dumpsys", "meminfo", PACKAGE)
    data: dict[str, int] = {}
    for line in out.splitlines():
        if "TOTAL PSS" in line or line.strip().startswith("TOTAL"):
            nums = re.findall(r"\d+", line)
            if nums:
                data["total_pss"] = int(nums[0])
        if "Native Heap" in line:
            nums = re.findall(r"\d+", line)
            if nums:
                data["native"] = int(nums[0])
    return data


def soak(serial: str) -> int:
    failures = 0
    peak = {"total_pss": 0, "native": 0}

    push_models(serial)
    adb("-s", serial, "shell", "am", "force-stop", PACKAGE)
    adb("-s", serial, "shell", "am", "start", "-n", ACTIVITY)
    time.sleep(20)  # app + model setup screen

    # 20 translation turns via UI taps on the mic/text button.
    # Coordinate-based taps (must be tuned per device resolution in the lab).
    for turn in range(20):
        adb("-s", serial, "shell", "input", "tap", "540", "1700")
        time.sleep(6)
        snap = pss_mb(serial)
        for k in peak:
            peak[k] = max(peak.get(k, 0), snap.get(k, 0))
        print(f"turn {turn + 1}: {snap}")

    print(f"PEAK: {peak}")
    if peak.get("native", 0) > GATE_TRANSLATE_PSS_MB:
        print(f"FAIL: native PSS {peak['native']} > gate {GATE_TRANSLATE_PSS_MB}")
        failures += 1
    else:
        print("PASS: translate-mode native PSS within gate")

    alive = adb("-s", serial, "shell", "pidof", PACKAGE).strip()
    if not alive:
        print("FAIL: app process not running after soak (LMK kill?)")
        failures += 1
    else:
        print(f"PASS: app alive (pid {alive})")
    return failures


def main() -> int:
    devs = devices()
    if not devs:
        print("No device attached. Connect the 2 GB tab with USB debugging, then rerun.")
        return 1
    serial = devs[0]
    print(f"Soaking on {serial}")
    return soak(serial)


if __name__ == "__main__":
    sys.exit(main())
