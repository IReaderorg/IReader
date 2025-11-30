#!/usr/bin/env python3
"""
I18n String Replacer Script
Automatically finds hardcoded strings in Kotlin files and replaces them with i18n strings.
"""

import re
import os
import sys
from pathlib import Path
from typing import List, Tuple, Dict, Set
import xml.etree.ElementTree as ET

class I18nReplacer:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.strings_xml_path = self.project_root / "i18n" / "src" / "commonMain" / "composeResources" / "values" / "strings.xml"
        self.presentation_path = self.project_root / "presentation"
        self.existing_strings: Dict[str, str] = {}
        self.new_strings: Dict[str, str] = {}
        
    def load_existing_strings(self):
        """Load existing strings from strings.xml"""
        if not self.strings_xml_path.exists():
            print(f"Warning: strings.xml not found at {self.strings_xml_path}")
            return
        
        try:
            tree = ET.parse(self.strings_xml_path)
            root = tree.getroot()
            
            for string_elem in root.findall('string'):
                name = string_elem.get('name')
                value = string_elem.text or ""
                self.existing_strings[value] = name
                
            print(f"Loaded {len(self.existing_strings)} existing strings from strings.xml")
        except Exception as e:
            print(f"Error loading strings.xml: {e}")
    
    def string_to_key(self, text: str) -> str:
        """Convert a string to a valid i18n key"""
        # Remove special characters and convert to snake_case
        key = re.sub(r'[^\w\s-]', '', text.lower())
        key = re.sub(r'[-\s]+', '_', key)
        key = key.strip('_')
        
        # Limit length
        if len(key) > 50:
            words = key.split('_')
            key = '_'.join(words[:5])  # Take first 5 words
        
        return key
    
    def should_skip_string(self, text: str) -> bool:
        """Determine if a string should be skipped (not localized)"""
        skip_patterns = [
            r'^[+-]$',  # Single +/- characters
            r'^https?://',  # URLs
            r'^[0-9]+$',  # Pure numbers
            r'^[a-zA-Z]$',  # Single letters
            r'^\$\{.*\}$',  # Template strings
            r'^\.\.\.+$',  # Ellipsis
            r'^[,.:;!?]+$',  # Punctuation only
            r'^\s*$',  # Empty or whitespace only
        ]
        
        for pattern in skip_patterns:
            if re.match(pattern, text):
                return True
        
        # Skip very short strings (less than 2 chars)
        if len(text.strip()) < 2:
            return True
            
        return False
    
    def find_hardcoded_strings(self, file_path: Path) -> List[Tuple[int, str, str]]:
        """Find all hardcoded strings in a Kotlin file"""
        results = []
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                lines = f.readlines()
            
            # Pattern to match Text("...") and similar patterns
            patterns = [
                r'Text\s*\(\s*"([^"]+)"\s*\)',
                r'title\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}',
                r'label\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}',
                r'placeholder\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}',
                r'text\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}',
                r'contentDescription\s*=\s*"([^"]+)"',
            ]
            
            for line_num, line in enumerate(lines, 1):
                # Skip lines that already use localization
                if 'localizeHelper.localize' in line or 'Res.string.' in line:
                    continue
                
                # Skip comments
                if line.strip().startswith('//'):
                    continue
                
                for pattern in patterns:
                    matches = re.finditer(pattern, line)
                    for match in matches:
                        text = match.group(1)
                        if not self.should_skip_string(text):
                            results.append((line_num, text, match.group(0)))
        
        except Exception as e:
            print(f"Error reading {file_path}: {e}")
        
        return results
    
    def get_or_create_key(self, text: str) -> str:
        """Get existing key or create a new one for the text"""
        # Check if we already have this string
        if text in self.existing_strings:
            return self.existing_strings[text]
        
        # Check if we've already created a key for this in this session
        if text in self.new_strings:
            return self.new_strings[text]
        
        # Create a new key
        base_key = self.string_to_key(text)
        key = base_key
        counter = 1
        
        # Ensure uniqueness
        all_keys = set(self.existing_strings.values()) | set(self.new_strings.values())
        while key in all_keys:
            key = f"{base_key}_{counter}"
            counter += 1
        
        self.new_strings[text] = key
        return key
    
    def replace_in_file(self, file_path: Path, dry_run: bool = True) -> int:
        """Replace hardcoded strings in a file with i18n calls"""
        hardcoded_strings = self.find_hardcoded_strings(file_path)
        
        if not hardcoded_strings:
            return 0
        
        print(f"\n{'[DRY RUN] ' if dry_run else ''}Processing: {file_path.relative_to(self.project_root)}")
        print(f"Found {len(hardcoded_strings)} hardcoded strings")
        
        if dry_run:
            for line_num, text, original in hardcoded_strings:
                key = self.get_or_create_key(text)
                print(f"  Line {line_num}: \"{text}\" -> Res.string.{key}")
            return len(hardcoded_strings)
        
        # Read the file
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Replace strings
        replacements_made = 0
        for line_num, text, original in hardcoded_strings:
            key = self.get_or_create_key(text)
            
            # Different replacement patterns based on the original pattern
            if 'Text("' in original:
                # Simple Text("...") -> Text(localizeHelper.localize(Res.string.key))
                new_text = f'Text(localizeHelper.localize(Res.string.{key}))'
                content = content.replace(original, new_text, 1)
            elif 'contentDescription' in original:
                # contentDescription = "..." -> contentDescription = localizeHelper.localize(Res.string.key)
                new_text = f'contentDescription = localizeHelper.localize(Res.string.{key})'
                content = content.replace(original, new_text, 1)
            
            replacements_made += 1
            print(f"  ✓ Replaced: \"{text}\" -> Res.string.{key}")
        
        # Write back to file
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        return replacements_made
    
    def update_strings_xml(self):
        """Add new strings to strings.xml"""
        if not self.new_strings:
            print("\nNo new strings to add to strings.xml")
            return
        
        print(f"\nAdding {len(self.new_strings)} new strings to strings.xml")
        
        try:
            # Parse existing XML
            tree = ET.parse(self.strings_xml_path)
            root = tree.getroot()
            
            # Add new strings
            for text, key in sorted(self.new_strings.items(), key=lambda x: x[1]):
                string_elem = ET.SubElement(root, 'string')
                string_elem.set('name', key)
                string_elem.text = text
                print(f"  + {key}: {text}")
            
            # Write back with proper formatting
            self._write_formatted_xml(tree, self.strings_xml_path)
            print(f"\n✓ Updated {self.strings_xml_path}")
            
        except Exception as e:
            print(f"Error updating strings.xml: {e}")
    
    def _write_formatted_xml(self, tree: ET.ElementTree, file_path: Path):
        """Write XML with proper indentation"""
        # Convert to string
        xml_str = ET.tostring(tree.getroot(), encoding='unicode')
        
        # Add proper indentation
        lines = []
        indent_level = 0
        for line in xml_str.split('>'):
            if not line.strip():
                continue
            
            if line.startswith('</'):
                indent_level -= 1
            
            lines.append('    ' * indent_level + line.strip() + '>')
            
            if not line.startswith('</') and not line.endswith('/>'):
                indent_level += 1
        
        # Write to file
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write('\n'.join(lines))
    
    def process_directory(self, directory: Path, dry_run: bool = True, file_pattern: str = "*.kt"):
        """Process all Kotlin files in a directory"""
        total_replacements = 0
        files_processed = 0
        
        for kt_file in directory.rglob(file_pattern):
            if kt_file.is_file():
                count = self.replace_in_file(kt_file, dry_run)
                if count > 0:
                    total_replacements += count
                    files_processed += 1
        
        print(f"\n{'[DRY RUN] ' if dry_run else ''}Summary:")
        print(f"  Files processed: {files_processed}")
        print(f"  Total replacements: {total_replacements}")
        print(f"  New strings to add: {len(self.new_strings)}")
        
        if not dry_run:
            self.update_strings_xml()
    
    def process_single_file(self, file_path: str, dry_run: bool = True):
        """Process a single file"""
        path = Path(file_path)
        if not path.exists():
            print(f"Error: File not found: {file_path}")
            return
        
        count = self.replace_in_file(path, dry_run)
        
        if not dry_run and count > 0:
            self.update_strings_xml()


def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Replace hardcoded strings with i18n strings')
    parser.add_argument('--project-root', default='.', help='Project root directory')
    parser.add_argument('--file', help='Process a single file')
    parser.add_argument('--directory', help='Process all files in a directory')
    parser.add_argument('--dry-run', action='store_true', help='Show what would be changed without making changes')
    parser.add_argument('--execute', action='store_true', help='Actually make the changes')
    
    args = parser.parse_args()
    
    if not args.dry_run and not args.execute:
        print("Please specify either --dry-run or --execute")
        sys.exit(1)
    
    dry_run = args.dry_run
    
    replacer = I18nReplacer(args.project_root)
    replacer.load_existing_strings()
    
    if args.file:
        replacer.process_single_file(args.file, dry_run)
    elif args.directory:
        replacer.process_directory(Path(args.directory), dry_run)
    else:
        # Default: process presentation directory
        replacer.process_directory(replacer.presentation_path, dry_run)


if __name__ == '__main__':
    main()
