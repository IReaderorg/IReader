#!/usr/bin/env python3
"""
Remove Duplicate and Invalid Strings from strings.xml
Keeps the first occurrence of each string key and removes duplicates.
Also removes entries with empty or invalid keys.
"""

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from collections import OrderedDict

# Fix Windows encoding issues
if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')


def is_valid_key(name: str) -> bool:
    """Check if a string key is valid for Android/Compose resources."""
    if not name or name.strip() == '':
        return False
    # Must start with letter or underscore, contain only alphanumeric and underscore
    # And be at least 2 characters for meaningful keys
    if not re.match(r'^[a-zA-Z_][a-zA-Z0-9_]*$', name):
        return False
    if len(name) < 2:
        return False
    return True


def remove_duplicates(xml_path: Path, dry_run: bool = True) -> int:
    """Remove duplicate and invalid string entries from strings.xml.
    Returns the number of entries removed."""
    
    tree = ET.parse(xml_path)
    root = tree.getroot()
    
    seen = OrderedDict()
    duplicates = []
    invalid_keys = []
    
    for elem in root.findall('string'):
        name = elem.get('name')
        
        # Check for invalid keys
        if not is_valid_key(name):
            invalid_keys.append((name, elem.text))
            continue
            
        if name in seen:
            duplicates.append((name, elem.text, seen[name]))
        else:
            seen[name] = elem.text
    
    total_issues = len(duplicates) + len(invalid_keys)
    
    if total_issues == 0:
        print("✓ No duplicates or invalid keys found")
        return 0
    
    if invalid_keys:
        print(f"Found {len(invalid_keys)} invalid key(s):\n")
        for name, value in invalid_keys:
            print(f"  Invalid key: \"{name}\" -> \"{value}\"")
        print()
    
    if duplicates:
        print(f"Found {len(duplicates)} duplicate(s):\n")
        for name, dup_value, original_value in duplicates:
            print(f"  Key: {name}")
            print(f"    Keeping:  \"{original_value}\"")
            print(f"    Removing: \"{dup_value}\"\n")
    
    if dry_run:
        print("💡 Run with --execute to remove these entries")
        return total_issues
    
    # Remove invalid keys and duplicates
    seen_names = set()
    elements_to_remove = []
    
    for elem in root.findall('string'):
        name = elem.get('name')
        
        # Remove invalid keys
        if not is_valid_key(name):
            elements_to_remove.append(elem)
            continue
            
        # Remove duplicates (keep first)
        if name in seen_names:
            elements_to_remove.append(elem)
        else:
            seen_names.add(name)
    
    for elem in elements_to_remove:
        root.remove(elem)
    
    # Write back with proper formatting
    indent_xml(root)
    tree.write(xml_path, encoding='utf-8', xml_declaration=False)
    
    print(f"✅ Removed {total_issues} entries ({len(invalid_keys)} invalid, {len(duplicates)} duplicates)")
    return total_issues


def indent_xml(elem, level=0):
    """Add proper indentation to XML"""
    indent = "\n" + "    " * level
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = indent + "    "
        if not elem.tail or not elem.tail.strip():
            elem.tail = indent
        for child in elem:
            indent_xml(child, level + 1)
        if not child.tail or not child.tail.strip():
            child.tail = indent
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = indent


def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Remove duplicate strings from strings.xml')
    parser.add_argument('--dry-run', action='store_true', help='Show duplicates without removing')
    parser.add_argument('--execute', action='store_true', help='Actually remove duplicates')
    parser.add_argument('--file', default='i18n/src/commonMain/composeResources/values/strings.xml',
                        help='Path to strings.xml')
    
    args = parser.parse_args()
    
    if not args.dry_run and not args.execute:
        parser.print_help()
        print("\n❌ Please specify --dry-run or --execute")
        sys.exit(1)
    
    xml_path = Path(args.file)
    if not xml_path.exists():
        print(f"❌ File not found: {xml_path}")
        sys.exit(1)
    
    remove_duplicates(xml_path, dry_run=args.dry_run)


if __name__ == '__main__':
    main()
