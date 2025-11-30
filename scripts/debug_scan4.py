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

# Check for book/components
for file_path, strings in scan_results.items():
    if 'book/components' in str(file_path) or 'book\\components' in str(file_path):
        print(f"{file_path.name}: {len(strings)} strings")
