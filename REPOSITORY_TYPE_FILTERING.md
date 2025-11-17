# Repository Type Filtering Implementation

## Changes Made

### 1. Database Schema Update
- Added `repository_type` column to `repository` table
- Created migration file `11.sqm` to add the column
- Updated SQL queries to handle the new field

### 2. ExtensionSource Model Update
- Added `repositoryType: String` field with default "IREADER"
- Added helper methods `isLNReaderRepository()` and `isIReaderRepository()`
- Updated companion object default to include repository type

### 3. Repository Layer Updates
- Updated `CatalogSourceRepositoryImpl.insert()` to save repository type
- Updated `extensionMapper` to map the new field from database
- Updated `RepositoryAddScreenSpec` to pass repository type when saving

### 4. UI Updates
- Updated `AddingRepositoryScreen` to include repository type in `RepositoryInfo`
- Added repository type selection to save operation
- Updated `SourceRepositoryViewModel.parseUrl()` to default to IREADER type

### 5. More Screen Scroll Fix
- Updated scroll state to use `rememberSaveable` with custom key
- Added LaunchedEffect to save scroll position to ViewModel
- This should persist scroll state across navigation

## Implementation Status

### ✅ Completed
- Database schema changes
- Model updates
- Repository layer updates
- UI form updates
- More screen scroll fix

### 🔄 In Progress
- Remote catalog filtering by repository type

## Next Steps for Complete Implementation

### 1. Add Repository Type to CatalogRemote
The `CatalogRemote` model needs to include repository information:

```kotlin
data class CatalogRemote(
    // ... existing fields
    val repositoryId: Long, // Reference to ExtensionSource
    val repositoryType: String, // "IREADER" or "LNREADER"
)
```

### 2. Update Remote Catalog Loading
Modify `GetRemoteCatalogs` to include repository information:

```kotlin
fun subscribe(
    withNsfw: Boolean = true,
    repositoryType: String? = null, // Filter by repository type
): Flow<List<CatalogRemote>>
```

### 3. Update ExtensionViewModel
Add repository type filtering in the ViewModel:

```kotlin
// Filter remote catalogs by selected repository type
val filteredRemoteCatalogs = remoteCatalogs.filter { catalog ->
    when (selectedRepositoryType) {
        "LNREADER" -> catalog.repositoryType == "LNREADER"
        "IREADER" -> catalog.repositoryType == "IREADER"
        else -> true // Show all
    }
}
```

### 4. Add Repository Type Selection UI
Add a filter/toggle in the Browse screen to switch between:
- All repositories
- IReader only
- LNReader only

## Current Behavior

### Before Changes
- All remote sources from all repositories are shown together
- No way to distinguish between IReader and LNReader sources
- Users see mixed content from different repository types

### After Database Changes
- Repository type is stored in database
- New repositories can be marked as IReader or LNReader
- Foundation is set for filtering

### After Complete Implementation
- Users can filter remote sources by repository type
- LNReader repositories only show LNReader-compatible sources
- IReader repositories only show IReader-compatible sources
- Clear separation between different source types

## Migration Strategy

### For Existing Users
1. Migration adds `repository_type` column with default "IREADER"
2. All existing repositories are marked as IREADER type
3. Users can add new LNReader repositories with correct type
4. Filtering will work immediately for new repositories

### For New Users
1. No default repositories (as requested)
2. Users must manually add repositories
3. Repository type is selected during addition
4. Filtering works from the start

## Testing

### Database Migration
1. Install app with existing repositories
2. Update to new version
3. Verify migration runs successfully
4. Check that existing repositories have "IREADER" type

### Repository Addition
1. Add new repository with LNReader type
2. Verify it's saved with correct type in database
3. Check that repository list shows type information

### Filtering (When Complete)
1. Add both IReader and LNReader repositories
2. Switch between repository types
3. Verify only relevant sources are shown
4. Test with mixed repository setup

## Files Modified

### Database
- `data/src/commonMain/sqldelight/data/repository.sq`
- `data/src/commonMain/sqldelight/migrations/11.sqm`

### Domain Models
- `domain/src/commonMain/kotlin/ireader/domain/models/entities/ExtensionSource.kt`

### Repository Layer
- `data/src/commonMain/kotlin/ireader/data/catalogrepository/CatalogSourceRepository.kt`

### Presentation Layer
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/repository/AddingRepositryScreen.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/RepositoryAddScreenSpec.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/repository/SourceRepositoryViewModel.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/MoreScreen.kt`

## Benefits

### For Users
- Clear separation between IReader and LNReader sources
- No confusion from mixed source types
- Better organization of content sources
- Ability to use both types simultaneously

### For Developers
- Clean architecture with proper type separation
- Extensible system for future repository types
- Better data organization
- Easier maintenance and debugging

## Future Enhancements

1. **Visual Indicators**: Add badges/icons to show repository type
2. **Smart Filtering**: Auto-detect repository type from URL
3. **Bulk Operations**: Convert repositories between types
4. **Statistics**: Show source count per repository type
5. **Validation**: Verify repository compatibility before adding