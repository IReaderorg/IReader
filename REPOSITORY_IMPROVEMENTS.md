# Repository Management Improvements

## Changes Made

### 1. Unified Repository Add Screen
Merged IReader and LNReader repository support into a single, user-friendly screen.

### 2. Repository Type Selection
Added a type selector to choose between:
- **IReader Format**: For IReader extensions and plugins
- **LNReader Format**: For LNReader-compatible plugins

### 3. Quick Add Feature
Added "Quick Add" button with popular repository presets:
- **IReader Official**: `https://raw.githubusercontent.com/IReaderorg/IReader-extensions/repo/index.min.json`
- **LNReader Plugins**: `https://raw.githubusercontent.com/LNReader/lnreader-plugins/plugins/v3.0.0/.dist/plugins.min.json`

### 4. Removed Default Repository
- No default repositories are pre-installed
- Users must manually add repositories
- This gives users full control over their content sources

### 5. Enhanced UI/UX
- Repository type chips with visual feedback
- Quick add dialog with preset options
- Type-specific information cards
- Better form validation
- Improved error messages

## Features

### Quick Add Dialog
- One-tap addition of popular repositories
- Shows repository name, owner, and type
- Color-coded type badges (IReader/LNReader)
- Easy to use for beginners

### Manual Add Form
- Repository type selector (IReader/LNReader)
- Required fields: Name, URL
- Optional fields: Owner, Source
- Optional authentication: Username, Password
- Real-time validation
- Help dialog with examples

### Repository List
- View all added repositories
- Delete custom repositories (ID >= 0)
- Set default repository with switch
- Empty state with helpful message

## User Flow

### Adding a Repository

**Option 1: Quick Add (Recommended for beginners)**
1. Tap "Add Repository" FAB
2. Tap "Quick Add Popular Repositories"
3. Select from preset list
4. Tap "Save Repository"

**Option 2: Manual Add (For custom repositories)**
1. Tap "Add Repository" FAB
2. Select repository type (IReader/LNReader)
3. Fill in required fields (Name, URL)
4. Optionally fill owner, source, authentication
5. Tap "Save Repository"

### Managing Repositories
1. Go to Settings → Repositories
2. View list of added repositories
3. Delete unwanted repositories (trash icon)
4. Set default repository (switch)

## Technical Details

### Repository Types

**IReader Format**:
- URL format: `https://domain.com/path/index.min.json`
- Contains extensions and plugins
- Example: IReader Official repository

**LNReader Format**:
- URL format: `https://domain.com/path/v3.json`
- Compatible with LNReader plugins
- Example: LNReader Plugins repository

### Data Structure

```kotlin
enum class RepositoryType {
    IREADER,
    LNREADER
}

data class QuickAddPreset(
    val name: String,
    val url: String,
    val owner: String,
    val source: String,
    val type: RepositoryType
)

data class RepositoryInfo(
    val name: String,
    val key: String,      // URL
    val owner: String,
    val source: String,
    val username: String,
    val password: String
)
```

### Database Schema

```sql
CREATE TABLE repository (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    key TEXT NOT NULL,
    owner TEXT,
    source TEXT,
    last_update INTEGER,
    is_enable INTEGER DEFAULT 1
)
```

## Benefits

### For Users
- ✅ Full control over content sources
- ✅ Easy to add popular repositories
- ✅ Support for both IReader and LNReader formats
- ✅ No unwanted default repositories
- ✅ Clear visual feedback

### For Developers
- ✅ Cleaner codebase
- ✅ Unified repository management
- ✅ Extensible preset system
- ✅ Better separation of concerns

## Migration Notes

### For Existing Users
- Default IReader repository is removed
- Users need to manually add repositories
- Quick Add makes this easy with one tap
- All existing custom repositories are preserved

### For New Users
- No repositories on first launch
- Guided to add repositories via empty state
- Quick Add provides instant access to popular sources
- Help dialog explains the concept

## Future Enhancements

Consider adding:
1. **Repository validation**: Test URL before saving
2. **Auto-update**: Check for repository updates
3. **Import/Export**: Share repository lists
4. **Categories**: Group repositories by type/genre
5. **Search**: Find repositories by name/owner
6. **Statistics**: Show extension count per repository
7. **Backup**: Save repository list to cloud
8. **Community**: Share and discover repositories

## Testing

### Test Cases

1. **Quick Add IReader Repository**
   - Open repository settings
   - Tap Add FAB
   - Tap Quick Add
   - Select "IReader Official"
   - Verify fields are populated
   - Save and verify in list

2. **Quick Add LNReader Repository**
   - Open repository settings
   - Tap Add FAB
   - Tap Quick Add
   - Select "LNReader Plugins"
   - Verify fields are populated
   - Save and verify in list

3. **Manual Add Custom Repository**
   - Open repository settings
   - Tap Add FAB
   - Select repository type
   - Fill in name and URL
   - Save and verify in list

4. **Delete Repository**
   - Open repository settings
   - Tap trash icon on custom repository
   - Verify repository is removed

5. **Set Default Repository**
   - Open repository settings
   - Toggle switch on desired repository
   - Verify it's set as default

6. **Empty State**
   - Fresh install or delete all repositories
   - Verify empty state message
   - Tap "Add Repository" button
   - Verify navigation to add screen

## Related Files

### Modified Files
1. `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/repository/AddingRepositryScreen.kt`
   - Added repository type selection
   - Added quick add feature
   - Enhanced UI/UX

### Unchanged Files
1. `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/RepositoryScreenSpec.kt`
   - Repository list screen (no changes needed)

2. `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/repository/SourceRepositoryViewModel.kt`
   - ViewModel (no changes needed)

3. `data/src/commonMain/kotlin/ireader/data/catalogrepository/CatalogSourceRepository.kt`
   - Repository implementation (no changes needed)

## Configuration

### Quick Add Presets

To add more presets, update the `quickAddPresets` list in `AddingRepositryScreen.kt`:

```kotlin
val quickAddPresets = remember {
    listOf(
        QuickAddPreset(
            name = "Your Repository Name",
            url = "https://your-repo-url.com/index.json",
            owner = "Owner Name",
            source = "https://github.com/owner/repo",
            type = RepositoryType.IREADER // or LNREADER
        ),
        // Add more presets here
    )
}
```

### Default Repository

The default repository is stored in preferences:
```kotlin
uiPreferences.defaultRepository().set(repositoryId)
```

## Troubleshooting

### Issue: LNReader repository not working
**Solution**: Ensure the URL points to a valid LNReader v3.json file and the repository type is set to LNREADER.

### Issue: Can't delete repository
**Solution**: Only custom repositories (ID >= 0) can be deleted. System repositories cannot be removed.

### Issue: Quick Add not showing
**Solution**: Ensure the presets list is properly configured in the code.

### Issue: Repository not loading extensions
**Solution**: Check the repository URL is accessible and the format matches the selected type.

## Summary

This update provides a unified, user-friendly repository management system that:
- Supports both IReader and LNReader formats
- Removes default repositories for user control
- Adds quick access to popular repositories
- Improves overall user experience
- Maintains backward compatibility

Users now have full control over their content sources while still having easy access to popular repositories through the Quick Add feature.
