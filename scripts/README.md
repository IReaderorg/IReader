# I18n String Replacement Scripts

This directory contains Python scripts to automate the process of replacing hardcoded strings with i18n localization calls in the IReader project.

## Scripts

### 1. `batch_i18n_replacer.py` (Recommended)

The main script for batch processing multiple files. It provides comprehensive scanning, reporting, and replacement capabilities.

#### Features
- 🔍 Scans entire directories for hardcoded strings
- 📊 Provides detailed summary reports
- 🎯 Smart string detection (skips URLs, single characters, etc.)
- 🔄 Reuses existing i18n keys when possible
- ✅ Dry-run mode to preview changes
- 📝 Automatically updates `strings.xml`

#### Usage

**Dry run (preview changes):**
```bash
python scripts/batch_i18n_replacer.py --dry-run
```

**Apply changes to entire presentation directory:**
```bash
python scripts/batch_i18n_replacer.py --execute
```

**Process specific directory (dry run):**
```bash
python scripts/batch_i18n_replacer.py --directory presentation/src/desktopMain --dry-run
```

**Process specific directory (execute):**
```bash
python scripts/batch_i18n_replacer.py --directory presentation/src/commonMain --execute
```

#### What it detects

The script finds and replaces these patterns:
- `Text("hardcoded string")` → `Text(localizeHelper.localize(Res.string.key))`
- `title = { Text("...") }` → `title = { Text(localizeHelper.localize(Res.string.key)) }`
- `label = { Text("...") }` → `label = { Text(localizeHelper.localize(Res.string.key)) }`
- `placeholder = { Text("...") }` → Similar replacement
- `contentDescription = "..."` → `contentDescription = localizeHelper.localize(Res.string.key)`
- `TitleText("...")` → `TitleText(localizeHelper.localize(Res.string.key))`

#### What it skips

The script intelligently skips:
- URLs (http://, https://)
- Single characters (+, -, etc.)
- Pure numbers
- Strings with variable interpolation (${ })
- Very short strings (< 2 characters)
- Lines already using localization
- Comments

### 2. `i18n_replacer.py`

A simpler version for processing individual files or directories.

#### Usage

**Process single file (dry run):**
```bash
python scripts/i18n_replacer.py --file path/to/file.kt --dry-run
```

**Process single file (execute):**
```bash
python scripts/i18n_replacer.py --file path/to/file.kt --execute
```

**Process directory:**
```bash
python scripts/i18n_replacer.py --directory presentation/src/commonMain --execute
```

## Workflow

### Recommended Workflow

1. **First, run a dry-run to see what will be changed:**
   ```bash
   python scripts/batch_i18n_replacer.py --dry-run
   ```

2. **Review the output carefully:**
   - Check the summary statistics
   - Review sample replacements
   - Verify new string keys make sense

3. **Process a small subset first (e.g., one directory):**
   ```bash
   python scripts/batch_i18n_replacer.py --directory presentation/src/desktopMain/kotlin/ireader/presentation/ui/settings --execute
   ```

4. **Build and test:**
   ```bash
   ./gradlew build
   ```

5. **If successful, process more directories:**
   ```bash
   python scripts/batch_i18n_replacer.py --directory presentation/src/commonMain --execute
   ```

6. **Finally, process everything:**
   ```bash
   python scripts/batch_i18n_replacer.py --execute
   ```

7. **Review and commit changes:**
   ```bash
   git diff
   git add .
   git commit -m "chore: replace hardcoded strings with i18n"
   ```

## Output Files

The scripts will:
1. Modify `.kt` files in place (only with `--execute`)
2. Update `i18n/src/commonMain/resources/MR/en/strings.xml` with new strings

## Key Generation

String keys are generated automatically:
- Converted to lowercase
- Special characters removed
- Spaces replaced with underscores
- Limited to ~50 characters
- Made unique with numeric suffixes if needed

Examples:
- `"TTS Performance Settings"` → `tts_performance_settings`
- `"Sign in to ChatGPT"` → `sign_in_to_chatgpt`
- `"Maximum Concurrent Processes"` → `maximum_concurrent_processes`

## Requirements

- Python 3.6+
- No external dependencies (uses only standard library)

## Troubleshooting

### Script doesn't find strings.xml
Make sure you're running from the project root:
```bash
cd /path/to/IReader
python scripts/batch_i18n_replacer.py --dry-run
```

Or specify the project root:
```bash
python scripts/batch_i18n_replacer.py --project-root /path/to/IReader --dry-run
```

### Build errors after replacement
1. Check that `localizeHelper` is available in the file's scope
2. Verify `Res.string.*` imports are correct
3. Review the replacements - some complex patterns may need manual adjustment

### Strings not being detected
The script is conservative to avoid false positives. You may need to:
1. Manually handle complex string interpolations
2. Review skipped patterns in the code
3. Adjust the regex patterns if needed

## Manual Review Needed

After running the script, manually review:
1. Strings with variable interpolation
2. Multi-line strings
3. Strings in annotations
4. Format strings with placeholders

## Contributing

To improve the scripts:
1. Add new patterns to detect in `find_hardcoded_strings_in_file()`
2. Improve key generation in `string_to_key()`
3. Add more skip patterns in `should_skip_string()`
4. Enhance replacement logic in `generate_replacement()`
