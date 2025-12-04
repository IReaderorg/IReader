#!/usr/bin/env python3
"""
Source Migration Script for iOS Support

This script helps migrate existing IReader sources to support iOS via Kotlin/JS.

Usage:
    python migrate-source-to-js.py <source_path>
    
Example:
    python migrate-source-to-js.py ../IReader-extensions/sources/en/novelupdates
"""

import os
import sys
import re
from pathlib import Path

def migrate_imports(content: str) -> str:
    """Migrate Jsoup imports to Ksoup."""
    replacements = [
        # Jsoup to Ksoup
        (r'import org\.jsoup\.Jsoup', 'import com.fleeksoft.ksoup.Ksoup'),
        (r'import org\.jsoup\.nodes\.Document', 'import com.fleeksoft.ksoup.nodes.Document'),
        (r'import org\.jsoup\.nodes\.Element', 'import com.fleeksoft.ksoup.nodes.Element'),
        (r'import org\.jsoup\.select\.Elements', 'import com.fleeksoft.ksoup.select.Elements'),
        (r'import org\.jsoup\.', 'import com.fleeksoft.ksoup.'),
        
        # Jsoup.parse to Ksoup.parse
        (r'Jsoup\.parse\(', 'Ksoup.parse('),
        (r'Jsoup\.connect\(', 'Ksoup.parse('), # Note: connect needs different handling
    ]
    
    for pattern, replacement in replacements:
        content = re.sub(pattern, replacement, content)
    
    return content

def add_js_init_file(source_dir: Path, source_name: str, package_name: str):
    """Create jsMain init file for the source."""
    js_main_dir = source_dir / "src" / "jsMain" / "kotlin" / package_name.replace('.', '/')
    js_main_dir.mkdir(parents=True, exist_ok=True)
    
    init_content = f'''package {package_name}

import ireader.js.runtime.registerSource
import ireader.js.runtime.JsDependencies
import kotlin.js.JsExport

/**
 * Initialize {source_name} for iOS/JS runtime.
 * Call this after loading the JS file.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun init{source_name.replace(" ", "")}() {{
    registerSource("{source_name.lower().replace(" ", "-")}") {{ deps ->
        {source_name}(deps.toDependencies())
    }}
    console.log("{source_name} registered")
}}
'''
    
    init_file = js_main_dir / "Init.kt"
    with open(init_file, 'w') as f:
        f.write(init_content)
    
    print(f"Created: {init_file}")

def update_build_gradle(source_dir: Path):
    """Update build.gradle.kts to add JS target."""
    build_file = source_dir / "build.gradle.kts"
    
    if not build_file.exists():
        print(f"Warning: {build_file} not found")
        return
    
    with open(build_file, 'r') as f:
        content = f.read()
    
    # Check if JS target already exists
    if 'js(IR)' in content or 'js {' in content:
        print("JS target already configured")
        return
    
    # Add JS target configuration
    js_config = '''
    // JS target for iOS support
    js(IR) {
        browser {
            webpackTask {
                outputFileName = "${project.name}.js"
            }
        }
        binaries.executable()
    }
'''
    
    # Find kotlin { block and add JS target
    kotlin_block_pattern = r'(kotlin\s*\{)'
    if re.search(kotlin_block_pattern, content):
        # Add after kotlin {
        content = re.sub(
            kotlin_block_pattern,
            r'\1' + js_config,
            content
        )
    
    # Add jsMain dependencies
    js_deps = '''
        val jsMain by getting {
            dependencies {
                implementation(project(":source-runtime-js"))
            }
        }
'''
    
    # Find sourceSets block and add jsMain
    sourcesets_pattern = r'(sourceSets\s*\{)'
    if re.search(sourcesets_pattern, content):
        content = re.sub(
            sourcesets_pattern,
            r'\1' + js_deps,
            content
        )
    
    with open(build_file, 'w') as f:
        f.write(content)
    
    print(f"Updated: {build_file}")

def migrate_source_file(file_path: Path):
    """Migrate a single source file."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original = content
    content = migrate_imports(content)
    
    if content != original:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Migrated: {file_path}")
    else:
        print(f"No changes: {file_path}")

def find_source_class(source_dir: Path) -> tuple:
    """Find the main source class name and package."""
    for kt_file in source_dir.rglob("*.kt"):
        with open(kt_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Look for class extending SourceFactory
        match = re.search(r'class\s+(\w+).*:\s*SourceFactory', content)
        if match:
            class_name = match.group(1)
            
            # Get package
            pkg_match = re.search(r'package\s+([\w.]+)', content)
            package = pkg_match.group(1) if pkg_match else ""
            
            return class_name, package
    
    return None, None

def migrate_source(source_path: str):
    """Main migration function."""
    source_dir = Path(source_path)
    
    if not source_dir.exists():
        print(f"Error: {source_dir} does not exist")
        return
    
    print(f"Migrating source: {source_dir}")
    print("=" * 50)
    
    # Find source class
    source_name, package_name = find_source_class(source_dir)
    if not source_name:
        print("Warning: Could not find source class")
        source_name = source_dir.name.title()
        package_name = f"ireader.{source_dir.name}"
    
    print(f"Source: {source_name}")
    print(f"Package: {package_name}")
    print()
    
    # Migrate Kotlin files
    print("Migrating imports...")
    for kt_file in source_dir.rglob("*.kt"):
        if "jsMain" not in str(kt_file):
            migrate_source_file(kt_file)
    
    print()
    
    # Update build.gradle.kts
    print("Updating build configuration...")
    update_build_gradle(source_dir)
    
    print()
    
    # Create JS init file
    print("Creating JS init file...")
    add_js_init_file(source_dir, source_name, package_name)
    
    print()
    print("Migration complete!")
    print()
    print("Next steps:")
    print("1. Review the changes")
    print("2. Build with: ./gradlew :sources:<name>:jsBrowserProductionWebpack")
    print("3. Test the JS output")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    
    migrate_source(sys.argv[1])
