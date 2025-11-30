#!/usr/bin/env python3
import re

test_line = '                            text = { Text("Download all unread") },'

patterns = [
    (r'Text\s*\(\s*"([^"]+)"\s*\)', 'Text("{}")'),
    (r'title\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}', 'title = {{ Text("{}") }}'),
    (r'label\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}', 'label = {{ Text("{}") }}'),
    (r'placeholder\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}', 'placeholder = {{ Text("{}") }}'),
    (r'text\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}', 'text = {{ Text("{}") }}'),
    (r'contentDescription\s*=\s*"([^"]+)"', 'contentDescription = "{}"'),
    (r'TitleText\s*\(\s*"([^"]+)"\s*\)', 'TitleText("{}")'),
]

for pattern, template in patterns:
    matches = list(re.finditer(pattern, test_line))
    if matches:
        for m in matches:
            print(f"Pattern: {pattern}")
            print(f"Match: {m.group(0)}")
            print(f"Captured: {m.group(1)}")
            print()
