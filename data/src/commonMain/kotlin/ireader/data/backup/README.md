# Google Drive Backup Implementation

## Overview

This package contains the complete implementation of Google Drive backup functionality for IReader. The implementation follows the requirements from task 11 of the TODO Implementation Roadmap.

## Components

### 1. GoogleDriveClient.kt
A complete Google Drive API v3 wrapper using Ktor HTTP client that provides:
- **uploadFile()**: Uploads files to appDataFolder using multipart upload
- **listFiles()**: Lists files in appDataFolder with pattern matching
- **downloadFile()**: Downloads files by ID
- **deleteFile()**: Deletes files by ID (handles 404 gracefully)
- **Exponential backoff retry**: Automatically retries network errors with exponential backoff (max 3 retries)

### 2. GoogleDriveBackupServiceImpl.kt
Complete implementation of the GoogleDriveBackupService interface:
- **authenticate()**: Delegates to platform-specific authenticator
- **createBackup()**: Serializes, compresses (GZIP), and uploads backup to Drive
- **listBackups()**: Queries Drive for backup files, parses timestamps, sorts by date
- **downloadBackup()**: Downloads, decompresses, and deserializes backup data
- **deleteBackup()**: Removes backup files from Drive
- Uses existing `compressData()` and `decompressData()` methods

### 3. GoogleDriveAuthenticator.kt
Interface for platform-specific OAuth2 authentication:
- Defines contract for authentication, token management, and disconnection
- Platform implementations provide secure token storage

### 4. Platform Implementations

#### GoogleDriveAuthenticatorAndroid.kt
Stub implementation for Android that needs to be completed in Task 10:
- Will use GoogleSignInClient with ActivityResultContracts
- Will store tokens in EncryptedSharedPreferences
- Will request drive.file scope for appDataFolder access

#### GoogleDriveAuthenticatorDesktop.kt
Stub implementation for Desktop that needs to be completed in Task 10:
- Will use browser-based OAuth 2.0 flow
- Will create local HTTP server for OAuth callback
- Will store tokens in platform-specific secure storage

## Dependency Injection

### BackupModule.kt
Main module that wires up the backup services:
- Includes platform-specific backup module
- Provides GoogleDriveBackupService implementation

### Platform Modules
- **BackupPlatformModule.android.kt**: Provides Android authenticator
- **BackupPlatformModule.desktop.kt**: Provides Desktop authenticator

The backup module is included in `repositoryInjectModule` for automatic registration.

## Usage

```kotlin
// Inject the service
val backupService: GoogleDriveBackupService by inject()

// Authenticate
val authResult = backupService.authenticate()
if (authResult.isSuccess) {
    val email = authResult.getOrNull()
    println("Authenticated as: $email")
}

// Create backup
val backupData = BackupData(
    novels = listOf(...),
    chapters = listOf(...),
    readingProgress = listOf(...),
    bookmarks = listOf(...)
)
val createResult = backupService.createBackup(backupData)
if (createResult.isSuccess) {
    val fileId = createResult.getOrNull()
    println("Backup created: $fileId")
}

// List backups
val listResult = backupService.listBackups()
if (listResult.isSuccess) {
    val backups = listResult.getOrNull()
    backups?.forEach { backup ->
        println("${backup.name} - ${backup.timestamp}")
    }
}

// Download backup
val downloadResult = backupService.downloadBackup(fileId)
if (downloadResult.isSuccess) {
    val restoredData = downloadResult.getOrNull()
    // Restore data to database
}

// Delete backup
val deleteResult = backupService.deleteBackup(fileId)
```

## File Naming Convention

Backup files follow the pattern: `ireader_backup_yyyyMMdd_HHmmss.json.gz`

Example: `ireader_backup_20250116_143022.json.gz`

This allows for:
- Easy identification of backup files
- Timestamp parsing for sorting
- Automatic filtering in Drive queries

## Error Handling

All operations return `Result<T>` types:
- Success: Contains the expected value
- Failure: Contains exception with descriptive error message

The GoogleDriveClient implements exponential backoff retry for:
- Network timeouts
- Connection errors
- 503 Service Unavailable
- 429 Too Many Requests

## Security

- Authentication tokens are managed by platform-specific authenticators
- Tokens are stored in secure platform storage (EncryptedSharedPreferences, Keychain, etc.)
- All API calls use HTTPS
- Backup files are stored in appDataFolder (app-private space in Drive)

## Next Steps (Task 10)

To complete the backup functionality, implement the platform-specific authenticators:

1. **Android**: Implement GoogleDriveAuthenticatorAndroid
   - Add Google Sign-In dependencies
   - Configure OAuth2 credentials in Google Cloud Console
   - Implement ActivityResultContracts for sign-in flow
   - Store tokens in EncryptedSharedPreferences

2. **Desktop**: Implement GoogleDriveAuthenticatorDesktop
   - Implement browser-based OAuth flow
   - Create local HTTP server for callback
   - Store tokens in platform-specific secure storage

## Testing

To test the implementation:

1. Implement a platform-specific authenticator (or create a mock)
2. Configure Google Drive API credentials
3. Test each operation:
   - Authentication flow
   - Backup creation with various data sizes
   - Listing backups
   - Downloading and verifying backup data
   - Deleting backups
4. Test error scenarios:
   - Network failures (verify retry logic)
   - Invalid credentials
   - Missing files (404 handling)

## Requirements Satisfied

This implementation satisfies the following requirements from the spec:

- ✅ 1.3: Create and upload backup to Google Drive
- ✅ 1.4: Serialize library data to JSON and compress with GZIP
- ✅ 1.5: Upload to appDataFolder using Drive API v3
- ✅ 1.6: List all available backups from Google Drive
- ✅ 1.7: Download and restore backup data
- ✅ 1.9: Delete backups from Google Drive

Requirements 1.1, 1.2, and 1.10 (authentication) will be completed in Task 10.
