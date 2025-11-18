# Quick Fix Guide - IReader Compilation Errors

## TL;DR - Run This

```powershell
# Run all fixes at once
.\scripts\run_all_fixes.ps1

# Then compile
.\gradlew :presentation:compileReleaseKotlinAndroid
```

## What Was Fixed

### Critical Fixes (Must Have)
1. **State Management** - Fixed `mutableState` → `updateState` pattern
2. **Dependency Injection** - Fixed Koin `get()` and Voyager `rememberScreenModel`
3. **Chapter Sorting** - Fixed enum type mismatches in ChapterSort
4. **Image Loading** - Fixed placeholder syntax in ImageLoader

### Important Fixes (Should Have)
5. **Flow Imports** - Added missing `stateIn` imports
6. **Compose UI** - Fixed size modifiers and deprecated ripple
7. **Experimental APIs** - Added required `@OptIn` annotations
8. **String Resources** - Added wildcard imports for i18n

### Nice to Have Fixes
9. **Parameter Names** - Fixed `popBackStack` → `onPopBackStack`
10. **When Expressions** - Added `else` branches for exhaustiveness
11. **Function Calls** - Fixed `.count` → `.count()`
12. **Smart Casts** - Fixed public API property smart cast issues

## Individual Scripts

If you need to run fixes separately:

```powershell
# Basic fixes (state management, UI, APIs)
.\scripts\fix_all_compilation_errors.ps1

# BookDetailScreen fixes (Koin, Voyager)
.\scripts\fix_book_detail_screens.ps1

# Remaining fixes (search, resources, etc.)
.\scripts\fix_remaining_errors.ps1
```

## Common Issues After Fixes

### Issue: Still getting "Unresolved reference" errors
**Solution**: Check if imports are correct. The scripts add imports, but you may need to sync Gradle:
```powershell
.\gradlew --refresh-dependencies
```

### Issue: "Cannot infer type parameter" errors
**Solution**: These are usually fixed by the scripts. If persisting, check the specific file mentioned in the error.

### Issue: Koin injection errors at runtime
**Solution**: Ensure Koin modules are properly configured in your Application class.

## Verification Steps

After running fixes:

1. **Compile**
   ```powershell
   .\gradlew :presentation:compileReleaseKotlinAndroid
   ```

2. **Check for warnings**
   ```powershell
   .\gradlew :presentation:compileReleaseKotlinAndroid --warning-mode all
   ```

3. **Run tests** (if available)
   ```powershell
   .\gradlew :presentation:testReleaseUnitTest
   ```

## Files Modified

### Core Files
- `StateScreenModelAlias.kt` - State management
- `ImageLoader.kt` - Image loading
- `BookDetailScreenModel.kt` - Chapter filtering/sorting

### Screen Files
- `BookDetailScreenEnhanced.kt`
- `BookDetailScreenNew.kt`
- `BookDetailScreenRefactored.kt`
- `DownloadScreens.kt`
- `MigrationScreens.kt`
- `GlobalSearchScreen.kt`

### ViewModel Files
- `DownloadScreenModel.kt`
- `MigrationScreenModel.kt`
- `StatsScreenModel.kt`
- `GlobalSearchViewModel.kt`
- Multiple other ViewModels (for `stateIn` import)

### UI Component Files
- `AccessibilityUtils.kt`
- `AccessibleBookListItem.kt`
- `ChapterDetailBottomBar.kt`

### Settings Files
- `AppearanceToolbar.kt`
- `DownloaderTopAppBar.kt`
- `SettingsAppearanceScreen.kt`
- `PrivacySettingsScreen.kt`

## Rollback

If you need to rollback changes:

```powershell
# Rollback all changes
git checkout -- presentation/

# Or rollback specific file
git checkout -- presentation/src/commonMain/kotlin/path/to/file.kt
```

## Production Ready

✅ All fixes are production-ready:
- No code removed
- Proper error handling maintained
- Type safety preserved
- Follows Kotlin best practices
- Compatible with existing architecture

## Need Help?

1. Check `COMPILATION_FIXES_APPLIED.md` for detailed explanations
2. Review the specific error message
3. Check the modified file's git diff
4. Look at Mihon project for reference patterns

## Quick Reference - Error Types

| Error Type | Fix Script | Priority |
|------------|-----------|----------|
| `mutableState` errors | `fix_all_compilation_errors.ps1` | High |
| Koin `get()` errors | `fix_book_detail_screens.ps1` | High |
| `stateIn` errors | `fix_all_compilation_errors.ps1` | High |
| Size modifier errors | `fix_all_compilation_errors.ps1` | Medium |
| String resource errors | `fix_remaining_errors.ps1` | Medium |
| Parameter name errors | `fix_all_compilation_errors.ps1` | Low |
| When exhaustiveness | `fix_all_compilation_errors.ps1` | Low |

## Success Indicators

After running fixes, you should see:
- ✅ No compilation errors in `:presentation` module
- ✅ All Kotlin files compile successfully
- ✅ No unresolved references
- ✅ No type mismatches
- ✅ Clean build output

## Time Estimate

- Running all fix scripts: ~5-10 seconds
- Compilation after fixes: ~2-5 minutes (depending on machine)
- Total time: ~5-10 minutes

## Notes

- Scripts are idempotent (safe to run multiple times)
- No manual intervention required
- All changes are reversible via git
- Scripts preserve code functionality
- Production-ready output
