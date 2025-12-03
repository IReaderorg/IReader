# iOS Implementation Issues & Migration Notes

This document tracks known issues, limitations, and incomplete functionality in the iOS implementations of expect/actual classes.

## Critical Issues

### 1. EpubCreator - Stub ChapterRepository Method ✅ FIXED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/epub/EpubCreator.ios.kt`

**Issue:** The `findChaptersByBookId` extension function returned an empty list, making EPUB creation non-functional.

**Fix Applied:**
- Removed the stub extension function
- Now uses `findChaptersByBookIdWithContent()` to get chapters WITH their text content
- Uses `Chapter.isEmpty()` method to filter chapters without content (handles `List<Page>` content type)
- Added better error messages and documentation

---

### 2. ImportEpub - No Decompression Support ✅ FIXED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/epub/ImportEpub.ios.kt`

**Issue:** The ZIP extraction only handled uncompressed (STORED) entries. Most EPUB files use DEFLATE compression.

**Fix Applied:**
- Implemented pure Kotlin RFC 1951 DEFLATE decompression
- Added `DeflateInflater` class with full Huffman decoding support
- Supports both STORED (0) and DEFLATE (8) compression methods
- No iOS Compression framework dependency (works in domain module)
- Added extraction count logging

---

### 3. Services Missing Actual Implementation Logic

**Files:**
- `StartDownloadServicesUseCase.ios.kt`
- `StartLibraryUpdateServicesUseCase.ios.kt`
- `StartTTSServicesUseCase.ios.kt`

**Issue:** These services have the background task scheduling infrastructure but lack the actual business logic implementation.

**Example from StartDownloadServicesUseCase:**
```kotlin
private fun startImmediateDownload(bookIds: LongArray?, chapterIds: LongArray?, downloadModes: Boolean) {
    downloadJob = scope.launch {
        try {
            // Only logging, no actual download logic
            println("[DownloadService] Starting download for ${bookIds.size} books")
        }
    }
}
```

**Fix Required:** Inject and use the actual download/update use cases:
```kotlin
class StartDownloadServicesUseCase(
    private val downloadChaptersUseCase: DownloadChaptersUseCase,
    private val chapterRepository: ChapterRepository
) {
    // Use injected dependencies for actual work
}
```

---

## Medium Priority Issues

### 4. Cloud Providers - No OAuth Flow

**Files:**
- `DropboxProvider.ios.kt`
- `GoogleDriveProvider.ios.kt`

**Issue:** Both providers require manual access token setting. There's no OAuth authentication flow implemented.

```kotlin
// Current approach requires manual token:
fun setAccessToken(token: String) {
    accessToken = token
}
```

**Fix Required:** Implement OAuth 2.0 flow using ASWebAuthenticationSession:
```kotlin
import platform.AuthenticationServices.*

suspend fun authenticate(): Result<Unit> {
    // Use ASWebAuthenticationSession for OAuth
}
```

---

### 5. GoogleTranslateML - Rate Limiting

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/translate/GoogleTranslateML.ios.kt`

**Issue:** Uses unofficial Google Translate API which has aggressive rate limiting and may break.

**Recommendation:** 
- Add proper error handling for rate limits
- Consider using Apple's Translation framework (iOS 17.4+)
- Add fallback to LibreTranslate

---

### 6. OpenLocalFolder - Files App Integration

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/local/OpenLocalFolder.ios.kt`

**Issue:** The `shareddocuments://` URL scheme may not work on all iOS versions.

```kotlin
val filesAppUrl = NSURL.URLWithString("shareddocuments://${documentsUrl.path}")
```

**Fix:** Add fallback and version checking:
```kotlin
if #available(iOS 11.0, *) {
    // Use Files app integration
} else {
    // Fallback behavior
}
```

---

## Low Priority / Intentional Stubs

### 7. ExtensionWatcherService - No-op by Design

**File:** `domain/src/iosMain/kotlin/ireader/domain/services/ExtensionWatcherService.ios.kt`

**Status:** Intentionally stubbed. iOS uses JS plugins via JavaScriptCore, not file-based extensions.

---

### 8. PluginClassLoader - Unsupported by Design

**File:** `domain/src/iosMain/kotlin/ireader/domain/plugins/PluginClassLoader.ios.kt`

**Status:** Throws `UnsupportedOperationException`. iOS cannot load native plugins due to App Store restrictions.

---

### 9. AITTSManager - Limited Provider Support

**File:** `domain/src/iosMain/kotlin/ireader/domain/services/tts/AITTSManager.ios.kt`

**Status:** Only supports native iOS voices. Piper TTS and other providers are not available.

**Reason:** Piper requires native libraries that can't be easily ported to iOS.

---

## Fixed Implementations

### 10. ScheduleAutomaticBackupImpl ✅ ADDED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/backup/ScheduleAutomaticBackupImpl.ios.kt`

**Status:** New implementation added using BGTaskScheduler.

**Features:**
- Uses `BGProcessingTaskRequest` for longer execution time
- Falls back to `BGAppRefreshTaskRequest` if processing task fails
- Supports all backup frequencies (6h, 12h, daily, 2 days, weekly)
- Includes `registerAutomaticBackupTasks()` helper for AppDelegate setup

---

### 11. PluginLoader Decompression ✅ FIXED

**File:** `domain/src/iosMain/kotlin/ireader/domain/plugins/PluginLoader.ios.kt`

**Status:** Added proper DEFLATE decompression using pure Kotlin implementation.

**Changes:**
- Implemented RFC 1951 DEFLATE decompression in pure Kotlin
- Added `DeflateInflater` class with full Huffman decoding support
- Now supports both STORED (0) and DEFLATE (8) compression methods
- No external dependencies required

---

### 12. LNReaderBackupParser Decompression ✅ FIXED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/backup/lnreader/LNReaderBackupParserPlatform.ios.kt`

**Status:** Added proper DEFLATE decompression using pure Kotlin implementation.

**Changes:**
- Implemented RFC 1951 DEFLATE decompression in pure Kotlin
- Added `DeflateInflater` class with full Huffman decoding support
- Now supports both STORED and DEFLATE compression methods
- Better error logging

---

### 13. ImportEpub Decompression ✅ FIXED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/epub/ImportEpub.ios.kt`

**Status:** Added proper DEFLATE decompression using pure Kotlin implementation.

**Changes:**
- Implemented RFC 1951 DEFLATE decompression in pure Kotlin
- Added `DeflateInflater` class with full Huffman decoding support
- Now supports both STORED (0) and DEFLATE (8) compression methods
- No iOS Compression framework dependency required

---

## Info.plist Requirements

Ensure these are added for full iOS functionality:

```xml
<!-- Background Tasks -->
<key>BGTaskSchedulerPermittedIdentifiers</key>
<array>
    <!-- Automatic Backup -->
    <string>com.ireader.backup.automatic</string>
    <string>com.ireader.backup.refresh</string>
    <!-- Download Service -->
    <string>com.ireader.download.processing</string>
    <string>com.ireader.download.refresh</string>
    <!-- Library Update -->
    <string>com.ireader.library.update</string>
    <string>com.ireader.library.refresh</string>
    <!-- Plugin Updates -->
    <string>com.ireader.plugin.update</string>
</array>

<!-- Background Modes -->
<key>UIBackgroundModes</key>
<array>
    <string>audio</string>
    <string>fetch</string>
    <string>processing</string>
</array>
```

## AppDelegate Setup

Register all background tasks in `didFinishLaunchingWithOptions`:

```swift
// Register automatic backup tasks
registerAutomaticBackupTasks(backupScheduler)

// Register download background tasks
registerDownloadBackgroundTasks(downloadService)

// Register library update background tasks
registerLibraryUpdateBackgroundTasks(libraryUpdateService)

// Register plugin update background tasks
registerPluginUpdateBackgroundTask(pluginUpdateScheduler)
```

---

## Dependency Injection Notes

Several iOS implementations need proper DI setup. The following should be registered in `DomainModulePlatform.ios.kt`:

```kotlin
// Backup scheduling
single<ScheduleAutomaticBackup> { ScheduleAutomaticBackupImpl() }

// Cloud providers (need OAuth setup)
single { DropboxProvider() }
single { GoogleDriveProvider() }

// Services (need actual use case injection)
single { StartDownloadServicesUseCase(/* inject dependencies */) }
single { StartLibraryUpdateServicesUseCase(/* inject dependencies */) }
```

---

## Testing Recommendations

1. **EPUB Export:** Test with books that have chapters with content
2. **EPUB Import:** Test with both compressed and uncompressed EPUBs
3. **Background Tasks:** Use Xcode's "Simulate Background Fetch" feature
4. **Cloud Backup:** Test OAuth flow on real device (simulator has limitations)
5. **TTS:** Test with various languages and voice settings

---

## Version History

| Date | Change |
|------|--------|
| 2025-12-03 | Initial documentation created |
| 2025-12-03 | Added ScheduleAutomaticBackupImpl for iOS |
