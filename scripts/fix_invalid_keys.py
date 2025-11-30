#!/usr/bin/env python3
"""
Fix invalid string keys that start with numbers
"""

import sys
import re
from pathlib import Path

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

# Keys that need to be renamed (old -> new)
key_renames = {
    '0x': 'wallet_placeholder',
    '5_min': 'five_min',
    '10_min': 'ten_min',
    '15_min': 'fifteen_min',
    '30_min': 'thirty_min',
}

# Fix strings.xml
strings_path = Path('i18n/src/commonMain/composeResources/values/strings.xml')
with open(strings_path, 'r', encoding='utf-8') as f:
    content = f.read()

for old_key, new_key in key_renames.items():
    content = content.replace(f'name="{old_key}"', f'name="{new_key}"')
    print(f'Renamed {old_key} -> {new_key} in strings.xml')

with open(strings_path, 'w', encoding='utf-8') as f:
    f.write(content)

# Fix Kotlin files
presentation_path = Path('presentation')
for kt_file in presentation_path.rglob('*.kt'):
    try:
        with open(kt_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        modified = False
        for old_key, new_key in key_renames.items():
            if f'Res.string.{old_key}' in content:
                # Use backticks for keys starting with numbers
                content = content.replace(f'Res.string.{old_key}', f'Res.string.{new_key}')
                modified = True
                print(f'Fixed {old_key} -> {new_key} in {kt_file.name}')
        
        if modified:
            with open(kt_file, 'w', encoding='utf-8') as f:
                f.write(content)
    except Exception as e:
        print(f'Error processing {kt_file}: {e}')

print('Done')
