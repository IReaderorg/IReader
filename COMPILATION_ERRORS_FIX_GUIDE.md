# Compilation Errors Fix Guide

This document provides a comprehensive guide to fixing the compilation errors in the IReader project.

## Summary of Errors

The project has approximately 500+ compilation errors across multiple categories:

1. **Missing Voyager Library** (~200 errors)
2. **StateFlow Conversion Issues** (~50 errors)
3. **Parameter Name Mismatches** (~30 errors)
4. **Missing String Resources** (~100 errors)
5. **Type Mismatches** (~50 errors)
6. **Deprecated API Usage** (~20 errors)
7. **Other Issues** (~50 errors)

## Fixes Applied

### 1. StateFlow Conversion (✅ FIXED)

**Problem**: `.asStateFlow()` called without scope parameter on `Preference<T>` objects.

**Files Fixed**:
- `SettingsDownloadViewModel.kt` - All 11 StateFlow properties
- `SettingsLibraryViewModel.kt` - autoUpdateRestrictions property
- `SettingsDataViewModel.kt` - totalCacheSize with proper stateIn parameters

**Solution**:
```kotlin
// Before
val pref: StateFlow<T> = preferenceStore.getType("key", default).asStateFlow()

// After
val pref: StateFlow<T> = preferenceStore.getType("key", default).stateIn(scope)

// For combined flows
val combined: StateFlow<T> = combine(flow1, flow2) { a, b -> a + b }
    .stateIn(scope, SharingStarted.WhileSubscribed(5000), initialValue)
```

**Files Created**:
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/core/utils/StateFlowUtils.kt`
- `scripts/fix_stateflow_errors.ps1`

### 2. StateScreenModel Migration (✅ PARTIALLY FIXED)

**Problem**: Code references `cafe.adriel.voyager.core.model.StateScreenModel` which is not available.

**Solution**: Created `IReaderStateScreenModel` as a replacement.

**Files Created**:
- `presentation-core/src/commonMain/kotlin/ireader/presentation/core/viewmodel/IReaderStateScreenModel.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/core/viewmodel/StateScreenModelAlias.kt`

**Migration Pattern**:
```kotlin
// Old (Voyager)
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope

class MyScreenModel : StateScreenModel<MyState>(MyState()) {
    init {
        screenModelScope.launch {
            mutableState { copy(loading = true) }
        }
    }
}

// New (IReader)
import ireader.presentation.core.viewmodel.StateScreenModel

class MyScreenModel : StateScreenModel<MyState>(MyState()) {
    init {
        screenModelScope.launch {
            updateState { copy(loading = true) }
        }
    }
}
```

**Files Needing Migration**:
- `DownloadScreenModel.kt` (~50 errors)
- `MigrationScreenModel.kt` (~40 errors)
- `StatsScreenModel.kt` (~30 errors)

### 3. ScreenModelModule Import (✅ FIXED)

**Problem**: Unresolved reference to `ireader.presentation` in import.

**Fix**: Changed from fully qualified name to simple import.

```kotlin
// Before
factoryOf(::ireader.presentation.ui.settings.statistics.StatsScreenModel)

// After
import ireader.presentation.ui.settings.statistics.StatsScreenModel
factoryOf(::StatsScreenModel)
```

### 4. ImageLoader Return Type (✅ ALREADY FIXED)

**Problem**: `placeholder()` expects `(ImageRequest) -> Image?` but received `ColorPainter`.

**Fix**: Already wrapped in lambda in previous fixes.

## Remaining Issues

### High Priority

#### 1. StateScreenModel Migration

**Files**: `DownloadScreenModel.kt`, `MigrationScreenModel.kt`, `StatsScreenModel.kt`

**Errors**: ~120 total
- Unresolved reference 'StateScreenModel'
- Unresolved reference 'screenModelScope'
- Unresolved reference 'mutableState'

**Fix Strategy**:
1. Add import: `import ireader.presentation.core.viewmodel.StateScreenModel`
2. Replace `mutableState { }` with `updateState { }`
3. `screenModelScope` is already provided by base class

#### 2. Missing String Resources

**Files**: Multiple UI files

**Errors**: ~100 total
- Unresolved reference to string resources like 'loading_book_details', 'no_chapters_found', etc.

**Fix Strategy**:
1. Check `i18n` module for existing resources
2. Add missing resources to appropriate files
3. Or use hardcoded strings temporarily for testing

#### 3. Parameter Name Mismatches

**Files**: All Settings screens

**Errors**: ~30 total
- No parameter with name 'onPopBackStack' found
- No value passed for parameter 'popBackStack'

**Fix Strategy**:
Replace `onPopBackStack =` with `popBackStack =` in all Settings screen calls.

**Script**: `scripts/batch_fix_errors.ps1` (Fix #3)

#### 4. BookDetailScreen Issues

**Files**: `BookDetailScreenEnhanced.kt`, `BookDetailScreenNew.kt`, `BookDetailScreenRefactored.kt`

**Errors**: ~80 total
- Unresolved references to state properties
- Missing imports for `rememberScreenModel`, `getScreenModel`
- Type inference issues

**Fix Strategy**:
1. Add missing Voyager imports or create alternatives
2. Fix state property access patterns
3. Ensure proper ViewModel integration

### Medium Priority

#### 5. ChapterSort/ChapterFilters Redeclaration

**Files**: `ChapterSort.kt`, `ChapterFilters.kt`

**Errors**: ~20 total
- Redeclaration errors
- Missing enum values
- Unresolved references to 'types'

**Fix Strategy**:
1. Check for duplicate class definitions
2. Ensure all enum values are defined
3. Fix property access patterns

#### 6. Preference.map() Issues

**Files**: `SettingsNotificationViewModel.kt`, `SettingsSecurityViewModel.kt`

**Errors**: ~10 total
- None of the following candidates is applicable for `.map()`

**Fix Strategy**:
```kotlin
// Before
val pref = preferenceStore.getType("key", default).map { it }

// After
val pref = preferenceStore.getType("key", default).changes().map { it }.stateIn(scope, SharingStarted.WhileSubscribed(5000), default)
```

#### 7. Global Search Issues

**Files**: `GlobalSearchScreen.kt`, `GlobalSearchViewModel.kt`

**Errors**: ~30 total
- Unresolved reference 'SearchResult'
- Type inference issues
- Flow operation errors

**Fix Strategy**:
1. Define or import `SearchResult` data class
2. Fix Flow operations with proper type parameters
3. Ensure proper state management

### Low Priority

#### 8. Deprecated API Warnings

**Files**: `AccessibilityUtils.kt`

**Errors**: ~6 warnings
- `rememberRipple()` is deprecated

**Fix Strategy**:
Use Material3's new ripple APIs or suppress warnings temporarily.

#### 9. Experimental API Warnings

**Files**: Multiple

**Errors**: ~10 warnings
- Material API is experimental

**Fix Strategy**:
Add `@OptIn(ExperimentalMaterial3Api::class)` annotations.

#### 10. Syntax Errors

**Files**: `AdvancedStatisticsScreen.kt`

**Errors**: 1 error
- Unclosed comment

**Fix Strategy**:
Find and close the comment block.

## Scripts Created

### 1. `fix_stateflow_errors.ps1`
Fixes `.asStateFlow()` patterns in presentation module.

### 2. `fix_common_compilation_errors.ps1`
Comprehensive fix for multiple error patterns:
- StateScreenModel imports
- asStateFlow() calls
- onPopBackStack parameters
- Preference.map() calls

### 3. `batch_fix_errors.ps1`
Batch processing for common fixes across multiple files.

## Execution Plan

### Phase 1: Critical Fixes (Immediate)
1. ✅ Fix StateFlow conversion issues
2. ✅ Fix ScreenModelModule imports
3. ⏳ Migrate StateScreenModel classes
4. ⏳ Fix parameter name mismatches

### Phase 2: High Priority (Next)
1. Add missing string resources
2. Fix BookDetailScreen issues
3. Resolve ChapterSort/ChapterFilters redeclarations
4. Fix Preference.map() issues

### Phase 3: Medium Priority
1. Fix Global Search issues
2. Resolve type inference problems
3. Fix remaining unresolved references

### Phase 4: Low Priority
1. Update deprecated APIs
2. Add experimental API annotations
3. Fix syntax errors
4. Clean up warnings

## Testing Strategy

After each phase:
1. Run compilation: `./gradlew :presentation:compileDebugKotlinAndroid`
2. Check error count reduction
3. Test affected features manually
4. Run unit tests if available

## Notes

- All fixes maintain production-ready code quality
- No files are removed, only modified
- Changes follow Kotlin and Compose best practices
- Error handling is moved to appropriate layers
- Type safety is maintained throughout

## Related Documentation

- `FIXES_APPLIED.md` - Previous fixes applied
- `TEST_MOCK_FIXES.md` - Test-related fixes
- `STATEFLOW_FIX_SUMMARY.md` - StateFlow conversion details
