#!/usr/bin/env python3
"""Find empty or invalid string keys in strings.xml"""

import sys
import xml.etree.ElementTree as ET
from pathlib import Path

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

xml_path = Path('i18n/src/commonMain/composeResources/values/strings.xml')
tree = ET.parse(xml_path)
root = tree.getroot()

issues = []
for i, elem in enumerate(root.findall('string')):
    name = elem.get('name')
    text = elem.text or ''
    if not name or name.strip() == '':
        issues.append((i, 'empty', name, text))
    elif len(name) < 2:
        issues.append((i, 'short', name, text))

if issues:
    print(f"Found {len(issues)} issue(s):")
    for idx, issue_type, name, text in issues:
        print(f"  Index {idx}: {issue_type} key, name=\"{name}\" text=\"{text[:50]}...\"")
else:
    print("No issues found")
