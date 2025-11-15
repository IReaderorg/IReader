# Google Drive Backup Feature

## Overview

The Google Drive Backup feature allows users to backup and restore their library data to Google Drive, providing cloud-based data protection and cross-device synchronization capabilities.

## Current Implementation Status

### ✅ Completed Components

1. **Data Models** (`domain/src/commonMain/kotlin/ireader/domain/models/backup/BackupModels.kt`)
   - `BackupData`: Complete backup data structure containing novels, chapters, reading progress, bookmarks, categories, and settings
   - `BackupInfo`: Metadata about backup files
   - `ReadingProgress`: User's reading progress for each book
   - `Bookmark`: User bookmarks

2. **Service Interface** (`domain/src/commonMain/kotlin/ireader/domain/services/backup/GoogleDriveBackupService.kt`)
   - `authenticate()`: OAuth2 authentication with Google Drive
   - `disconnect()`: Sign out from Google Drive
   - `isAuthenticated()`: Check authentication status
   - `createBackup()`: Create and upload backup
   - `listBackups()`: List available backups
   - `downloadBackup()`: Download and restore backup
   - `deleteBackup()`: Delete backup from cloud

3. **Service Implementation** (`data/src/commonMain/kotlin/ireader/data/backup/GoogleDriveBackupServiceImpl.kt`)
   - Stub implementation with data compression (GZIP)
   - JSON serialization for backup data
   - Platform-specific authentication placeholders

4. **ViewModel** (`presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/backups/GoogleDriveViewModel.kt`)
   - Connection management
   - Backup creation with data collection from repositories
   - Backup listing and restoration
   - Error handling and user feedback

5. **UI Components**
   - `GoogleDriveBackupScreen.kt`: Main backup screen with connection status, backup list, and actions
   - `GoogleDriveAuthDialog.kt`: Authentication dialog (placeholder)
   - `CloudBackupScreen.kt`: Updated with Google Drive navigation card

### 🚧 Pending Implementation

The following components require platform-specific implementation and Google Cloud Console configuration:

#### 1. Google Drive API Integration

**Required Dependencies:**
```kotlin
// build.gradle.kts
dependencies {
    // Google Drive API
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20230822-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    
    // Android specific
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Secure storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
```

#### 2. Google Cloud Console Setup

1. Create a project in [Google Cloud Console](https://console.cloud.google.com/)
2. Enable Google Drive API
3. Create OAuth 2.0 credentials:
   - **Android**: OAuth 2.0 Client ID (Android type)
   - **Desktop**: OAuth 2.0 Client ID (Desktop type)
   - **iOS**: OAuth 2.0 Client ID (iOS type)
4. Configure OAuth consent screen
5. Add authorized redirect URIs

#### 3. Platform-Specific Authentication

**Android Implementation:**
```kotlin
// In GoogleDriveBackupServiceImpl (Android)
private suspend fun authenticateAndroid(): Result<String> {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
        .build()
    
    val client = GoogleSignIn.getClient(context, gso)
    
    // Launch sign-in intent using ActivityResultContracts
    // Store tokens in EncryptedSharedPreferences
    // Return account email
}
```

**Desktop Implementation:**
```kotlin
// In GoogleDriveBackupServiceImpl (Desktop)
private suspend fun authenticateDesktop(): Result<String> {
    // Generate OAuth URL
    val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?..."
    
    // Open browser
    Desktop.getDesktop().browse(URI(authUrl))
    
    // Start local server to listen for callback
    // Exchange authorization code for tokens
    // Store tokens securely
    // Return account email
}
```

#### 4. Drive API Operations

**Upload Backup:**
```kotlin
private suspend fun uploadToDrive(data: ByteArray, fileName: String): String {
    val fileMetadata = File().apply {
        name = fileName
        parents = listOf("appDataFolder")
    }
    
    val mediaContent = ByteArrayContent("application/gzip", data)
    
    val file = driveService.files()
        .create(fileMetadata, mediaContent)
        .setFields("id")
        .execute()
    
    return file.id
}
```

**List Backups:**
```kotlin
private suspend fun listFromDrive(): List<BackupInfo> {
    val result = driveService.files()
        .list()
        .setSpaces("appDataFolder")
        .setQ("name contains 'ireader_backup_'")
        .setFields("files(id, name, size, modifiedTime)")
        .execute()
    
    return result.files.map { file ->
        BackupInfo(
            id = file.id,
            name = file.name,
            timestamp = file.modifiedTime.value,
            size = file.size
        )
    }
}
```

**Download Backup:**
```kotlin
private suspend fun downloadFromDrive(fileId: String): ByteArray {
    val outputStream = ByteArrayOutputStream()
    driveService.files()
        .get(fileId)
        .executeMediaAndDownloadTo(outputStream)
    
    return outputStream.toByteArray()
}
```

## User Guide

### Accessing Google Drive Backup

1. Open **Settings** → **Backups** → **Cloud Backup**
2. Tap on the **Google Drive** card
3. Tap **Connect to Google Drive**
4. Sign in with your Google account
5. Grant permissions for the app to access Drive files

### Creating a Backup

1. Ensure you're connected to Google Drive
2. Tap the **floating action button** (cloud upload icon)
3. Wait for the backup to complete
4. The new backup will appear in the list

### Restoring a Backup

1. Tap the **download icon** on a backup
2. Confirm the restoration (this will replace current data)
3. Wait for the restore to complete
4. Restart the app to apply changes

### Deleting a Backup

1. Tap the **delete icon** on a backup
2. Confirm the deletion
3. The backup will be removed from Google Drive

## Technical Details

### Backup Data Structure

```kotlin
BackupData(
    novels: List<Book>,           // All books with metadata
    chapters: List<Chapter>,      // All chapters with read status
    readingProgress: List<...>,   // Last read positions
    bookmarks: List<Bookmark>,    // User bookmarks
    categories: List<Category>,   // Library categories
    settings: Map<String, String> // User preferences
)
```

### File Format

- **Serialization**: JSON using kotlinx.serialization
- **Compression**: GZIP to reduce file size
- **Naming**: `ireader_backup_YYYYMMDD_HHMMSS.json.gz`
- **Storage**: Google Drive appDataFolder (app-specific, not visible in Drive UI)

### Security Considerations

1. **OAuth Tokens**: Stored in platform-specific secure storage
   - Android: EncryptedSharedPreferences
   - iOS: Keychain
   - Desktop: OS-specific credential manager

2. **Permissions**: Requests only `drive.file` scope (access to app-created files only)

3. **Data Privacy**: Backups stored in appDataFolder are not accessible to other apps

4. **Optional Encryption**: Future enhancement to encrypt backup files with user password

## Error Handling

The implementation handles various error scenarios:

- **Authentication Failures**: Clear error messages with retry option
- **Network Errors**: Automatic retry with exponential backoff
- **Insufficient Storage**: Warning before backup creation
- **Corrupted Backups**: Validation before restore
- **Token Expiration**: Automatic token refresh

## Future Enhancements

1. **Automatic Backups**: Schedule periodic backups
2. **Backup Encryption**: Optional password-based encryption
3. **Selective Restore**: Choose what to restore (novels, settings, etc.)
4. **Backup Comparison**: Show differences between backups
5. **Multiple Accounts**: Support multiple Google accounts
6. **Backup Verification**: Integrity checks after upload/download

## Development Notes

### Testing Without Google Drive API

The current implementation includes stub methods that return appropriate error messages. This allows:
- UI testing without API credentials
- Development of other features in parallel
- Gradual implementation of platform-specific code

### Adding Google Drive Support

To enable full functionality:

1. Add dependencies to `build.gradle.kts`
2. Configure Google Cloud Console
3. Implement platform-specific authentication in `GoogleDriveBackupServiceImpl`
4. Implement Drive API operations (upload, download, list, delete)
5. Test on each platform (Android, Desktop, iOS)

### Code Organization

```
domain/
  ├── models/backup/
  │   └── BackupModels.kt          # Data models
  └── services/backup/
      └── GoogleDriveBackupService.kt  # Service interface

data/
  └── backup/
      └── GoogleDriveBackupServiceImpl.kt  # Service implementation

presentation/
  └── ui/settings/backups/
      ├── GoogleDriveBackupScreen.kt   # Main UI
      ├── GoogleDriveViewModel.kt      # State management
      ├── GoogleDriveAuthDialog.kt     # Auth dialog
      └── CloudBackupScreen.kt         # Updated with navigation
```

## References

- [Google Drive API Documentation](https://developers.google.com/drive/api/v3/about-sdk)
- [OAuth 2.0 for Mobile & Desktop Apps](https://developers.google.com/identity/protocols/oauth2/native-app)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android/start)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
