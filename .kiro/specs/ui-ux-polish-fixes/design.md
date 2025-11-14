# Design Document

## Overview

This design addresses 17 critical UI/UX issues across the IReader multiplatform application. The solution focuses on fixing broken interactions, implementing missing features, and enhancing visual design while maintaining production-ready code quality. All implementations will follow Material Design 3 guidelines, support accessibility standards, and work consistently across Android, iOS, and Desktop platforms.

The design leverages the existing Compose Multiplatform architecture with minimal structural changes, focusing on targeted fixes and enhancements to existing components and screens.

## Architecture

### Component Architecture

The application uses a declarative component system (`Components` sealed class) for building settings screens. This system will be enhanced to support:

1. **Real-time Value Display**: Slider components will be updated to show live value changes
2. **Dismissible Panels**: Reader toolbar panels will support tap-outside-to-dismiss behavior
3. **Platform-Specific Behaviors**: Desktop scrolling and mobile touch targets will be optimized

### State Management

All state is managed through ViewModels using Kotlin StateFlow and Compose State. The design maintains this pattern while adding:

1. **Brightness State**: New state management for screen brightness control
2. **Extension Monitoring**: File system watcher for real-time extension detection
3. **Download State**: Enhanced download progress and error tracking
4. **Sync Feedback**: User-facing feedback for cloud sync operations

### Platform Layer

Platform-specific implementations will be added for:

- **Desktop**: File system monitoring for extensions, enhanced scroll behavior
- **Android**: Brightness control, TTS control sizing
- **iOS**: Brightness control (where supported)


## Components and Interfaces

### 1. Enhanced Slider Component

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/components/SliderPreference.kt`

**Changes**:
- Add real-time value display that updates during drag
- Support formatting functions for display values
- Maintain existing preference binding system

**Interface**:
```kotlin
@Composable
fun SliderPreference(
    // Existing parameters...
    trailing: String? = null,  // Already exists - will be updated in real-time
    onValueChange: ((Float) -> Unit)? = null,  // Will trigger on every change
    // ...
)
```

### 2. Desktop Scroll Enhancement

**Location**: `presentation/src/desktopMain/kotlin/ireader/presentation/ui/core/modifier/ScrollModifiers.kt` (new)

**Implementation**:
- Create desktop-specific scroll modifiers
- Add mouse wheel and trackpad support
- Implement smooth scrolling with momentum

**Interface**:
```kotlin
fun Modifier.desktopScroll(
    scrollState: ScrollState,
    enabled: Boolean = true
): Modifier
```

### 3. Download Manager Enhancement

**Location**: `domain/src/commonMain/kotlin/ireader/domain/usecases/download/DownloadManager.kt`

**Changes**:
- Add progress tracking per download
- Implement error handling with user-friendly messages
- Add success/failure notifications

**Interface**:
```kotlin
data class DownloadState(
    val chapterId: Long,
    val progress: Float,
    val status: DownloadStatus,
    val error: String? = null
)

enum class DownloadStatus {
    QUEUED, DOWNLOADING, COMPLETED, FAILED
}
```

### 4. Cloud Backup Service

**Location**: `data/src/commonMain/kotlin/ireader/data/repository/BackupRepository.kt`

**Implementation**:
- Implement Supabase sync for reading progress, bookmarks, and library
- Add sync status feedback via SnackBar
- Handle sync errors with retry logic

**Interface**:
```kotlin
interface BackupRepository {
    suspend fun syncToCloud(): Result<SyncResult>
    suspend fun restoreFromCloud(): Result<RestoreResult>
}

data class SyncResult(
    val progressSynced: Int,
    val bookmarksSynced: Int,
    val librarySynced: Int,
    val timestamp: Long
)
```


### 5. Font Management System

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/font_screens/FontManager.kt`

**Implementation**:
- File picker integration for font import
- Font validation (TTF, OTF formats)
- Font storage and retrieval
- Real-time font preview

**Interface**:
```kotlin
interface FontManager {
    suspend fun importFont(uri: String): Result<Font>
    suspend fun getAvailableFonts(): List<Font>
    suspend fun deleteFont(fontId: String): Result<Unit>
}

data class Font(
    val id: String,
    val name: String,
    val path: String,
    val isCustom: Boolean
)
```

### 6. Security Password Manager

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/security/SecuritySettingsViewModel.kt`

**Changes**:
- Implement password setup dialog with confirmation
- Add secure password storage using platform keystore
- Implement password validation

**Interface**:
```kotlin
sealed class AuthMethod {
    data class PIN(val pin: String) : AuthMethod()
    data class Password(val password: String) : AuthMethod()
    object Biometric : AuthMethod()
}

fun setupAuthMethod(method: AuthMethod): Result<Unit>
fun validatePassword(password: String, confirmation: String): ValidationResult
```

### 7. Repository UI Redesign

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/repository/AddRepositoryScreen.kt`

**Implementation**:
- Material Design 3 form layout
- Clear input validation
- Helpful placeholder text
- Visual feedback for validation

**Design Pattern**:
- Use OutlinedTextField with proper labels
- Add input validation with error messages
- Implement loading states during repository addition
- Show success confirmation

### 8. Extension File Watcher (Desktop)

**Location**: `desktop/src/desktopMain/kotlin/ireader/desktop/extensions/ExtensionWatcher.kt` (new)

**Implementation**:
- Monitor extensions directory using Java WatchService
- Detect new/removed extension files
- Trigger extension list refresh
- Show notification on extension changes

**Interface**:
```kotlin
class ExtensionWatcher(
    private val extensionsDir: File,
    private val onExtensionChanged: (ExtensionEvent) -> Unit
) {
    fun start()
    fun stop()
}

sealed class ExtensionEvent {
    data class Added(val extensionFile: File) : ExtensionEvent()
    data class Removed(val extensionFile: File) : ExtensionEvent()
}
```


### 9. Global Search UI Enhancement

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/GlobalSearchScreen.kt`

**Implementation**:
- Modern card-based layout for results
- Skeleton loading states
- Empty state illustrations
- Smooth animations

**Design Pattern**:
- Use Material Design 3 Cards for results
- Implement shimmer loading effect
- Add search suggestions
- Show source badges on results

### 10. History Management Enhancement

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/history/HistoryScreen.kt`

**Implementation**:
- Add "Clear All" button in top app bar
- Implement confirmation dialog
- Batch delete operation
- Show deletion progress

**Interface**:
```kotlin
suspend fun clearAllHistory(): Result<Int>  // Returns count of deleted items
suspend fun deleteHistoryItem(itemId: Long): Result<Unit>
```

### 11. Authentication Error Handler

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/auth/AuthViewModel.kt`

**Implementation**:
- Map error codes to user-friendly messages
- Provide actionable guidance
- Show errors in Material Design 3 dialogs

**Error Mapping**:
```kotlin
sealed class AuthError {
    object InvalidCredentials : AuthError()
    object NetworkError : AuthError()
    object ServerError : AuthError()
    data class Unknown(val message: String) : AuthError()
}

fun AuthError.toUserMessage(): String
```

### 12. Book Detail Back Button Enhancement

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailTopAppBar.kt`

**Implementation**:
- Make back button always visible with elevated surface
- Add shadow/elevation for contrast
- Ensure proper z-index layering

**Design Pattern**:
- Use FloatingActionButton for back button
- Position with absolute positioning
- Add semi-transparent background
- Ensure 48dp minimum touch target


### 13. WebView UI Enhancement

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/video/component/WebViewScreen.kt`

**Implementation**:
- Add modern top app bar with navigation
- Show loading progress bar
- Add refresh and share actions
- Implement error states

**Design Pattern**:
- Material Design 3 TopAppBar
- LinearProgressIndicator for loading
- Error screen with retry button
- Navigation controls (back, forward, refresh)

### 14. Reader Toolbar Panel Dismissal

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/`

**Implementation**:
- Add click-outside-to-dismiss behavior
- Use Compose's Popup or ModalBottomSheet
- Maintain existing toggle functionality

**Pattern**:
```kotlin
@Composable
fun DismissiblePanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    if (visible) {
        Box(modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
        ) {
            // Panel content with click propagation stopped
            Box(modifier = Modifier.clickable(enabled = false) {}) {
                content()
            }
        }
    }
}
```

### 15. Brightness Control Implementation

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/BrightnessControl.kt`

**Implementation**:
- Platform-specific brightness APIs
- Slider for brightness adjustment
- Persist brightness preference
- Show current brightness percentage

**Platform Implementations**:
- **Android**: Use WindowManager.LayoutParams.screenBrightness
- **Desktop**: Use system brightness APIs where available
- **iOS**: Use UIScreen.brightness

**Interface**:
```kotlin
expect class BrightnessManager {
    fun getBrightness(): Float
    fun setBrightness(value: Float)
}
```

### 16. TTS Control Size Enhancement

**Location**: `presentation/src/androidMain/kotlin/ireader/presentation/ui/home/tts/TTSScreen.kt`

**Implementation**:
- Increase icon sizes to 48dp minimum
- Add proper touch target padding
- Ensure accessibility compliance

**Design Pattern**:
- Use IconButton with size modifier
- Add contentDescription for accessibility
- Ensure 48dp x 48dp minimum touch target


### 17. Rest Reminder Feature

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/RestReminderDialog.kt` (new)

**Implementation**:
- Track reading time in ReaderViewModel
- Show reminder dialog at configured intervals
- Add settings for reminder configuration
- Implement snooze functionality

**Interface**:
```kotlin
data class RestReminderState(
    val enabled: Boolean,
    val intervalMinutes: Int,
    val lastReminderTime: Long,
    val totalReadingTime: Long
)

@Composable
fun RestReminderDialog(
    onDismiss: () -> Unit,
    onSnooze: (minutes: Int) -> Unit,
    onTakeBreak: () -> Unit
)
```

## Data Models

### Download State Model
```kotlin
data class DownloadState(
    val chapterId: Long,
    val bookId: Long,
    val chapterName: String,
    val progress: Float,
    val status: DownloadStatus,
    val error: DownloadError? = null,
    val startTime: Long,
    val endTime: Long? = null
)

enum class DownloadStatus {
    QUEUED, DOWNLOADING, COMPLETED, FAILED, CANCELLED
}

sealed class DownloadError {
    object NetworkError : DownloadError()
    object StorageError : DownloadError()
    object ContentNotFound : DownloadError()
    data class ServerError(val code: Int) : DownloadError()
}
```

### Sync State Model
```kotlin
data class SyncState(
    val isSyncing: Boolean,
    val lastSyncTime: Long?,
    val syncResult: SyncResult?
)

sealed class SyncResult {
    data class Success(
        val itemsSynced: Int,
        val timestamp: Long
    ) : SyncResult()
    
    data class Failure(
        val error: SyncError,
        val timestamp: Long
    ) : SyncResult()
}

sealed class SyncError {
    object NetworkError : SyncError()
    object AuthenticationError : SyncError()
    object ServerError : SyncError()
}
```

### Font Model
```kotlin
data class CustomFont(
    val id: String,
    val name: String,
    val filePath: String,
    val fileSize: Long,
    val importDate: Long,
    val isValid: Boolean
)
```


## Error Handling

### Principles
1. **User-Friendly Messages**: All errors shown to users must be clear and actionable
2. **Graceful Degradation**: Features should fail gracefully without crashing
3. **Retry Mechanisms**: Network operations should support retry with exponential backoff
4. **Logging**: All errors logged for debugging while showing simplified messages to users

### Error Handling Patterns

#### Network Errors
```kotlin
sealed class NetworkError {
    object NoConnection : NetworkError()
    object Timeout : NetworkError()
    data class HttpError(val code: Int, val message: String) : NetworkError()
}

fun NetworkError.toUserMessage(): String = when (this) {
    is NoConnection -> "No internet connection. Please check your network."
    is Timeout -> "Request timed out. Please try again."
    is HttpError -> when (code) {
        401 -> "Authentication failed. Please sign in again."
        403 -> "Access denied. Check your permissions."
        404 -> "Content not found."
        500, 502, 503 -> "Server error. Please try again later."
        else -> "Network error occurred. Please try again."
    }
}
```

#### File Operation Errors
```kotlin
sealed class FileError {
    object NotFound : FileError()
    object PermissionDenied : FileError()
    object InvalidFormat : FileError()
    object StorageFull : FileError()
}

fun FileError.toUserMessage(): String = when (this) {
    is NotFound -> "File not found."
    is PermissionDenied -> "Permission denied. Please grant storage access."
    is InvalidFormat -> "Invalid file format. Please select a valid font file."
    is StorageFull -> "Storage full. Please free up space and try again."
}
```

#### Validation Errors
```kotlin
sealed class ValidationError {
    object EmptyField : ValidationError()
    object InvalidFormat : ValidationError()
    object PasswordMismatch : ValidationError()
    object WeakPassword : ValidationError()
}

fun ValidationError.toUserMessage(fieldName: String): String = when (this) {
    is EmptyField -> "$fieldName cannot be empty."
    is InvalidFormat -> "Invalid $fieldName format."
    is PasswordMismatch -> "Passwords do not match."
    is WeakPassword -> "Password must be at least 6 characters."
}
```

## Testing Strategy

### Unit Tests
- **ViewModels**: Test state management and business logic
- **Use Cases**: Test download, sync, and font management logic
- **Validators**: Test input validation functions
- **Error Mappers**: Test error message generation

### Integration Tests
- **Download Flow**: Test complete download process
- **Sync Flow**: Test cloud backup and restore
- **Font Import**: Test font file validation and import
- **Authentication**: Test password setup and validation

### UI Tests
- **Slider Interaction**: Verify real-time value updates
- **Panel Dismissal**: Verify tap-outside-to-dismiss behavior
- **Form Validation**: Verify error messages display correctly
- **Accessibility**: Verify touch targets meet minimum size requirements

### Platform-Specific Tests
- **Desktop**: Test file watcher and scroll behavior
- **Android**: Test brightness control and TTS controls
- **iOS**: Test brightness control where supported


## Performance Considerations

### Slider Performance
- Use `remember` to avoid recomposition of formatting functions
- Debounce preference writes during drag (write only on release)
- Update display value immediately but persist on drag end

### Extension Monitoring
- Use efficient file system watchers (WatchService on JVM)
- Debounce file system events to avoid excessive updates
- Run monitoring on background coroutine

### Download Management
- Limit concurrent downloads based on user preference
- Use WorkManager/background tasks for reliability
- Implement proper cancellation support

### Sync Operations
- Batch sync operations to reduce API calls
- Implement incremental sync (only changed data)
- Use background sync with periodic work

### UI Rendering
- Use LazyColumn for scrollable lists
- Implement proper keys for list items
- Avoid unnecessary recompositions with `remember` and `derivedStateOf`

## Accessibility

### Touch Targets
- Minimum 48dp x 48dp for all interactive elements
- Proper spacing between interactive elements
- Clear visual feedback on interaction

### Screen Readers
- Proper contentDescription for all icons and images
- Semantic roles for interactive elements
- Announce state changes (e.g., "Download complete")

### Keyboard Navigation
- Support tab navigation on desktop
- Implement keyboard shortcuts where appropriate
- Ensure focus indicators are visible

### Color Contrast
- Ensure 4.5:1 contrast ratio for text
- Use Material Design 3 color system
- Support dark mode with proper contrast

## Security Considerations

### Password Storage
- Use platform keystore (Android Keystore, iOS Keychain, Windows Credential Manager)
- Never store passwords in plain text
- Hash passwords with bcrypt or similar

### Biometric Authentication
- Use platform biometric APIs
- Fallback to PIN/password if biometric fails
- Respect user's biometric settings

### Secure Screen
- Prevent screenshots in sensitive screens
- Block screen recording where supported
- Clear sensitive data from memory when not needed

## Migration Strategy

### Existing Data
- No breaking changes to existing data models
- Add new fields with default values
- Implement migration for preference changes

### Backward Compatibility
- Maintain existing API contracts
- Add new features as opt-in where possible
- Provide fallbacks for unsupported platforms

### Rollout Plan
1. Deploy slider and UI fixes first (low risk)
2. Deploy download and sync enhancements (medium risk)
3. Deploy security features (requires user action)
4. Deploy extension monitoring (desktop only)
