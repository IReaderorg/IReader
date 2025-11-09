# Design Document

## Overview

This design document outlines the architecture and implementation approach for integrating unused features into the IReader application. The integration follows the existing Clean Architecture pattern with three main layers: Domain, Data, and Presentation. The design leverages existing implementations and focuses on wiring components together through dependency injection and navigation.

### Design Principles

1. **Reuse Existing Code**: Maximize use of already-implemented features
2. **Minimal Changes**: Make targeted modifications to connect components
3. **Backward Compatibility**: Ensure existing functionality remains intact
4. **Platform Agnostic**: Support both Android and Desktop where applicable
5. **Clean Architecture**: Maintain separation of concerns across layers
6. **Dependency Injection**: Use Koin for all dependencies
7. **Navigation**: Use Voyager for screen navigation
8. **Reactive State**: Use StateFlow and Compose State for UI updates

---

## Architecture

### Layer Structure

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Screens    │  │  ViewModels  │  │  Components  │      │
│  │  (Compose)   │  │   (State)    │  │   (Reusable) │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Use Cases   │  │  Repositories│  │   Entities   │      │
│  │  (Business)  │  │  (Interfaces)│  │   (Models)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Repositories │  │   Database   │  │   Network    │      │
│  │    (Impl)    │  │  (SQLDelight)│  │   (Ktor)     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### Navigation Flow

```
SettingScreenSpec
    ├── StatisticsScreenSpec → StatisticsScreen
    ├── SecuritySettingSpec → SecuritySettingsScreen
    ├── BackupScreenSpec
    │   └── CloudBackupScreenSpec → CloudBackupScreen
    └── AboutScreenSpec
        └── ChangelogScreenSpec → ChangelogScreen

ExtensionScreenSpec
    └── SourceDetailScreen (already integrated)

ReaderScreen
    ├── ReportBrokenChapterDialog
    ├── BrightnessControl
    ├── FontPicker
    ├── FindInChapterBar
    └── TranslationMenu
```

---

## Components and Interfaces

### 1. Statistics Feature

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    StatisticsScreen                          │
│  - Displays reading statistics                              │
│  - Shows charts and metrics                                 │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  StatisticsViewModel                         │
│  - statistics: StateFlow<ReadingStatistics>                 │
│  + loadStatistics()                                         │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              GetReadingStatisticsUseCase                     │
│  + invoke(): Flow<ReadingStatistics>                        │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│            ReadingStatisticsRepository                       │
│  + getTotalStatistics(): ReadingStatistics                  │
│  + getStatisticsForBook(bookId): ReadingStatistics          │
└─────────────────────────────────────────────────────────────┘
```

#### Integration Points

1. **Navigation**: Add `StatisticsScreenSpec` to `SettingScreenSpec` menu
2. **DI**: `StatisticsViewModel` already registered in `PresentationModules`
3. **Tracking**: Call `TrackReadingProgressUseCase` from `ReaderScreenViewModel`

#### Data Flow

```
User reads chapter
    → ReaderScreenViewModel.onChapterRead()
    → TrackReadingProgressUseCase.invoke(chapterId, duration)
    → ReadingStatisticsRepository.insertStatistics()
    → Database update
    
User opens Statistics
    → StatisticsScreen.Content()
    → StatisticsViewModel.statistics.collectAsState()
    → GetReadingStatisticsUseCase.invoke()
    → ReadingStatisticsRepository.getTotalStatistics()
    → Display in UI
```

---

### 2. Security Settings Feature

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                SecuritySettingsScreen                        │
│  - App Lock section                                         │
│  - Privacy section                                          │
│  - Information section                                      │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│             SecuritySettingsViewModel                        │
│  - appLockEnabled: State<Boolean>                           │
│  - secureScreenEnabled: State<Boolean>                      │
│  - hideContentEnabled: State<Boolean>                       │
│  - adultSourceLockEnabled: State<Boolean>                   │
│  - biometricEnabled: State<Boolean>                         │
│  + toggleAppLock(enabled: Boolean)                          │
│  + setupAuthMethod(method: AuthMethod)                      │
│  + showSetupDialog(type: AuthMethod)                        │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  SecurityRepository                          │
│  + saveAuthMethod(method: AuthMethod)                       │
│  + getAuthMethod(): AuthMethod?                             │
│  + verifyAuth(input: String): Boolean                       │
└─────────────────────────────────────────────────────────────┘
```

#### Platform-Specific Implementation

**Android:**
- Uses BiometricPrompt for biometric authentication
- Integrates with existing `UnlockActivity`
- Supports FLAG_SECURE for screenshot blocking

**Desktop:**
- PIN/Password only (no biometric)
- Window-level security flags
- Keyboard-based authentication

#### Integration Points

1. **Replace**: Update `SecuritySettingSpec` implementations (Android/Desktop)
2. **DI**: Register `SecuritySettingsViewModel` in `PresentationModules`
3. **Repository**: Create `SecurityRepository` implementation

---

### 3. Report Broken Chapter Feature

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│             ReportBrokenChapterDialog                        │
│  - Reason selection                                         │
│  - Description input                                        │
│  - Submit button                                            │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                ReaderScreenViewModel                         │
│  + reportBrokenChapter(reason, description)                 │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│            ReportBrokenChapterUseCase                        │
│  + invoke(chapterId, bookId, sourceId, reason, desc)        │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              ChapterReportRepository                         │
│  + insertReport(report: ChapterReport)                      │
│  + getAllPendingReports(): List<ChapterReport>              │
└─────────────────────────────────────────────────────────────┘
```

#### Report Submission Strategy

**Phase 1: Local Storage**
- Store reports in local database
- Display success message to user
- Queue for later submission

**Phase 2: Backend Integration** (Future)
- Submit to REST API endpoint
- Create GitHub issues automatically
- Sync with cloud service

---

### 4. WorkManager Automatic Backups

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                  BackupSettings UI                           │
│  - Frequency selector                                       │
│  - Enable/Disable toggle                                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│           ScheduleAutomaticBackupImpl                        │
│  + schedule(frequency: AutomaticBackup)                     │
│  + cancel()                                                 │
│  + isScheduled(): Boolean                                   │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                   WorkManager                                │
│  - Periodic work request                                    │
│  - Constraints (battery, storage)                           │
│  - Backoff policy                                           │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                 AutoBackupWorker                             │
│  + doWork(): Result                                         │
│  - Executes CreateBackup use case                           │
└─────────────────────────────────────────────────────────────┘
```

#### Work Constraints

```kotlin
Constraints {
    requiredNetworkType = NOT_REQUIRED
    requiresBatteryNotLow = true
    requiresStorageNotLow = true
}
```

#### Frequency Options

| Option | Interval | Use Case |
|--------|----------|----------|
| Every 6 Hours | 6h | Frequent readers |
| Every 12 Hours | 12h | Daily readers |
| Daily | 24h | Regular readers |
| Every 2 Days | 48h | Casual readers |
| Weekly | 7d | Occasional readers |
| Off | - | Manual only |

---

### 5. Cloud Backup Feature

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                  CloudBackupScreen                           │
│  - Provider selection (Dropbox/Drive)                       │
│  - Authentication status                                    │
│  - Backup/Restore buttons                                   │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                CloudBackupManager                            │
│  + uploadBackup(provider, file)                             │
│  + downloadBackup(provider): File                           │
│  + listBackups(provider): List<BackupInfo>                  │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              CloudStorageProvider                            │
│  + authenticate()                                           │
│  + upload(file): Result                                     │
│  + download(path): Result<File>                             │
│  + list(): List<FileInfo>                                   │
└─────────────────────────────────────────────────────────────┘
         ↓                                    ↓
┌──────────────────┐              ┌──────────────────┐
│ DropboxProvider  │              │GoogleDriveProvider│
│  - OAuth 2.0     │              │  - OAuth 2.0      │
│  - API v2        │              │  - Drive API v3   │
└──────────────────┘              └──────────────────┘
```

#### Authentication Flow

```
User taps "Connect Dropbox"
    → CloudBackupScreen.onProviderSelected(Dropbox)
    → DropboxProvider.authenticate()
    → Launch OAuth browser flow
    → Receive auth code
    → Exchange for access token
    → Save to SourceCredentialsRepository
    → Update UI to show "Connected"
```

---

### 6. Reader Enhancement Components

#### Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     ReaderScreen                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              ReaderContent                          │   │
│  │  - Text rendering                                   │   │
│  │  - Scroll handling                                  │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │          ReaderSettingsBottomSheet                  │   │
│  │  ├── BrightnessControl                              │   │
│  │  ├── FontPicker                                     │   │
│  │  ├── AutoScrollSpeedControl                         │   │
│  │  └── QuickFontSizeAdjuster                          │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │            FindInChapterBar                         │   │
│  │  - Search input                                     │   │
│  │  - Match navigation                                 │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         ParagraphTranslationMenu                    │   │
│  │  - Translate option                                 │   │
│  │  - Bilingual display                                │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

#### State Management

```kotlin
@Composable
fun ReaderScreen(vm: ReaderScreenViewModel) {
    val showSettings by vm.showSettings.collectAsState()
    val brightness by vm.brightness.collectAsState()
    val selectedFont by vm.selectedFont.collectAsState()
    val autoScrollSpeed by vm.autoScrollSpeed.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()
    
    // Render components based on state
}
```

---

### 7. Library Batch Operations

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                   LibraryScreen                              │
│  - Selection mode                                           │
│  - Selected books count                                     │
│  - Batch operations button                                  │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              BatchOperationDialog                            │
│  - Mark as Read/Unread                                      │
│  - Download chapters                                        │
│  - Delete                                                   │
│  - Change category                                          │
│  - Archive                                                  │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                LibraryViewModel                              │
│  - selectedBooks: StateFlow<Set<Long>>                      │
│  + toggleSelection(bookId: Long)                            │
│  + performBatchOperation(operation: BatchOp)                │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              Batch Operation Use Cases                       │
│  - MarkBookAsReadOrNotUseCase                               │
│  - DownloadUnreadChaptersUseCase                            │
│  - DeleteBookById                                           │
│  - ArchiveBookUseCase                                       │
└─────────────────────────────────────────────────────────────┘
```

#### Operation Flow

```
User long-presses book
    → LibraryViewModel.toggleSelection(bookId)
    → Enter selection mode
    → User selects more books
    → User taps batch operations
    → BatchOperationDialog appears
    → User selects "Download"
    → LibraryViewModel.performBatchOperation(Download)
    → For each selected book:
        → DownloadUnreadChaptersUseCase.invoke(bookId)
    → Show progress
    → Display completion message
```

---

## Data Models

### ReadingStatistics

```kotlin
data class ReadingStatistics(
    val totalChaptersRead: Int,
    val totalReadingTimeMinutes: Long,
    val booksCompleted: Int,
    val currentlyReading: Int,
    val readingStreak: Int,
    val averageReadingSpeedWPM: Int,
    val favoriteGenres: List<GenreCount>
)

data class GenreCount(
    val genre: String,
    val count: Int
)
```

### ChapterReport

```kotlin
data class ChapterReport(
    val id: Long,
    val chapterId: Long,
    val bookId: Long,
    val sourceId: Long,
    val reason: String,
    val description: String,
    val timestamp: Long,
    val status: String // "pending", "submitted", "resolved"
)
```

### AuthMethod

```kotlin
sealed class AuthMethod {
    data class PIN(val pin: String) : AuthMethod()
    data class Password(val password: String) : AuthMethod()
    object Biometric : AuthMethod()
    object None : AuthMethod()
}
```

### BackupConfig

```kotlin
data class BackupConfig(
    val includeLibrary: Boolean = true,
    val includeCategories: Boolean = true,
    val includeChapters: Boolean = true,
    val includeHistory: Boolean = true,
    val includeSettings: Boolean = true,
    val includeExtensions: Boolean = false
)
```

---

## Error Handling

### Error Categories

1. **Network Errors**: Cloud backup, translation API
2. **Database Errors**: Statistics tracking, report storage
3. **Authentication Errors**: Biometric, cloud providers
4. **File System Errors**: Cache calculation, backup creation
5. **Validation Errors**: User input, preferences

### Error Handling Strategy

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

// Usage in Use Cases
suspend fun invoke(): Result<ReadingStatistics> {
    return try {
        val stats = repository.getTotalStatistics()
        Result.Success(stats)
    } catch (e: Exception) {
        Log.error("Failed to get statistics", e)
        Result.Error(e)
    }
}

// Usage in ViewModels
viewModelScope.launch {
    when (val result = getStatisticsUseCase()) {
        is Result.Success -> {
            _statistics.value = result.data
        }
        is Result.Error -> {
            showSnackBar("Failed to load statistics: ${result.exception.message}")
        }
    }
}
```

### User-Facing Error Messages

| Error Type | User Message |
|------------|--------------|
| Network failure | "Unable to connect. Check your internet connection." |
| Database error | "Failed to save data. Please try again." |
| Auth failure | "Authentication failed. Please try again." |
| File not found | "File not found. It may have been deleted." |
| Permission denied | "Permission required. Please grant access in settings." |

---

## Testing Strategy

### Unit Tests

**Domain Layer:**
- Use case logic
- Business rules
- Data transformations

```kotlin
class TrackReadingProgressUseCaseTest {
    @Test
    fun `should insert statistics when duration exceeds threshold`() {
        // Given
        val repository = mockk<ReadingStatisticsRepository>()
        val useCase = TrackReadingProgressUseCase(repository)
        
        // When
        runBlocking {
            useCase.invoke(chapterId = 1, durationMinutes = 15)
        }
        
        // Then
        verify { repository.insertStatistics(any()) }
    }
}
```

**Presentation Layer:**
- ViewModel state management
- User interactions
- Navigation logic

```kotlin
class StatisticsViewModelTest {
    @Test
    fun `should load statistics on init`() {
        // Given
        val useCase = mockk<GetReadingStatisticsUseCase>()
        every { useCase.invoke() } returns flowOf(mockStatistics)
        
        // When
        val viewModel = StatisticsViewModel(useCase)
        
        // Then
        assertEquals(mockStatistics, viewModel.statistics.value)
    }
}
```

### Integration Tests

**Database Operations:**
- Insert/update/delete operations
- Query correctness
- Transaction handling

**Repository Implementations:**
- Data mapping
- Error handling
- Caching behavior

### UI Tests

**Compose Tests:**
- Screen rendering
- User interactions
- State updates

```kotlin
@Test
fun statisticsScreen_displaysCorrectData() {
    composeTestRule.setContent {
        StatisticsScreen()
    }
    
    composeTestRule
        .onNodeWithText("Chapters Read")
        .assertIsDisplayed()
}
```

### Manual Testing Checklist

- [ ] Statistics screen displays correct data
- [ ] Security settings save and apply correctly
- [ ] Report submission creates database entry
- [ ] WorkManager schedules backup correctly
- [ ] Cloud backup authenticates and uploads
- [ ] Reader enhancements work smoothly
- [ ] Batch operations complete successfully
- [ ] Translation displays correctly
- [ ] Font picker loads and applies fonts
- [ ] All navigation flows work correctly

---

## Performance Considerations

### Database Optimization

1. **Indexing**: Add indexes on frequently queried columns
   ```sql
   CREATE INDEX idx_reading_stats_book_id ON ReadingStatistics(bookId);
   CREATE INDEX idx_chapter_reports_status ON ChapterReport(status);
   ```

2. **Batch Operations**: Use transactions for multiple inserts
   ```kotlin
   database.transaction {
       selectedBooks.forEach { bookId ->
           repository.markAsRead(bookId)
       }
   }
   ```

3. **Pagination**: Load statistics in chunks for large datasets

### Memory Management

1. **Image Caching**: Use Coil's memory cache for cover images
2. **Font Caching**: Cache loaded fonts to avoid repeated file I/O
3. **State Management**: Use StateFlow instead of LiveData for better performance

### Background Processing

1. **WorkManager**: Use for long-running backup operations
2. **Coroutines**: Use for database and network operations
3. **Dispatchers**: Use appropriate dispatchers (IO, Default, Main)

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    val result = heavyOperation()
    withContext(Dispatchers.Main) {
        updateUI(result)
    }
}
```

---

## Security Considerations

### Data Protection

1. **Encrypted Storage**: Use Android Keystore for sensitive data
2. **Secure Preferences**: Encrypt authentication credentials
3. **Biometric Authentication**: Use BiometricPrompt API

### Privacy

1. **Screenshot Protection**: FLAG_SECURE when enabled
2. **Content Hiding**: Blur sensitive content in app switcher
3. **18+ Content**: Require authentication for adult sources

### Network Security

1. **HTTPS Only**: All cloud backup communications
2. **Certificate Pinning**: For critical API endpoints
3. **OAuth 2.0**: For cloud provider authentication

---

## Migration Strategy

### Database Migrations

**New Tables:**
- ReadingStatistics
- ChapterReport
- CustomFont
- UpdateHistory

**Migration Script:**
```sql
-- Migration 1: Add ReadingStatistics table
CREATE TABLE IF NOT EXISTS ReadingStatistics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bookId INTEGER,
    chapterId INTEGER,
    readingTimeMinutes INTEGER,
    timestamp INTEGER,
    FOREIGN KEY (bookId) REFERENCES Book(id)
);

-- Migration 2: Add ChapterReport table
CREATE TABLE IF NOT EXISTS ChapterReport (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    chapterId INTEGER,
    bookId INTEGER,
    sourceId INTEGER,
    reason TEXT,
    description TEXT,
    timestamp INTEGER,
    status TEXT,
    FOREIGN KEY (chapterId) REFERENCES Chapter(id)
);
```

### Preference Migration

**New Preferences:**
- `reading_speed_wpm`
- `app_lock_enabled`
- `secure_screen_mode`
- `hide_content_enabled`
- `automatic_backup_frequency`

**Migration Code:**
```kotlin
fun migratePreferences(oldPrefs: Preferences): Preferences {
    return oldPrefs.copy(
        readingSpeedWPM = oldPrefs.readingSpeedWPM ?: 225,
        appLockEnabled = false, // Default to disabled
        secureScreenMode = SecureScreenMode.NEVER
    )
}
```

---

## Deployment Plan

### Phase 1: Core Features (Week 1)
- Statistics screen integration
- Security settings update
- Report broken chapter
- Configurable reading speed
- Cache size calculation

### Phase 2: Automation (Week 2)
- WorkManager automatic backups
- Cloud backup screen
- Statistics tracking implementation

### Phase 3: Reader Enhancements (Week 3)
- Brightness control
- Font picker
- Find in chapter
- Auto-scroll

### Phase 4: Advanced Features (Week 4)
- Batch operations
- Smart categories
- Translation features
- Font management

### Phase 5: Polish & Testing (Week 5)
- Bug fixes
- Performance optimization
- UI/UX improvements
- Documentation

---

## Rollback Strategy

### Feature Flags

```kotlin
object FeatureFlags {
    val STATISTICS_ENABLED = true
    val CLOUD_BACKUP_ENABLED = true
    val TRANSLATION_ENABLED = true
    val SMART_CATEGORIES_ENABLED = false // Gradual rollout
}
```

### Rollback Procedure

1. Disable feature flag
2. Revert navigation changes
3. Remove DI registrations
4. Keep database tables (for future re-enable)
5. Deploy hotfix

---

## Documentation

### User Documentation

1. **Statistics Guide**: How to interpret reading statistics
2. **Security Setup**: Step-by-step authentication setup
3. **Cloud Backup**: How to connect and use cloud providers
4. **Reader Features**: Guide to new reader controls

### Developer Documentation

1. **Architecture Overview**: System design and patterns
2. **API Reference**: Use case and repository interfaces
3. **Integration Guide**: How to add new features
4. **Testing Guide**: How to write and run tests

---

## Conclusion

This design leverages existing implementations and focuses on integration rather than new development. The architecture maintains clean separation of concerns, supports both Android and Desktop platforms, and provides a solid foundation for future enhancements. The phased deployment approach allows for iterative testing and refinement while minimizing risk to existing functionality.
