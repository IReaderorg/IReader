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

### 3. Services Missing Actual Implementation Logic ⚠️ ARCHITECTURAL LIMITATION

**Files:**
- `StartDownloadServicesUseCase.ios.kt`
- `StartLibraryUpdateServicesUseCase.ios.kt`
- `StartTTSServicesUseCase.ios.kt`

**Issue:** These services have the background task scheduling infrastructure but lack the actual business logic implementation.

**Root Cause:** The `expect class` declarations in commonMain don't have constructor parameters, so actual implementations can't inject dependencies through constructors.

**Current State:**
- ✅ Background task scheduling works (BGTaskScheduler)
- ✅ Task registration helpers provided
- ⚠️ Actual download/update logic is placeholder (logging only)

**Workaround Options:**
1. **Service Locator Pattern:** Use Koin's `get()` inside the service to retrieve dependencies
2. **Setter Injection:** Add `setDependencies()` method similar to EpubCreator
3. **Common Module Change:** Modify expect declarations to include constructor parameters

**Example Workaround (Service Locator):**
```kotlin
actual class StartDownloadServicesUseCase {
    private val downloadUseCases: DownloadUseCases by lazy {
        // Get from Koin
        org.koin.core.context.GlobalContext.get().get()
    }
    
    private fun startImmediateDownload(...) {
        downloadJob = scope.launch {
            downloadUseCases.downloadChapters(chapterIds)
        }
    }
}
```

**Note:** Full implementation requires either common module changes or service locator pattern adoption.

---

## Medium Priority Issues

### 4. Cloud Providers - OAuth Flow ✅ IMPROVED

**Files:**
- `DropboxProvider.ios.kt`
- `GoogleDriveProvider.ios.kt`

**Issue:** Both providers required manual access token setting with no OAuth flow.

**Fix Applied:**
- Added OAuth 2.0 PKCE flow support to both providers
- Added `configure(appKey/clientId)` method for OAuth setup
- Added `startOAuthFlow()` to generate authorization URL
- Added `handleOAuthCallback(url)` to complete authentication
- Token storage using NSUserDefaults (Keychain ready)
- Automatic token refresh for Google Drive
- Backward compatible `setAccessToken()` still available

**Usage Example:**
```kotlin
// Configure provider
val provider = DropboxProvider()
provider.configure("your_app_key")

// Start OAuth flow
val authUrl = provider.startOAuthFlow()
// Open authUrl in ASWebAuthenticationSession

// Handle callback
provider.handleOAuthCallback(callbackUrl)

// Now authenticated
provider.uploadBackup(localPath, fileName)
```

**Note:** Full ASWebAuthenticationSession integration requires UI layer implementation.

---

### 5. GoogleTranslateML - Rate Limiting ✅ FIXED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/translate/GoogleTranslateML.ios.kt`

**Issue:** Uses unofficial Google Translate API which has aggressive rate limiting and may break.

**Fix Applied:**
- Added `RateLimitException` class for proper error handling
- Implemented exponential backoff retry logic (up to 3 retries)
- Added `enforceRateLimit()` to ensure minimum interval between requests
- Smaller batch sizes for free API (5 vs 50 for paid API)
- Longer delays between batches for free API
- HTTP 429 status code detection
- Graceful fallback to original text on persistent failures

---

### 6. OpenLocalFolder - Files App Integration ✅ FIXED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/local/OpenLocalFolder.ios.kt`

**Issue:** The `shareddocuments://` URL scheme may not work on all iOS versions.

**Fix Applied:**
- Added iOS version checking (Files app requires iOS 11+)
- Multiple fallback approaches:
  1. `tryOpenFilesAppWithPath()` - Opens Files app with specific path
  2. `tryOpenFilesAppDirect()` - Opens Files app without path
  3. `tryOpenDocumentsDirectory()` - Fallback to file:// URL
- Added `isFilesAppSupported()` helper method
- Added `getIOSVersion()` helper method
- Better error logging and completion handlers
- Fixed `isDirectory` detection using `NSFileType`

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

### 14. GoogleTranslateML Rate Limiting ✅ FIXED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/translate/GoogleTranslateML.ios.kt`

**Status:** Added comprehensive rate limiting and retry logic.

**Changes:**
- Exponential backoff retry (up to 3 attempts)
- HTTP 429 detection and handling
- Minimum request interval enforcement
- Smaller batch sizes for free API
- Graceful fallback to original text

---

### 15. OpenLocalFolder iOS Version Support ✅ FIXED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/local/OpenLocalFolder.ios.kt`

**Status:** Added iOS version checking and multiple fallback approaches.

**Changes:**
- iOS 11+ version check for Files app
- Multiple URL scheme fallbacks
- Better directory detection
- Helper methods for version checking

---

### 16. DropboxProvider OAuth Support ✅ IMPROVED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/backup/DropboxProvider.ios.kt`

**Status:** Added OAuth 2.0 PKCE flow support.

**Changes:**
- `configure(appKey, redirectUri)` for OAuth setup
- `startOAuthFlow()` generates authorization URL
- `handleOAuthCallback(url)` completes authentication
- PKCE code verifier/challenge generation
- Token storage with NSUserDefaults
- Backward compatible `setAccessToken()` method

---

### 17. GoogleDriveProvider OAuth Support ✅ IMPROVED

**File:** `domain/src/iosMain/kotlin/ireader/domain/usecases/backup/GoogleDriveProvider.ios.kt`

**Status:** Added OAuth 2.0 PKCE flow with token refresh.

**Changes:**
- `configure(clientId, redirectUri)` for OAuth setup
- `startOAuthFlow()` generates authorization URL
- `handleOAuthCallback(url)` completes authentication
- Automatic token refresh when expired
- `getValidToken()` handles token lifecycle
- Token storage with NSUserDefaults
- Proper OAuth scopes for Drive API

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

The iOS DI module (`DomainModulePlatform.ios.kt`) has been updated with all iOS-specific implementations:

```kotlin
// Backup scheduling
single<ScheduleAutomaticBackup> { ScheduleAutomaticBackupImpl() }

// Cloud providers (require OAuth tokens via setAccessToken())
single { DropboxProvider() }
single { GoogleDriveProvider() }

// Background services (infrastructure ready, need use case injection for full functionality)
single { StartDownloadServicesUseCase() }
single { StartLibraryUpdateServicesUseCase() }
single { StartTTSServicesUseCase() }
single { StartExtensionManagerService() }

// Translation with rate limiting
single { GoogleTranslateML() }

// EPUB (EpubCreator needs setDependencies() call)
single { EpubCreator() }
single { ImportEpub() }

// JavaScript engine
single { JSEngine() }
single { JSPluginUpdateScheduler() }
single { JSPluginUpdateNotifier() }

// TTS and other services
single { AITTSManager() }
single { ExtensionWatcherService() }
single { PluginClassLoader() }
```

### Post-Injection Setup Required

Some components need additional setup after injection:

1. **EpubCreator**: Call `setDependencies(httpClient, chapterRepository)` before use
2. **DropboxProvider/GoogleDriveProvider**: Call `setAccessToken(token)` after OAuth flow
3. **GoogleTranslateML**: Optionally call `setApiKey(key)` for Cloud API

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
| 2025-12-03 | Fixed EpubCreator content type handling |
| 2025-12-03 | Added pure Kotlin DEFLATE decompression to ImportEpub, PluginLoader, LNReaderBackupParser |
| 2025-12-03 | Added rate limiting and retry logic to GoogleTranslateML |
| 2025-12-03 | Added iOS version checking and fallbacks to OpenLocalFolder |
| 2025-12-03 | Added OAuth 2.0 PKCE flow to DropboxProvider and GoogleDriveProvider |
| 2025-12-03 | Updated DomainModulePlatform.ios.kt with full DI registration |
