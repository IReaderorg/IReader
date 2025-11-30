#!/usr/bin/env python3
"""
Iterative I18n Fixer
Runs i18n replacement, builds, checks errors, fixes imports, and repeats until successful.
"""

import subprocess
import re
import sys
from pathlib import Path
from typing import Set, List

class IterativeI18nFixer:
    def __init__(self, project_root: str = '.'):
        if sys.platform == 'win32':
            sys.stdout.reconfigure(encoding='utf-8')
            sys.stderr.reconfigure(encoding='utf-8')
        self.project_root = Path(project_root)
        self.max_iterations = 10
        
    def run_script(self, script_name: str) -> tuple[bool, str]:
        """Run a Python script and return success status and output"""
        parts = script_name.split()
        script = parts[0]
        args = parts[1:]
        
        cmd = ['python', f'scripts/{script}'] + args
        
        result = subprocess.run(
            cmd,
            cwd=self.project_root,
            capture_output=True,
            text=True,
            encoding='utf-8',
            errors='replace'
        )
        return result.returncode == 0, result.stdout + result.stderr
        
    def run_i18n_replacement(self):
        """Run the batch i18n replacer"""
        print("\n" + "="*60)
        print("[Step 1] Running i18n string replacement...")
        print("="*60)
        
        success, output = self.run_script('batch_i18n_replacer.py --execute')
        print(output)
        return success
    
    def fix_duplicate_keys(self):
        """Fix duplicate keys in strings.xml"""
        print("\n[Step 2] Fixing duplicate keys in strings.xml...")
        success, output = self.run_script('fix_duplicate_keys.py')
        print(output)
        return success
    
    def fix_invalid_keys(self):
        """Fix invalid string keys (starting with numbers)"""
        print("\n[Step 3] Fixing invalid string keys...")
        success, output = self.run_script('fix_invalid_keys.py')
        print(output)
        return success
    
    def fix_localize_helper(self):
        """Add localizeHelper declarations to composable functions"""
        print("\n[Step 4] Adding localizeHelper declarations...")
        success, output = self.run_script('fix_localize_helper.py')
        print(output)
        return success
    
    def fix_res_import(self):
        """Fix Res imports to use wildcard import"""
        print("\n[Step 5] Fixing Res imports...")
        success, output = self.run_script('fix_res_import.py')
        print(output)
        return success
    
    def add_res_import(self):
        """Add missing Res imports"""
        print("\n[Step 6] Adding missing Res imports...")
        success, output = self.run_script('add_res_import.py')
        print(output)
        return success
    
    def try_build(self) -> tuple[bool, str]:
        """Try to build and return success status and error output"""
        print("\n" + "="*60)
        print("[Build] Attempting build...")
        print("="*60)
        
        # Use gradlew.bat on Windows
        gradlew = '.\\gradlew.bat' if sys.platform == 'win32' else './gradlew'
        
        result = subprocess.run(
            [gradlew, 'compileKotlin', '--no-daemon'],
            cwd=self.project_root,
            capture_output=True,
            text=True,
            timeout=600,
            encoding='utf-8',
            errors='replace',
            shell=True
        )
        
        if result.returncode == 0:
            print("[SUCCESS] Build successful!")
            return True, ""
        else:
            print("[FAILED] Build failed, analyzing errors...")
            return False, result.stdout + result.stderr
    
    def rollback_changes(self):
        """Rollback changes using git"""
        print("\n[Rolling back] Rolling back changes...")
        subprocess.run(
            ['git', 'checkout', '--', 'presentation/', 'i18n/src/commonMain/composeResources/values/strings.xml'],
            cwd=self.project_root,
            capture_output=True
        )
        print("[Rolled back] Changes rolled back")
    
    def run(self):
        """Main execution flow"""
        print("\n" + "="*60)
        print("[START] Starting Iterative I18n Fixer")
        print("="*60)
        
        # Step 1: Run i18n replacement
        if not self.run_i18n_replacement():
            print("[FAILED] Failed to run i18n replacement")
            return False
        
        # Step 2: Fix duplicate keys
        self.fix_duplicate_keys()
        
        # Step 3: Fix invalid keys (starting with numbers)
        self.fix_invalid_keys()
        
        # Step 4: Add localizeHelper declarations
        self.fix_localize_helper()
        
        # Step 5: Fix Res imports (use wildcard)
        self.fix_res_import()
        
        # Step 6: Add missing Res imports
        self.add_res_import()
        
        # Step 7: Try to build
        for iteration in range(1, self.max_iterations + 1):
            print(f"\n\n{'#'*60}")
            print(f"# BUILD ATTEMPT {iteration}/{self.max_iterations}")
            print(f"{'#'*60}")
            
            build_success, build_output = self.try_build()
            
            if build_success:
                print("\n" + "="*60)
                print("[SUCCESS] Build completed successfully!")
                print("="*60)
                print("\n[SUCCESS] All hardcoded strings have been replaced with i18n!")
                print("[SUCCESS] All necessary imports have been added!")
                print("\n[Note] Next steps:")
                print("  1. Review the changes: git diff")
                print("  2. Test the application")
                print("  3. Commit the changes")
                return True
            
            # Check for remaining errors
            unresolved = re.findall(r"Unresolved reference[:\s]+['\"]?(\w+)['\"]?", build_output, re.IGNORECASE)
            
            if 'localizeHelper' in unresolved:
                print("\n[Fixing] Found missing localizeHelper declarations...")
                self.fix_localize_helper()
                continue
            
            if not unresolved:
                # Check for other errors
                if 'Only safe (?.) or non-null asserted' in build_output:
                    print("\n[Warning] Null safety errors detected. Manual fix may be required.")
                elif 'Conflicting declarations' in build_output:
                    print("\n[Warning] Conflicting declarations detected. Manual fix may be required.")
                
                print("\nShowing last 30 lines of build output:")
                print("-" * 60)
                lines = build_output.split('\n')
                for line in lines[-30:]:
                    print(line)
                
                print("\n[FAILED] Cannot automatically fix the remaining errors.")
                return False
            
            print(f"\n[Found] Unresolved references: {set(unresolved)}")
            print("[FAILED] Cannot automatically fix these errors.")
            return False
        
        print("\n" + "="*60)
        print(f"[FAILED] Max iterations ({self.max_iterations}) reached without success")
        print("="*60)
        return False


def main():
    fixer = IterativeI18nFixer()
    success = fixer.run()
    sys.exit(0 if success else 1)


if __name__ == '__main__':
    main()
