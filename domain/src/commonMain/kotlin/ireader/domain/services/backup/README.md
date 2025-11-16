# Google Drive Authentication System

This directory contains the Google Drive authentication implementation for IReader's backup system.

## Overview

The `GoogleDriveAuthenticator` interface provides platform-specific OAuth2 authentication for Google Drive API access. Each platform (Android, Desktop) has its own implementation that handles the OAuth2 flow appropriately.

## Architecture

### Common Interface
- **Location**: `GoogleDriveAuthenticator.kt`
- **Purpose**: Defines the contract for Google Drive authentication across all platforms
- **Methods**:
  - `authenticate()`: Initiates OAuth2 flow and returns user email
  - `refreshToken()`: Refreshes expired access tokens
  - `isAuthenticated()`: Checks if user has valid authentication
  - `getAccessToken()`: Retrieves current access token for API calls
  - `disconnect()`: Revokes tokens and signs out

### Android Implementation
- **Location**: `androidMain/kotlin/.../GoogleDriveAuthenticator.android.kt`
- **Features**:
  - Browser-based OAuth2 flow
  - Secure token storage using `EncryptedSharedPreferences`
  - Automatic token refresh
  - Token revocation on disconnect

### Desktop Implementation
- **Location**: `desktopMain/kotlin/.../GoogleDriveAuthenticator.desktop.kt`
- **Features**:
  - Browser-based OAuth2 flow with local HTTP server
  - Random port selection for OAuth callback
  - Token storage using Java Preferences API
  - Automatic token refresh
  - Cross-platform support (Windows, macOS, Linux)

## Setup Requirements

### 1. Google Cloud Console Configuration

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing project
3. Enable Google Drive API
4. Create OAuth2 credentials:
   - Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client ID"
   - For Android:
     - Application type: Android
     - Package name: `com.ireader.app` (or your app's package)
     - SHA-1 certificate fingerprint (from your keystore)
   - For Desktop:
     - Application type: Desktop app
     - Name: IReader Desktop
5. Note your `CLIENT_ID` and `CLIENT_SECRET`

### 2. Configure Redirect URIs

Add the following redirect URIs in Google Cloud Console:

**Android:**
```
com.ireader.app:/oauth2redirect
```

**Desktop:**
```
http://localhost:8000/oauth2callback
http://localhost:8001/oauth2callback
... (add ports 8000-9000 or use wildcard if supported)
```

### 3. Code Integration

#### Android Example

```kotlin
// In your Android module (presentation or app layer)
import ireader.domain.services.backup.AndroidGoogleDriveAuthenticator

class BackupActivity : ComponentActivity() {
    private lateinit var authenticator: AndroidGoogleDriveAuthenticator
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize authenticator
        authenticator = AndroidGoogleDriveAuthenticator(
            context = applicationContext,
            clientId = "YOUR_CLIENT_ID.apps.googleusercontent.com",
            clientSecret = "YOUR_CLIENT_SECRET"
        )
        
        // Start OAuth flow
        lifecycleScope.launch {
            // Open browser with authorization URL
            val authUrl = authenticator.getAuthorizationUrl()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
            startActivity(intent)
        }
    }
    
    // Handle OAuth redirect
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        
        intent?.data?.let { uri ->
            if (uri.scheme == "com.ireader.app" && uri.host == "oauth2redirect") {
                val code = uri.getQueryParameter("code")
                if (code != null) {
                    lifecycleScope.launch {
                        val result = authenticator.exchangeAuthorizationCode(code)
                        result.onSuccess { email ->
                            // Authentication successful
                            Toast.makeText(this@BackupActivity, 
                                "Authenticated as $email", 
                                Toast.LENGTH_SHORT).show()
                        }.onFailure { error ->
                            // Handle error
                            Toast.makeText(this@BackupActivity, 
                                "Authentication failed: ${error.message}", 
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
```

**AndroidManifest.xml:**
```xml
<activity android:name=".BackupActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="com.ireader.app"
            android:host="oauth2redirect" />
    </intent-filter>
</activity>
```

#### Desktop Example

```kotlin
// In your Desktop module
import ireader.domain.services.backup.DesktopGoogleDriveAuthenticator

class BackupViewModel {
    private val authenticator = DesktopGoogleDriveAuthenticator(
        clientId = "YOUR_CLIENT_ID.apps.googleusercontent.com",
        clientSecret = "YOUR_CLIENT_SECRET"
    )
    
    suspend fun authenticate() {
        val result = authenticator.authenticate()
        result.onSuccess { email ->
            println("Authenticated as $email")
            // Proceed with backup operations
        }.onFailure { error ->
            println("Authentication failed: ${error.message}")
        }
    }
    
    suspend fun createBackup() {
        // Check if authenticated
        if (!authenticator.isAuthenticated()) {
            authenticate()
            return
        }
        
        // Get access token for API calls
        val accessToken = authenticator.getAccessToken()
        if (accessToken != null) {
            // Use token for Google Drive API calls
            // ...
        }
    }
}
```

## Token Management

### Automatic Token Refresh

Both implementations automatically check token expiry and provide a `refreshToken()` method:

```kotlin
// Check if token needs refresh
if (!authenticator.isAuthenticated()) {
    val result = authenticator.refreshToken()
    result.onSuccess {
        // Token refreshed, proceed with API call
    }.onFailure {
        // Refresh failed, need to re-authenticate
        authenticator.authenticate()
    }
}
```

### Token Storage

- **Android**: Tokens are stored in `EncryptedSharedPreferences` with AES256-GCM encryption
- **Desktop**: Tokens are stored in Java Preferences API (platform-specific secure storage)

### Token Expiry

Tokens are automatically considered expired 5 minutes before their actual expiry time to prevent race conditions.

## Security Considerations

1. **Never hardcode credentials**: Store `CLIENT_ID` and `CLIENT_SECRET` in:
   - Environment variables
   - Build configuration
   - Secure configuration files (not in version control)

2. **Token Security**:
   - Android: Uses Android Keystore via EncryptedSharedPreferences
   - Desktop: Uses platform-specific secure storage

3. **Scope Limitation**: Only requests `drive.file` scope (access to files created by the app)

4. **Token Revocation**: Always call `disconnect()` when user signs out

## Error Handling

All methods return `Result<T>` for proper error handling:

```kotlin
authenticator.authenticate().fold(
    onSuccess = { email ->
        // Handle success
    },
    onFailure = { error ->
        when {
            error.message?.contains("user_cancelled") == true -> {
                // User cancelled authentication
            }
            error.message?.contains("network") == true -> {
                // Network error
            }
            else -> {
                // Other errors
            }
        }
    }
)
```

## Testing

### Manual Testing

1. **Android**:
   - Run app on device or emulator
   - Trigger authentication flow
   - Verify browser opens with Google sign-in
   - Complete sign-in and verify redirect back to app
   - Check that tokens are stored securely

2. **Desktop**:
   - Run desktop application
   - Trigger authentication flow
   - Verify browser opens with Google sign-in
   - Complete sign-in and verify callback to local server
   - Check that tokens are stored in preferences

### Integration with GoogleDriveBackupService

The authenticator is designed to be used by `GoogleDriveBackupServiceImpl`:

```kotlin
class GoogleDriveBackupServiceImpl(
    private val authenticator: GoogleDriveAuthenticator
) : GoogleDriveBackupService {
    
    override suspend fun authenticate(): Result<String> {
        return authenticator.authenticate()
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return authenticator.isAuthenticated()
    }
    
    override suspend fun createBackup(data: BackupData): Result<String> {
        // Ensure authenticated
        if (!authenticator.isAuthenticated()) {
            return Result.failure(Exception("Not authenticated"))
        }
        
        // Get access token
        val accessToken = authenticator.getAccessToken()
            ?: return Result.failure(Exception("No access token"))
        
        // Use token for Drive API calls
        // ...
    }
}
```

## Limitations

1. **Android**: Requires Activity context for OAuth redirect handling
2. **Desktop**: Requires system browser support
3. **iOS**: Not yet implemented (would use Google Sign-In iOS SDK)

## Future Improvements

1. Add iOS implementation using Google Sign-In iOS SDK
2. Add support for service account authentication (for server-side backups)
3. Implement token encryption at rest for desktop
4. Add support for multiple Google accounts
5. Implement automatic token refresh before API calls

## References

- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Google Drive API](https://developers.google.com/drive/api/v3/about-sdk)
- [Android EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)
