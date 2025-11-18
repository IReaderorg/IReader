# Manual Fixes Still Needed

## Date
November 18, 2025

## Overview
Most compilation errors have been fixed automatically. The remaining errors require manual intervention because they involve missing domain models or methods that need to be implemented.

## Remaining Errors (Approximately 10-15)

### 1. GlobalSearchViewModel (sources) - SearchResult Missing
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/global_search/viewmodel/GlobalSearchViewModel.kt`

**Errors**:
- Line 73, 76: Unresolved reference 'SearchResult'
- Line 81: Unresolved reference 'id'
- Line 85: Unresolved reference 'results'

**Solution**:
Either:
1. Use the SearchResult from explore GlobalSearchViewModel
2. Or define SearchResult in this file:
```kotlin
data class SearchResult(
    val sourceId: Long,
    val sourceName: String,
    val books: List<Book>,
    val isLoading: Boolean = false,
    val error: Throwable? = null
)
```

Then fix property references:
- `.id` should be `.sourceId`
- `.results` should be `.books`

### 2. MigrationScreenModel - Missing Methods and Models
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreenModel.kt`

**Errors**:
- Line 45: Unresolved reference 'getFavoriteBooksFlow'
- Lines 46, 49, 55: Cannot infer type for type parameter 'T'
- Lines 85, 146: Unresolved reference 'MigrationBook'
- Lines 85, 146: Cannot infer type for value parameter

**Solution**:
1. Find the correct method name for getting library books:
   - Check `GetLibraryUseCase` or similar
   - Might be `getLibraryBooks()`, `getFavorites()`, or `getInLibraryBooks()`

2. Import MigrationBook from domain:
```kotlin
import ireader.domain.models.migration.MigrationBook
```

3. Fix the map calls with explicit types:
```kotlin
.map<List<Book>, List<MigrationBook>> { books ->
    books.map { book ->
        MigrationBook(
            id = book.id,
            title = book.title,
            // ... other properties
        )
    }
}
```

### 3. MigrationScreens - Property References
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreens.kt`

**Error**:
- Line 501: WideNavigationRailValue condition type mismatch

**Solution**:
The fix script attempted this, but verify the correct comparison:
```kotlin
// Instead of:
if (!wideNavigationRail)

// Use:
if (wideNavigationRail == WideNavigationRailValue.Disabled)
// Or:
if (wideNavigationRail != WideNavigationRailValue.Enabled)
```

### 4. VoiceSelectionViewModel (reader) - Missing BaseViewModel Extension
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/VoiceSelectionViewModel.kt`

**Errors**:
- Line 32: Type mismatch StateFlow<VoiceSelectionState> vs StateFlow<VoiceSelectionState?>
- Line 32: Unresolved reference 'scope'

**Solution**:
1. Ensure the class extends BaseViewModel:
```kotlin
class VoiceSelectionViewModel(
    // dependencies
) : BaseViewModel() {
    // ...
}
```

2. Fix the nullable type:
```kotlin
// If the state can be null:
val state: StateFlow<VoiceSelectionState?> = ...

// Or ensure it's never null:
val state: StateFlow<VoiceSelectionState> = MutableStateFlow(VoiceSelectionState()).asStateFlow()
```

### 5. SettingsAppearanceScreen - When Expression Exhaustiveness
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/appearance/SettingsAppearanceScreen.kt`

**Errors**:
- Lines 181, 320: Missing when branches for Seconds, Minutes, Hour

**Solution**:
Add the missing branches or an else:
```kotlin
when (timeUnit) {
    Days -> { /* existing code */ }
    Seconds -> { /* handle seconds */ }
    Minutes -> { /* handle minutes */ }
    Hour -> { /* handle hour */ }
    else -> { /* fallback */ }
}
```

### 6. Statistics Screens - Count Property
**Files**:
- `EnhancedStatisticsScreen.kt:533`
- `StatisticsScreen.kt:135`

**Error**: Function invocation 'count(...)' expected

**Solution**:
The fix script attempted this. Verify the objects have a `count` property:
```kotlin
// Should be:
genreCount.count  // property access

// Not:
genreCount.count()  // function call
```

## Priority Order

### High Priority (Blocking Compilation)
1. **MigrationScreenModel** - Fix getFavoriteBooksFlow and MigrationBook imports
2. **GlobalSearchViewModel (sources)** - Add SearchResult definition
3. **VoiceSelectionViewModel (reader)** - Extend BaseViewModel

### Medium Priority
4. **SettingsAppearanceScreen** - Add when branches
5. **MigrationScreens** - Fix WideNavigationRailValue condition

### Low Priority (May already be fixed)
6. **Statistics screens** - Verify count property access

## How to Fix

### Step 1: Check Domain Models
```bash
# Find MigrationBook definition
Get-ChildItem -Recurse -Filter "*.kt" | Select-String "class MigrationBook"

# Find correct library books method
Get-ChildItem -Recurse -Filter "*.kt" | Select-String "fun get.*Books.*Flow"
```

### Step 2: Fix MigrationScreenModel
1. Import correct domain models
2. Use correct method name for getting library books
3. Add explicit type parameters to map/combine calls

### Step 3: Fix GlobalSearchViewModel
1. Copy SearchResult from explore GlobalSearchViewModel
2. Fix property references (id -> sourceId, results -> books)

### Step 4: Fix VoiceSelectionViewModel
1. Ensure it extends BaseViewModel
2. Fix nullable state type

### Step 5: Compile and Test
```powershell
.\gradlew :presentation:compileKotlinDesktop
```

## Expected Outcome

After these manual fixes:
- Compilation should succeed
- All type mismatches resolved
- All unresolved references fixed
- Ready for full build: `.\gradlew build`

## Notes

- Most errors are in Migration and GlobalSearch features
- These features may be incomplete/in-development
- Consider commenting out incomplete features if they're not critical
- All other errors have been fixed automatically

## Alternative: Comment Out Incomplete Features

If these features are not critical for current development:

```kotlin
// In MigrationScreenModel.kt - comment out problematic code
// In GlobalSearchViewModel.kt - comment out problematic code
// In MigrationScreens.kt - simplify or comment out migration UI
```

This will allow compilation to succeed while these features are being developed.

