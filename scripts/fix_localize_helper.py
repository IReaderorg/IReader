#!/usr/bin/env python3
"""
Fix LocalizeHelper Import Script
Adds the required localizeHelper declaration to ALL @Composable functions that use it.
"""

import re
import sys
from pathlib import Path
from typing import Set, List, Tuple

# Fix Windows encoding issues
if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

def find_composable_functions(content: str) -> List[Tuple[int, int, str]]:
    """Find all @Composable functions and their body positions.
    Returns list of (func_start, brace_pos, func_name)"""
    results = []
    
    # Find all @Composable annotations
    composable_pattern = re.compile(r'@Composable\s*\n?\s*(?:@\w+(?:\([^)]*\))?\s*\n?\s*)*(?:private\s+|internal\s+|public\s+)?fun\s+(\w+)', re.MULTILINE)
    
    for match in composable_pattern.finditer(content):
        func_name = match.group(1)
        func_start = match.start()
        
        # Find the opening brace of the function body
        # We need to skip past the parameter list and return type
        search_start = match.end()
        
        # First find the opening paren of the parameter list
        open_paren = content.find('(', search_start)
        if open_paren == -1:
            continue
        
        # Now find the matching closing paren
        paren_depth = 1
        pos = open_paren + 1
        while pos < len(content) and paren_depth > 0:
            if content[pos] == '(':
                paren_depth += 1
            elif content[pos] == ')':
                paren_depth -= 1
            pos += 1
        
        if paren_depth != 0:
            continue
        
        # Now find the opening brace after the closing paren
        # Skip any return type annotation
        brace_pos = content.find('{', pos)
        
        if brace_pos != -1:
            results.append((func_start, brace_pos, func_name))
    
    return results

def function_uses_localize_helper(content: str, brace_pos: int) -> bool:
    """Check if the function body uses localizeHelper"""
    # Find the matching closing brace
    depth = 1
    pos = brace_pos + 1
    while pos < len(content) and depth > 0:
        if content[pos] == '{':
            depth += 1
        elif content[pos] == '}':
            depth -= 1
        pos += 1
    
    func_body = content[brace_pos:pos]
    return 'localizeHelper.localize' in func_body

def function_has_localize_helper_declaration(content: str, brace_pos: int) -> bool:
    """Check if the function already has localizeHelper declaration"""
    # Look at the first 500 chars after the opening brace
    snippet = content[brace_pos:brace_pos + 500]
    return 'val localizeHelper' in snippet

def fix_file(file_path: Path) -> int:
    """Fix a single file by adding localizeHelper to all composable functions that need it.
    Returns number of functions fixed."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check if file uses localizeHelper at all
        if 'localizeHelper.localize' not in content:
            return 0
        
        # Find all composable functions
        composable_funcs = find_composable_functions(content)
        
        if not composable_funcs:
            return 0
        
        # Track which functions need fixing (work backwards to preserve positions)
        funcs_to_fix = []
        for func_start, brace_pos, func_name in composable_funcs:
            if function_uses_localize_helper(content, brace_pos):
                if not function_has_localize_helper_declaration(content, brace_pos):
                    funcs_to_fix.append((brace_pos, func_name))
        
        if not funcs_to_fix:
            return 0
        
        # Sort by position descending to insert from end to start
        funcs_to_fix.sort(key=lambda x: x[0], reverse=True)
        
        # Add LocalLocalizeHelper import if not present
        if 'import ireader.presentation.ui.core.theme.LocalLocalizeHelper' not in content:
            import_matches = list(re.finditer(r'^import\s+[^\n]+$', content, re.MULTILINE))
            if import_matches:
                last_import = import_matches[-1]
                insert_import_pos = last_import.end()
                content = (content[:insert_import_pos] + 
                         '\nimport ireader.presentation.ui.core.theme.LocalLocalizeHelper' +
                         content[insert_import_pos:])
                # Adjust positions for the insertion
                import_len = len('\nimport ireader.presentation.ui.core.theme.LocalLocalizeHelper')
                funcs_to_fix = [(pos + import_len, name) for pos, name in funcs_to_fix]
        
        # Add Res import if not present
        if 'Res.string' in content and 'import ireader.i18n.resources.Res' not in content:
            import_matches = list(re.finditer(r'^import\s+[^\n]+$', content, re.MULTILINE))
            if import_matches:
                last_import = import_matches[-1]
                insert_import_pos = last_import.end()
                content = (content[:insert_import_pos] + 
                         '\nimport ireader.i18n.resources.Res' +
                         content[insert_import_pos:])
                import_len = len('\nimport ireader.i18n.resources.Res')
                funcs_to_fix = [(pos + import_len, name) for pos, name in funcs_to_fix]
        
        # Insert localizeHelper declaration into each function
        for brace_pos, func_name in funcs_to_fix:
            # Find indentation of next line
            next_line_start = content.find('\n', brace_pos) + 1
            if next_line_start > 0:
                indent = 0
                while next_line_start + indent < len(content) and content[next_line_start + indent] == ' ':
                    indent += 1
                indent_str = ' ' * indent
            else:
                indent_str = '    '
            
            localize_helper_code = f'\n{indent_str}val localizeHelper = requireNotNull(LocalLocalizeHelper.current) {{ "LocalLocalizeHelper not provided" }}'
            
            insert_pos = brace_pos + 1
            content = content[:insert_pos] + localize_helper_code + content[insert_pos:]
        
        # Write back
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        return len(funcs_to_fix)
        
    except Exception as e:
        print(f"  Error processing {file_path}: {e}")
        return 0

def main():
    project_root = Path('.')
    presentation_path = project_root / 'presentation'
    
    print("Fixing localizeHelper imports...\n")
    
    total_fixed = 0
    fixed_files = []
    
    # Find all Kotlin files
    for kt_file in presentation_path.rglob('*.kt'):
        count = fix_file(kt_file)
        if count > 0:
            rel_path = kt_file.relative_to(project_root)
            print(f"Fixed: {rel_path} ({count} functions)")
            fixed_files.append((kt_file, count))
            total_fixed += count
    
    print(f"\n{'='*60}")
    print(f"Fixed {total_fixed} functions in {len(fixed_files)} files")
    print(f"{'='*60}")

if __name__ == '__main__':
    main()
