# I18n String Replacement - Quick Start Guide

## Summary

I've created automated Python scripts to replace hardcoded strings with i18n localization calls across your IReader project. The scripts found **835 hardcoded strings** in **171 files** that need to be localized.

## What Was Created

### 1. **batch_i18n_replacer.py** (Main Script - Recommended)
   - **Location**: `scripts/batch_i18n_replacer.py`
   - Comprehensive batch processing with detailed reporting
   - Smart detection and replacement
   - Automatically updates `strings.xml`

### 2. **i18n_replacer.py** (Simple Version)
   - **Location**: `scripts/i18n_replacer.py`
   - For processing individual files
   - Simpler interface

### 3. **README.md**
   - **Location**: `scripts/README.md`
   - Complete documentation with examples

## Current Status

✅ **Scanned**: 171 files with hardcoded strings  
✅ **Found**: 835 hardcoded strings to replace  
✅ **Loaded**: 800 existing i18n strings from `strings.xml`  
✅ **Will Reuse**: 449 existing strings  
✅ **Will Create**: 386 new string keys  

## Quick Start

### Step 1: Preview Changes (Dry Run)
```bash
python scripts/batch_i18n_replacer.py --dry-run
```

This shows you what will be changed without making any modifications.

### Step 2: Process a Small Directory First (Test)
```bash
python scripts/batch_i18n_replacer.py --directory presentation/src/desktopMain/kotlin/ireader/presentation/ui/settings --execute
```

### Step 3: Build and Test
```bash
./gradlew build
```

### Step 4: If Successful, Process Everything
```bash
python scripts/batch_i18n_replacer.py --execute
```

## What the Script Does

### Detects These Patterns:
- `Text("hardcoded")` → `Text(localizeHelper.localize(Res.string.key))`
- `title = { Text("...") }` → `title = { Text(localizeHelper.localize(Res.string.key)) }`
- `label = { Text("...") }` → Similar replacement
- `placeholder = { Text("...") }` → Similar replacement
- `contentDescription = "..."` → `contentDescription = localizeHelper.localize(Res.string.key)`
- `TitleText("...")` → `TitleText(localizeHelper.localize(Res.string.key))`

### Intelligently Skips:
- URLs (http://, https://)
- Single characters (+, -, etc.)
- Pure numbers
- Strings with variable interpolation (${ })
- Very short strings (< 2 characters)
- Lines already using localization
- Comments

### Generates Keys Automatically:
- `"TTS Performance Settings"` → `tts_performance_settings`
- `"Sign in to ChatGPT"` → `sign_in_to_chatgpt`
- `"Maximum Concurrent Processes"` → `maximum_concurrent_processes`

## Top Files with Most Hardcoded Strings

Based on the scan, here are the files with the most hardcoded strings:

1. **TTSEngineManagerScreen.kt** - Desktop TTS settings
2. **TTSEngineSettingsScreen.kt** - Android TTS settings  
3. **VoiceModelManagementPanel.kt** - Voice model management
4. **TTSSettingsPanel.kt** - TTS settings panel
5. **DesktopTTSControlPanel.kt** - Desktop TTS controls

## Recommended Workflow

### Option A: Process Everything at Once (Fast)
```bash
# 1. Review what will change
python scripts/batch_i18n_replacer.py --dry-run

# 2. Apply all changes
python scripts/batch_i18n_replacer.py --execute

# 3. Build and test
./gradlew build
```

### Option B: Process Incrementally (Safer)
```bash
# 1. Process desktop UI first
python scripts/batch_i18n_replacer.py --directory presentation/src/desktopMain --execute
./gradlew build

# 2. Process common UI
python scripts/batch_i18n_replacer.py --directory presentation/src/commonMain --execute
./gradlew build

# 3. Process Android UI
python scripts/batch_i18n_replacer.py --directory presentation/src/androidMain --execute
./gradlew build
```

## Files Modified

When you run with `--execute`, the script will:
1. Modify `.kt` files in place (replaces hardcoded strings)
2. Update `i18n/src/commonMain/composeResources/values/strings.xml` (adds new string entries)

## After Running

1. **Review the changes**:
   ```bash
   git diff
   ```

2. **Build the project**:
   ```bash
   ./gradlew build
   ```

3. **Test the application** to ensure all strings display correctly

4. **Commit the changes**:
   ```bash
   git add .
   git commit -m "chore: replace hardcoded strings with i18n localization"
   ```

## Troubleshooting

### Build Errors After Replacement
- Check that `localizeHelper` is available in the file's scope
- Verify `Res.string.*` imports are correct
- Some complex patterns may need manual adjustment

### Strings Not Being Detected
The script is conservative to avoid false positives. You may need to manually handle:
- Strings with variable interpolation
- Multi-line strings
- Strings in annotations
- Format strings with placeholders

## Manual Review Needed

After running the script, manually review:
1. Strings with variable interpolation (e.g., `"Downloaded ${count} files"`)
2. Multi-line strings
3. Strings in annotations
4. Format strings with placeholders

## Example Output

When you run the script, you'll see output like:
```
✓ Loaded 800 existing strings from strings.xml
🔍 Scanning for hardcoded strings in: presentation

============================================================
📊 SCAN SUMMARY
============================================================
Files with hardcoded strings: 171
Total hardcoded strings found: 835
New strings to add: 386
Existing strings to reuse: 449
============================================================

Top 10 files with most hardcoded strings:
  1. presentation/src/desktopMain/.../TTSEngineManagerScreen.kt: 45 strings
  2. presentation/src/androidMain/.../TTSEngineSettingsScreen.kt: 38 strings
  ...

🚀 Applying replacements...
✓ presentation/src/desktopMain/.../TTSPerformanceSettings.kt: 12 replacements
✓ presentation/src/desktopMain/.../TTSEngineManagerScreen.kt: 45 replacements
...

✅ Successfully replaced 835 hardcoded strings

📝 Adding 386 new strings to strings.xml
✓ Updated i18n/src/commonMain/composeResources/values/strings.xml

✅ Done! Don't forget to:
   1. Review the changes
   2. Run a build to check for errors
   3. Commit the changes
```

## Need Help?

- Check `scripts/README.md` for detailed documentation
- Run with `--help` flag: `python scripts/batch_i18n_replacer.py --help`
- Review the script code for customization options

## Next Steps

1. **Run a dry-run** to see what will change
2. **Choose your approach** (all at once or incremental)
3. **Execute the script**
4. **Build and test**
5. **Commit your changes**

Good luck with the localization! 🚀
