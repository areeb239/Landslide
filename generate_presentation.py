import os
import sys
import json
import base64
from pathlib import Path

# Paths
WORKSPACE = Path(r"c:\Users\HP\.gemini\antigravity-ide\scratch\Landslide")
OUT_DIR = WORKSPACE / "sih_presentation"
OUT_DIR.mkdir(exist_ok=True)

# Helper to read image as base64
def img_to_b64(path):
    if os.path.exists(path):
        with open(path, "rb") as f:
            ext = Path(path).suffix.lstrip(".").lower()
            if ext == "jpg": ext = "jpeg"
            return f"data:image/{ext};base64,{base64.b64encode(f.read()).decode('utf-8')}"
    return ""

logo_b64 = img_to_b64(OUT_DIR / "logo.png")
home_b64 = img_to_b64(OUT_DIR / "screen_home.png")
map_b64 = img_to_b64(OUT_DIR / "screen_map.png")
doppler_b64 = img_to_b64(OUT_DIR / "s_doppler.png")
report_b64 = img_to_b64(OUT_DIR / "s_report.png")

print("Building SIH 2026 Presentation HTML...")
