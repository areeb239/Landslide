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
TEMPLATE_DIR = WORKSPACE / "template_assets"

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

# Official extracted assets from SIH template
sih_logo_b64 = img_to_b64(TEMPLATE_DIR / "sih_logo_2026.png")
sih_bulb_b64 = img_to_b64(TEMPLATE_DIR / "sih_bulb_brain.png")

def make_top_left_oval():
    return f'''
    <div style="width: 225px; height: 72px; border: 2.5px solid #5a3e85; border-radius: 50%; display: flex; align-items: center; justify-content: center; gap: 10px; background: #ffffff; box-shadow: 0 2px 6px rgba(90,62,133,0.15);">
      <img src="{logo_b64}" style="width: 44px; height: 44px; border-radius: 10px; object-fit: cover;" />
      <div style="text-align: center; line-height: 1.15;">
        <div style="font-size: 19px; font-weight: 900; color: #d32f2f; letter-spacing: 0.8px;">TERRATECH</div>
        <div style="font-size: 13.5px; font-weight: 800; color: #1e293b; letter-spacing: 0.5px;">Bhoochetak</div>
      </div>
    </div>
    '''

def make_header(title):
    return f'''
    <div class="slide-header">
      {make_top_left_oval()}
      <div class="slide-title">{title}</div>
      <img src="{sih_logo_b64}" style="height: 74px; width: auto; object-fit: contain;" />
    </div>
    '''

def make_footer(page_num):
    return f'''
    <div class="slide-footer">
      <span>@SIH 2026</span>
      <span class="page-num">{page_num}</span>
    </div>
    '''

def make_pill(num, text, color="#0072ce"):
    return f'''
    <div class="section-pill" style="background: {color};">
      <div class="pill-badge" style="color: {color};">{num}</div>
      <span class="pill-text">{text}</span>
    </div>
    '''

html_content = f'''<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Bhoochetak — Smart India Hackathon 2026 (SIH26001)</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap');

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
      height: 1080px;
    }}
    .slide {{
      box-shadow: none;
      margin: 0;
      page-break-after: always;
    }}
  }}

  /* Header Area matching Alpha Company & SIH template */
  .slide-header {{
    height: 96px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 45px 10px 35px;
    border-bottom: 2.5px solid #1a62a0;
    background: #ffffff;
    position: relative;
    flex-shrink: 0;
  }}

  .slide-title {{
    font-family: 'Times New Roman', serif;
    font-size: 38px;
    font-weight: bold;
    color: #000000;
    letter-spacing: 0.8px;
    text-transform: uppercase;
    text-align: center;
  }}

  /* Bottom Footer Bar */
  .slide-footer {{
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 38px;
    background: #1865a4;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #ffffff;
    font-size: 16px;
    font-weight: 700;
    z-index: 100;
    flex-shrink: 0;
  }}

  .page-num {{
    position: absolute;
    right: 45px;
    font-size: 19px;
    font-weight: 800;
  }}

  /* Main Slide Content */
  .slide-body {{
    padding: 16px 35px 48px 35px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    box-sizing: border-box;
  }}

  /* Numbered Section Pills */
  .section-pill {{
    display: inline-flex;
    align-items: center;
    border-radius: 30px;
    padding: 6px 20px 6px 8px;
    color: #ffffff;
    box-shadow: 0 2px 6px rgba(0,0,0,0.12);
    margin-bottom: 10px;
  }}

  .pill-badge {{
    background: #ffffff;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    font-weight: 900;
    font-size: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
  }}

  .pill-text {{
    font-size: 16.5px;
    font-weight: 800;
    letter-spacing: 0.5px;
    text-transform: uppercase;
  }}

  /* Typography tuned for mobile readability */
  .subtitle-text {{
    font-size: 14.5px;
    font-weight: 600;
    color: #334155;
    line-height: 1.35;
    margin-bottom: 10px;
  }}

  .card-box {{
    background: #f8fafc;
    border: 1.5px solid #e2e8f0;
    border-radius: 12px;
    padding: 10px 14px;
    display: flex;
    align-items: center;
    gap: 12px;
  }}

  .chevron-ribbon {{
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #f1f5f9;
    border: 1.5px solid #cbd5e1;
    border-radius: 10px;
    padding: 10px 20px;
    margin-top: 10px;
  }}

  .ribbon-item {{
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    font-weight: 800;
    color: #1e293b;
    text-transform: uppercase;
  }}

  .ribbon-arrow {{
    color: #0284c7;
    font-size: 18px;
    font-weight: 900;
  }}

  /* Table styling */
  table.data-table {{
    width: 100%;
    border-collapse: collapse;
    font-size: 13.5px;
  }}

  table.data-table th {{
    background: #e2e8f0;
    color: #0f172a;
    font-weight: 800;
    padding: 8px 10px;
    text-align: left;
    border-bottom: 2px solid #94a3b8;
  }}

  table.data-table td {{
    padding: 8px 10px;
    border-bottom: 1px solid #e2e8f0;
    color: #1e293b;
    vertical-align: middle;
  }}

  table.data-table tr:nth-child(even) {{
    background: #f8fafc;
  }}
</style>
</head>
<body>

<!-- =========================================================================
     SLIDE 1: TITLE PAGE (Exact Match to Official SIH Template & Alpha Company)
     ========================================================================= -->
<div class="slide" style="justify-content: space-between; padding: 25px 60px 40px 60px;">
  <!-- Top Header Title -->
  <div style="display: flex; align-items: center; justify-content: space-between; position: relative;">
    <div style="flex: 1; text-align: center; font-family: 'Garamond', 'Georgia', serif; font-size: 46px; font-weight: bold; color: #1f497d; letter-spacing: 1px;">
      SMART INDIA HACKATHON 2026
    </div>
    <img src="{sih_logo_b64}" style="height: 80px; width: auto; object-fit: contain; position: absolute; right: 0; top: -5px;" />
  </div>

  <!-- Center Area: Bullets on Left, SIH Bulb Graphic on Right -->
  <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 40px; margin-bottom: 30px; padding: 0 40px;">
    
    <!-- Left Pointers (Exact Official SIH Template text with red values) -->
    <div style="display: flex; flex-direction: column; gap: 36px; max-width: 960px;">
      <div style="font-size: 28px; font-family: Arial, sans-serif; color: #000000; line-height: 1.45;">
        <span style="font-weight: bold;">• Problem Statement ID – </span>
        <span style="color: #d32f2f; font-weight: bold; letter-spacing: 0.5px;">SIH26001</span>
      </div>
      <div style="font-size: 28px; font-family: Arial, sans-serif; color: #000000; line-height: 1.45;">
        <span style="font-weight: bold;">• Problem Statement Title- </span>
        <span style="color: #d32f2f; font-weight: bold; line-height: 1.4;">AI-Based early warning and landslide Risk Monitoring System in NER</span>
      </div>
      <div style="font-size: 28px; font-family: Arial, sans-serif; color: #000000; line-height: 1.45;">
        <span style="font-weight: bold;">• Theme- </span>
        <span style="color: #d32f2f; font-weight: bold;">Disaster Management</span>
      </div>
      <div style="font-size: 28px; font-family: Arial, sans-serif; color: #000000; line-height: 1.45;">
        <span style="font-weight: bold;">• PS Category- </span>
        <span style="color: #d32f2f; font-weight: bold;">Software</span>
      </div>
      <div style="font-size: 28px; font-family: Arial, sans-serif; color: #000000; line-height: 1.45;">
        <span style="font-weight: bold;">• Team Name – </span>
        <span style="color: #d32f2f; font-weight: bold;">TerraTech</span>
      </div>
      <div style="font-size: 28px; font-family: Arial, sans-serif; color: #000000; line-height: 1.45;">
        <span style="font-weight: bold;">• Team Leader – </span>
        <span style="color: #d32f2f; font-weight: bold;">Dhruv Soni</span>
      </div>
    </div>

    <!-- Right: Official SIH Brain-Bulb Graphic -->
    <div style="display: flex; align-items: center; justify-content: center; padding-right: 20px;">
      <img src="{sih_bulb_b64}" style="width: 410px; height: auto; object-fit: contain; filter: drop-shadow(0 6px 16px rgba(0,0,0,0.12));" />
    </div>
  </div>

  <!-- Bottom Spacing -->
  <div style="height: 20px;"></div>
</div>


<!-- =========================================================================
     SLIDE 2: IDEA TITLE (Exact Match to Alpha Page 2)
     ========================================================================= -->
<div class="slide">
  {make_header("IDEA TITLE")}
  <div class="slide-body">
    
    <!-- 3 Columns Layout matching Alpha Company -->
    <div style="display: grid; grid-template-columns: 1fr 1.15fr 1fr; gap: 24px; flex: 1;">
      
      <!-- Column 1: Proposed Solution -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("1", "PROPOSED SOLUTION", "#0072ce")}
          <div class="subtitle-text">
            A secure, AI-powered offline-resilient early warning ecosystem connecting <b>Hill Citizens</b>, <b>District SEOCs</b>, and <b>First Responders</b>.
          </div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 11px; flex: 1; justify-content: space-between;">
          <div class="card-box" style="border-left: 4.5px solid #0284c7;">
            <div style="font-size: 22px; width: 36px; height: 36px; border-radius: 50%; background: #e0f2fe; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🤖</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #0369a1;">Early Warning AI Engine</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.3;">XGBoost ML predicting slope failure probability with <b>81.8% recall</b>.</div>
            </div>
          </div>

          <div class="card-box" style="border-left: 4.5px solid #7c3aed;">
            <div style="font-size: 22px; width: 36px; height: 36px; border-radius: 50%; background: #ede9fe; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🛰️</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #6d28d9;">Multi-Modal Satellite Telemetry</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.3;">Ingests GPM rainfall, SRTM DEM slope, ESA land cover & lithology.</div>
            </div>
          </div>

          <div class="card-box" style="border-left: 4.5px solid #16a34a;">
            <div style="font-size: 22px; width: 36px; height: 36px; border-radius: 50%; background: #dcfce7; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🗺️</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #15803d;">Offline Tactical GIS Navigation</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.3;">100% offline vector maps with pre-computed safe corridors.</div>
            </div>
          </div>

          <div class="card-box" style="border-left: 4.5px solid #ea580c;">
            <div style="font-size: 22px; width: 36px; height: 36px; border-radius: 50%; background: #ffedd5; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">📢</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #c2410c;">Native Dialects & Voice Sirens</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.3;">Audio voice sirens in <b>8 native languages</b> (Assamese, Bodo, Khasi).</div>
            </div>
          </div>

          <div class="card-box" style="border-left: 4.5px solid #0d9488;">
            <div style="font-size: 22px; width: 36px; height: 36px; border-radius: 50%; background: #ccfbf1; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">📷</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #0f766e;">Field Ground-Truth Reporting</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.3;">Citizen geotagged photos & crack reporting with offline sync queue.</div>
            </div>
          </div>

          <div class="card-box" style="border-left: 4.5px solid #e11d48;">
            <div style="font-size: 22px; width: 36px; height: 36px; border-radius: 50%; background: #ffe4e6; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🏛️</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #be123c;">SEOC & Disaster Command Portal</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.3;">Tactical GIS heatmaps & evacuation SOPs for district authorities.</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Column 2: How it addresses the problem -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("2", "HOW IT ADDRESSES THE PROBLEM", "#582c8b")}
          
          <!-- Top Problem vs Solution Comparison Box -->
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 12px;">
            <div style="flex: 1; background: #fee2e2; border: 1.5px solid #f87171; border-radius: 8px; padding: 8px 10px; text-align: center;">
              <div style="font-size: 13px; font-weight: 900; color: #991b1b; text-transform: uppercase;">NER Terrain Challenge</div>
              <div style="font-size: 12.5px; color: #7f1d1d; font-weight: 600;">Fragile Young Geology & 0% Connectivity</div>
            </div>
            <div style="font-size: 22px; font-weight: 900; color: #475569;">↔</div>
            <div style="flex: 1; background: #dcfce7; border: 1.5px solid #4ade80; border-radius: 8px; padding: 8px 10px; text-align: center;">
              <div style="font-size: 13px; font-weight: 900; color: #166534; text-transform: uppercase;">Bhoochetak Response</div>
              <div style="font-size: 12.5px; color: #14532d; font-weight: 600;">Satellite Telemetry & Offline Voice Sirens</div>
            </div>
          </div>
        </div>

        <!-- Center Ecosystem Diagram (Alpha Company Style) -->
        <div style="background: #ffffff; border: 1.5px solid #cbd5e1; border-radius: 12px; padding: 14px 10px; display: flex; flex-direction: column; align-items: center; justify-content: center; flex: 1; margin-bottom: 12px; position: relative;">
          
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; width: 100%; margin-bottom: 14px;">
            <div style="background: #eff6ff; border: 1.5px solid #93c5fd; border-radius: 10px; padding: 8px 10px;">
              <div style="font-size: 14px; font-weight: 800; color: #1e40af;">🧑‍🌾 Hill Citizens & Hamlets</div>
              <div style="font-size: 12.5px; color: #3b82f6; font-weight: 600;">1–3d sirens & safe evacuation escape routes</div>
            </div>
            <div style="background: #fdf2f8; border: 1.5px solid #f472b6; border-radius: 10px; padding: 8px 10px;">
              <div style="font-size: 14px; font-weight: 800; color: #9d174d;">🏛️ SDMA & State SEOCs</div>
              <div style="font-size: 12.5px; color: #db2777; font-weight: 600;">Centralized GIS risk heatmaps & deployments</div>
            </div>
          </div>

          <!-- Central Core Circle -->
          <div style="width: 140px; height: 140px; border-radius: 50%; background: linear-gradient(135deg, #1e3a8a, #0284c7); border: 4px solid #ffffff; box-shadow: 0 4px 14px rgba(2,132,199,0.3); display: flex; flex-direction: column; align-items: center; justify-content: center; color: #ffffff; text-align: center; margin: 4px 0;">
            <div style="font-size: 20px;">⚡ AI</div>
            <div style="font-size: 13px; font-weight: 900; letter-spacing: 0.5px;">BHOOCHETAK</div>
            <div style="font-size: 11px; font-weight: 700; color: #7dd3fc;">SMART INFERENCE</div>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; width: 100%; margin-top: 14px;">
            <div style="background: #f0fdf4; border: 1.5px solid #86efac; border-radius: 10px; padding: 8px 10px;">
              <div style="font-size: 14px; font-weight: 800; color: #166534;">🚨 NDRF / SDRF Responders</div>
              <div style="font-size: 12.5px; color: #16a34a; font-weight: 600;">Exact GPS coordinates via 2G SMS in Golden Hr</div>
            </div>
            <div style="background: #fff7ed; border: 1.5px solid #fdba74; border-radius: 10px; padding: 8px 10px;">
              <div style="font-size: 14px; font-weight: 800; color: #9a3412;">🚛 BRO & Highway Patrol</div>
              <div style="font-size: 12.5px; color: #ea580c; font-weight: 600;">Pre-emptive closures on NH-10 & NH-29</div>
            </div>
          </div>
        </div>

        <div style="background: #eff6ff; border: 1.5px solid #bfdbfe; border-radius: 10px; padding: 9px 14px; text-align: center;">
          <span style="font-size: 14px; font-weight: 800; color: #1e40af;">Bridges the mountain communication gap with zero-network 2G SMS resilience.</span>
        </div>
      </div>

      <!-- Column 3: Innovation & Uniqueness -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("3", "INNOVATION & UNIQUENESS", "#0f8a42")}
          <div class="subtitle-text">
            Novel disaster management engineering built specifically for Himalayan extremes:
          </div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 11px; flex: 1; justify-content: space-between;">
          <div class="card-box">
            <div style="font-size: 20px;">⚡</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #0f172a;">Physics-Guided ML Integration</div>
              <div style="font-size: 13.5px; color: #475569;">Fuses Caine empirical rainfall thresholds with ensemble gradient boosting.</div>
            </div>
          </div>

          <div class="card-box">
            <div style="font-size: 20px;">🗺️</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #0f172a;">Offline-First Vector GIS Engine</div>
              <div style="font-size: 13.5px; color: #475569;">Zero-cloud map navigation and evacuation corridors cached on-device.</div>
            </div>
          </div>

          <div class="card-box">
            <div style="font-size: 20px;">🗣️</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #0f172a;">Hyper-Local 8-Language Sirens</div>
              <div style="font-size: 13.5px; color: #475569;">Eliminates literacy barriers during midnight monsoon emergencies.</div>
            </div>
          </div>

          <div class="card-box">
            <div style="font-size: 20px;">📡</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #0f172a;">Dual-Tier Failover Alert Pipeline</div>
              <div style="font-size: 13.5px; color: #475569;">Android push notifications + automated 2G GSM SMS broadcast fallback.</div>
            </div>
          </div>

          <div class="card-box">
            <div style="font-size: 20px;">⏱️</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #0f172a;">Sub-50ms Edge Inference</div>
              <div style="font-size: 13.5px; color: #475569;">Quantized lightweight model running on budget Android smartphones.</div>
            </div>
          </div>

          <!-- Key Differentiator Box (Alpha Company Style) -->
          <div style="background: #fef9c3; border: 2px solid #ca8a04; border-radius: 12px; padding: 12px 14px;">
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
              <span style="font-size: 20px;">💡</span>
              <span style="font-size: 14.5px; font-weight: 900; color: #854d0e; text-transform: uppercase;">Key Differentiator</span>
            </div>
            <div style="font-size: 13.5px; font-weight: 700; color: #713f12; line-height: 1.35;">
              Not just a weather app → An end-to-end, offline-resilient <b>Disaster-to-Safety early warning ecosystem</b> saving lives in the most remote Himalayan corridors.
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- Bottom Chevron Process Ribbon (Alpha Company Style) -->
    <div class="chevron-ribbon">
      <div class="ribbon-item"><span>🛰️ Data Sensing</span></div>
      <div class="ribbon-arrow">➔</div>
      <div class="ribbon-item"><span>🤖 ML Risk Inference</span></div>
      <div class="ribbon-arrow">➔</div>
      <div class="ribbon-item"><span>📢 Voice Audio Siren</span></div>
      <div class="ribbon-arrow">➔</div>
      <div class="ribbon-item"><span>📡 2G SMS SOS Beacon</span></div>
      <div class="ribbon-arrow">➔</div>
      <div class="ribbon-item"><span>🏃 Escorted Evacuation</span></div>
      <div class="ribbon-arrow">➔</div>
      <div class="ribbon-item" style="color: #16a34a;"><span>🛡️ Zero Fatalities</span></div>
    </div>

  </div>
  {make_footer("2")}
</div>


<!-- =========================================================================
     SLIDE 3: TECHNICAL APPROACH (Exact Match to Alpha Page 3)
     ========================================================================= -->
<div class="slide">
  {make_header("TECHNICAL APPROACH")}
  <div class="slide-body">

    <!-- 3 Columns Layout matching Alpha Company -->
    <div style="display: grid; grid-template-columns: 1fr 1.05fr 1fr; gap: 24px; flex: 1;">
      
      <!-- Column 1: Technologies to be Used -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("1", "Technologies to be Used", "#0072ce")}
          <div class="subtitle-text">Built with widely adopted, scalable, and production-grade technologies:</div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 11px; flex: 1; justify-content: space-between;">
          <div style="background: #eff6ff; border: 1.5px solid #bfdbfe; border-radius: 10px; padding: 10px 12px;">
            <div style="font-size: 14px; font-weight: 800; color: #1e40af; margin-bottom: 3px;">🐍 ML & Intelligence Stack</div>
            <div style="font-size: 13.5px; color: #1e293b; line-height: 1.35;"><b>Python 3.10+</b>, <b>XGBoost</b>, <b>Scikit-learn</b>, <b>GeoPandas</b>, <b>NumPy</b>, <b>Joblib</b> pipeline serialization.</div>
          </div>

          <div style="background: #f0fdf4; border: 1.5px solid #bbf7d0; border-radius: 10px; padding: 10px 12px;">
            <div style="font-size: 14px; font-weight: 800; color: #166534; margin-bottom: 3px;">📱 Native Android & Edge Architecture</div>
            <div style="font-size: 13.5px; color: #1e293b; line-height: 1.35;"><b>Kotlin</b>, <b>Jetpack Compose UI</b>, <b>Room SQLite DB</b>, <b>WorkManager</b> background sync, <b>OsmDroid</b> vector GIS.</div>
          </div>

          <div style="background: #fdf2f8; border: 1.5px solid #fbcfe8; border-radius: 10px; padding: 10px 12px;">
            <div style="font-size: 14px; font-weight: 800; color: #9d174d; margin-bottom: 3px;">⚙️ Backend & API Gateway</div>
            <div style="font-size: 13.5px; color: #1e293b; line-height: 1.35;"><b>FastAPI (Asynchronous)</b>, <b>Uvicorn</b>, <b>Pydantic v2</b> validation, REST microservice architecture.</div>
          </div>

          <div style="background: #fff7ed; border: 1.5px solid #fed7aa; border-radius: 10px; padding: 10px 12px;">
            <div style="font-size: 14px; font-weight: 800; color: #9a3412; margin-bottom: 3px;">🛰️ Earth Observation & Climate Rasters</div>
            <div style="font-size: 13.5px; color: #1e293b; line-height: 1.35;"><b>NASA GLC & COOLR</b> (570 events), <b>SRTM 30m DEM</b>, <b>ESA WorldCover 10m</b>, <b>Open-Meteo Weather</b>.</div>
          </div>

          <div style="background: #f1f5f9; border: 1.5px solid #cbd5e1; border-radius: 10px; padding: 10px 12px;">
            <div style="font-size: 14px; font-weight: 800; color: #334155; margin-bottom: 3px;">📡 Hardware & Failover Channels</div>
            <div style="font-size: 13.5px; color: #1e293b; line-height: 1.35;">On-Device Mobile Edge (&lt;50ms), Linux Cloud VPS, GSM SMS modem fallback transmitters.</div>
          </div>
        </div>
      </div>

      <!-- Column 2: Methodology and Process for Implementation -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("2", "Methodology and Process for Implementation", "#028a42")}
          <div class="subtitle-text">Modular, iterative engineering pipeline from data intake to community alert:</div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 8px; flex: 1; justify-content: space-between;">
          <div class="card-box" style="padding: 8px 12px;">
            <div style="font-size: 16px; font-weight: 900; width: 28px; height: 28px; border-radius: 50%; background: #0284c7; color: #fff; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">1</div>
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #0f172a;">Requirement & Vulnerability Scoping</div>
              <div style="font-size: 12.8px; color: #475569;">Assessing all 8 NER states, hazard history, and district SEOC needs.</div>
            </div>
          </div>

          <div style="text-align: center; color: #94a3b8; font-size: 12px; margin: -5px 0;">▼</div>

          <div class="card-box" style="padding: 8px 12px;">
            <div style="font-size: 16px; font-weight: 900; width: 28px; height: 28px; border-radius: 50%; background: #059669; color: #fff; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">2</div>
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #0f172a;">Multi-Source Data Ingestion & Harmonization</div>
              <div style="font-size: 12.8px; color: #475569;">Extracting DEM slope, 7-day cumulative rainfall, lithology & land cover.</div>
            </div>
          </div>

          <div style="text-align: center; color: #94a3b8; font-size: 12px; margin: -5px 0;">▼</div>

          <div class="card-box" style="padding: 8px 12px;">
            <div style="font-size: 16px; font-weight: 900; width: 28px; height: 28px; border-radius: 50%; background: #7c3aed; color: #fff; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">3</div>
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #0f172a;">Machine Learning Model Training & Tuning</div>
              <div style="font-size: 12.8px; color: #475569;">Stratified 5-fold CV comparing XGBoost vs RF; tuned for 81.8% recall.</div>
            </div>
          </div>

          <div style="text-align: center; color: #94a3b8; font-size: 12px; margin: -5px 0;">▼</div>

          <div class="card-box" style="padding: 8px 12px;">
            <div style="font-size: 16px; font-weight: 900; width: 28px; height: 28px; border-radius: 50%; background: #d97706; color: #fff; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">4</div>
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #0f172a;">Offline Mobile & Edge GIS Engineering</div>
              <div style="font-size: 12.8px; color: #475569;">Jetpack Compose UI, offline vector tiles, local Room DB caching.</div>
            </div>
          </div>

          <div style="text-align: center; color: #94a3b8; font-size: 12px; margin: -5px 0;">▼</div>

          <div class="card-box" style="padding: 8px 12px;">
            <div style="font-size: 16px; font-weight: 900; width: 28px; height: 28px; border-radius: 50%; background: #dc2626; color: #fff; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">5</div>
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #0f172a;">Dual-Tier Siren & 2G SOS Testing</div>
              <div style="font-size: 12.8px; color: #475569;">Voice sirens in 8 native dialects + zero-network 2G SMS broadcast validation.</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Column 3: Working Prototype / System Architecture -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("3", "Working Prototype / System Architecture", "#582c8b")}
          <div class="subtitle-text">End-to-end 4-tier architecture connecting field sensors, edge AI, and responders:</div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 10px; flex: 1; justify-content: space-between;">
          <div style="background: #ffffff; border: 2px solid #0284c7; border-radius: 10px; padding: 10px 14px;">
            <div style="font-size: 14px; font-weight: 800; color: #0284c7;">Tier 1: Data Ingestion & Geospatial Telemetry</div>
            <div style="font-size: 13px; color: #475569; margin-top: 2px;">NASA GLC / COOLR (570 events), SRTM 30m DEM slope, ESA WorldCover, Open-Meteo precipitation radar.</div>
          </div>

          <div style="text-align: center; color: #0284c7; font-weight: 900; font-size: 14px; margin: -4px 0;">▼</div>

          <div style="background: #ffffff; border: 2px solid #7c3aed; border-radius: 10px; padding: 10px 14px;">
            <div style="font-size: 14px; font-weight: 800; color: #7c3aed;">Tier 2: Backend Core & Machine Learning Engine</div>
            <div style="font-size: 13px; color: #475569; margin-top: 2px;">FastAPI microservice, feature standardizer, XGBoost pipeline with sub-50ms inference time.</div>
          </div>

          <div style="text-align: center; color: #7c3aed; font-weight: 900; font-size: 14px; margin: -4px 0;">▼</div>

          <div style="background: #ffffff; border: 2px solid #059669; border-radius: 10px; padding: 10px 14px;">
            <div style="font-size: 14px; font-weight: 800; color: #059669;">Tier 3: Transmission Layer & 2G GSM Failover</div>
            <div style="font-size: 13px; color: #475569; margin-top: 2px;">Cloud REST over HTTPS + automated GSM modem 2G SMS emergency beacon broadcast.</div>
          </div>

          <div style="text-align: center; color: #059669; font-weight: 900; font-size: 14px; margin: -4px 0;">▼</div>

          <div style="background: #ffffff; border: 2px solid #ea580c; border-radius: 10px; padding: 10px 14px;">
            <div style="font-size: 14px; font-weight: 800; color: #ea580c;">Tier 4: End-User Clients & Tactical Response</div>
            <div style="font-size: 13px; color: #475569; margin-top: 2px;">Bhoochetak Mobile App (audio voice sirens), BRO Highway Dispatch, SDMA GIS Console.</div>
          </div>

          <div style="background: #f1f5f9; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 7px 12px; text-align: center;">
            <span style="font-size: 13.5px; font-weight: 800; color: #1e293b;">100% Offline-Resilient Early Warning Ecosystem for NER</span>
          </div>
        </div>
      </div>

    </div>

  </div>
  {make_footer("3")}
</div>


<!-- =========================================================================
     SLIDE 4: FEASIBILITY AND VIABILITY (Exact Match to Alpha Page 4)
     ========================================================================= -->
<div class="slide">
  {make_header("FEASIBILITY AND VIABILITY")}
  <div class="slide-body">

    <!-- 3 Columns Layout matching Alpha Company -->
    <div style="display: grid; grid-template-columns: 1.15fr 1fr 1.25fr; gap: 24px; flex: 1;">
      
      <!-- Column 1: Analysis of Feasibility -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("1", "Analysis of the Feasibility of the Idea", "#0072ce")}
          <div class="subtitle-text">The idea is feasible as it leverages existing infrastructure and open data:</div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 10px; flex: 1; justify-content: space-between;">
          <div class="card-box" style="padding: 9px 12px;">
            <div style="font-size: 20px;">💻</div>
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #0284c7;">Technical Feasibility</div>
              <div style="font-size: 13px; color: #334155; line-height: 1.3;">Runs on budget Android phones; sub-50ms inference with open-source ML stack.</div>
            </div>
          </div>

          <div class="card-box" style="padding: 9px 12px;">
            <div style="font-size: 20px;">⚙️</div>
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #059669;">Operational Feasibility</div>
              <div style="font-size: 13px; color: #334155; line-height: 1.3;">Complements official NDMA/SDMA protocols; 8 native dialects remove literacy barriers.</div>
            </div>
          </div>

          <div class="card-box" style="padding: 9px 12px;">
            <div style="font-size: 20px;">💰</div>
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #d97706;">Economic Feasibility</div>
              <div style="font-size: 13px; color: #334155; line-height: 1.3;">Zero expensive ground sensors; utilizes free satellite feeds; saves ₹1,200+ Cr in damages.</div>
            </div>
          </div>

          <div class="card-box" style="padding: 9px 12px;">
            <div style="font-size: 20px;">👥</div>
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #7c3aed;">Social Feasibility</div>
              <div style="font-size: 13px; color: #334155; line-height: 1.3;">Empowers 45M+ citizens across all 8 NER states with autonomous evacuation alerts.</div>
            </div>
          </div>

          <!-- Feasibility Summary Table -->
          <div style="border: 1.5px solid #cbd5e1; border-radius: 10px; overflow: hidden;">
            <div style="background: #e2e8f0; padding: 6px 12px; font-size: 13.5px; font-weight: 800; color: #0f172a;">Feasibility Summary</div>
            <table class="data-table">
              <tr>
                <td><b>Technical</b></td>
                <td><span style="color: #16a34a; font-weight: 800;">High</span> (81.8% Recall Validated)</td>
              </tr>
              <tr>
                <td><b>Operational</b></td>
                <td><span style="color: #16a34a; font-weight: 800;">High</span> (Deployable across 8 NER States)</td>
              </tr>
              <tr>
                <td><b>Economic</b></td>
                <td><span style="color: #16a34a; font-weight: 800;">Extremely High</span> (ROI &gt; 50x)</td>
              </tr>
              <tr>
                <td><b>Social</b></td>
                <td><span style="color: #16a34a; font-weight: 800;">High</span> (Life-Saving Coverage)</td>
              </tr>
            </table>
          </div>
        </div>
      </div>

      <!-- Column 2: Potential Challenges and Risks -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("2", "Potential Challenges and Risks", "#dc2626")}
          <div class="subtitle-text">Himalayan operational hazards and risk assessment:</div>
        </div>

        <div style="border: 1.5px solid #fca5a5; border-radius: 10px; overflow: hidden; flex: 1;">
          <table class="data-table">
            <thead>
              <tr style="background: #fee2e2;">
                <th style="width: 40px;">No.</th>
                <th>Challenge / Risk</th>
                <th style="width: 100px; text-align: center;">Impact</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style="font-weight: 800; text-align: center;">1</td>
                <td><b>Remote Mountain Network Blackouts</b><br><span style="font-size: 12px; color: #64748b;">Severe 4G failure during storms</span></td>
                <td style="text-align: center;"><span style="background: #fee2e2; color: #991b1b; padding: 3px 8px; border-radius: 6px; font-weight: 800; font-size: 12px;">High</span></td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">2</td>
                <td><b>Sparse Weather Station Density</b><br><span style="font-size: 12px; color: #64748b;">Limited rain gauges in remote hills</span></td>
                <td style="text-align: center;"><span style="background: #fee2e2; color: #991b1b; padding: 3px 8px; border-radius: 6px; font-weight: 800; font-size: 12px;">High</span></td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">3</td>
                <td><b>False Alarm Fatigue Among Citizens</b><br><span style="font-size: 12px; color: #64748b;">Unnecessary panic from over-warning</span></td>
                <td style="text-align: center;"><span style="background: #ffedd5; color: #9a3412; padding: 3px 8px; border-radius: 6px; font-weight: 800; font-size: 12px;">Medium</span></td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">4</td>
                <td><b>Diverse Linguistic Dialects</b><br><span style="font-size: 12px; color: #64748b;">Language barriers across tribes</span></td>
                <td style="text-align: center;"><span style="background: #ffedd5; color: #9a3412; padding: 3px 8px; border-radius: 6px; font-weight: 800; font-size: 12px;">Medium</span></td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">5</td>
                <td><b>Extreme Monsoon Battery Drain</b><br><span style="font-size: 12px; color: #64748b;">GPS power usage during long blackouts</span></td>
                <td style="text-align: center;"><span style="background: #ffedd5; color: #9a3412; padding: 3px 8px; border-radius: 6px; font-weight: 800; font-size: 12px;">Medium</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Column 3: Strategies for Overcoming These Challenges -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("3", "Strategies for Overcoming Challenges", "#582c8b")}
          <div class="subtitle-text">Engineered mitigations guaranteeing high system availability:</div>
        </div>

        <div style="border: 1.5px solid #cbd5e1; border-radius: 10px; overflow: hidden; flex: 1;">
          <table class="data-table">
            <thead>
              <tr style="background: #f1f5f9;">
                <th style="width: 40px;">No.</th>
                <th style="width: 130px;">Challenge</th>
                <th>Strategy / Solution</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style="font-weight: 800; text-align: center;">1</td>
                <td><b>Network Blackouts</b></td>
                <td>100% offline Room SQLite DB + OsmDroid + <b>2G SMS emergency beacon broadcast</b>.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">2</td>
                <td><b>Sparse Rain Gauges</b></td>
                <td>High-res satellite precipitation interpolation fusing <b>GPM Radar + Open-Meteo</b>.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">3</td>
                <td><b>False Alarms</b></td>
                <td><b>Tiered confidence thresholds</b> (Green/Yellow/Orange/Red) with 81.8% validated recall.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">4</td>
                <td><b>Linguistic Dialects</b></td>
                <td>Localized <b>audio voice sirens</b> pre-rendered in 8 native North Eastern languages.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">5</td>
                <td><b>Battery Drain</b></td>
                <td>Ultra-low power <b>WorkManager background sync</b> with on-demand GPS lock.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div>

    <!-- Expected Outcome Box (Alpha Company Style) -->
    <div style="background: #eff6ff; border: 1.5px solid #93c5fd; border-radius: 10px; padding: 10px 20px; display: flex; align-items: center; justify-content: space-between; margin-top: 12px;">
      <div style="display: flex; align-items: center; gap: 8px;">
        <span style="font-size: 20px;">📈</span>
        <span style="font-size: 15px; font-weight: 900; color: #1e40af; text-transform: uppercase;">Expected Outcome:</span>
      </div>
      <div style="display: flex; align-items: center; gap: 14px; font-size: 14px; font-weight: 800; color: #1e293b;">
        <span>Real-Time Hazard Visibility</span>
        <span style="color: #0284c7;">➔</span>
        <span>Pre-Emptive Evacuations</span>
        <span style="color: #0284c7;">➔</span>
        <span>Coordinated SDMA & BRO Action</span>
        <span style="color: #0284c7;">➔</span>
        <span style="color: #16a34a;">Zero Preventable Fatalities</span>
      </div>
    </div>

  </div>
  {make_footer("4")}
</div>


<!-- =========================================================================
     SLIDE 5: IMPACT AND BENEFITS (Exact Match to Alpha Page 5)
     ========================================================================= -->
<div class="slide">
  {make_header("IMPACT AND BENEFITS")}
  <div class="slide-body">

    <!-- 2 Main Columns Layout matching Alpha Company -->
    <div style="display: grid; grid-template-columns: 1fr 1.15fr; gap: 28px; flex: 1;">
      
      <!-- Column 1: Potential Impact on the Target Audience -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("1", "Potential Impact on the Target Audience", "#0072ce")}
          <div class="subtitle-text">Unified disaster resilience ecosystem serving all key regional stakeholders:</div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 11px; flex: 1; justify-content: space-between;">
          <div class="card-box" style="border-left: 4.5px solid #0284c7;">
            <div style="font-size: 26px; width: 44px; height: 44px; border-radius: 50%; background: #e0f2fe; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🧑‍🌾</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #0369a1;">Hill Citizens & Isolated Hamlets</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.35;">1–3 days advance warning, voice sirens in 8 native dialects, offline vector escape paths, and 1-tap 2G SMS SOS beacon.</div>
            </div>
          </div>

          <div class="card-box" style="border-left: 4.5px solid #7c3aed;">
            <div style="font-size: 26px; width: 44px; height: 44px; border-radius: 50%; background: #ede9fe; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🏛️</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #6d28d9;">MDoNER & State Disaster Authorities (SDMA / SEOC)</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.35;">Centralized tactical GIS heatmaps, real-time rainfall saturation rates, and field-verified ground crack reports for proactive dispatch.</div>
            </div>
          </div>

          <div class="card-box" style="border-left: 4.5px solid #e11d48;">
            <div style="font-size: 26px; width: 44px; height: 44px; border-radius: 50%; background: #ffe4e6; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🚨</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #be123c;">NDRF / SDRF Emergency First Responders</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.35;">Pinpoint GPS distress coordinates transmitted via offline SMS packets, cutting search-and-rescue response time by &gt;60% in Golden Hour.</div>
            </div>
          </div>

          <div class="card-box" style="border-left: 4.5px solid #ea580c;">
            <div style="font-size: 26px; width: 44px; height: 44px; border-radius: 50%; background: #ffedd5; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🚛</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #c2410c;">Border Roads Organisation (BRO) & National Highways</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.35;">Pre-emptive traffic diversions on NH-10 & NH-29; enabling heavy earthmover staging before mudslides wash out roadbeds.</div>
            </div>
          </div>

          <div class="card-box" style="border-left: 4.5px solid #16a34a;">
            <div style="font-size: 26px; width: 44px; height: 44px; border-radius: 50%; background: #dcfce7; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">🌲</div>
            <div>
              <div style="font-size: 14.5px; font-weight: 800; color: #15803d;">Forest Departments & Mountain Ecology</div>
              <div style="font-size: 13.5px; color: #334155; line-height: 1.35;">Prioritizes bio-engineering, retaining wall reinforcements, and reforestation along 45,000+ hectares of high-risk mountain slopes.</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Column 2: Benefits of the Solution -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("2", "Benefits of the Solution", "#028a42")}
          <div class="subtitle-text">Multi-dimensional benefits across academic, industrial, social, and economic dimensions:</div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; flex: 1;">
          
          <!-- Social Benefits -->
          <div style="background: #ffffff; border: 1.5px solid #bfdbfe; border-radius: 12px; padding: 12px 14px; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
              <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                <span style="font-size: 20px;">👥</span>
                <span style="font-size: 14.5px; font-weight: 800; color: #1e40af;">Social Benefits</span>
              </div>
              <div style="display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #334155;">
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> <b>Zero preventable fatalities</b> via 1–3d sirens.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Universal access across <b>8 native languages</b>.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Protects remote hamlets with offline alerts.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Prevents prolonged community displacement.</div>
              </div>
            </div>
            <div style="background: #eff6ff; border-radius: 6px; padding: 4px 8px; font-size: 12px; font-weight: 800; color: #1e40af; text-align: center; margin-top: 6px;">
              ZERO CASUALTIES GOAL
            </div>
          </div>

          <!-- Economic Benefits -->
          <div style="background: #ffffff; border: 1.5px solid #bbf7d0; border-radius: 12px; padding: 12px 14px; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
              <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                <span style="font-size: 20px;">💰</span>
                <span style="font-size: 14.5px; font-weight: 800; color: #166534;">Economic Benefits</span>
              </div>
              <div style="display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #334155;">
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> <b>Saves ₹1,200+ Cr</b> in emergency road repairs.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Pre-stages earthmovers before road washouts.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Minimizes expensive helicopter airlifts.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Avoids catastrophic highway logistics paralysis.</div>
              </div>
            </div>
            <div style="background: #f0fdf4; border-radius: 6px; padding: 4px 8px; font-size: 12px; font-weight: 800; color: #166534; text-align: center; margin-top: 6px;">
              ₹1,200+ CRORE SAVED
            </div>
          </div>

          <!-- Technological Benefits -->
          <div style="background: #ffffff; border: 1.5px solid #e9d5ff; border-radius: 12px; padding: 12px 14px; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
              <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                <span style="font-size: 20px;">⚡</span>
                <span style="font-size: 14.5px; font-weight: 800; color: #6b21a8;">Technological Benefits</span>
              </div>
              <div style="display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #334155;">
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> First unified <b>2,280-pt validated NER dataset</b>.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> 100% offline-first vector GIS map rendering.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Sub-50ms inference time on budget phones.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Crowdsourced field ground-truth verification.</div>
              </div>
            </div>
            <div style="background: #faf5ff; border-radius: 6px; padding: 4px 8px; font-size: 12px; font-weight: 800; color: #6b21a8; text-align: center; margin-top: 6px;">
              &lt;50MS EDGE INFERENCE
            </div>
          </div>

          <!-- Environmental Benefits -->
          <div style="background: #ffffff; border: 1.5px solid #fed7aa; border-radius: 12px; padding: 12px 14px; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
              <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                <span style="font-size: 20px;">🌲</span>
                <span style="font-size: 14.5px; font-weight: 800; color: #9a3412;">Environmental Benefits</span>
              </div>
              <div style="display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #334155;">
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Identifies fragile slopes for bio-engineering.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Mitigates deforestation-induced soil erosion.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Reduces post-disaster river debris runoff.</div>
                <div><span style="color: #16a34a; font-weight: 900;">✔</span> Promotes ecological slope stabilization.</div>
              </div>
            </div>
            <div style="background: #fff7ed; border-radius: 6px; padding: 4px 8px; font-size: 12px; font-weight: 800; color: #9a3412; text-align: center; margin-top: 6px;">
              45,000+ HECTARES PROTECTED
            </div>
          </div>

        </div>

        <!-- MDoNER Alignment Banner -->
        <div style="background: #fef9c3; border: 1.5px solid #ca8a04; border-radius: 10px; padding: 8px 14px; text-align: center; margin-top: 10px;">
          <span style="font-size: 13.5px; font-weight: 800; color: #854d0e;">
            🏛️ Direct alignment with MDoNER <b>PM-DevINE</b> and NDMA <b>National Landslide Risk Management Strategy</b>.
          </span>
        </div>
      </div>

    </div>

    <!-- Bottom Ribbon (Alpha Company Style) -->
    <div class="chevron-ribbon">
      <div class="ribbon-item"><span>🏔️ Vulnerable Hill Slopes</span></div>
      <div class="ribbon-arrow">➔</div>
      <div class="ribbon-item"><span>🛰️ Satellite Telemetry</span></div>
      <div class="ribbon-arrow">➔</div>
      <div class="ribbon-item"><span>🤖 81.8% Recall ML Engine</span></div>
      <div class="ribbon-arrow">➔</div>
      <div class="ribbon-item"><span>📢 8-Language Sirens</span></div>
      <div class="ribbon-arrow">➔</div>
      <div class="ribbon-item" style="color: #16a34a;"><span>🛡️ Resilient North Eastern Region</span></div>
    </div>

  </div>
  {make_footer("5")}
</div>


<!-- =========================================================================
     SLIDE 6: RESEARCH AND REFERENCES (Exact Match to Alpha Page 6)
     ========================================================================= -->
<div class="slide">
  {make_header("RESEARCH AND REFERENCES")}
  <div class="slide-body">

    <!-- 2 Main Columns Layout matching Alpha Company -->
    <div style="display: grid; grid-template-columns: 1fr 1.35fr; gap: 28px; flex: 1;">
      
      <!-- Column 1: Project Prototype (Bhoochetak App) -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("1", "Project Prototype (Bhoochetak App)", "#0072ce")}
          <div class="subtitle-text">Field-tested native Android prototype with offline telemetry & live ML inference:</div>
        </div>

        <!-- 4 Phone Screens Row -->
        <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; background: #0f172a; padding: 14px 10px; border-radius: 14px;">
          <div style="display: flex; flex-direction: column; align-items: center; gap: 6px;">
            <img src="{home_b64}" style="width: 100%; border-radius: 8px; border: 1.5px solid #334155; object-fit: cover;" />
            <span style="color: #7dd3fc; font-size: 11.5px; font-weight: 800; text-align: center;">Early Warning</span>
          </div>
          <div style="display: flex; flex-direction: column; align-items: center; gap: 6px;">
            <img src="{map_b64}" style="width: 100%; border-radius: 8px; border: 1.5px solid #334155; object-fit: cover;" />
            <span style="color: #7dd3fc; font-size: 11.5px; font-weight: 800; text-align: center;">Tactical GIS</span>
          </div>
          <div style="display: flex; flex-direction: column; align-items: center; gap: 6px;">
            <img src="{doppler_b64}" style="width: 100%; border-radius: 8px; border: 1.5px solid #334155; object-fit: cover;" />
            <span style="color: #7dd3fc; font-size: 11.5px; font-weight: 800; text-align: center;">Doppler Radar</span>
          </div>
          <div style="display: flex; flex-direction: column; align-items: center; gap: 6px;">
            <img src="{report_b64}" style="width: 100%; border-radius: 8px; border: 1.5px solid #334155; object-fit: cover;" />
            <span style="color: #7dd3fc; font-size: 11.5px; font-weight: 800; text-align: center;">Incident Report</span>
          </div>
        </div>

        <!-- Prototype Spec Highlights -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 10px;">
          <div class="card-box" style="padding: 8px 10px;">
            <div style="font-size: 18px;">📱</div>
            <div>
              <div style="font-size: 13.5px; font-weight: 800; color: #0284c7;">Target OS & Packaging</div>
              <div style="font-size: 12.5px; color: #475569;">Android 8.0–15 (API 26–35) | 26.6 MB APK</div>
            </div>
          </div>
          <div class="card-box" style="padding: 8px 10px;">
            <div style="font-size: 18px;">⚡</div>
            <div>
              <div style="font-size: 13.5px; font-weight: 800; color: #059669;">Zero-Cloud Resilience</div>
              <div style="font-size: 12.5px; color: #475569;">100% offline Room DB + OsmDroid</div>
            </div>
          </div>
        </div>

        <!-- Project Repository Link Box (Alpha Company Style) -->
        <div style="background: #f8fafc; border: 1.5px solid #cbd5e1; border-radius: 10px; padding: 10px 14px; margin-top: 10px;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="font-size: 18px;">🌐</span>
            <span style="font-size: 13.5px; font-weight: 800; color: #0f172a;">GitHub Repository:</span>
            <a href="https://github.com/areeb239/Landslide" style="font-size: 13.5px; font-weight: 700; color: #0284c7; text-decoration: none;">https://github.com/areeb239/Landslide</a>
          </div>
        </div>
      </div>

      <!-- Column 2: Details / Links of the Reference and Research Work -->
      <div style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          {make_pill("2", "Details / Links of the Reference and Research Work", "#028a42")}
          <div class="subtitle-text">Grounded in peer-reviewed scientific literature and official disaster guidelines:</div>
        </div>

        <div style="border: 1.5px solid #cbd5e1; border-radius: 10px; overflow: hidden; flex: 1;">
          <table class="data-table">
            <thead>
              <tr style="background: #e2e8f0;">
                <th style="width: 32px;">No.</th>
                <th style="width: 180px;">Reference / Research Work</th>
                <th style="width: 170px;">Source / Authority</th>
                <th>Relevance to Bhoochetak</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style="font-weight: 800; text-align: center;">1</td>
                <td><b>Global Landslide Catalog</b></td>
                <td>Kirschbaum et al. (NASA)</td>
                <td>Primary source for 251 historical NER landslide trigger events.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">2</td>
                <td><b>High Mountain Asia Catalog</b></td>
                <td>NASA / NSIDC (COOLR)</td>
                <td>Supplied 498 NER events; deduplicated into 570 point set.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">3</td>
                <td><b>SRTM 30m DEM</b></td>
                <td>NASA / USGS</td>
                <td>High-resolution elevation and local gradient slope computation.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">4</td>
                <td><b>ESA WorldCover 10m</b></td>
                <td>European Space Agency</td>
                <td>High-resolution land cover proving built-up risk correlation.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">5</td>
                <td><b>Open-Meteo Weather API</b></td>
                <td>Historical Weather Archive</td>
                <td>1-day, 3-day, and 7-day cumulative rainfall extraction.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">6</td>
                <td><b>Global Lithological Map</b></td>
                <td>Hartmann & Moosdorf</td>
                <td>Rock-type classification across 2,000 NER spatial polygons.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">7</td>
                <td><b>National Landslide Strategy</b></td>
                <td>NDMA, Govt. of India</td>
                <td>Framework for community early warning and SOP guidelines.</td>
              </tr>
              <tr>
                <td style="font-weight: 800; text-align: center;">8</td>
                <td><b>AI-Based Landslide Prototype</b></td>
                <td>GitHub (Our Team)</td>
                <td>Working prototype, production APK, and trained ML pipeline.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div>

    <!-- Bottom Research-to-Solution Ribbon (Alpha Company Style) -->
    <div style="background: #f1f5f9; border: 1.5px solid #cbd5e1; border-radius: 10px; padding: 9px 20px; display: flex; align-items: center; justify-content: space-between; margin-top: 10px;">
      <div style="display: flex; align-items: center; gap: 8px;">
        {make_pill("3", "Research-to-Solution Connection", "#582c8b")}
      </div>
      <div style="display: flex; align-items: center; gap: 14px; font-size: 14px; font-weight: 800; color: #1e293b;">
        <span>Peer-Reviewed Earth Data</span>
        <span style="color: #0284c7;">➔</span>
        <span>Regional Hazard Scoring</span>
        <span style="color: #0284c7;">➔</span>
        <span>Edge-Optimized AI</span>
        <span style="color: #0284c7;">➔</span>
        <span style="color: #16a34a;">Life-Saving Community Alerts</span>
      </div>
    </div>

  </div>
  {make_footer("6")}
</div>

</body>
</html>
'''

# Write HTML
html_path = OUT_DIR / "index.html"
with open(html_path, "w", encoding="utf-8") as f:
    f.write(html_content)
print(f"Generated {html_path} ({len(html_content)} chars)")

# Render to PDF using Headless Edge
edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
pdf_path = OUT_DIR / "Bhoochetak_SIH_2026.pdf"

args = [
    edge_path,
    "--headless",
    "--disable-gpu",
    f"--print-to-pdf={pdf_path}",
    "--no-pdf-header-footer",
    str(html_path.resolve())
]
print("Rendering PDF via headless Edge...")
res = subprocess.run(args, capture_output=True, text=True)
print("Edge exit code:", res.returncode)

if not pdf_path.exists():
    print("ERROR: PDF was not generated!")
    sys.exit(1)

# Render high-res slide images using PyMuPDF
doc = pymupdf.open(pdf_path)
print(f"Total Pages in PDF: {len(doc)}")
images = []
for i, page in enumerate(doc):
    pix = page.get_pixmap(dpi=200)
    img_path = OUT_DIR / f"slide_{i+1}.png"
    pix.save(img_path)
    images.append(img_path)
    print(f"Rendered slide {i+1} -> {img_path} ({pix.width}x{pix.height})")

# Compile PPTX
prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
blank_layout = prs.slide_layouts[6]

for img_p in images:
    slide = prs.slides.add_slide(blank_layout)
    slide.shapes.add_picture(str(img_p), Inches(0), Inches(0), width=Inches(13.333), height=Inches(7.5))

pptx_path = OUT_DIR / "Bhoochetak_SIH_2026.pptx"
prs.save(pptx_path)
print(f"Generated PPTX -> {pptx_path}")

# Copy to root workspace
shutil.copy(pdf_path, WORKSPACE / "Bhoochetak_SIH_2026.pdf")
shutil.copy(pptx_path, WORKSPACE / "Bhoochetak_SIH_2026.pptx")
print("Successfully copied final PDF and PPTX to workspace root!")
