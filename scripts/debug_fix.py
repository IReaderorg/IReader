#!/usr/bin/env python3
import sys
import re
from pathlib import Path

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

file_path = Path('presentation/src/commonMain/kotlin/ireader/presentation/ui/book/components/ChapterBar.kt')

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

print(f"File has 'localizeHelper.localize': {'localizeHelper.localize' in content}")

# Find composable functions
pattern = re.compile(r'@Composable\s*\n?\s*(?:@\w+(?:\([^)]*\))?\s*\n?\s*)*(?:private\s+|internal\s+|public\s+)?fun\s+(\w+)', re.MULTILINE)

for match in pattern.finditer(content):
    func_name = match.group(1)
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
    
    brace_pos = content.find('{', pos)
    snippet = content[brace_pos:brace_pos + 500]
    has_decl = 'val localizeHelper' in snippet
    print(f"Function: {func_name}, brace_pos: {brace_pos}, has_decl: {has_decl}")
    print(f"  First 200 chars: {snippet[:200]}")
