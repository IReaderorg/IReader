#!/usr/bin/env python3
"""
Validate i18n String References
Finds Res.string.* references in code that don't exist in strings.xml
and either adds them or reverts to hardcoded strings.
"""

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from collections import defaultdict

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')


def load_existing_strings(xml_path: Path) -> set:
    """Load all string keys from strings.xml"""
    if not xml_path.exists():
        print(f"❌ strings.xml not found at {xml_path}")
        return set()
    
    tree = ET.parse(xml_path)
    root = tree.getroot()
    
    keys = set()
    for elem in root.findall('string'):
        name = elem.get('name')
        if name:
            keys.add(name)
    
    return keys


def find_string_references(file_path: Path) -> list:
    """Find all Res.string.* references in a file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
        return []
    
    # Find all Res.string.key_name references
    pattern = re.compile(r'Res\.string\.(\w+)')
    matches = []
    
    for match in pattern.finditer(content):
        key = match.group(1)
        matches.append((match.start(), match.end(), key))
    
    return matches


def scan_for_missing_strings(project_root: Path, existing_keys: set) -> dict:
    """Scan all Kotlin files for missing string references"""
    presentation_path = project_root / 'presentation'
    missing = defaultdict(list)
    
    # Scan all source sets (commonMain, desktopMain, androidMain, iosMain)
    for kt_file in presentation_path.rglob('*.kt'):
        refs = find_string_references(kt_file)
        for start, end, key in refs:
            if key not in existing_keys:
                rel_path = kt_file.relative_to(project_root)
                missing[key].append(str(rel_path))
    
    return missing


def add_missing_strings_to_xml(xml_path: Path, missing_keys: dict, dry_run: bool = True):
    """Add missing string keys to strings.xml with placeholder values"""
    if not missing_keys:
        return
    
    tree = ET.parse(xml_path)
    root = tree.getroot()
    
    # Generate placeholder values for missing keys
    new_strings = {}
    for key in missing_keys:
        # Convert key to readable text
        text = key.replace('_', ' ').title()
        new_strings[key] = text
    
    if dry_run:
        print(f"\nWould add {len(new_strings)} strings to strings.xml:")
        for key, text in sorted(new_strings.items()):
            print(f"  <string name=\"{key}\">{text}</string>")
        return
    
    # Add new strings
    for key, text in sorted(new_strings.items()):
        elem = ET.SubElement(root, 'string')
        elem.set('name', key)
        elem.text = text
    
    # Format and write
    indent_xml(root)
    tree.write(xml_path, encoding='utf-8', xml_declaration=False)
    
    print(f"✅ Added {len(new_strings)} strings to strings.xml")


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
    
    parser = argparse.ArgumentParser(description='Validate and fix i18n string references')
    parser.add_argument('--dry-run', action='store_true', help='Show missing strings without fixing')
    parser.add_argument('--execute', action='store_true', help='Add missing strings to strings.xml')
    
    args = parser.parse_args()
    
    if not args.dry_run and not args.execute:
        parser.print_help()
        print("\n❌ Please specify --dry-run or --execute")
        sys.exit(1)
    
    project_root = Path('.')
    xml_path = project_root / 'i18n' / 'src' / 'commonMain' / 'composeResources' / 'values' / 'strings.xml'
    
    print("Loading existing strings...")
    existing_keys = load_existing_strings(xml_path)
    print(f"✓ Found {len(existing_keys)} existing strings\n")
    
    print("Scanning for missing string references...")
    missing = scan_for_missing_strings(project_root, existing_keys)
    
    if not missing:
        print("✓ All string references are valid!")
        return
    
    print(f"\n❌ Found {len(missing)} missing string key(s):\n")
    for key, files in sorted(missing.items()):
        print(f"  {key}")
        for f in files[:3]:
            print(f"    - {f}")
        if len(files) > 3:
            print(f"    ... and {len(files) - 3} more files")
    
    add_missing_strings_to_xml(xml_path, missing, dry_run=args.dry_run)


if __name__ == '__main__':
    main()
