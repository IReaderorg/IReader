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

# Prepare replacements
replacer.prepare_replacements(scan_results)

# Check for ChapterBar.kt replacements
for repl in replacer.replacements:
    if 'ChapterBar.kt' in str(repl.file_path) and 'FindIn' not in str(repl.file_path):
        print(f"Line {repl.line_number}: {repl.original_text}")
        print(f"  Original: {repl.original_match}")
        print(f"  Replace:  {repl.replacement}")
        print()
