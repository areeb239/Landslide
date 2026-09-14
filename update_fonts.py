import re

with open("build_sih_deck.py", "r", encoding="utf-8") as f:
    code = f.read()

# 1. Update make_top_left_badge to larger font & badge
old_badge = '''def make_top_left_badge():
    return f\'\'\'
    <div style="display: flex; align-items: center; gap: 10px; border: 2px solid #5c2d91; border-radius: 28px; padding: 4px 14px; background: #ffffff; box-shadow: 0 2px 6px rgba(92,45,145,0.12);">
      <img src="{logo_b64}" style="width: 32px; height: 32px; border-radius: 8px; object-fit: cover;" />
      <div style="display: flex; flex-direction: column; line-height: 1.15; text-align: left;">
        <span style="font-size: 15px; font-weight: 900; color: #1e293b; letter-spacing: 0.5px;">Bhoochetak</span>
        <span style="font-size: 11px; font-weight: 800; color: #d32f2f; letter-spacing: 1px;">TERRATECH</span>
      </div>
    </div>
    \'\'\''''

new_badge = '''def make_top_left_badge():
    return f\'\'\'
    <div style="display: flex; align-items: center; gap: 12px; border: 2.5px solid #5c2d91; border-radius: 30px; padding: 5px 18px; background: #ffffff; box-shadow: 0 3px 8px rgba(92,45,145,0.15);">
      <img src="{logo_b64}" style="width: 40px; height: 40px; border-radius: 9px; object-fit: cover;" />
      <div style="display: flex; flex-direction: column; line-height: 1.15; text-align: left;">
        <span style="font-size: 18px; font-weight: 900; color: #1e293b; letter-spacing: 0.5px;">Bhoochetak</span>
        <span style="font-size: 13px; font-weight: 800; color: #d32f2f; letter-spacing: 1.2px;">TERRATECH</span>
      </div>
    </div>
    \'\'\''''

if old_badge in code:
    code = code.replace(old_badge, new_badge)
    print("Replaced top-left badge with larger version")
else:
    print("Warning: old_badge not found exactly")

# 2. Update Ecosystem SVG text sizes
old_svg_text = '''    <text x="20" y="38" font-family="Inter, sans-serif" font-size="11.5" font-weight="800" fill="#1e40af">🛰️ NASA GLC + COOLR</text>
    <text x="20" y="58" font-family="Inter, sans-serif" font-size="10" font-weight="600" fill="#2563eb">570 Real Events (Label 1)</text>'''

new_svg_text = '''    <text x="18" y="37" font-family="Inter, sans-serif" font-size="13" font-weight="800" fill="#1e40af">🛰️ NASA GLC + COOLR</text>
    <text x="18" y="57" font-family="Inter, sans-serif" font-size="12" font-weight="700" fill="#2563eb">570 Real Events (Label 1)</text>'''

code = code.replace(old_svg_text, new_svg_text)

old_svg_text_2 = '''    <text x="315" y="38" font-family="Inter, sans-serif" font-size="11.5" font-weight="800" fill="#15803d">🛡️ Pseudo-Absence Grid</text>
    <text x="315" y="58" font-family="Inter, sans-serif" font-size="10" font-weight="600" fill="#16a34a">1,710 Sloped Pts (Label 0)</text>'''

new_svg_text_2 = '''    <text x="315" y="37" font-family="Inter, sans-serif" font-size="13" font-weight="800" fill="#15803d">🛡️ Pseudo-Absence Grid</text>
    <text x="315" y="57" font-family="Inter, sans-serif" font-size="12" font-weight="700" fill="#16a34a">1,710 Sloped Pts (Label 0)</text>'''

code = code.replace(old_svg_text_2, new_svg_text_2)

old_svg_center = '''    <text x="230" y="92" font-family="Inter, sans-serif" font-size="11.5" font-weight="900" fill="#ffffff" text-anchor="middle">XGBOOST AI</text>
    <text x="230" y="110" font-family="Inter, sans-serif" font-size="15" font-weight="900" fill="#38bdf8" text-anchor="middle">81.8%</text>
    <text x="230" y="126" font-family="Inter, sans-serif" font-size="9.5" font-weight="700" fill="#e2e8f0" text-anchor="middle">CV RECALL</text>'''

new_svg_center = '''    <text x="230" y="91" font-family="Inter, sans-serif" font-size="13" font-weight="900" fill="#ffffff" text-anchor="middle">XGBOOST AI</text>
    <text x="230" y="112" font-family="Inter, sans-serif" font-size="18" font-weight="900" fill="#38bdf8" text-anchor="middle">81.8%</text>
    <text x="230" y="128" font-family="Inter, sans-serif" font-size="11" font-weight="800" fill="#e2e8f0" text-anchor="middle">CV RECALL</text>'''

code = code.replace(old_svg_center, new_svg_center)

old_svg_bottom = '''    <text x="20" y="158" font-family="Inter, sans-serif" font-size="11.5" font-weight="800" fill="#9d174d">🏔️ Hill Communities</text>
    <text x="20" y="178" font-family="Inter, sans-serif" font-size="10" font-weight="600" fill="#be185d">1-3d Alerts & 2G SOS</text>
  </g>

  <!-- Bottom-Right: MDoNER & SDMA -->
  <g filter="url(#shadow)">
    <rect x="305" y="136" width="145" height="58" rx="8" fill="#fff7ed" stroke="#f97316" stroke-width="1.5" />
    <text x="315" y="158" font-family="Inter, sans-serif" font-size="11.5" font-weight="800" fill="#9a3412">🏛️ MDoNER / SDMA</text>
    <text x="315" y="178" font-family="Inter, sans-serif" font-size="10" font-weight="600" fill="#c2410c">Tactical GIS Corridors</text>'''

new_svg_bottom = '''    <text x="18" y="157" font-family="Inter, sans-serif" font-size="13" font-weight="800" fill="#9d174d">🏔️ Hill Communities</text>
    <text x="18" y="177" font-family="Inter, sans-serif" font-size="12" font-weight="700" fill="#be185d">1-3d Alerts & 2G SOS</text>
  </g>

  <!-- Bottom-Right: MDoNER & SDMA -->
  <g filter="url(#shadow)">
    <rect x="305" y="136" width="145" height="58" rx="8" fill="#fff7ed" stroke="#f97316" stroke-width="1.5" />
    <text x="315" y="157" font-family="Inter, sans-serif" font-size="13" font-weight="800" fill="#9a3412">🏛️ MDoNER / SDMA</text>
    <text x="315" y="177" font-family="Inter, sans-serif" font-size="12" font-weight="700" fill="#c2410c">Tactical GIS Corridors</text>'''

code = code.replace(old_svg_bottom, new_svg_bottom)

with open("build_sih_deck.py", "w", encoding="utf-8") as f:
    f.write(code)

print("Updated SVGs and Top-Left Badge!")
