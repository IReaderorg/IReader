# Remaining Compilation Errors

## Summary
After applying initial fixes, there are still several categories of errors that need to be addressed. This document categorizes them and provides solutions.

## Date
November 18, 2025

## Error Categories

### 1. ImageLoader Placeholder Issue
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/imageloader/ImageLoader.kt:183`
**Error**: `placeholder` function signature mismatch
**Solution**: The placeholder should accept a Painter directly, not wrapped in a lambda
```kotlin
// Current (wrong):
placeholder?.let { placeholderPainter -> placeholder(placeholderPainter) }

// Should be:
placeholder?.let { placeholder(it) }
```

### 2. AccessibilityUtils - Ripple and Size Issues
**Files**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/accessibility/AccessibilityUtils.kt`
**Errors**:
- Unresolved reference 'ripple' (lines 4, 9, 56, 86)
- Size modifier issues (lines 50, 80, 153)

**Solution**: 
- Import `androidx.compose.material.ripple.ripple` or use Material3's ripple
- Ensure all size values have `.dp` unit

### 3. BookDetailScreenNew - Error References
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreenNew.kt`
**Errors**: Lines 85, 87 - Unresolved reference 'error'
**Solution**: Use proper error state from ViewModel

### 4. String Resources Missing
**Files**: Multiple screens
**Errors**: Unresolved references to string resources like:
- `loading_books`, `no_books_found`, `filters`, `show_filters` (ExploreScreenEnhanced.kt)
- `navigate_up`, `information` (SourceDetailScreenEnhanced.kt)
- `global_search`, `search_for_books_across_sources` (GlobalSearchScreenEnhanced.kt)

**Solution**: Add wildcard import `import ireader.i18n.resources.*` or define missing strings

### 5. GlobalSearchViewModel Issues
**Files**: 
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/global_search/GlobalSearchViewModel.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/global_search/viewmodel/GlobalSearchViewModel.kt`

**Errors**:
- SearchResult class conflicts between domain and presentation
- Flow type inference issues
- Property reference issues (id, results)

**Solution**: Use domain SearchResult class or properly define presentation SearchResult

### 6. LibraryViewModel - stateIn Parameter Issues
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/LibraryViewModel.kt`
**Errors**: Lines 135, 136, 522 - Too many arguments for stateIn
**Solution**: Preference.stateIn() only takes scope parameter, not SharingStarted

### 7. Migration Feature - Type Mismatches
**Files**: 
- `MigrationScreenModel.kt`
- `MigrationScreens.kt`

**Errors**:
- MigrationFlags/MigrationSource conflicts between domain and presentation
- Property references (migrateChapters, migrateBookmarks, etc.)
- getLibraryBooks method doesn't exist

**Solution**: Use domain models consistently, fix property names

### 8. Badge ViewModels - Type Inference
**Files**:
- `BadgeManagementViewModel.kt`
- `NFTBadgeViewModel.kt`
- `BadgeStoreViewModel.kt`

**Errors**: Cannot infer type for stateIn
**Solution**: Provide explicit initial values for stateIn

### 9. SettingsNotificationViewModel - Flow to StateFlow
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/notifications/SettingsNotificationViewModel.kt`
**Errors**: Lines 43, 44 - Flow<Pair<Int, Int>> instead of StateFlow
**Solution**: Add `.stateIn()` to convert Flow to StateFlow

### 10. VoiceSelectionViewModel (reader) - Scope Reference
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/VoiceSelectionViewModel.kt`
**Error**: Line 32 - Unresolved reference 'scope'
**Solution**: Ensure ViewModel extends BaseViewModel which provides scope

### 11. SettingsAppearanceScreen - When Exhaustiveness
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/appearance/SettingsAppearanceScreen.kt`
**Errors**: Lines 181, 320 - Missing when branches
**Solution**: Add else branches or missing enum cases

### 12. Statistics Screens - Count Property vs Function
**Files**:
- `EnhancedStatisticsScreen.kt:533`
- `StatisticsScreen.kt:135`

**Errors**: Calling count() on objects that have count property
**Solution**: Use `.count` property instead of `.count()` function

### 13. VoiceSelectionScreen - Collection vs Set
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/screens/VoiceSelectionScreen.kt:35`
**Error**: Collection<String> vs Set<String> mismatch
**Solution**: Convert Collection to Set or change parameter type

## Priority Fixes

### High Priority (Blocking Compilation)
1. Fix Preference.stateIn() calls - they only take scope parameter
2. Fix Migration domain vs presentation model conflicts
3. Fix GlobalSearchViewModel SearchResult conflicts
4. Fix string resource imports

### Medium Priority (Many Occurrences)
5. Fix AccessibilityUtils ripple imports
6. Fix Badge ViewModels type inference
7. Fix count property vs function issues

### Low Priority (Minor Issues)
8. Fix when expression exhaustiveness
9. Fix Collection to Set conversions
10. Fix ImageLoader placeholder

## Recommended Approach

1. **Use Domain Models**: Don't duplicate MigrationFlags/MigrationSource in presentation layer
2. **Fix Preference.stateIn()**: Remove extra parameters, it only needs scope
3. **Import String Resources**: Add `import ireader.i18n.resources.*` to all screens
4. **Fix Ripple**: Use Material3 ripple or import from material.ripple
5. **Type Inference**: Provide explicit types for complex stateIn calls

## Next Steps

1. Create targeted fix scripts for each category
2. Test compilation after each category fix
3. Document any architectural decisions made
4. Update coding standards to prevent similar issues

