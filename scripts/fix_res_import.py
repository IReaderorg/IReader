#!/usr/bin/env python3
"""
Fix Res imports to use wildcard import for string accessors
"""

import re
import sys
from pathlib import Path

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

def fix_file(file_path: Path) -> bool:
    """Fix Res import in a file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check if file uses Res.string
        if 'Res.string.' not in content:
            return False
        
        modified = False
        
        # Add wildcard import if not present
        if 'import ireader.i18n.resources.*' not in content:
            # Check if we have the specific Res import
            if 'import ireader.i18n.resources.Res' in content:
                # Replace with wildcard
                content = content.replace(
                    'import ireader.i18n.resources.Res',
                    'import ireader.i18n.resources.*'
                )
                modified = True
            else:
                # Add the wildcard import
                import_pattern = re.compile(r'^import\s+[^\n]+$', re.MULTILINE)
                imports = list(import_pattern.finditer(content))
                if imports:
                    last_import = imports[-1]
                    insert_pos = last_import.end()
                    content = (content[:insert_pos] + 
                              '\nimport ireader.i18n.resources.*' +
                              content[insert_pos:])
                    modified = True
        
        if modified:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        
        return False
        
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return False

def main():
    project_root = Path('.')
    presentation_path = project_root / 'presentation'
    
    print("Fixing Res imports...\n")
    
    fixed_files = []
    
    for kt_file in presentation_path.rglob('*.kt'):
        if fix_file(kt_file):
            rel_path = kt_file.relative_to(project_root)
            print(f"Fixed: {rel_path}")
            fixed_files.append(kt_file)
    
    print(f"\nFixed {len(fixed_files)} files")

if __name__ == '__main__':
    main()
