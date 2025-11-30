#!/usr/bin/env python3
"""
Batch I18n String Replacer
Process multiple files at once with progress tracking and better error handling.
"""

import re
import os
import sys
from pathlib import Path
from typing import List, Tuple, Dict, Set
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from collections import defaultdict

# Fix Windows encoding issues
if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

@dataclass
class StringReplacement:
    file_path: Path
    line_number: int
    original_text: str
    original_match: str
    key: str
    replacement: str

class BatchI18nReplacer:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.strings_xml_path = self.project_root / "i18n" / "src" / "commonMain" / "composeResources" / "values" / "strings.xml"
        self.presentation_path = self.project_root / "presentation"
        self.existing_strings: Dict[str, str] = {}
        self.new_strings: Dict[str, str] = {}
        self.replacements: List[StringReplacement] = []
        
    def load_existing_strings(self):
        """Load existing strings from strings.xml"""
        if not self.strings_xml_path.exists():
            print(f"⚠️  Warning: strings.xml not found at {self.strings_xml_path}")
            return
        
        try:
            tree = ET.parse(self.strings_xml_path)
            root = tree.getroot()
            
            for string_elem in root.findall('string'):
                name = string_elem.get('name')
                value = string_elem.text or ""
                self.existing_strings[value] = name
                
            print(f"✓ Loaded {len(self.existing_strings)} existing strings from strings.xml")
        except Exception as e:
            print(f"❌ Error loading strings.xml: {e}")
    
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
            r'^\$\{.*\}$',  # Template strings with variables
            r'^\.\.\.+$',  # Ellipsis
            r'^[,.:;!?]+$',  # Punctuation only
            r'^\s*$',  # Empty or whitespace only
            r'^/[a-z_]+$',  # Paths like /predict
        ]
        
        for pattern in skip_patterns:
            if re.match(pattern, text):
                return True
        
        # Skip very short strings (less than 2 chars)
        if len(text.strip()) < 2:
            return True
        
        # Skip strings with variable interpolation
        if '${' in text or '$' in text:
            return True
            
        return False
    
    def find_hardcoded_strings_in_file(self, file_path: Path) -> List[Tuple[int, str, str]]:
        """Find all hardcoded strings in a Kotlin file"""
        results = []
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                lines = content.split('\n')
            
            # Patterns to match various string usages - ORDER MATTERS!
            # More specific patterns (with = { Text) should come FIRST
            patterns = [
                (r'title\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}', 'title = {{ Text("{}") }}'),
                (r'label\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}', 'label = {{ Text("{}") }}'),
                (r'placeholder\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}', 'placeholder = {{ Text("{}") }}'),
                (r'text\s*=\s*\{\s*Text\s*\(\s*"([^"]+)"\s*\)\s*\}', 'text = {{ Text("{}") }}'),
                (r'contentDescription\s*=\s*"([^"]+)"', 'contentDescription = "{}"'),
                (r'text\s*=\s*"([^"]+)"', 'text = "{}"'),
                (r'title\s*=\s*"([^"]+)"', 'title = "{}"'),
                (r'label\s*=\s*"([^"]+)"', 'label = "{}"'),
                (r'TitleText\s*\(\s*"([^"]+)"\s*\)', 'TitleText("{}")'),
                # Simple Text("...") should be LAST to avoid matching inside other patterns
                (r'Text\s*\(\s*"([^"]+)"\s*\)', 'Text("{}")'),
            ]
            
            for line_num, line in enumerate(lines, 1):
                # Skip lines that already use localization
                if 'localizeHelper.localize' in line or 'Res.string.' in line:
                    continue
                
                # Skip comments
                stripped = line.strip()
                if stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
                    continue
                
                # Track which positions have been matched to avoid duplicates
                matched_positions = set()
                
                for pattern, _ in patterns:
                    matches = re.finditer(pattern, line)
                    for match in matches:
                        # Skip if this position was already matched by a more specific pattern
                        if match.start() in matched_positions:
                            continue
                        text = match.group(1)
                        if not self.should_skip_string(text):
                            results.append((line_num, text, match.group(0)))
                            # Mark all positions in this match as used
                            for pos in range(match.start(), match.end()):
                                matched_positions.add(pos)
        
        except Exception as e:
            print(f"❌ Error reading {file_path}: {e}")
        
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
    
    def generate_replacement(self, original: str, text: str, key: str) -> str:
        """Generate the replacement string based on the original pattern"""
        # Check more specific patterns first (with = { Text)
        if 'title = { Text("' in original or 'title = {Text("' in original:
            return f'title = {{ Text(localizeHelper.localize(Res.string.{key})) }}'
        elif 'label = { Text("' in original or 'label = {Text("' in original:
            return f'label = {{ Text(localizeHelper.localize(Res.string.{key})) }}'
        elif 'placeholder = { Text("' in original or 'placeholder = {Text("' in original:
            return f'placeholder = {{ Text(localizeHelper.localize(Res.string.{key})) }}'
        elif 'text = { Text("' in original or 'text = {Text("' in original:
            return f'text = {{ Text(localizeHelper.localize(Res.string.{key})) }}'
        elif 'text = "' in original:
            return f'text = localizeHelper.localize(Res.string.{key})'
        elif 'title = "' in original:
            return f'title = localizeHelper.localize(Res.string.{key})'
        elif 'label = "' in original:
            return f'label = localizeHelper.localize(Res.string.{key})'
        # Then check simple Text("...")
        elif 'Text("' in original:
            return f'Text(localizeHelper.localize(Res.string.{key}))'
        elif 'contentDescription' in original:
            return f'contentDescription = localizeHelper.localize(Res.string.{key})'
        elif 'TitleText("' in original:
            return f'TitleText(localizeHelper.localize(Res.string.{key}))'
        else:
            # Default replacement
            return original.replace(f'"{text}"', f'localizeHelper.localize(Res.string.{key})')
    
    def scan_files(self, directory: Path, file_pattern: str = "*.kt") -> Dict[Path, List[Tuple[int, str, str]]]:
        """Scan all files and collect hardcoded strings"""
        results = {}
        
        for kt_file in directory.rglob(file_pattern):
            if kt_file.is_file():
                hardcoded = self.find_hardcoded_strings_in_file(kt_file)
                if hardcoded:
                    results[kt_file] = hardcoded
        
        return results
    
    def prepare_replacements(self, scan_results: Dict[Path, List[Tuple[int, str, str]]]):
        """Prepare all replacements"""
        for file_path, strings in scan_results.items():
            for line_num, text, original in strings:
                key = self.get_or_create_key(text)
                replacement = self.generate_replacement(original, text, key)
                
                self.replacements.append(StringReplacement(
                    file_path=file_path,
                    line_number=line_num,
                    original_text=text,
                    original_match=original,
                    key=key,
                    replacement=replacement
                ))
    
    def apply_replacements(self):
        """Apply all prepared replacements"""
        files_by_path = defaultdict(list)
        for repl in self.replacements:
            files_by_path[repl.file_path].append(repl)
        
        total_replaced = 0
        for file_path, repls in files_by_path.items():
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # Sort by line number descending to avoid offset issues
                repls.sort(key=lambda x: x.line_number, reverse=True)
                
                for repl in repls:
                    # Replace first occurrence
                    if repl.original_match in content:
                        content = content.replace(repl.original_match, repl.replacement, 1)
                        total_replaced += 1
                
                # Add localizeHelper if needed
                if 'localizeHelper.localize' in content:
                    content = self._ensure_localize_helper(content)
                
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                
                print(f"✓ {file_path.relative_to(self.project_root)}: {len(repls)} replacements")
                
            except Exception as e:
                print(f"❌ Error processing {file_path}: {e}")
        
        return total_replaced
    
    def _ensure_localize_helper(self, content: str) -> str:
        """Ensure imports are added (localizeHelper declaration is handled by fix_localize_helper.py)"""
        # Add LocalLocalizeHelper import if not present
        if 'import ireader.presentation.ui.core.theme.LocalLocalizeHelper' not in content:
            # Find last import
            import_pattern = re.compile(r'^import\s+[^\n]+$', re.MULTILINE)
            imports = list(import_pattern.finditer(content))
            if imports:
                last_import = imports[-1]
                insert_pos = last_import.end()
                content = (content[:insert_pos] + 
                          '\nimport ireader.presentation.ui.core.theme.LocalLocalizeHelper' +
                          content[insert_pos:])
        
        # Add Res import if not present and Res.string is used
        if 'Res.string' in content and 'import ireader.i18n.resources.Res' not in content:
            import_pattern = re.compile(r'^import\s+[^\n]+$', re.MULTILINE)
            imports = list(import_pattern.finditer(content))
            if imports:
                last_import = imports[-1]
                insert_pos = last_import.end()
                content = (content[:insert_pos] + 
                          '\nimport ireader.i18n.resources.Res' +
                          content[insert_pos:])
        
        # NOTE: localizeHelper declaration is handled by fix_localize_helper.py
        # to properly handle multiple @Composable functions in a file
        
        return content
    
    def update_strings_xml(self):
        """Add new strings to strings.xml"""
        if not self.new_strings:
            print("\n✓ No new strings to add to strings.xml")
            return
        
        print(f"\n📝 Adding {len(self.new_strings)} new strings to strings.xml")
        
        try:
            tree = ET.parse(self.strings_xml_path)
            root = tree.getroot()
            
            # Add new strings sorted by key
            for text, key in sorted(self.new_strings.items(), key=lambda x: x[1]):
                string_elem = ET.SubElement(root, 'string')
                string_elem.set('name', key)
                string_elem.text = text
            
            # Format and write
            self._indent_xml(root)
            tree.write(self.strings_xml_path, encoding='utf-8', xml_declaration=False)
            
            print(f"✓ Updated {self.strings_xml_path.relative_to(self.project_root)}")
            
            # Print sample of new strings
            print("\nSample of new strings added:")
            for i, (text, key) in enumerate(sorted(self.new_strings.items(), key=lambda x: x[1])[:10]):
                print(f"  • {key}: \"{text}\"")
            if len(self.new_strings) > 10:
                print(f"  ... and {len(self.new_strings) - 10} more")
            
        except Exception as e:
            print(f"❌ Error updating strings.xml: {e}")
    
    def _indent_xml(self, elem, level=0):
        """Add proper indentation to XML"""
        indent = "\n" + "    " * level
        if len(elem):
            if not elem.text or not elem.text.strip():
                elem.text = indent + "    "
            if not elem.tail or not elem.tail.strip():
                elem.tail = indent
            for child in elem:
                self._indent_xml(child, level + 1)
            if not child.tail or not child.tail.strip():
                child.tail = indent
        else:
            if level and (not elem.tail or not elem.tail.strip()):
                elem.tail = indent
    
    def print_summary(self, scan_results: Dict[Path, List[Tuple[int, str, str]]]):
        """Print a summary of what will be changed"""
        total_strings = sum(len(strings) for strings in scan_results.values())
        
        print(f"\n{'='*60}")
        print(f"📊 SCAN SUMMARY")
        print(f"{'='*60}")
        print(f"Files with hardcoded strings: {len(scan_results)}")
        print(f"Total hardcoded strings found: {total_strings}")
        print(f"New strings to add: {len(self.new_strings)}")
        print(f"Existing strings to reuse: {total_strings - len(self.new_strings)}")
        print(f"{'='*60}\n")
        
        # Show files with most strings
        sorted_files = sorted(scan_results.items(), key=lambda x: len(x[1]), reverse=True)
        print("Top 10 files with most hardcoded strings:")
        for i, (file_path, strings) in enumerate(sorted_files[:10], 1):
            rel_path = file_path.relative_to(self.project_root)
            print(f"  {i}. {rel_path}: {len(strings)} strings")
    
    def run(self, directory: Path = None, dry_run: bool = True):
        """Main execution method"""
        if directory is None:
            directory = self.presentation_path
        
        print(f"🔍 Scanning for hardcoded strings in: {directory.relative_to(self.project_root)}\n")
        
        # Scan all files
        scan_results = self.scan_files(directory)
        
        if not scan_results:
            print("✓ No hardcoded strings found!")
            return
        
        # Prepare replacements
        self.prepare_replacements(scan_results)
        
        # Print summary
        self.print_summary(scan_results)
        
        if dry_run:
            print("\n🔍 DRY RUN MODE - No changes will be made")
            print("\nSample replacements (first 20):")
            for i, repl in enumerate(self.replacements[:20], 1):
                rel_path = repl.file_path.relative_to(self.project_root)
                print(f"\n{i}. {rel_path}:{repl.line_number}")
                print(f"   Original: {repl.original_match}")
                print(f"   Replace:  {repl.replacement}")
            
            if len(self.replacements) > 20:
                print(f"\n... and {len(self.replacements) - 20} more replacements")
            
            print("\n💡 To apply these changes, run with --execute flag")
        else:
            print("\n🚀 Applying replacements...")
            total = self.apply_replacements()
            print(f"\n✅ Successfully replaced {total} hardcoded strings")
            
            self.update_strings_xml()
            
            print("\n✅ Done! Don't forget to:")
            print("   1. Review the changes")
            print("   2. Run a build to check for errors")
            print("   3. Commit the changes")


def main():
    import argparse
    
    parser = argparse.ArgumentParser(
        description='Batch replace hardcoded strings with i18n strings',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Dry run on entire presentation directory
  python batch_i18n_replacer.py --dry-run
  
  # Apply changes to entire presentation directory
  python batch_i18n_replacer.py --execute
  
  # Dry run on specific directory
  python batch_i18n_replacer.py --directory presentation/src/desktopMain --dry-run
  
  # Apply changes to specific directory
  python batch_i18n_replacer.py --directory presentation/src/commonMain --execute
        """
    )
    
    parser.add_argument('--project-root', default='.', help='Project root directory (default: current directory)')
    parser.add_argument('--directory', help='Process files in specific directory (default: presentation/)')
    parser.add_argument('--dry-run', action='store_true', help='Show what would be changed without making changes')
    parser.add_argument('--execute', action='store_true', help='Actually make the changes')
    
    args = parser.parse_args()
    
    if not args.dry_run and not args.execute:
        parser.print_help()
        print("\n❌ Error: Please specify either --dry-run or --execute")
        sys.exit(1)
    
    if args.dry_run and args.execute:
        print("❌ Error: Cannot specify both --dry-run and --execute")
        sys.exit(1)
    
    replacer = BatchI18nReplacer(args.project_root)
    replacer.load_existing_strings()
    
    directory = Path(args.directory) if args.directory else None
    replacer.run(directory, dry_run=args.dry_run)


if __name__ == '__main__':
    main()
