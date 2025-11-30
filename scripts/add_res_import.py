#!/usr/bin/env python3
"""
Add missing Res import to files that use Res.string
"""

import re
import sys
from pathlib import Path

# Fix Windows encoding issues
if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

def add_res_import(file_path: Path) -> bool:
    """Add Res import if needed"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check if file uses Res.string but doesn't import it
        if 'Res.string' in content and 'import ireader.i18n.resources.Res' not in content:
            # Find last import
            import_pattern = re.compile(r'^import\s+[^\n]+$', re.MULTILINE)
            imports = list(import_pattern.finditer(content))
            if imports:
                last_import = imports[-1]
                insert_pos = last_import.end()
                new_content = (content[:insert_pos] + 
                              '\nimport ireader.i18n.resources.Res' +
                              content[insert_pos:])
                
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                
                return True
        
        return False
        
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return False

def main():
    project_root = Path('.')
    presentation_path = project_root / 'presentation'
    
    print("🔧 Adding missing Res imports...\n")
    
    fixed_files = []
    
    for kt_file in presentation_path.rglob('*.kt'):
        if add_res_import(kt_file):
            rel_path = kt_file.relative_to(project_root)
            print(f"✓ Added Res import: {rel_path}")
            fixed_files.append(kt_file)
    
    print(f"\n✅ Fixed {len(fixed_files)} files")

if __name__ == '__main__':
    main()
