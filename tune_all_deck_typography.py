import re

with open("build_sih_deck.py", "r", encoding="utf-8") as f:
    text = f.read()

# 1. Update CSS styles in build_sih_deck.py
replacements = [
    # Header & Footer & Titles
    ("font-size: 32px;\n    font-weight: 900;\n    color: #0f172a;", "font-size: 36px;\n    font-weight: 900;\n    color: #0f172a;"),
    ("font-size: 12px;\n    color: #64748b;\n    font-weight: 600;", "font-size: 13.5px;\n    color: #64748b;\n    font-weight: 700;"),
    ("padding: 6px 16px;\n    font-size: 13.5px;\n    font-weight: 800;", "padding: 6px 18px;\n    font-size: 15.5px;\n    font-weight: 800;"),
    ("width: 22px;\n    height: 22px;\n    border-radius: 50%;\n    background: #ffffff;\n    display: inline-flex;\n    align-items: center;\n    justify-content: center;\n    margin-right: 9px;\n    font-size: 11.5px;",
     "width: 25px;\n    height: 25px;\n    border-radius: 50%;\n    background: #ffffff;\n    display: inline-flex;\n    align-items: center;\n    justify-content: center;\n    margin-right: 10px;\n    font-size: 13px;"),
    
    # Feature item
    ("padding: 8px 10px;", "padding: 8px 12px;"),
    ("font-size: 11px;\n    line-height: 1.35;\n    color: #334155;", "font-size: 13.5px;\n    line-height: 1.4;\n    color: #334155;"),
    
    # Callout
    ("font-size: 10.5px;\n    font-weight: 800;\n    color: #d9480f;", "font-size: 13px;\n    font-weight: 800;\n    color: #d9480f;"),
    ("font-size: 11px;\n    color: #495057;\n    line-height: 1.35;", "font-size: 13px;\n    color: #495057;\n    line-height: 1.4;"),
    
    # Process ribbon
    ("height: 42px;", "height: 46px;"),
    ("height: 40px;\n    background: #eef2f6;\n    border-radius: 6px;\n    display: flex;\n    align-items: center;\n    justify-content: center;\n    font-size: 10.5px;\n    font-weight: 700;",
     "height: 44px;\n    background: #eef2f6;\n    border-radius: 6px;\n    display: flex;\n    align-items: center;\n    justify-content: center;\n    font-size: 12.5px;\n    font-weight: 800;"),
    
    # Tables
    ("table.sih-table {\n    width: 100%;\n    border-collapse: collapse;\n    font-size: 10px;",
     "table.sih-table {\n    width: 100%;\n    border-collapse: collapse;\n    font-size: 12px;"),
    ("table.sih-table th {\n    background: #edf2f7;\n    color: #1e293b;\n    font-weight: 800;\n    padding: 7px 9px;\n    text-align: left;\n    border-bottom: 2px solid #cbd5e1;\n    font-size: 10px;",
     "table.sih-table th {\n    background: #edf2f7;\n    color: #1e293b;\n    font-weight: 800;\n    padding: 8px 10px;\n    text-align: left;\n    border-bottom: 2px solid #cbd5e1;\n    font-size: 12.5px;"),
    ("table.sih-table td {\n    padding: 6.5px 9px;", "table.sih-table td {\n    padding: 6px 10px;"),
    
    # Phone label
    (".phone-label {\n    background: #f8fafc;\n    padding: 5px 8px;\n    font-size: 10px;",
     ".phone-label {\n    background: #f8fafc;\n    padding: 5px 8px;\n    font-size: 12px;"),
     
    # Subtitles under section pills
    ("font-size: 10.5px; color: #64748b; margin-top: 3px; font-weight: 600;", "font-size: 13px; color: #64748b; margin-top: 3px; font-weight: 700;"),
]

for old, new in replacements:
    if old in text:
        text = text.replace(old, new)
        print(f"Replaced: {old[:30]}...")
    else:
        print(f"NOT FOUND: {old[:30]}...")

# Slide 1 font size increase
text = text.replace(
    'font-size: 22px; color: #000000; font-weight: 700; max-width: 1050px; line-height: 1.35;',
    'font-size: 24px; color: #000000; font-weight: 700; max-width: 1080px; line-height: 1.4;'
)

# Slide 2 font size tuning
text = text.replace(
    'font-size: 12px; color: #01579b; line-height: 1.4;',
    'font-size: 14px; color: #01579b; line-height: 1.45;'
)
text = text.replace(
    'font-size: 10.5px; font-weight: 800; color: #6b21a8; text-transform: uppercase; margin-bottom: 2px;',
    'font-size: 13.5px; font-weight: 800; color: #6b21a8; text-transform: uppercase; margin-bottom: 2px;'
)
text = text.replace(
    'font-size: 10px; color: #4a044e; line-height: 1.4;',
    'font-size: 12.5px; color: #4a044e; line-height: 1.4;'
)
text = text.replace(
    'font-size: 10.5px; font-weight: 800; color: #1e40af; text-transform: uppercase; margin-bottom: 2px;',
    'font-size: 13.5px; font-weight: 800; color: #1e40af; text-transform: uppercase; margin-bottom: 2px;'
)
text = text.replace(
    'font-size: 10px; color: #1e3a8a; line-height: 1.4;',
    'font-size: 12.5px; color: #1e3a8a; line-height: 1.4;'
)
text = text.replace(
    'strong style="font-size: 10px; color: #5b21b6;"',
    'strong style="font-size: 12.5px; color: #5b21b6;"'
)
text = text.replace(
    'span style="font-size: 9.5px; color: #4c1d95;"',
    'span style="font-size: 12px; color: #4c1d95;"'
)
text = text.replace(
    'padding: 6px 10px; font-size: 10px; color: #334155;',
    'padding: 6px 10px; font-size: 12.5px; color: #334155;'
)
text = text.replace(
    'padding: 6px 12px; font-size: 10.5px; color: #1e3a8a;',
    'padding: 6px 12px; font-size: 12.5px; color: #1e3a8a;'
)

# Slide 3 font tuning
text = text.replace(
    'font-size: 10.5px; font-weight: 800; color: #1e40af; text-transform: uppercase;',
    'font-size: 13px; font-weight: 800; color: #1e40af; text-transform: uppercase;'
)
text = text.replace(
    'font-size: 10.5px; font-weight: 800; color: #6b21a8; text-transform: uppercase;',
    'font-size: 13px; font-weight: 800; color: #6b21a8; text-transform: uppercase;'
)
text = text.replace(
    'font-size: 10.5px; font-weight: 800; color: #15803d; text-transform: uppercase;',
    'font-size: 13px; font-weight: 800; color: #15803d; text-transform: uppercase;'
)
text = text.replace(
    'font-size: 10.5px; font-weight: 800; color: #c2410c; text-transform: uppercase;',
    'font-size: 13px; font-weight: 800; color: #c2410c; text-transform: uppercase;'
)
text = text.replace(
    'font-size: 10.5px; font-weight: 800; color: #475569; text-transform: uppercase;',
    'font-size: 13px; font-weight: 800; color: #475569; text-transform: uppercase;'
)
text = text.replace(
    'font-size: 10.5px; font-weight: 800; color: #0f766e; text-transform: uppercase;',
    'font-size: 13px; font-weight: 800; color: #0f766e; text-transform: uppercase;'
)
text = text.replace(
    'padding: 6px 9px; font-size: 10.5px; line-height: 1.35;',
    'padding: 6px 9px; font-size: 12.5px; line-height: 1.4;'
)
text = text.replace(
    'padding: 7px 10px; font-size: 10px; color: #166534;',
    'padding: 7px 10px; font-size: 12.5px; color: #166534;'
)
text = text.replace(
    'font-size: 9.5px; color: #166534; line-height: 1.35;',
    'font-size: 12px; color: #166534; line-height: 1.4;'
)
text = text.replace(
    'font-size: 9.3px; color: #9a3412; line-height: 1.35;',
    'font-size: 12px; color: #9a3412; line-height: 1.4;'
)
text = text.replace(
    'font-size: 9.3px; color: #701a75; margin-top: auto;',
    'font-size: 12.5px; color: #701a75; margin-top: auto;'
)
text = text.replace(
    'font-size: 9.5px; font-weight: 800;',
    'font-size: 12.5px; font-weight: 800;'
)
text = text.replace(
    'font-size: 9.8px; color: #1e293b;',
    'font-size: 12px; color: #1e293b;'
)
text = text.replace(
    'padding: 5px 9px; font-size: 10px; color: #334155; text-align: center; margin-top: auto;',
    'padding: 6px 10px; font-size: 12.5px; color: #334155; text-align: center; margin-top: auto;'
)

# Slide 4 font tuning
text = text.replace(
    '<table class="sih-table" style="font-size: 9.8px;">',
    '<table class="sih-table" style="font-size: 12px;">'
)
text = text.replace(
    'font-size: 9.8px; color: #334155; line-height: 1.35; background: #e0f2fe; padding: 6px 9px;',
    'font-size: 12.5px; color: #334155; line-height: 1.4; background: #e0f2fe; padding: 6px 9px;'
)
text = text.replace(
    'padding: 5px 9px; font-size: 9.5px; color: #334155;',
    'padding: 5px 9px; font-size: 12px; color: #334155;'
)
text = text.replace(
    'font-size: 9.8px; font-weight: 800; color: #334155; margin-bottom: 3px; text-transform: uppercase;',
    'font-size: 12.5px; font-weight: 800; color: #334155; margin-bottom: 3px; text-transform: uppercase;'
)
text = text.replace(
    'gap: 5px; font-size: 9.5px;',
    'gap: 5px; font-size: 12px;'
)
text = text.replace(
    'font-size: 9.8px; color: #7c2d12; line-height: 1.35;',
    'font-size: 12px; color: #7c2d12; line-height: 1.4;'
)
text = text.replace(
    '<table class="sih-table mt-auto" style="font-size: 9.5px;">',
    '<table class="sih-table mt-auto" style="font-size: 12px;">'
)
text = text.replace(
    '<table class="sih-table" style="font-size: 9.8px;">',
    '<table class="sih-table" style="font-size: 12px;">'
)
text = text.replace(
    'font-size: 10px; font-weight: 800; color: #166534; text-transform: uppercase; margin-bottom: 2px;',
    'font-size: 12.5px; font-weight: 800; color: #166534; text-transform: uppercase; margin-bottom: 2px;'
)
text = text.replace(
    'font-size: 9.5px; color: #14532d; line-height: 1.35;',
    'font-size: 12px; color: #14532d; line-height: 1.4;'
)
text = text.replace(
    'font-size: 9.5px; color: #701a75; line-height: 1.35;',
    'font-size: 12px; color: #701a75; line-height: 1.4;'
)
text = text.replace(
    'strong style="font-size: 10px; color: #1e293b;"',
    'strong style="font-size: 12.5px; color: #1e293b;"'
)
text = text.replace(
    'font-size: 9.2px; color: #475569; line-height: 1.35; margin-top: 1px;',
    'font-size: 12px; color: #475569; line-height: 1.4; margin-top: 1px;'
)

# Slide 5 font tuning
text = text.replace(
    'font-size: 10.5px; font-weight: 800; color: #1e40af; text-transform: uppercase; margin-bottom: 3px;',
    'font-size: 13px; font-weight: 800; color: #1e40af; text-transform: uppercase; margin-bottom: 3px;'
)
text = text.replace(
    'gap: 5px; font-size: 9.8px; font-weight: 700; color: #1e3a8a;',
    'gap: 5px; font-size: 12px; font-weight: 800; color: #1e3a8a;'
)
text = text.replace(
    'font-size: 9.5px; color: #334155; line-height: 1.35;',
    'font-size: 12px; color: #334155; line-height: 1.4;'
)
text = text.replace(
    'strong style="font-size: 10.5px;',
    'strong style="font-size: 13px;'
)
text = text.replace(
    'font-size: 9.5px; color: #475569; line-height: 1.35;',
    'font-size: 12px; color: #475569; line-height: 1.38;'
)
text = text.replace(
    'font-size: 10px; font-weight: 800; color: #1e40af; margin-bottom: 2px;',
    'font-size: 12.5px; font-weight: 800; color: #1e40af; margin-bottom: 2px;'
)
text = text.replace(
    'font-size: 9.2px; color: #1e3a8a; line-height: 1.35;',
    'font-size: 12px; color: #1e3a8a; line-height: 1.4;'
)
text = text.replace(
    'font-size: 9.5px; color: #701a75; margin-top: auto;',
    'font-size: 12px; color: #701a75; margin-top: auto;'
)
text = text.replace(
    'strong style="font-size: 10.5px; color: #1e40af;"',
    'strong style="font-size: 13px; color: #1e40af;"'
)
text = text.replace(
    'strong style="font-size: 10.5px; color: #166534;"',
    'strong style="font-size: 13px; color: #166534;"'
)
text = text.replace(
    'strong style="font-size: 10.5px; color: #6b21a8;"',
    'strong style="font-size: 13px; color: #6b21a8;"'
)
text = text.replace(
    'strong style="font-size: 10.5px; color: #854d0e;"',
    'strong style="font-size: 13px; color: #854d0e;"'
)
text = text.replace(
    'font-size: 8.5px; font-weight: 800; padding: 1px 6px;',
    'font-size: 10.5px; font-weight: 900; padding: 2px 7px;'
)
text = text.replace(
    'font-size: 9.2px; color: #1e3a8a; line-height: 1.4; padding-left: 10px;',
    'font-size: 11.5px; color: #1e3a8a; line-height: 1.38; padding-left: 10px;'
)
text = text.replace(
    'font-size: 9.2px; color: #14532d; line-height: 1.4; padding-left: 10px;',
    'font-size: 11.5px; color: #14532d; line-height: 1.38; padding-left: 10px;'
)
text = text.replace(
    'font-size: 9.2px; color: #581c87; line-height: 1.4; padding-left: 10px;',
    'font-size: 11.5px; color: #581c87; line-height: 1.38; padding-left: 10px;'
)
text = text.replace(
    'font-size: 9.2px; color: #713f12; line-height: 1.4; padding-left: 10px;',
    'font-size: 11.5px; color: #713f12; line-height: 1.38; padding-left: 10px;'
)
text = text.replace(
    '<table class="sih-table" style="font-size: 9.2px;">',
    '<table class="sih-table" style="font-size: 11.5px;">'
)
text = text.replace(
    'font-size: 11px; font-weight: 800; color: #ffffff;',
    'font-size: 13px; font-weight: 800; color: #ffffff;'
)

# Slide 6 font tuning
text = text.replace(
    'gap: 6px; font-size: 9px; color: #334155; line-height: 1.3;',
    'gap: 6px; font-size: 11.5px; color: #334155; line-height: 1.35;'
)
text = text.replace(
    'padding: 5px 8px; font-size: 9.5px; color: #1e40af;',
    'padding: 6px 10px; font-size: 12px; color: #1e40af;'
)
text = text.replace(
    'padding: 6px 10px; font-size: 10px; color: #334155;',
    'padding: 6px 10px; font-size: 12px; color: #334155;'
)
text = text.replace(
    '<table class="sih-table" style="font-size: 9.3px;">',
    '<table class="sih-table" style="font-size: 11.5px;">'
)
text = text.replace(
    'padding: 5px 8px; font-size: 9.2px; color: #1e40af; line-height: 1.35;',
    'padding: 5px 8px; font-size: 11.5px; color: #1e40af; line-height: 1.38;'
)
text = text.replace(
    'padding: 5px 8px; font-size: 9.2px; color: #166534; line-height: 1.35;',
    'padding: 5px 8px; font-size: 11.5px; color: #166534; line-height: 1.38;'
)
text = text.replace(
    'padding: 5px 8px; font-size: 9.2px; color: #701a75; line-height: 1.35;',
    'padding: 5px 8px; font-size: 11.5px; color: #701a75; line-height: 1.38;'
)
text = text.replace(
    'font-size: 9px; font-weight: 700; color: #4a044e;',
    'font-size: 11.5px; font-weight: 800; color: #4a044e;'
)
text = text.replace(
    'padding: 6px 18px; display: flex; align-items: center; justify-content: space-between; font-size: 11px; font-weight: 700; color: #334155;',
    'padding: 6px 18px; display: flex; align-items: center; justify-content: space-between; font-size: 13px; font-weight: 700; color: #334155;'
)

with open("build_sih_deck.py", "w", encoding="utf-8") as f:
    f.write(text)

print("build_sih_deck.py typography scaled up successfully!")
