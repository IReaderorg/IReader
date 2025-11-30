#!/usr/bin/env python3
import sys
import re
from pathlib import Path

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

file_path = Path('presentation/src/commonMain/kotlin/ireader/presentation/ui/book/components/ChapterBar.kt')

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()
    lines = content.split('\n')

patterns = [
    (r'Text\s*\(\s*"([^"]+)"\s*\)', 'Text("{}")'),
    (r'text\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}', 'text = {{ Text("{}") }}'),
]

for line_num, line in enumerate(lines, 1):
    # Skip lines that already use localization
    if 'localizeHelper.localize' in line or 'Res.string.' in line:
        continue
    
    # Skip comments
    stripped = line.strip()
    if stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
        continue
    
    for pattern, _ in patterns:
        matches = list(re.finditer(pattern, line))
        for match in matches:
            text = match.group(1)
            print(f"Line {line_num}: {text}")
            print(f"  Full match: {match.group(0)}")
