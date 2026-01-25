# MyAnimeList API Setup Guide

## Overview
IReader uses MyAnimeList's OAuth 2.0 API with PKCE (Proof Key for Code Exchange) for tracking manga/novels. This guide will help you configure your MAL API application.

## Current Configuration
- **Client ID**: `6114d00ca681b7701d1e15fe11a4987e` (already configured in the app)
- **OAuth Flow**: Authorization Code with PKCE
- **Redirect URI**: **Not required** (manual code copy flow)

## Important: No Redirect URI Needed!

Unlike traditional OAuth flows, MAL with PKCE doesn't require a redirect URI because:
1. The authorization happens in a web browser
2. The user manually copies the authorization code from the URL
3. The app never receives an automatic callback

This is the same approach used by Mihon/Tachiyomi and other manga tracking apps.

## How to Register Your Own MAL API Application

If you want to use your own Client ID instead of the shared one:

### Step 1: Create MAL API Application
1. Go to https://myanimelist.net/apiconfig
2. Log in with your MAL account
3. Click "Create ID" to create a new API application

### Step 2: Fill in Application Details
- **App Name**: IReader (or your preferred name)
- **App Type**: Select "web"
- **App Description**: Novel/Manga reading and tracking application
- **App Redirect URL**: Leave this **EMPTY** or use `urn:ietf:wg:oauth:2.0:oob`
  - The special URI `urn:ietf:wg:oauth:2.0:oob` tells MAL this is an "out-of-band" flow
  - This means the authorization code will be displayed to the user instead of redirected
- **Homepage URL**: https://github.com/IReaderorg/IReader (or your fork)
- **Commercial / Non-Commercial**: Non-Commercial

### Step 3: Get Your Client ID
After creating the application:
1. You'll receive a **Client ID** (a long alphanumeric string)
2. **Note**: MAL doesn't provide a Client Secret for PKCE flows - you only need the Client ID

### Step 4: Update IReader Configuration
Replace the Client ID in the code:

**File**: `IReader/data/src/commonMain/kotlin/ireader/data/tracking/mal/MyAnimeListApi.kt`

```kotlin
companion object {
    const val API_URL = "https://api.myanimelist.net/v2"
    const val AUTH_URL = "https://myanimelist.net/v1/oauth2/authorize"
    const val TOKEN_URL = "https://myanimelist.net/v1/oauth2/token"
    const val BASE_MANGA_URL = "https://myanimelist.net/manga/"
    
    // Replace with your Client ID
    const val CLIENT_ID = "YOUR_CLIENT_ID_HERE"
}
```

## How the OAuth Flow Works

### 1. User Initiates Login
- User clicks "Login to MAL" in IReader settings
- App generates a random 128-character `code_verifier`
- App creates a `code_challenge` from the verifier
- App builds the authorization URL with the challenge

### 2. Authorization URL Format
```
https://myanimelist.net/v1/oauth2/authorize?
  response_type=code&
  client_id=YOUR_CLIENT_ID&
  code_challenge=GENERATED_CHALLENGE&
  code_challenge_method=plain
```

### 3. User Authorizes in Browser
- User opens the URL in their browser
- Logs in to MAL (if not already logged in)
- Clicks "Allow" to authorize the app
- MAL redirects to a page showing the authorization code

### 4. User Copies Authorization Code
- The authorization code appears in the URL or on the page
- User copies the code
- User pastes it back into IReader

### 5. App Exchanges Code for Token
- App sends the authorization code + stored `code_verifier` to MAL
- MAL validates the code and verifier match
- MAL returns an access token and refresh token
- App stores the tokens for future API calls

## Why PKCE?

PKCE (Proof Key for Code Exchange) adds security to the OAuth flow:
- Prevents authorization code interception attacks
- No need for a client secret (which can't be kept secret in mobile apps)
- Industry standard for mobile and desktop applications

## Testing Your Configuration

1. Build and run IReader
2. Go to Settings → Tracking
3. Enable MyAnimeList
4. Click "Login"
5. Copy the generated URL and open in browser
6. Authorize the app
7. Copy the authorization code from the URL
8. Paste it in IReader
9. Login should succeed

## Troubleshooting

### Error: "invalid_request" or "Check the code_challenge parameter"
- **Cause**: The app is not generating the PKCE challenge correctly
- **Solution**: Make sure you're using the latest version with the PKCE fix

### Error: "invalid_client"
- **Cause**: The Client ID is incorrect or the API application is not configured
- **Solution**: Double-check your Client ID in the MAL API config page

### Error: "invalid_grant"
- **Cause**: The authorization code has expired or was already used
- **Solution**: Generate a new authorization URL and try again

### Authorization code not appearing
- **Cause**: Redirect URL might be configured incorrectly
- **Solution**: Make sure the redirect URL is empty or set to `urn:ietf:wg:oauth:2.0:oob`

## API Rate Limits

MyAnimeList API has rate limits:
- **Requests per minute**: Not officially documented, but generally generous
- **Best practice**: Cache responses and avoid unnecessary API calls
- IReader automatically handles token refresh when needed

## References

- [MAL API Documentation](https://myanimelist.net/apiconfig/references/api/v2)
- [MAL API Forum](https://myanimelist.net/forum/?topicid=1973077)
- [OAuth 2.0 PKCE RFC](https://datatracker.ietf.org/doc/html/rfc7636)
- [OAuth 2.0 for Native Apps](https://datatracker.ietf.org/doc/html/rfc8252)

## Current Status

✅ The app is already configured with a working Client ID  
✅ PKCE implementation is complete and working  
✅ No redirect URI configuration needed  
✅ Ready to use out of the box  

You can use the existing configuration without any changes to the MAL API settings!
