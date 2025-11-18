# IReader Compilation Fix Scripts

This directory contains PowerShell scripts to automatically fix common compilation errors in the IReader project.

## Available Scripts

### 1. apply_all_fixes.ps1 (Master Script)
**Purpose**: Runs all fixes in the correct order.

**Usage**:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\apply_all_fixes.ps1
```

**What it does**:
- Fixes mutableState.value usage
- Fixes StateFlow conversions
- Fixes Settings screen parameters
- Adds missing imports

---

### 2. fix_mutablestate_usage.ps1
**Purpose**: Converts Voyager-style state updates to IReaderStateScreenModel pattern.

**Usage**:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fix_mutablestate_usage.ps1
```

**Fixes**:
- `mutableState.value = mutableState.value.copy(...)` → `updateState { it.copy(...) }`
- `mutableState.value.property` → `state.value.property`

**Target Files**:
- DownloadScreenModel.kt
- MigrationScreenModel.kt
- StatsScreenModel.kt

---

### 3. fix_stateflow_errors.ps1
**Purpose**: Fixes `.asStateFlow()` calls without parameters.

**Usage**:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fix_stateflow_errors.ps1
```

**Fixes**:
- `.asStateFlow()` → `.stateIn(scope)`

**Target**: All ViewModel files in presentation module

---

### 4. fix_major_errors.ps1
**Purpose**: Fixes the most critical compilation errors.

**Usage**:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fix_major_errors.ps1
```

**Fixes**:
- StateScreenModel imports
- Settings screen parameters
- Preference.map() calls

---

### 5. batch_fix_errors.ps1
**Purpose**: Batch processing for common error patterns.

**Usage**:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\batch_fix_errors.ps1
```

**Fixes**:
- asStateFlow() calls
- map() on Preference objects
- onPopBackStack parameters

---

### 6. fix_common_compilation_errors.ps1
**Purpose**: Comprehensive fix for multiple error patterns.

**Usage**:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fix_common_compilation_errors.ps1
```

**Fixes**:
- StateScreenModel imports
- asStateFlow() calls
- onPopBackStack parameters
- .map() on Preferences

---

## Quick Start

### Fix Everything
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\apply_all_fixes.ps1
```

### Fix Specific Issues

**StateFlow errors only**:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fix_stateflow_errors.ps1
```

**MutableState pattern only**:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fix_mutablestate_usage.ps1
```

**Critical errors only**:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\fix_major_errors.ps1
```

## Script Features

### Safety
- ✅ All scripts are idempotent (safe to run multiple times)
- ✅ No files are deleted
- ✅ Only modifies files that need changes
- ✅ Preserves file formatting

### Reporting
- Shows which files were modified
- Counts total fixes applied
- Color-coded output for easy reading
- Execution time tracking

### Error Handling
- Checks if files exist before modifying
- Validates patterns before replacement
- Reports errors clearly

## Common Issues

### Execution Policy Error
**Error**: "running scripts is disabled on this system"

**Solution**: Use `-ExecutionPolicy Bypass` flag:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\script_name.ps1
```

### No Changes Made
**Possible Reasons**:
1. Files already fixed
2. Pattern doesn't match exactly
3. Files in different location

**Solution**: Check the script output for details

### Permission Denied
**Solution**: Run PowerShell as Administrator

## Pattern Reference

### StateFlow Conversion
```kotlin
// Before
val pref: StateFlow<T> = preferenceStore.getType("key", default).asStateFlow()

// After
val pref: StateFlow<T> = preferenceStore.getType("key", default).stateIn(scope)
```

### MutableState Pattern
```kotlin
// Before
mutableState.value = mutableState.value.copy(field = value)

// After
updateState { it.copy(field = value) }
```

### Settings Parameters
```kotlin
// Before
onPopBackStack = { }

// After
popBackStack = { }
```

## Documentation

For detailed information about the fixes, see:
- `../COMPILATION_FIXES_SUMMARY.md` - Complete fix documentation
- `../FIXES_COMPLETED.md` - Summary of completed fixes
- `../STATEFLOW_FIX_SUMMARY.md` - StateFlow-specific documentation
- `../TEST_MOCK_FIXES.md` - All fixes including test mocks

## Contributing

When adding new scripts:
1. Follow the naming convention: `fix_<category>_<issue>.ps1`
2. Add color-coded output
3. Make scripts idempotent
4. Document in this README
5. Test on sample files first

## Support

If scripts don't work as expected:
1. Check the error message
2. Verify file paths are correct
3. Review the pattern being matched
4. Check documentation for manual fix instructions
5. Some errors require manual intervention

## Version History

- **v1.0** - Initial scripts for StateFlow and MutableState fixes
- **v1.1** - Added master script and batch processing
- **v1.2** - Enhanced error reporting and documentation

---

**Note**: These scripts are designed for the IReader project structure. Modify paths if your project structure differs.
