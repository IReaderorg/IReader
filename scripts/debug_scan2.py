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

file_path = Path('presentation/src/commonMain/kotlin/ireader/presentation/ui/book/components/ChapterBar.kt')
results = replacer.find_hardcoded_strings_in_file(file_path)

print(f"Found {len(results)} hardcoded strings in ChapterBar.kt:")
for line_num, text, original in results:
    print(f"  Line {line_num}: {text}")
