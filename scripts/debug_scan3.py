#!/usr/bin/env python3
import sys
sys.path.insert(0, 'scripts')
from batch_i18n_replacer import BatchI18nReplacer
from pathlib import Path

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

replacer = BatchI18nReplacer('.')
replacer.load_existing_strings()

# Scan all files
scan_results = replacer.scan_files(replacer.presentation_path)

# Check if ChapterBar.kt is in results
for file_path, strings in scan_results.items():
    if 'ChapterBar.kt' in str(file_path):
        print(f"Found ChapterBar.kt with {len(strings)} strings")
        for line_num, text, original in strings:
            print(f"  Line {line_num}: {text}")
        break
else:
    print("ChapterBar.kt NOT found in scan results!")
    
print(f"\nTotal files scanned: {len(scan_results)}")
