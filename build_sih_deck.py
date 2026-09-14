import os
import sys
import json
import base64
import subprocess
import shutil
from pathlib import Path
import pymupdf
from pptx import Presentation
from pptx.util import Inches

WORKSPACE = Path(r"c:\Users\HP\.gemini\antigravity-ide\scratch\Landslide")
OUT_DIR = WORKSPACE / "sih_presentation"
OUT_DIR.mkdir(exist_ok=True)

def img_to_b64(path):
    p = Path(path)
    if p.exists():
        with open(p, "rb") as f:
            ext = p.suffix.lstrip(".").lower()
            if ext == "jpg": ext = "jpeg"
            return f"data:image/{ext};base64,{base64.b64encode(f.read()).decode('utf-8')}"
    return ""

logo_b64 = img_to_b64(OUT_DIR / "logo.png")
home_b64 = img_to_b64(OUT_DIR / "screen_home.png")
map_b64 = img_to_b64(OUT_DIR / "screen_map.png")
doppler_b64 = img_to_b64(OUT_DIR / "s_doppler.png")
report_b64 = img_to_b64(OUT_DIR / "s_report.png")
feat_importance_b64 = img_to_b64(OUT_DIR / "feat_importance.png")
rf_vs_xgb_b64 = img_to_b64(OUT_DIR / "rf_vs_xgb.png")
sys_infographic_b64 = img_to_b64(OUT_DIR / "sys_infographic.jpg")

# Official SIH Brain Bulb Graphic (Center Right on Slide 1) with subtle hexagon background
sih_brain_bulb_svg = '''
<svg viewBox="0 0 440 500" width="390" height="460" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="gradOrange" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#ff9800" />
      <stop offset="100%" stop-color="#e65100" />
    </linearGradient>
    <linearGradient id="gradGreen" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#4caf50" />
      <stop offset="100%" stop-color="#1b5e20" />
    </linearGradient>
  </defs>
  
  <!-- Subtle hexagon polygon background like in official template -->
  <polygon points="220,15 375,105 375,285 220,375 65,285 65,105" fill="#f1f5f9" stroke="#cbd5e1" stroke-width="2.5" />

  <!-- Bulb Outline -->
  <path d="M 220,40 C 130,40 85,105 85,195 C 85,255 125,305 145,345 C 155,365 165,380 165,405 L 275,405 C 275,380 285,365 295,345 C 315,305 355,255 355,195 C 355,105 310,40 220,40 Z" 
        fill="#ffffff" stroke="#cbd5e1" stroke-width="6" />
        
  <!-- Ray lines -->
  <line x1="220" y1="12" x2="220" y2="30" stroke="#475569" stroke-width="6" stroke-linecap="round"/>
  <line x1="95" y1="75" x2="110" y2="88" stroke="#475569" stroke-width="6" stroke-linecap="round"/>
  <line x1="345" y1="75" x2="330" y2="88" stroke="#475569" stroke-width="6" stroke-linecap="round"/>
  <line x1="45" y1="195" x2="65" y2="195" stroke="#475569" stroke-width="6" stroke-linecap="round"/>
  <line x1="395" y1="195" x2="375" y2="195" stroke="#475569" stroke-width="6" stroke-linecap="round"/>
  <line x1="90" y1="315" x2="105" y2="305" stroke="#475569" stroke-width="6" stroke-linecap="round"/>
  <line x1="350" y1="315" x2="335" y2="305" stroke="#475569" stroke-width="6" stroke-linecap="round"/>

  <!-- Left Brain Half (Orange Circuit) -->
  <path d="M 215,75 C 180,75 160,90 150,105 C 135,110 120,130 120,155 C 120,175 130,190 135,200 C 125,215 125,235 135,255 C 145,275 165,290 180,300 C 195,310 210,320 215,340 Z" 
        fill="url(#gradOrange)" />
  <circle cx="160" cy="125" r="4.5" fill="#ffffff" />
  <circle cx="185" cy="105" r="4.5" fill="#ffffff" />
  <circle cx="145" cy="175" r="4.5" fill="#ffffff" />
  <circle cx="165" cy="205" r="4.5" fill="#ffffff" />
  <circle cx="155" cy="245" r="4.5" fill="#ffffff" />
  <circle cx="190" cy="275" r="4.5" fill="#ffffff" />
  <line x1="160" y1="125" x2="185" y2="105" stroke="#ffffff" stroke-width="2.5" />
  <line x1="145" y1="175" x2="165" y2="205" stroke="#ffffff" stroke-width="2.5" />
  <line x1="165" y1="205" x2="155" y2="245" stroke="#ffffff" stroke-width="2.5" />
  <line x1="155" y1="245" x2="190" y2="275" stroke="#ffffff" stroke-width="2.5" />
  <line x1="160" y1="125" x2="215" y2="125" stroke="#ffffff" stroke-width="2" />
  <line x1="165" y1="205" x2="215" y2="205" stroke="#ffffff" stroke-width="2" />
  <line x1="190" y1="275" x2="215" y2="275" stroke="#ffffff" stroke-width="2" />

  <!-- Right Brain Half (Green Binary) -->
  <path d="M 225,75 C 260,75 280,90 290,105 C 305,110 320,130 320,155 C 320,175 310,190 305,200 C 315,215 315,235 305,255 C 295,275 275,290 260,300 C 245,310 230,320 225,340 Z" 
        fill="url(#gradGreen)" />
  <text x="235" y="115" font-family="monospace" font-size="16" font-weight="bold" fill="#ffffff">10101</text>
  <text x="235" y="142" font-family="monospace" font-size="16" font-weight="bold" fill="#ffffff">01010</text>
  <text x="235" y="169" font-family="monospace" font-size="16" font-weight="bold" fill="#ffffff">101010</text>
  <text x="235" y="196" font-family="monospace" font-size="16" font-weight="bold" fill="#ffffff">010101</text>
  <text x="235" y="223" font-family="monospace" font-size="16" font-weight="bold" fill="#ffffff">10101</text>
  <text x="235" y="250" font-family="monospace" font-size="16" font-weight="bold" fill="#ffffff">01010</text>
  <text x="235" y="277" font-family="monospace" font-size="16" font-weight="bold" fill="#ffffff">1010</text>
  <text x="235" y="304" font-family="monospace" font-size="16" font-weight="bold" fill="#ffffff">010</text>

  <!-- Base -->
  <rect x="170" y="415" width="100" height="12" rx="6" fill="#64748b" />
  <rect x="175" y="433" width="90" height="12" rx="6" fill="#475569" />
  <rect x="185" y="451" width="70" height="12" rx="6" fill="#334155" />
  <path d="M 200,468 Q 220,480 240,468 Z" fill="#1e293b" />

  <rect x="175" y="358" width="90" height="36" rx="6" fill="#ffffff" stroke="#334155" stroke-width="2" />
  <text x="220" y="383" font-family="Arial, sans-serif" font-size="20" font-weight="900" fill="#1e293b" text-anchor="middle" letter-spacing="2">SIH</text>
</svg>
'''

# Official SIH Top Right Header Logo
sih_top_right_logo_svg = '''
<div style="display: flex; align-items: center; gap: 10px;">
  <svg viewBox="0 0 100 120" width="48" height="58" xmlns="http://www.w3.org/2000/svg">
    <path d="M 50,5 L 85,25 L 85,65 L 50,85 L 15,65 L 15,25 Z" fill="#ffffff" stroke="#ff9800" stroke-width="2"/>
    <path d="M 50,5 L 50,85 L 15,65 L 15,25 Z" fill="#e65100"/>
    <path d="M 50,5 L 85,25 L 85,65 L 50,85 Z" fill="#2e7d32"/>
    <circle cx="32" cy="30" r="3" fill="#fff"/>
    <circle cx="26" cy="50" r="3" fill="#fff"/>
    <circle cx="40" cy="65" r="3" fill="#fff"/>
    <line x1="32" y1="30" x2="26" y2="50" stroke="#fff" stroke-width="1.5"/>
    <line x1="26" y1="50" x2="40" y2="65" stroke="#fff" stroke-width="1.5"/>
    <text x="60" y="36" font-family="monospace" font-size="8" font-weight="bold" fill="#fff">101</text>
    <text x="56" y="48" font-family="monospace" font-size="8" font-weight="bold" fill="#fff">0101</text>
    <text x="56" y="60" font-family="monospace" font-size="8" font-weight="bold" fill="#fff">1010</text>
    <rect x="30" y="90" width="40" height="18" rx="4" fill="#37474f"/>
    <text x="50" y="103" font-family="Arial, sans-serif" font-size="11" font-weight="900" fill="#ffffff" text-anchor="middle">SIH</text>
  </svg>
  <div style="text-align: left; line-height: 1.1;">
    <div style="font-family: 'Times New Roman', serif; font-size: 15px; font-weight: 900; color: #1a237e; letter-spacing: 0.5px;">SMART INDIA</div>
    <div style="font-family: 'Times New Roman', serif; font-size: 15px; font-weight: 900; color: #1a237e; letter-spacing: 0.5px;">HACKATHON</div>
    <div style="font-family: Arial, sans-serif; font-size: 16px; font-weight: 800; color: #37474f;">2026</div>
  </div>
</div>
'''

# Header badge with TerraTech team name + Bhoochetak logo (for pages 2, 3, 4, 5, 6)
def make_top_left_badge():
    return f'''
    <div style="display: flex; align-items: center; gap: 12px; border: 2.5px solid #5c2d91; border-radius: 30px; padding: 6px 20px; background: #ffffff; box-shadow: 0 3px 8px rgba(92,45,145,0.15);">
      <img src="{logo_b64}" style="width: 42px; height: 42px; border-radius: 9px; object-fit: cover;" />
      <div style="display: flex; flex-direction: column; line-height: 1.15; text-align: left;">
        <span style="font-size: 20px; font-weight: 900; color: #1e293b; letter-spacing: 0.5px;">Bhoochetak</span>
        <span style="font-size: 13.5px; font-weight: 800; color: #d32f2f; letter-spacing: 1.2px;">TERRATECH</span>
      </div>
    </div>
    '''

# Slide 2 Center Ecosystem Diagram SVG with larger, high-contrast labels
slide_2_ecosystem_svg = '''
<svg viewBox="0 0 460 210" width="100%" height="210" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="coreGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#7e22ce" />
      <stop offset="100%" stop-color="#4c1d95" />
    </linearGradient>
    <filter id="shadow" x="-10%" y="-10%" width="120%" height="120%">
      <feDropShadow dx="0" dy="2" stdDeviation="3" flood-opacity="0.15" />
    </filter>
  </defs>

  <!-- Connecting Lines -->
  <line x1="150" y1="46" x2="200" y2="85" stroke="#93c5fd" stroke-width="3" stroke-dasharray="5,3"/>
  <line x1="310" y1="46" x2="260" y2="85" stroke="#86efac" stroke-width="3" stroke-dasharray="5,3"/>
  <line x1="200" y1="125" x2="150" y2="164" stroke="#f472b6" stroke-width="3" stroke-dasharray="5,3"/>
  <line x1="260" y1="125" x2="310" y2="164" stroke="#fdba74" stroke-width="3" stroke-dasharray="5,3"/>

  <!-- Top-Left: NASA GLC & COOLR -->
  <g filter="url(#shadow)">
    <rect x="8" y="14" width="150" height="60" rx="8" fill="#eff6ff" stroke="#3b82f6" stroke-width="2" />
    <text x="18" y="37" font-family="Inter, sans-serif" font-size="13.5" font-weight="900" fill="#1e40af">🛰️ NASA GLC + COOLR</text>
    <text x="18" y="58" font-family="Inter, sans-serif" font-size="12.5" font-weight="800" fill="#2563eb">570 Real Events (Label 1)</text>
  </g>

  <!-- Top-Right: Pseudo-Absence Safe Points -->
  <g filter="url(#shadow)">
    <rect x="302" y="14" width="150" height="60" rx="8" fill="#f0fdf4" stroke="#22c55e" stroke-width="2" />
    <text x="312" y="37" font-family="Inter, sans-serif" font-size="13.5" font-weight="900" fill="#15803d">🛡️ Pseudo-Absence Grid</text>
    <text x="312" y="58" font-family="Inter, sans-serif" font-size="12.5" font-weight="800" fill="#16a34a">1,710 Sloped Pts (Label 0)</text>
  </g>

  <!-- Center Core Hub -->
  <g filter="url(#shadow)">
    <circle cx="230" cy="105" r="52" fill="url(#coreGrad)" stroke="#ffffff" stroke-width="3.5" />
    <text x="230" y="89" font-family="Inter, sans-serif" font-size="13.5" font-weight="900" fill="#ffffff" text-anchor="middle">XGBOOST AI</text>
    <text x="230" y="113" font-family="Inter, sans-serif" font-size="21" font-weight="900" fill="#38bdf8" text-anchor="middle">81.8%</text>
    <text x="230" y="130" font-family="Inter, sans-serif" font-size="11.5" font-weight="800" fill="#f1f5f9" text-anchor="middle">CV RECALL</text>
  </g>

  <!-- Bottom-Left: Hill Communities -->
  <g filter="url(#shadow)">
    <rect x="8" y="136" width="150" height="60" rx="8" fill="#fdf2f8" stroke="#ec4899" stroke-width="2" />
    <text x="18" y="159" font-family="Inter, sans-serif" font-size="13.5" font-weight="900" fill="#9d174d">🏔️ Hill Communities</text>
    <text x="18" y="180" font-family="Inter, sans-serif" font-size="12.5" font-weight="800" fill="#be185d">1-3d Alerts & 2G SOS</text>
  </g>

  <!-- Bottom-Right: MDoNER & SDMA -->
  <g filter="url(#shadow)">
    <rect x="302" y="136" width="150" height="60" rx="8" fill="#fff7ed" stroke="#f97316" stroke-width="2" />
    <text x="312" y="159" font-family="Inter, sans-serif" font-size="13.5" font-weight="900" fill="#9a3412">🏛️ MDoNER / SDMA</text>
    <text x="312" y="180" font-family="Inter, sans-serif" font-size="12.5" font-weight="800" fill="#c2410c">Tactical GIS Corridors</text>
  </g>
</svg>
'''

html_content = f'''<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Bhoochetak — Smart India Hackathon 2026 (SIH26001)</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&family=Merriweather:wght@700;900&display=swap');

  * {{
    box-sizing: border-box;
    margin: 0;
    padding: 0;
  }}

  body {{
    font-family: 'Inter', system-ui, -apple-system, sans-serif;
    background: #525659;
    color: #0f172a;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 30px;
    padding: 30px 0;
  }}

  .slide {{
    width: 1920px;
    height: 1080px;
    background: #ffffff;
    position: relative;
    overflow: hidden;
    box-shadow: 0 12px 36px rgba(0,0,0,0.3);
    page-break-after: always;
    display: flex;
    flex-direction: column;
  }}

  @page {{
    size: 1920px 1080px;
    margin: 0;
  }}

  @media print {{
    @page {{
      size: 1920px 1080px;
      margin: 0;
    }}
    body {{
      background: none;
      padding: 0;
      gap: 0;
      width: 1920px;
      height: auto;
    }}
    .slide {{
      box-shadow: none;
      width: 1920px !important;
      height: 1080px !important;
      max-width: 1920px !important;
      max-height: 1080px !important;
      page-break-after: always !important;
      page-break-inside: avoid !important;
    }}
  }}

  .slide-header {{
    height: 80px;
    padding: 8px 40px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 2px solid #e2e8f0;
    background: #ffffff;
    flex-shrink: 0;
  }}

  .slide-title {{
    font-family: 'Times New Roman', serif;
    font-size: 38px;
    font-weight: 900;
    color: #0f172a;
    letter-spacing: 1px;
    text-transform: uppercase;
  }}

  .slide-body {{
    height: 964px;
    padding: 12px 40px 10px 40px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    background: #f8fafc;
    overflow: hidden;
  }}

  .slide-footer {{
    height: 36px;
    padding: 6px 40px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-top: 1.5px solid #e2e8f0;
    background: #ffffff;
    font-size: 14px;
    color: #64748b;
    font-weight: 700;
    flex-shrink: 0;
  }}

  .grid-3 {{
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 14px;
    height: 844px;
  }}

  .grid-2 {{
    display: grid;
    grid-template-columns: 1.05fr 0.95fr;
    gap: 14px;
    height: 844px;
  }}

  .col-card {{
    background: #ffffff;
    border-radius: 10px;
    border: 1px solid #cbd5e1;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    padding: 14px 16px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 100%;
  }}

  .section-pill {{
    display: inline-flex;
    align-items: center;
    border-radius: 24px;
    padding: 6px 18px;
    font-size: 16.5px;
    font-weight: 800;
    color: #ffffff;
    letter-spacing: 0.5px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.12);
    align-self: flex-start;
  }}

  .pill-blue {{ background: linear-gradient(90deg, #1565c0, #1976d2); }}
  .pill-purple {{ background: linear-gradient(90deg, #6a1b9a, #8e24aa); }}
  .pill-green {{ background: linear-gradient(90deg, #1b5e20, #2e7d32); }}
  .pill-teal {{ background: linear-gradient(90deg, #00695c, #00897b); }}
  .pill-orange {{ background: linear-gradient(90deg, #e65100, #f57c00); }}

  .pill-num {{
    width: 26px;
    height: 26px;
    border-radius: 50%;
    background: #ffffff;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    margin-right: 10px;
    font-size: 13.5px;
    font-weight: 900;
  }}
  .pill-blue .pill-num {{ color: #1565c0; }}
  .pill-purple .pill-num {{ color: #6a1b9a; }}
  .pill-green .pill-num {{ color: #1b5e20; }}
  .pill-teal .pill-num {{ color: #00695c; }}
  .pill-orange .pill-num {{ color: #e65100; }}

  .feature-item {{
    display: flex;
    align-items: center;
    gap: 12px;
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 9px 12px;
  }}

  .feature-icon {{
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    flex-shrink: 0;
  }}

  .feature-text {{
    font-size: 14px;
    line-height: 1.45;
    color: #1e293b;
  }}
  .feature-text strong {{
    color: #0f172a;
    font-weight: 800;
  }}

  .callout-box {{
    background: linear-gradient(135deg, #fff9db 0%, #fff3bf 100%);
    border: 1.5px solid #fcc419;
    border-radius: 8px;
    padding: 11px 14px;
  }}
  .callout-title {{
    font-size: 14.5px;
    font-weight: 800;
    color: #d9480f;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 4px;
  }}
  .callout-desc {{
    font-size: 14px;
    color: #334155;
    line-height: 1.45;
    font-weight: 600;
  }}

  .process-ribbon {{
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
    height: 48px;
  }}

  .chevron-step {{
    flex: 1;
    height: 46px;
    background: #eef2f6;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13.5px;
    font-weight: 800;
    color: #1e293b;
    border-left: 4.5px solid #1976d2;
    padding: 0 8px;
    text-align: center;
    box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  }}

  table.sih-table {{
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;
    background: #ffffff;
    border-radius: 8px;
    overflow: hidden;
  }}

  table.sih-table th {{
    background: #edf2f7;
    color: #0f172a;
    font-weight: 800;
    padding: 9px 12px;
    text-align: left;
    border-bottom: 2px solid #cbd5e1;
    font-size: 13.5px;
  }}

  table.sih-table td {{
    padding: 7.5px 12px;
    border-bottom: 1px solid #e2e8f0;
    color: #1e293b;
    vertical-align: middle;
    font-size: 13px;
    line-height: 1.4;
  }}

  table.sih-table tbody tr:nth-child(even) {{
    background: #f8fafc;
  }}

  .badge-high {{
    display: inline-block;
    padding: 3px 9px;
    border-radius: 10px;
    background: #fee2e2;
    color: #b91c1c;
    font-weight: 800;
    font-size: 12.5px;
  }}
  .badge-med {{
    display: inline-block;
    padding: 3px 9px;
    border-radius: 10px;
    background: #fef3c7;
    color: #b45309;
    font-weight: 800;
    font-size: 12.5px;
  }}

  .phone-mockup {{
    background: #0f172a;
    border-radius: 12px;
    padding: 6px;
    box-shadow: 0 4px 14px rgba(0,0,0,0.22);
    display: flex;
    flex-direction: column;
    align-items: center;
  }}
  .phone-mockup img {{
    width: 100%;
    height: auto;
    max-height: 380px;
    object-fit: cover;
    border-radius: 8px;
    display: block;
  }}
  .phone-label {{
    font-size: 13.5px;
    font-weight: 800;
    color: #ffffff;
    margin-top: 6px;
    text-align: center;
  }}
</style>
</head>
<body>

<!-- ========================================================================= -->
<!-- SLIDE 1: TITLE PAGE (Exact SIH format, standard alignment, no app logo) -->
<!-- ========================================================================= -->
<div class="slide" id="slide-1">
  <div class="slide-header" style="justify-content: flex-end; border: none; height: 95px; padding: 20px 60px 0 60px;">
    {sih_top_right_logo_svg}
  </div>

  <div class="slide-body" style="padding: 24px 80px 40px 80px; justify-content: flex-start; background: #ffffff;">
    <div style="font-family: 'Times New Roman', serif; font-size: 62px; font-weight: 900; color: #1a237e; letter-spacing: 2px; margin-bottom: 25px;">
      SMART INDIA HACKATHON 2026
    </div>
    
    <div style="font-family: 'Times New Roman', serif; font-size: 38px; font-weight: 800; color: #111827; letter-spacing: 1.5px; margin-bottom: 45px;">
      TITLE PAGE
    </div>

    <div style="display: grid; grid-template-columns: 1.3fr 0.7fr; gap: 40px; align-items: center;">
      <!-- Details Column: Standard position, only required team & problem statement info, large font for mobile -->
      <div style="display: flex; flex-direction: column; gap: 30px; font-size: 28px; line-height: 1.5; color: #111827;">
        <div>
          <span style="font-weight: 700;">• Problem Statement ID – </span>
          <span style="font-weight: 900; color: #d32f2f;">SIH26001</span>
        </div>
        
        <div>
          <span style="font-weight: 700;">• Problem Statement Title- </span>
          <span style="font-weight: 900; color: #d32f2f;">AI-Based early warning and landslide Risk Monitoring System in NER</span>
        </div>
        
        <div>
          <span style="font-weight: 700;">• Theme- </span>
          <span style="font-weight: 900; color: #d32f2f;">Disaster Management</span>
        </div>
        
        <div>
          <span style="font-weight: 700;">• PS Category- </span>
          <span style="font-weight: 800; color: #111827;">Software</span>
        </div>
        
        <div>
          <span style="font-weight: 700;">• Team ID- </span>
          <span style="font-weight: 900; color: #d32f2f;">[Your Team ID]</span>
        </div>
        
        <div>
          <span style="font-weight: 700;">• Team Name (Registered on portal) – </span>
          <span style="font-weight: 900; color: #d32f2f;">TerraTech</span>
        </div>
      </div>

      <!-- Center-Right SIH Brain Lightbulb Illustration with Hexagon Polygon -->
      <div style="display: flex; justify-content: center; align-items: center;">
        {sih_brain_bulb_svg}
      </div>
    </div>
  </div>

  <div class="slide-footer" style="background: transparent; border-top: 2px solid #f1f3f7; color: #64748b; font-size: 14px;">
    <span>@SIH Idea submission- Template</span>
    <span>1</span>
  </div>
</div>

<!-- ========================================================================= -->
<!-- SLIDE 2: IDEA TITLE (PROPOSED SOLUTION & HOW WE TAKE DATA) -->
<!-- ========================================================================= -->
<div class="slide" id="slide-2">
  <div class="slide-header">
    {make_top_left_badge()}
    <div class="slide-title">IDEA TITLE</div>
    {sih_top_right_logo_svg}
  </div>

  <div class="slide-body">
    <div style="font-size: 17px; font-weight: 800; color: #1565c0; display: flex; align-items: center; gap: 8px;">
      <span>❖</span> Proposed Solution (Describe your Idea/Solution/Prototype)
    </div>

    <div class="grid-3" style="height: 825px;">
      <!-- Section 1: Proposed Solution (Dense, 6 full features + scope) -->
      <div class="col-card">
        <div class="section-pill pill-blue">
          <span class="pill-num">1</span> Proposed Solution
        </div>
        
        <div style="font-size: 14.5px; font-weight: 600; color: #0f172a; line-height: 1.45; background: #e0f2fe; padding: 10px 14px; border-radius: 8px; border-left: 4px solid #0288d1;">
          <strong>Bhoochetak</strong> is an AI-driven, offline-first landslide early warning system predicting risk zones <strong>1–3 days in advance</strong> across all 8 NER states.
        </div>

        <div class="feature-item">
          <div class="feature-icon" style="background: #e0f2fe; color: #0288d1;">🛰️</div>
          <div class="feature-text"><strong>Multi-Source Satellite Fusion:</strong> Integrates NASA SRTM 30m DEM, ESA WorldCover 10m, and GLiM lithology with live weather telemetry.</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: #f3e8ff; color: #7e22ce;">🧠</div>
          <div class="feature-text"><strong>High-Recall XGBoost ML Engine:</strong> 81.8% recall catching real landslide events, trained on 2,280 validated NER coordinates.</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: #dcfce7; color: #15803d;">🗺️</div>
          <div class="feature-text"><strong>Offline Tactical GIS Mapping:</strong> Interactive hazard polygons, risk contours, and designated safe corridors rendered via Leaflet GIS.</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: #ffedd5; color: #c2410c;">📡</div>
          <div class="feature-text"><strong>Zero-Internet SMS SOS Beacon:</strong> 1-tap distress dispatch transmitting GPS coordinates directly over 2G cellular signalling.</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: #f1f5f9; color: #475569;">🏛️</div>
          <div class="feature-text"><strong>MDoNER Administrative Hub:</strong> Real-time situational telemetry for highway authorities and district emergency officers.</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: #fdf4ff; color: #c026d3;">🗣️</div>
          <div class="feature-text"><strong>Vernacular Audio Broadcast:</strong> Native text-to-speech sirens in 8 regional dialects ensuring zero linguistic exclusion.</div>
        </div>

        <div style="background: #eff6ff; border: 1.5px solid #bfdbfe; border-radius: 8px; padding: 10px 14px; font-size: 13.5px; color: #1e40af; line-height: 1.4;">
          <strong>Geographical Scope (8 NER States):</strong> Assam, Meghalaya, Sikkim, Arunachal Pradesh, Nagaland, Manipur, Mizoram, Tripura (262,179 km²).
        </div>
      </div>

      <!-- Section 2: How We Take Data & Solve Gaps (Complete Scientific Pipeline + Diagram) -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-purple">
            <span class="pill-num">2</span> How We Take Data & Solve Gaps
          </div>
        </div>

        <!-- Positive Class Data Card -->
        <div style="background: #fdf4ff; border: 1.5px solid #d8b4fe; border-radius: 8px; padding: 10px 14px;">
          <div style="font-size: 14.5px; font-weight: 800; color: #6b21a8; text-transform: uppercase; margin-bottom: 4px;">
            📊 Positive Class: 570 Real Landslide Events
          </div>
          <div style="font-size: 13.5px; color: #4a044e; line-height: 1.45;">
            • <strong>NASA Global Landslide Catalog (GLC):</strong> 251 NER points via data.nasa.gov.<br>
            • <strong>NASA/NSIDC High Mountain Asia v2 (COOLR):</strong> 498 NER points.<br>
            • <strong>Deduplication:</strong> Merged to 749 rows → removed 179 duplicates via rounded lat/long + date → <strong>570 unique real events</strong> across all 8 states.
          </div>
        </div>

        <!-- Negative Class Sampling Card -->
        <div style="background: #eff6ff; border: 1.5px solid #93c5fd; border-radius: 8px; padding: 10px 14px;">
          <div style="font-size: 14.5px; font-weight: 800; color: #1e40af; text-transform: uppercase; margin-bottom: 4px;">
            🛡️ Negative Class: 1,710 Pseudo-Absence Points
          </div>
          <div style="font-size: 13.5px; color: #1e3a8a; line-height: 1.45;">
            • <strong>Actual NER State Polygons:</strong> GeoBoundaries polygons (no bbox leakage into Tibet/Bhutan/Myanmar/Bangladesh).<br>
            • <strong>KDTree 5km Exclusion:</strong> Drops points within 5km of any known slide.<br>
            • <strong>Slope &ge;5&deg; Constraint:</strong> Excludes flat valleys to prevent trivial shortcuts.<br>
            • <strong>1:3 Ratio:</strong> 570 positive : 1,710 negative = <strong>2,280 validated rows</strong>.
          </div>
        </div>

        <!-- Ecosystem Data Flow SVG Diagram -->
        <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 4px 0;">
          {slide_2_ecosystem_svg}
        </div>

        <div style="background: #f5f3ff; border: 1.5px solid #ddd6fe; border-radius: 8px; padding: 9px 12px; text-align: center;">
          <strong style="font-size: 13.5px; color: #5b21b6;">Missing Data Recovery:</strong>
          <span style="font-size: 13.5px; color: #4c1d95;"> Tile edge gaps recovered via elevation API; 127 rock gaps fixed via <code>sjoin_nearest</code>. 100% complete feature coverage!</span>
        </div>
      </div>

      <!-- Section 3: Innovation & Uniqueness -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-green">
            <span class="pill-num">3</span> Innovation & Uniqueness
          </div>
        </div>

        <div class="feature-item">
          <div class="feature-icon" style="background: #ecfdf5; color: #059669;">⚡</div>
          <div class="feature-text"><strong>Hybrid Offline Android Architecture:</strong> Works 100% locally with Room DB caching; auto-syncs when network returns.</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: #eff6ff; color: #2563eb;">🌧️</div>
          <div class="feature-text"><strong>Multi-Day Soil Saturation Bias Fix:</strong> Evaluates 3-day and 7-day cumulative rainfall rather than naive 1-day downpours.</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: #fdf4ff; color: #c026d3;">🗣️</div>
          <div class="feature-text"><strong>8 Native Regional Languages:</strong> Assamese, Bodo, Hindi, Khasi, Manipuri, Mizo, Nepali, and English with audio broadcasts.</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: #fff1f2; color: #e11d48;">🚨</div>
          <div class="feature-text"><strong>Zero-Data GSM SMS SOS Dispatch:</strong> Encodes GPS coordinates and battery telemetry over cellular voice channels.</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: #fefce8; color: #854d0e;">📈</div>
          <div class="feature-text"><strong>Graduated Tiered Risk Index:</strong> Outputs 0–100% continuous probability with Low/Moderate/High tiers preventing false panic.</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: #f0fdf4; color: #166534;">⏱️</div>
          <div class="feature-text"><strong>Sub-50ms Edge Execution:</strong> High-performance optimized pipeline runs on edge devices without requiring cloud compute.</div>
        </div>

        <!-- Comparative Advantage Box -->
        <div style="background: #f8fafc; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 9px 12px; font-size: 13.5px; color: #334155; line-height: 1.45;">
          <strong>Static Maps vs Bhoochetak:</strong> Existing atlases are static, published once every few years. Bhoochetak evaluates <em>live weather saturation on demand</em> for dynamic predictions.
        </div>

        <div class="callout-box">
          <div class="callout-title">⭐ KEY DIFFERENTIATOR</div>
          <div class="callout-desc">
            Not a static map → A <strong>dynamic, date-specific predictive system</strong> trained on 2,280 validated NER points with <strong>81.8% recall</strong> that protects communities even when cellular towers collapse.
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Process Ribbon -->
    <div class="process-ribbon">
      <div class="chevron-step" style="border-left-color: #1565c0;">1. NASA & Satellite Ingestion</div>
      <div style="color: #94a3b8; font-weight: 800;">➔</div>
      <div class="chevron-step" style="border-left-color: #7b1fa2;">2. 5-Feature Extraction</div>
      <div style="color: #94a3b8; font-weight: 800;">➔</div>
      <div class="chevron-step" style="border-left-color: #c2185b;">3. XGBoost Risk Scoring (81.8% Recall)</div>
      <div style="color: #94a3b8; font-weight: 800;">➔</div>
      <div class="chevron-step" style="border-left-color: #00796b;">4. Tactical GIS Mapping</div>
      <div style="color: #94a3b8; font-weight: 800;">➔</div>
      <div class="chevron-step" style="border-left-color: #e65100;">5. Zero-Data GSM SMS SOS</div>
      <div style="color: #94a3b8; font-weight: 800;">➔</div>
      <div class="chevron-step" style="border-left-color: #2e7d32; font-weight: 900; color: #1b5e20;">6. Pre-Emptive Evacuation</div>
    </div>
  </div>

  <div class="slide-footer">
    <span>@SIH 2026</span>
    <span>2</span>
  </div>
</div>

<!-- ========================================================================= -->
<!-- SLIDE 3: TECHNICAL APPROACH (TECHNOLOGY STACK, 5 FEATURES & SYSTEM ARCH) -->
<!-- ========================================================================= -->
<div class="slide" id="slide-3">
  <div class="slide-header">
    {make_top_left_badge()}
    <div class="slide-title">TECHNICAL APPROACH</div>
    {sih_top_right_logo_svg}
  </div>

  <div class="slide-body">
    <div class="grid-3" style="height: 865px;">
      <!-- Col 1: Technologies to be Used (Grouped cleanly like Alpha Co) -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-blue">
            <span class="pill-num">1</span> Technologies to be Used
          </div>
          <div style="font-size: 14px; color: #475569; margin-top: 4px; font-weight: 700;">Production-grade open-source software stack:</div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 8px;">
          <div>
            <div style="font-size: 14px; font-weight: 800; color: #1e40af; text-transform: uppercase; margin-bottom: 2px;">📱 Mobile Presentation Layer</div>
            <div style="background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 7px; padding: 7px 10px; font-size: 13.5px; line-height: 1.45;">
              <strong>Kotlin 2.0</strong>, <strong>Jetpack Compose</strong> (Material 3), <strong>Hilt DI</strong>, <strong>Coroutines & Flow</strong>, <strong>Room Database</strong>, Text-to-Speech audio engine.
            </div>
          </div>

          <div>
            <div style="font-size: 14px; font-weight: 800; color: #6b21a8; text-transform: uppercase; margin-bottom: 2px;">🧠 Machine Learning & Inference Engine</div>
            <div style="background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 7px; padding: 7px 10px; font-size: 13.5px; line-height: 1.45;">
              <strong>Python 3.11+</strong>, <strong>FastAPI</strong>, <strong>Uvicorn ASGI</strong>, <strong>XGBoost</strong>, <strong>Scikit-Learn</strong>, <strong>Joblib</strong>, automated 5-feature inference pipeline.
            </div>
          </div>

          <div>
            <div style="font-size: 14px; font-weight: 800; color: #15803d; text-transform: uppercase; margin-bottom: 2px;">🗺️ Geospatial & Meteorological Stack</div>
            <div style="background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 7px; padding: 7px 10px; font-size: 13.5px; line-height: 1.45;">
              <strong>Leaflet.js GIS</strong>, <strong>Open-Meteo Historical Archive API</strong>, <strong>GeoPandas</strong>, <strong>Shapely</strong>, <strong>SciPy KDTree</strong> spatial nearest-neighbor indexing.
            </div>
          </div>

          <div>
            <div style="font-size: 14px; font-weight: 800; color: #c2410c; text-transform: uppercase; margin-bottom: 2px;">💾 Persistence & Edge Storage</div>
            <div style="background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 7px; padding: 7px 10px; font-size: 13.5px; line-height: 1.45;">
              <strong>Room SQLite</strong> (Offline vector map & incident cache), <strong>Firebase Cloud Firestore</strong>, <strong>Encrypted Preferences DataStore</strong>.
            </div>
          </div>

          <div>
            <div style="font-size: 14px; font-weight: 800; color: #475569; text-transform: uppercase; margin-bottom: 2px;">📡 Life-Safety & Hardware Telemetry</div>
            <div style="background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 7px; padding: 7px 10px; font-size: 13.5px; line-height: 1.45;">
              <strong>GSM SMS Gateway</strong> (2G SMS PDU encoding), <strong>Android WorkManager</strong> (Adaptive polling), <strong>FusedLocationProvider</strong> (GNSS lock).
            </div>
          </div>

          <div>
            <div style="font-size: 14px; font-weight: 800; color: #0f766e; text-transform: uppercase; margin-bottom: 2px;">🌐 Data Engineering & Pipeline Utilities</div>
            <div style="background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 7px; padding: 7px 10px; font-size: 13.5px; line-height: 1.45;">
              <strong>NumPy</strong>, <strong>Pandas</strong>, <strong>PyProj</strong> (EPSG:4326 to UTM 46N), <strong>RichDEM</strong> gradient fallback, <strong>OpenTopography API</strong>.
            </div>
          </div>
        </div>

        <div style="background: #f0fdf4; border: 1.5px solid #86efac; border-radius: 8px; padding: 9px 12px; font-size: 13.5px; color: #166534;">
          <strong>Deployment Ready:</strong> Packaged as a single standalone <code>bhoochetak_pipeline.pkl</code> with automated <code>get_features(lat, lon, date)</code> helper.
        </div>
      </div>

      <!-- Col 2: The 5 Engineered Features & Physical Causality -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-green">
            <span class="pill-num">2</span> 5-Feature Pipeline & Sources
          </div>
          <div style="font-size: 14px; color: #475569; margin-top: 4px; font-weight: 700;">Physical features driving 81.8% recall across all 8 NER states:</div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 6px;">
          <div class="feature-item" style="padding: 7px 10px;">
            <div style="font-size: 18px; width: 26px; text-align: center;">⛰️</div>
            <div class="feature-text">
              <strong>Elevation (m):</strong> NASA SRTM 30m DEM (OpenTopography). Range: 6m–6,526m across Himalayan terrain. Orographic rain barrier analysis.
            </div>
          </div>

          <div class="feature-item" style="padding: 7px 10px;">
            <div style="font-size: 18px; width: 26px; text-align: center;">📐</div>
            <div class="feature-text">
              <strong>Slope (&deg;):</strong> Computed via local gradient method on 30m DEM grid. Range: 0&deg;–65.6&deg;. Dictates gravitational shear stress & slope instability.
            </div>
          </div>

          <div class="feature-item" style="padding: 7px 10px;">
            <div style="font-size: 18px; width: 26px; text-align: center;">🌧️</div>
            <div class="feature-text">
              <strong>Rainfall (1d, 3d, 7d mm):</strong> Open-Meteo historical archive API. 3d & 7d cumulative rain saturates soil pore water pressure (#1 failure trigger).
            </div>
          </div>

          <div class="feature-item" style="padding: 7px 10px;">
            <div style="font-size: 18px; width: 26px; text-align: center;">🏘️</div>
            <div class="feature-text">
              <strong>Land Cover:</strong> ESA WorldCover 10m (12 tiles). Built-up land shows ~20x higher incidence (21.9% vs 1.1%) due to construction slope cutting.
            </div>
          </div>

          <div class="feature-item" style="padding: 7px 10px;">
            <div style="font-size: 18px; width: 26px; text-align: center;">🪨</div>
            <div class="feature-text">
              <strong>Lithology:</strong> GLiM Global Lithological Map (~2,000 NER polygons). Metamorphic (2x) and mixed sedimentary (3x) rocks are structurally weaker.
            </div>
          </div>
        </div>

        <!-- How We Take & Recover Data Box -->
        <div style="background: #f0fdf4; border: 1.5px solid #86efac; border-radius: 8px; padding: 8px 11px; font-size: 13px; color: #166534; line-height: 1.45;">
          <strong style="font-size: 13.5px;">Satellite Ingestion & 100% Data Gap Recovery:</strong><br>
          • <strong>DEM Edge Gaps:</strong> Missing SRTM boundary tiles recovered via public OpenTopography elevation API.<br>
          • <strong>Lithology Recovery:</strong> 127 rock polygons resolved via <code>sjoin_nearest</code> spatial neighbor match.<br>
          • <strong>Weather Extraction:</strong> Live queries to Open-Meteo historical archive API for antecedent rainfall sums.
        </div>

        <!-- Excluded Features Card (Answers Prompt Rationale) -->
        <div style="background: #fff7ed; border: 1.5px solid #fed7aa; border-radius: 8px; padding: 8px 11px; font-size: 13px; color: #9a3412; line-height: 1.45;">
          <strong style="font-size: 13.5px;">Deliberately Excluded Features & Leakage Control:</strong><br>
          • <strong>Casualties/Injuries:</strong> Excluded as post-event outcomes (data leakage).<br>
          • <strong>State Dummies:</strong> Excluded location labels; model learns pure physics.<br>
          • <strong>Month/Season:</strong> Dropped after discovering synthetic negative dates introduced artificial seasonal bias; rainfall carries the true seasonal signal.<br>
          • <strong>OSM Road/River:</strong> Modern road network mismatches historical 1990 slides.
        </div>

        <div style="background: #fdf4ff; border: 1.5px solid #f0abfc; border-radius: 7px; padding: 7px 11px; font-size: 13.5px; color: #701a75;">
          <strong>Compounding Causal Reality:</strong> Slopes fail when structural weakness meets monsoon saturation and Seismic Zone V shaking.
        </div>
      </div>

      <!-- Col 3: System Architecture with Embedded 3D Diagram -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-purple">
            <span class="pill-num">3</span> Working System Architecture
          </div>
        </div>

        <!-- Embedded Visual System Diagram -->
        <div style="border-radius: 8px; overflow: hidden; border: 1.5px solid #cbd5e1; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
          <img src="{sys_infographic_b64}" style="width: 100%; height: auto; max-height: 380px; object-fit: cover; display: block;" />
        </div>

        <!-- Connected 4-Tier Architecture Breakdown -->
        <div style="display: flex; flex-direction: column; gap: 5px;">
          <div style="border: 1.5px solid #93c5fd; background: #eff6ff; border-radius: 7px; padding: 5px 10px;">
            <div style="font-size: 13.5px; font-weight: 800; color: #1e40af;">1. PRESENTATION TIER (KOTLIN COMPOSE)</div>
            <div style="font-size: 13px; color: #1e293b; font-weight: 600;">Bhoochetak Citizen App & Field Tool • M3 UI • 8 Regional Languages • Voice Audio</div>
          </div>

          <div style="border: 1.5px solid #86efac; background: #f0fdf4; border-radius: 7px; padding: 5px 10px;">
            <div style="font-size: 13.5px; font-weight: 800; color: #166534;">2. EDGE COMPUTING & RESILIENT TELEMETRY</div>
            <div style="font-size: 13px; color: #1e293b; font-weight: 600;">Room SQLite Database • 2G GSM SMS SOS Gateway • BackgroundRiskWorker</div>
          </div>

          <div style="border: 1.5px solid #d8b4fe; background: #faf5ff; border-radius: 7px; padding: 5px 10px;">
            <div style="font-size: 13.5px; font-weight: 800; color: #6b21a8;">3. API & AI INFERENCE SERVICE (FASTAPI)</div>
            <div style="font-size: 13px; color: #1e293b; font-weight: 600;">FastAPI Microservice (POST /predict) • XGBoost Pipeline (81.8% Recall) • &lt;50ms Latency</div>
          </div>

          <div style="border: 1.5px solid #fdba74; background: #fff7ed; border-radius: 7px; padding: 5px 10px;">
            <div style="font-size: 13.5px; font-weight: 800; color: #9a3412;">4. GLOBAL EARTH OBSERVATION & METEOROLOGY</div>
            <div style="font-size: 13px; color: #1e293b; font-weight: 600;">NASA SRTM DEM • ESA WorldCover 10m • Open-Meteo Weather • GLiM Lithology</div>
          </div>
        </div>

        <div style="background: #f1f5f9; border: 1.5px solid #cbd5e1; border-radius: 7px; padding: 7px 11px; font-size: 13.5px; color: #1e293b; text-align: center;">
          ⚡ <strong>Sub-50ms Inference:</strong> Edge pipeline returns tiered risk (0–100%) categorized into Low, Moderate, and High.
        </div>
      </div>
    </div>

    <!-- Bottom summary banner -->
    <div style="background: #ffffff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 8px 24px; display: flex; align-items: center; justify-content: space-between; font-size: 14.5px; font-weight: 800; color: #1e293b; box-shadow: 0 1px 4px rgba(0,0,0,0.05);">
      <span>🔒 Edge-First Offline Architecture</span>
      <span>📊 2,280 Validated NER Data Points</span>
      <span>⚡ 81.8% Cross-Validation Recall</span>
      <span>📡 Zero-Data GSM Life Safety</span>
    </div>
  </div>

  <div class="slide-footer">
    <span>@SIH 2026</span>
    <span>3</span>
  </div>
</div>

<!-- ========================================================================= -->
<!-- SLIDE 4: FEASIBILITY, MODEL EVALUATION & RISKS -->
<!-- ========================================================================= -->
<div class="slide" id="slide-4">
  <div class="slide-header">
    {make_top_left_badge()}
    <div class="slide-title">FEASIBILITY AND VIABILITY</div>
    {sih_top_right_logo_svg}
  </div>

  <div class="slide-body">
    <div class="grid-3" style="height: 865px;">
      <!-- Col 1: Model Evaluation Table & Analysis -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-blue">
            <span class="pill-num">1</span> Model Selection & Evaluation
          </div>
          <div style="font-size: 13px; font-weight: 800; color: #1e40af; text-transform: uppercase; margin-top: 4px;">
            🏆 Head-to-Head Comparison: Random Forest vs XGBoost
          </div>
        </div>

        <!-- Embedded Comparison Image from User -->
        <div style="border-radius: 8px; overflow: hidden; border: 1.5px solid #cbd5e1; background: #000;">
          <img src="{rf_vs_xgb_b64}" style="width: 100%; height: auto; display: block;" />
        </div>

        <!-- Metric Details & Rationale Table -->
        <table class="sih-table">
          <thead>
            <tr>
              <th>Evaluation Metric</th>
              <th>Random Forest</th>
              <th style="background: #dbeafe; color: #1e40af;">XGBoost (Chosen)</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>CV Recall (5-Fold)</strong></td>
              <td>75.7% &plusmn; 6.3%</td>
              <td style="background: #eff6ff; font-weight: 900; color: #1d4ed8;">81.8% &plusmn; 2.9%</td>
            </tr>
            <tr>
              <td><strong>Test Recall (Unseen)</strong></td>
              <td>78.1%</td>
              <td style="background: #eff6ff; font-weight: 900; color: #1d4ed8;">81.6%</td>
            </tr>
            <tr>
              <td><strong>Real Slides Missed</strong></td>
              <td>25 events</td>
              <td style="background: #eff6ff; font-weight: 900; color: #15803d;">21 (Lowest)</td>
            </tr>
            <tr>
              <td><strong>Test Accuracy</strong></td>
              <td>85.2%</td>
              <td style="background: #eff6ff; font-weight: 900; color: #1d4ed8;">88.0%</td>
            </tr>
            <tr>
              <td><strong>F1-Score (Slide Class)</strong></td>
              <td>0.72</td>
              <td style="background: #eff6ff; font-weight: 900; color: #1d4ed8;">0.77</td>
            </tr>
          </tbody>
        </table>

        <div style="font-size: 13.5px; color: #1e293b; line-height: 1.45; background: #e0f2fe; padding: 8px 12px; border-radius: 7px; border-left: 3.5px solid #0288d1;">
          <strong>Why Recall Matters:</strong> On imbalanced disaster data, naive models achieve 75% accuracy by predicting "safe". XGBoost caught <strong>93 of 114</strong> real landslide events on unseen test data (held-out set of 456 points).
        </div>

        <!-- Confusion Matrix Summary Card -->
        <div style="background: #f8fafc; border: 1.5px solid #cbd5e1; border-radius: 7px; padding: 7px 11px; font-size: 13px; color: #1e293b;">
          <strong>Held-Out Test Matrix (456 pts):</strong> TP: 93 | FN: 21 | TN: 308 | FP: 34 (Precision: 73.2%, Recall: 81.6%).
        </div>

        <div>
          <div style="font-size: 13px; font-weight: 800; color: #334155; margin-bottom: 4px; text-transform: uppercase;">Feasibility Dimensions Matrix</div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px; font-size: 13px;">
            <div style="background: #f0fdf4; padding: 6px 8px; border-radius: 6px; border-left: 3px solid #22c55e;"><strong>Technical:</strong> HIGH (&lt;30MB APK)</div>
            <div style="background: #f0fdf4; padding: 6px 8px; border-radius: 6px; border-left: 3px solid #22c55e;"><strong>Operational:</strong> HIGH (3-Tier)</div>
            <div style="background: #f0fdf4; padding: 6px 8px; border-radius: 6px; border-left: 3px solid #22c55e;"><strong>Economic:</strong> HIGH (Open Data)</div>
            <div style="background: #f0fdf4; padding: 6px 8px; border-radius: 6px; border-left: 3px solid #22c55e;"><strong>Social:</strong> HIGH (8 Langs)</div>
          </div>
        </div>
      </div>

      <!-- Col 2: Feature Importance Breakdown & Physics Insights -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-orange">
            <span class="pill-num">2</span> Feature Importance & Insights
          </div>
          <div style="font-size: 13px; font-weight: 800; color: #9a3412; text-transform: uppercase; margin-top: 4px;">
            📊 XGBoost Feature Importance (Top 15 Features)
          </div>
        </div>

        <!-- Embedded Feature Importance Image from User -->
        <div style="border-radius: 8px; overflow: hidden; border: 1.5px solid #cbd5e1; background: #000;">
          <img src="{feat_importance_b64}" style="width: 100%; height: auto; max-height: 250px; object-fit: cover; display: block;" />
        </div>

        <!-- Deep EDA & Causal Narrative -->
        <div style="background: #fff7ed; border: 1.5px solid #fed7aa; border-radius: 8px; padding: 10px 13px; font-size: 13.5px; color: #7c2d12; line-height: 1.5;">
          <strong style="font-size: 14px;">Key Empirical Insights from EDA:</strong><br>
          • <strong>Built-up Land (#1 Factor):</strong> ~20x higher incidence in landslides (21.9% vs 1.1%). Reflects road-cutting slope destabilization + reporting bias near settlements.<br>
          • <strong>Multi-Day Rain (3d/7d):</strong> Far outweighs 1-day rainfall; prolonged saturation triggers catastrophic pore-pressure failure.<br>
          • <strong>Elevation & Slope:</strong> Steep gradients in young Himalayan terrain govern shear stress.<br>
          • <strong>Lithology:</strong> Metamorphic and mixed sedimentary rocks overrepresented 2x–3x.
        </div>

        <!-- Guwahati Rainfall Formula Box (placed in Col 2 to fill gap perfectly) -->
        <div style="background: #f0fdf4; border: 1.5px solid #86efac; border-radius: 8px; padding: 9px 12px;">
          <div style="font-size: 13.5px; font-weight: 800; color: #166534; text-transform: uppercase; margin-bottom: 3px;">
            🌧️ Empirical Rainfall Threshold Physics
          </div>
          <div style="font-size: 13px; color: #14532d; line-height: 1.45;">
            Guwahati critical intensity curve: <strong><em>I = 5.9 &times; D<sup>-0.479</sup></em></strong> (shallow debris slides). Used as an empirical reference baseline alongside XGBoost's non-linear 3d/7d accumulation modeling across all 8 states.
          </div>
        </div>

        <!-- Rigorous Model Validation Box -->
        <div style="background: #f8fafc; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 8px 12px;">
          <strong style="font-size: 13.5px; color: #1e293b;">Rigorous Model Validation:</strong>
          <div style="font-size: 13px; color: #334155; line-height: 1.45; margin-top: 2px;">
            Evaluated using 5-fold cross-validation on precision, recall, and F1-score rather than deceptive raw accuracy. Stratified splits ensure zero test leakage.
          </div>
        </div>
      </div>

      <!-- Col 3: Strategies for Overcoming Challenges & Risk Table -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-green">
            <span class="pill-num">3</span> Strategies for Mitigation
          </div>
          <div style="font-size: 13px; font-weight: 800; color: #166534; text-transform: uppercase; margin-top: 4px;">
            🛡️ Engineering Solutions for Field Challenges
          </div>
        </div>

        <table class="sih-table">
          <thead>
            <tr>
              <th style="width: 20px;">No.</th>
              <th>Challenge</th>
              <th>Engineered Strategy / Solution</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>1</strong></td>
              <td>Data Blackouts</td>
              <td><strong>Zero-Data GSM SMS SOS:</strong> Encodes GPS coordinates directly via 2G cellular signalling.</td>
            </tr>
            <tr>
              <td><strong>2</strong></td>
              <td>Reporting Bias</td>
              <td><strong>Pseudo-Absence Sampling:</strong> 1:3 ratio with KDTree 5km exclusion & slope &ge;5&deg; controls bias.</td>
            </tr>
            <tr>
              <td><strong>3</strong></td>
              <td>Battery Drain</td>
              <td><strong>Power-Aware WorkManager:</strong> Adaptive sensor polling throttles when stationary.</td>
            </tr>
            <tr>
              <td><strong>4</strong></td>
              <td>False Alarms</td>
              <td><strong>Tiered Probability Scoring:</strong> Shows 0–100% risk categories (Low/Mod/High) avoiding blunt yes/no.</td>
            </tr>
            <tr>
              <td><strong>5</strong></td>
              <td>Language Barrier</td>
              <td><strong>8 Native NER Languages:</strong> Complete localization in Bodo, Khasi, Mizo, Nepali, etc.</td>
            </tr>
          </tbody>
        </table>

        <!-- Potential Challenge & Impact Matrix -->
        <table class="sih-table">
          <thead>
            <tr>
              <th style="width: 20px;">No.</th>
              <th>Potential Challenge / Risk</th>
              <th style="text-align: right;">Impact</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>1</strong></td>
              <td><strong>Himalayan Cellular Blackouts:</strong> Internet dead-zones.</td>
              <td style="text-align: right;"><span class="badge-high">High</span></td>
            </tr>
            <tr>
              <td><strong>2</strong></td>
              <td><strong>Historical Reporting Bias:</strong> Rural slides unrecorded.</td>
              <td style="text-align: right;"><span class="badge-high">High</span></td>
            </tr>
            <tr>
              <td><strong>3</strong></td>
              <td><strong>Alpine Battery Depletion:</strong> Cold weather battery drop.</td>
              <td style="text-align: right;"><span class="badge-med">Medium</span></td>
            </tr>
            <tr>
              <td><strong>4</strong></td>
              <td><strong>Evacuation Fatigue:</strong> False alarm complacency.</td>
              <td style="text-align: right;"><span class="badge-med">Medium</span></td>
            </tr>
          </tbody>
        </table>

        <!-- Accountability Framework -->
        <div style="background: #fdf4ff; border: 1.5px solid #f0abfc; border-radius: 8px; padding: 9px 12px; font-size: 13px; color: #701a75; line-height: 1.45;">
          <strong>Human-in-the-Loop Accountability:</strong> Bhoochetak operates as a decision-support system intended to complement official SDMA protocols, not issue unsupervised unilateral alerts.
        </div>
      </div>
    </div>

    <!-- Bottom Expected Outcome Ribbon -->
    <div class="process-ribbon">
      <div class="chevron-step" style="background: #e0f2fe; border-left-color: #0288d1;">81.8% ML Recall</div>
      <div style="color: #94a3b8; font-weight: 800;">➔</div>
      <div class="chevron-step" style="background: #f3e8ff; border-left-color: #7e22ce;">Zero-Data Alert Broadcast</div>
      <div style="color: #94a3b8; font-weight: 800;">➔</div>
      <div class="chevron-step" style="background: #dcfce7; border-left-color: #15803d;">Pre-Emptive Evacuation</div>
      <div style="color: #94a3b8; font-weight: 800;">➔</div>
      <div class="chevron-step" style="background: #ffedd5; border-left-color: #ea580c;">Rapid SDRF First Response</div>
      <div style="color: #94a3b8; font-weight: 800;">➔</div>
      <div class="chevron-step" style="background: #dcfce7; border-left-color: #166534; font-weight: 900; color: #14532d;">ZERO PREVENTABLE CASUALTIES</div>
    </div>
  </div>

  <div class="slide-footer">
    <span>@SIH 2026</span>
    <span>4</span>
  </div>
</div>

<!-- ========================================================================= -->
<!-- SLIDE 5: IMPACT AND BENEFITS (8 NER STATES FOCUS - ZERO GAPS) -->
<!-- ========================================================================= -->
<div class="slide" id="slide-5">
  <div class="slide-header">
    {make_top_left_badge()}
    <div class="slide-title">IMPACT AND BENEFITS</div>
    {sih_top_right_logo_svg}
  </div>

  <div class="slide-body">
    <div class="grid-2" style="height: 865px;">
      <!-- Col 1: Target Audience & 8 NER States Coverage -->
      <div class="col-card">
        <div class="section-pill pill-blue">
          <span class="pill-num">1</span> Impact Across all 8 NER States
        </div>
        
        <!-- 8 States Grid Badge -->
        <div style="background: #eff6ff; border: 1.5px solid #93c5fd; border-radius: 8px; padding: 8px 12px;">
          <div style="font-size: 14px; font-weight: 800; color: #1e40af; text-transform: uppercase; margin-bottom: 5px;">
            🗺️ Target Geography: 8 North Eastern Region States
          </div>
          <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; font-size: 13.5px; font-weight: 800; color: #1e3a8a; text-align: center;">
            <div style="background: #fff; padding: 5px; border-radius: 6px; border: 1px solid #bfdbfe;">Assam</div>
            <div style="background: #fff; padding: 5px; border-radius: 6px; border: 1px solid #bfdbfe;">Meghalaya</div>
            <div style="background: #fff; padding: 5px; border-radius: 6px; border: 1px solid #bfdbfe;">Sikkim</div>
            <div style="background: #fff; padding: 5px; border-radius: 6px; border: 1px solid #bfdbfe;">Arunachal</div>
            <div style="background: #fff; padding: 5px; border-radius: 6px; border: 1px solid #bfdbfe;">Nagaland</div>
            <div style="background: #fff; padding: 5px; border-radius: 6px; border: 1px solid #bfdbfe;">Manipur</div>
            <div style="background: #fff; padding: 5px; border-radius: 6px; border: 1px solid #bfdbfe;">Mizoram</div>
            <div style="background: #fff; padding: 5px; border-radius: 6px; border: 1px solid #bfdbfe;">Tripura</div>
          </div>
        </div>

        <!-- EDA State Findings -->
        <div style="background: #f8fafc; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 7px 11px; font-size: 13px; color: #1e293b; line-height: 1.45;">
          <strong>EDA Historical Frequency:</strong> Assam and Manipur record highest landslide counts in our data, followed by Nagaland, Arunachal Pradesh, Sikkim, Mizoram, Meghalaya, and Tripura. Over 90% of recorded triggers are rainfall-related (June–September monsoon peaks).
        </div>

        <!-- 5 Stakeholder Impact Cards -->
        <div style="display: flex; flex-direction: column; gap: 7px;">
          <div style="display: flex; gap: 10px; background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 8px; padding: 7px 11px;">
            <div style="font-size: 18px; width: 34px; height: 34px; border-radius: 7px; background: #eff6ff; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🏔️</div>
            <div>
              <strong style="font-size: 14px; color: #1e3a8a;">Hill Citizens & Isolated Hamlets</strong>
              <div style="font-size: 13px; color: #334155; line-height: 1.4;">1–3 days advance warning window, voice sirens in 8 native dialects, offline vector escape paths, and 1-tap 2G SMS SOS beacon to district control.</div>
            </div>
          </div>

          <div style="display: flex; gap: 10px; background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 8px; padding: 7px 11px;">
            <div style="font-size: 18px; width: 34px; height: 34px; border-radius: 7px; background: #fdf2f8; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🏛️</div>
            <div>
              <strong style="font-size: 14px; color: #831843;">MDoNER & State Disaster Authorities (SDMA / SEOC)</strong>
              <div style="font-size: 13px; color: #334155; line-height: 1.4;">Centralized tactical GIS risk heatmaps, real-time rainfall saturation rates, and field-verified ground crack reports for proactive resource deployment.</div>
            </div>
          </div>

          <div style="display: flex; gap: 10px; background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 8px; padding: 7px 11px;">
            <div style="font-size: 18px; width: 34px; height: 34px; border-radius: 7px; background: #f0fdf4; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🚨</div>
            <div>
              <strong style="font-size: 14px; color: #14532d;">NDRF / SDRF Emergency First Responders</strong>
              <div style="font-size: 13px; color: #334155; line-height: 1.4;">Pinpoint GPS distress coordinates transmitted via offline SMS packets, cutting search-and-rescue response time by over 60% during the critical Golden Hour.</div>
            </div>
          </div>

          <div style="display: flex; gap: 10px; background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 8px; padding: 7px 11px;">
            <div style="font-size: 18px; width: 34px; height: 34px; border-radius: 7px; background: #fff7ed; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🚛</div>
            <div>
              <strong style="font-size: 14px; color: #9a3412;">Border Roads Organisation (BRO) & National Highways</strong>
              <div style="font-size: 13px; color: #334155; line-height: 1.4;">Pre-emptive traffic diversions on critical Himalayan arteries (NH-10, NH-29) enabling heavy machinery staging before mudslides wash out roadbeds.</div>
            </div>
          </div>

          <div style="display: flex; gap: 10px; background: #ffffff; border: 1.5px solid #e2e8f0; border-radius: 8px; padding: 7px 11px;">
            <div style="font-size: 18px; width: 34px; height: 34px; border-radius: 7px; background: #faf5ff; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🌲</div>
            <div>
              <strong style="font-size: 14px; color: #6b21a8;">Vulnerable Slope Catchments & Forest Reserves</strong>
              <div style="font-size: 13px; color: #334155; line-height: 1.4;">Prioritizes bio-engineering, retaining wall reinforcements, and reforestation along 45,000+ hectares of high-risk mountain slopes.</div>
            </div>
          </div>
        </div>

        <!-- Phased Deployment Roadmap Box -->
        <div style="background: #eff6ff; border: 1.5px solid #bfdbfe; border-radius: 8px; padding: 8px 12px;">
          <div style="font-size: 14px; font-weight: 800; color: #1e40af; margin-bottom: 3px;">🚀 Phased Pan-NER Deployment Roadmap:</div>
          <div style="font-size: 13px; color: #1e3a8a; line-height: 1.45;">
            • <strong>Phase 1 (Months 1–3):</strong> Pilot corridor along NH-10 (Sevoke–Gangtok) & Guwahati urban slopes.<br>
            • <strong>Phase 2 (Months 4–6):</strong> Rollout to high-density hill districts in Manipur, Nagaland, and Meghalaya.<br>
            • <strong>Phase 3 (Months 7–12):</strong> Full integration with all 8 State Disaster Management Authorities (SDMAs).
          </div>
        </div>

        <!-- Critical Lifelines Box -->
        <div style="background: #fdf4ff; border: 1.5px solid #f0abfc; border-radius: 7px; padding: 7px 11px; font-size: 13.5px; color: #701a75;">
          <strong>Strategic Himalayan Arteries Protected:</strong> NH-10 (Sevoke–Gangtok lifeline), NH-29 (Dimapur–Kohima), and critical border logistics corridors.
        </div>
      </div>

      <!-- Col 2: Benefits of the Solution (Packed 4 Quadrants + SOP Protocols) -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-green">
            <span class="pill-num">2</span> Multi-Dimensional Benefits
          </div>
        </div>

        <!-- 4 Quadrants with Metric Badges -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
          <div style="background: #eff6ff; border: 1.5px solid #bfdbfe; border-radius: 8px; padding: 9px 11px;">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px;">
              <strong style="font-size: 14px; color: #1e40af;">👥 Social Benefits</strong>
              <span style="background: #dbeafe; color: #1e40af; font-size: 11px; font-weight: 900; padding: 2px 7px; border-radius: 4px;">ZERO CASUALTIES</span>
            </div>
            <ul style="font-size: 13px; color: #1e3a8a; line-height: 1.45; padding-left: 12px;">
              <li>Zero preventable fatalities via advance warnings.</li>
              <li>Universal accessibility across 8 native languages.</li>
              <li>Protects remote tribal hamlets with offline alerts.</li>
              <li>Prevents prolonged community displacement.</li>
              <li>Empowers citizens with autonomous self-evacuation.</li>
            </ul>
          </div>

          <div style="background: #f0fdf4; border: 1.5px solid #bbf7d0; border-radius: 8px; padding: 9px 11px;">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px;">
              <strong style="font-size: 14px; color: #166534;">💰 Economic Benefits</strong>
              <span style="background: #dcfce7; color: #166534; font-size: 11px; font-weight: 900; padding: 2px 7px; border-radius: 4px;">₹1,200+ CR SAVED</span>
            </div>
            <ul style="font-size: 13px; color: #14532d; line-height: 1.45; padding-left: 12px;">
              <li>Saves ₹1000s of Crores in emergency road repairs.</li>
              <li>Pre-stages earthmovers before road washouts occur.</li>
              <li>Minimizes expensive helicopter emergency airlifts.</li>
              <li>Avoids catastrophic highway logistics paralysis.</li>
              <li>Cuts post-disaster reconstruction expenses by >70%.</li>
            </ul>
          </div>

          <div style="background: #faf5ff; border: 1.5px solid #e9d5ff; border-radius: 8px; padding: 9px 11px;">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px;">
              <strong style="font-size: 14px; color: #6b21a8;">⚡ Technological Benefits</strong>
              <span style="background: #f3e8ff; color: #6b21a8; font-size: 11px; font-weight: 900; padding: 2px 7px; border-radius: 4px;">&lt;50MS INFERENCE</span>
            </div>
            <ul style="font-size: 13px; color: #581c87; line-height: 1.45; padding-left: 12px;">
              <li>First unified 2,280-point validated NER dataset.</li>
              <li>100% offline-first vector GIS map rendering.</li>
              <li>Dual-tier fallback: push notification + GSM SMS.</li>
              <li>Sub-50ms inference time deployable on mobile edge.</li>
              <li>Crowd-sourced field ground-truth verification loop.</li>
            </ul>
          </div>

          <div style="background: #fefce8; border: 1.5px solid #fef08a; border-radius: 8px; padding: 9px 11px;">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px;">
              <strong style="font-size: 14px; color: #854d0e;">🌲 Environmental Benefits</strong>
              <span style="background: #fef9c3; color: #854d0e; font-size: 11px; font-weight: 900; padding: 2px 7px; border-radius: 4px;">45,000+ HA SAVED</span>
            </div>
            <ul style="font-size: 13px; color: #713f12; line-height: 1.45; padding-left: 12px;">
              <li>Identifies fragile slopes for bio-engineering.</li>
              <li>Mitigates deforestation-induced soil erosion.</li>
              <li>Guides climate-resilient mountain highway cutting.</li>
              <li>Reduces post-disaster debris runoff in rivers.</li>
              <li>Promotes ecological slope stabilization.</li>
            </ul>
          </div>
        </div>

        <!-- Institutional Early Warning Alert Level Protocols Table -->
        <table class="sih-table">
          <thead>
            <tr>
              <th>Tier</th>
              <th>Risk Probability</th>
              <th>Siren Protocol</th>
              <th>Standard Operating Procedure (SOP) Action</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><span style="background: #dcfce7; color: #166534; font-weight: 800; padding: 3px 8px; border-radius: 5px;">GREEN</span></td>
              <td>0% – 30% (Low)</td>
              <td>Silent telemetry</td>
              <td>Routine slope monitoring; background weather sync.</td>
            </tr>
            <tr>
              <td><span style="background: #fef9c3; color: #854d0e; font-weight: 800; padding: 3px 8px; border-radius: 5px;">YELLOW</span></td>
              <td>31% – 60% (Moderate)</td>
              <td>Push notification</td>
              <td>Advisories issued to BRO road crews & village headmen.</td>
            </tr>
            <tr>
              <td><span style="background: #fee2e2; color: #b91c1c; font-weight: 800; padding: 3px 8px; border-radius: 5px;">ORANGE</span></td>
              <td>61% – 80% (High)</td>
              <td>Audio voice siren</td>
              <td>Pre-stage earthmovers; restrict non-essential mountain transit.</td>
            </tr>
            <tr>
              <td><span style="background: #991b1b; color: #ffffff; font-weight: 900; padding: 3px 8px; border-radius: 5px;">RED</span></td>
              <td>81% – 100% (Critical)</td>
              <td>GSM SMS broadcast</td>
              <td>Mandatory evacuation to designated safe high-ground shelters.</td>
            </tr>
          </tbody>
        </table>

        <div class="callout-box">
          <div class="callout-title">🏛️ MDoNER & NATIONAL STRATEGY ALIGNMENT</div>
          <div class="callout-desc">
            Directly supports MDoNER's <strong>PM-DevINE</strong> (Prime Minister's Development Initiative for North East), the <strong>NDMA National Landslide Risk Management Strategy (2019)</strong>, and <strong>UN Sendai Framework Target G</strong>.
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom summary banner -->
    <div style="background: linear-gradient(90deg, #1e3a8a, #0288d1); border-radius: 8px; padding: 8px 24px; display: flex; align-items: center; justify-content: space-between; font-size: 14.5px; font-weight: 800; color: #ffffff; box-shadow: 0 2px 6px rgba(0,0,0,0.12);">
      <span>From Vulnerable Hill Slopes</span>
      <span>➔</span>
      <span>NASA & Meteorological Telemetry</span>
      <span>➔</span>
      <span>81.8% Recall ML Engine</span>
      <span>➔</span>
      <span>Resilient North Eastern Region</span>
    </div>
  </div>

  <div class="slide-footer">
    <span>@SIH 2026</span>
    <span>5</span>
  </div>
</div>

<!-- ========================================================================= -->
<!-- SLIDE 6: RESEARCH AND REFERENCES (Working App & Scientific Citations) -->
<!-- ========================================================================= -->
<div class="slide" id="slide-6">
  <div class="slide-header">
    {make_top_left_badge()}
    <div class="slide-title">RESEARCH AND REFERENCES</div>
    {sih_top_right_logo_svg}
  </div>

  <div class="slide-body">
    <div class="grid-2" style="height: 865px;">
      <!-- Col 1: Bhoochetak Prototype Showcase -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-blue">
            <span class="pill-num">1</span> Project Prototype (Bhoochetak App)
          </div>
          <div style="font-size: 14px; color: #475569; margin-top: 4px; font-weight: 700;">Field-tested native Android prototype with offline telemetry & live ML inference:</div>
        </div>

        <!-- 4 Screen Mockups Row -->
        <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px;">
          <div class="phone-mockup">
            <img src="{home_b64}" />
            <div class="phone-label">Early Warning Hub</div>
          </div>
          <div class="phone-mockup">
            <img src="{map_b64}" />
            <div class="phone-label">Tactical GIS Map</div>
          </div>
          <div class="phone-mockup">
            <img src="{doppler_b64}" />
            <div class="phone-label">Doppler Radar</div>
          </div>
          <div class="phone-mockup">
            <img src="{report_b64}" />
            <div class="phone-label">Incident Telemetry</div>
          </div>
        </div>

        <!-- Feature Details for Each Screen -->
        <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 7px; font-size: 13px; color: #1e293b; line-height: 1.4;">
          <div style="background: #f8fafc; border: 1.5px solid #e2e8f0; border-radius: 7px; padding: 7px 9px;">
            <strong>Hub:</strong> Real-time risk %, danger level, and 1-tap emergency SOS beacon.
          </div>
          <div style="background: #f8fafc; border: 1.5px solid #e2e8f0; border-radius: 7px; padding: 7px 9px;">
            <strong>GIS Map:</strong> Offline vector polygons, safe corridors, and highway markers.
          </div>
          <div style="background: #f8fafc; border: 1.5px solid #e2e8f0; border-radius: 7px; padding: 7px 9px;">
            <strong>Doppler:</strong> 72h precipitation radar and soil saturation telemetry.
          </div>
          <div style="background: #f8fafc; border: 1.5px solid #e2e8f0; border-radius: 7px; padding: 7px 9px;">
            <strong>Report:</strong> Geotagged ground crack photos with offline sync queue.
          </div>
        </div>

        <!-- System Capabilities Box -->
        <div style="background: #eff6ff; border: 1.5px solid #bfdbfe; border-radius: 8px; padding: 8px 12px; font-size: 13.5px; color: #1e40af;">
          <strong>Edge Architecture:</strong> 100% offline-ready Room DB; automatic background syncing via WorkManager; sub-50ms ML inference.
        </div>

        <!-- Field Verification & Hardware Specs -->
        <div style="background: #f8fafc; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 8px 12px; font-size: 13.5px; color: #1e293b;">
          <div style="display: flex; justify-content: space-between; margin-bottom: 3px;">
            <span><strong>Target OS:</strong> Android 8.0 to Android 15 (API 26–35)</span>
            <span><strong>Package:</strong> Signed APK (25.4 MB)</span>
          </div>
          <div>
            <strong>GitHub Repository:</strong> <a href="https://github.com/areeb239/Landslide" style="color: #0288d1; text-decoration: underline;">https://github.com/areeb239/Landslide</a>
          </div>
        </div>
      </div>

      <!-- Col 2: Academic Literature & Dataset Citations (Dense & Gap-Free) -->
      <div class="col-card">
        <div>
          <div class="section-pill pill-green">
            <span class="pill-num">2</span> Academic Literature & Citations
          </div>
          <div style="font-size: 14px; color: #475569; margin-top: 4px; font-weight: 700;">Grounded in peer-reviewed hazard research and government datasets:</div>
        </div>

        <table class="sih-table" style="font-size: 12.5px;">
          <thead>
            <tr>
              <th style="width: 20px;">No.</th>
              <th>Reference / Research Work</th>
              <th>Source / Authority</th>
              <th>Relevance to Bhoochetak</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>1</strong></td>
              <td><strong>Global Landslide Catalog (GLC)</strong></td>
              <td>Kirschbaum et al. 2010 (NASA)</td>
              <td>Primary source for 251 historical NER landslide events.</td>
            </tr>
            <tr>
              <td><strong>2</strong></td>
              <td><strong>High Mountain Asia Catalog v2</strong></td>
              <td>NASA / NSIDC (COOLR project)</td>
              <td>Supplied 498 NER events; merged & deduplicated to 570 points.</td>
            </tr>
            <tr>
              <td><strong>3</strong></td>
              <td><strong>Global Lithological Map (GLiM)</strong></td>
              <td>Hartmann & Moosdorf 2012</td>
              <td>Rock-type classification across 2,000 NER spatial polygons.</td>
            </tr>
            <tr>
              <td><strong>4</strong></td>
              <td><strong>ESA WorldCover 10m</strong></td>
              <td>European Space Agency (2021)</td>
              <td>High-res land cover proving built-up risk correlation.</td>
            </tr>
            <tr>
              <td><strong>5</strong></td>
              <td><strong>SRTM 30m DEM</strong></td>
              <td>NASA / NGA (OpenTopography)</td>
              <td>High-resolution elevation and local gradient slope computation.</td>
            </tr>
            <tr>
              <td><strong>6</strong></td>
              <td><strong>Open-Meteo Weather API</strong></td>
              <td>Historical Archive Weather API</td>
              <td>1-day, 3-day, and 7-day cumulative rainfall extraction.</td>
            </tr>
            <tr>
              <td><strong>7</strong></td>
              <td><strong>National Landslide Strategy</strong></td>
              <td>NDMA, Govt. of India (2019)</td>
              <td>Framework for community early warning and SOP guidelines.</td>
            </tr>
            <tr>
              <td><strong>8</strong></td>
              <td><strong>Guwahati Rainfall Threshold</strong></td>
              <td>Amer. Jrnl of Engg & Tech</td>
              <td>Empirical curve: <em>I = 5.9 &times; D<sup>-0.479</sup></em> for debris flows.</td>
            </tr>
          </tbody>
        </table>

        <!-- FastAPI Backend & ML Pipeline Handoff Architecture Box -->
        <div style="background: #eff6ff; border: 1.5px solid #bfdbfe; border-radius: 8px; padding: 7px 11px; font-size: 13px; color: #1e40af; line-height: 1.45;">
          <strong style="font-size: 13.5px;">FastAPI Backend & ML Pipeline Handoff Architecture:</strong><br>
          • <strong>Trained Pipeline:</strong> <code>bhoochetak_pipeline.pkl</code> bundled with preprocessors, loaded via <code>joblib.load()</code>.<br>
          • <strong>Automated Endpoint:</strong> <code>POST /predict</code> accepts <code>{{latitude, longitude, date}}</code>, calls <code>get_features()</code>, &lt;50ms.<br>
          • <strong>Zero-Cloud Local Resilience:</strong> DEM, Land Cover, & Lithology evaluated 100% offline from local rasters/gdb.
        </div>

        <!-- Ethical Framework & Deployment Governance Box -->
        <div style="background: #f0fdf4; border: 1.5px solid #bbf7d0; border-radius: 8px; padding: 7px 11px; font-size: 13px; color: #166534; line-height: 1.45;">
          <strong style="font-size: 13.5px;">Ethical AI, Accountability & Bias Mitigation:</strong><br>
          • <strong>Decision-Support Tool:</strong> Complements official NDMA/SDMA disaster protocols rather than issuing unsupervised unilateral alerts.<br>
          • <strong>Continuous Probability:</strong> Tiered risk outputs (0–100%) prevent false confidence; paired with verified evacuation SOPs to avoid panic.
        </div>

        <!-- Continuous Learning & Scalability Pipeline Box -->
        <div style="background: #fdf4ff; border: 1.5px solid #f0abfc; border-radius: 8px; padding: 7px 11px; font-size: 13px; color: #701a75; line-height: 1.45;">
          <strong style="font-size: 13.5px;">Scalability & Retraining Pipeline:</strong><br>
          • <strong>Dynamic Model Retraining:</strong> Ingests new verified landslides and citizen-reported ground cracks from the Bhoochetak app.<br>
          • <strong>Pan-India Generalization:</strong> Pipeline accepts arbitrary lat/long bounding boxes, enabling rapid rollout to Western Ghats & NW Himalayas.
        </div>

        <!-- Research to Solution Connection Ribbon -->
        <div style="background: #faf5ff; border: 1.5px solid #e9d5ff; border-radius: 8px; padding: 6px 12px;">
          <div style="display: flex; align-items: center; justify-content: space-between; font-size: 13px; font-weight: 800; color: #4a044e;">
            <span>NASA GLC & COOLR (570)</span>
            <span>➔</span>
            <span>5-Feature Extraction</span>
            <span>➔</span>
            <span>XGBoost (81.8% Recall)</span>
            <span>➔</span>
            <span>Bhoochetak App</span>
            <span>➔</span>
            <span>Zero Fatalities</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom summary banner -->
    <div style="background: #ffffff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 8px 24px; display: flex; align-items: center; justify-content: space-between; font-size: 14.5px; font-weight: 800; color: #1e293b; box-shadow: 0 1px 4px rgba(0,0,0,0.05);">
      <span>📚 Grounded in NASA & ESA Earth Data</span>
      <span>🔬 Peer-Reviewed Causal Geology</span>
      <span>⚡ Field-Tested Native Android APK</span>
      <span>🇮🇳 Aligned with MDoNER & NDMA 2026</span>
    </div>
  </div>

  <div class="slide-footer">
    <span>@SIH 2026</span>
    <span>6</span>
  </div>
</div>

</body>
</html>
'''

html_path = OUT_DIR / "index.html"
with open(html_path, "w", encoding="utf-8") as f:
    f.write(html_content)
print(f"Generated Updated HTML Presentation at: {html_path}")

pdf_path = OUT_DIR / "Bhoochetak_SIH_2026.pdf"
edge_cmd = [
    r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
    "--headless",
    "--disable-gpu",
    "--no-pdf-header-footer",
    f"--print-to-pdf={pdf_path}",
    str(html_path.resolve())
]

print("Rendering PDF with Edge...")
res = subprocess.run(edge_cmd, capture_output=True, text=True)
print("Edge Status:", res.returncode)

doc = pymupdf.open(pdf_path)
print("Total Pages in PDF:", len(doc))

png_paths = []
for i in range(len(doc)):
    page = doc[i]
    pix = page.get_pixmap(dpi=200)
    out_png = OUT_DIR / f"slide_{i+1}.png"
    pix.save(out_png)
    png_paths.append(out_png)
    print(f"Rendered {out_png.relative_to(WORKSPACE)} ({pix.width}x{pix.height})")

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
blank_slide_layout = prs.slide_layouts[6]

for png in png_paths:
    slide = prs.slides.add_slide(blank_slide_layout)
    slide.shapes.add_picture(str(png), Inches(0), Inches(0), width=prs.slide_width, height=prs.slide_height)

pptx_path = OUT_DIR / "Bhoochetak_SIH_2026.pptx"
prs.save(pptx_path)
print(f"Saved PPTX to: {pptx_path.relative_to(WORKSPACE)}")

shutil.copy(pdf_path, WORKSPACE / "Bhoochetak_SIH_2026.pdf")
shutil.copy(pptx_path, WORKSPACE / "Bhoochetak_SIH_2026.pptx")
print("All updated artifacts generated and copied to root successfully!")
