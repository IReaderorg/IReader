package ireader.data.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Android implementation of Google Drive authenticator using Google Sign-In SDK
 * 
 * This is the proper Android approach that uses Google Play Services.
 * It handles OAuth2 automatically without needing custom redirect URIs.
 */
class GoogleDriveAuthenticatorAndroid : GoogleDriveAuthenticator {
    
    private var _context: Context? = null
    private var _prefs: SharedPreferences? = null
    private var _clientId: String? = null
    private var _googleSignInClient: GoogleSignInClient? = null
    
    // Callback for sign-in result
    private var signInResultCallback: ((Result<String>) -> Unit)? = null
    
    private fun getPrefs(context: Context): SharedPreferences {
        if (_prefs == null || _context != context) {
            _context = context
            _prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return _prefs!!
    }
    
    private fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        if (_googleSignInClient == null) {
            // Use DRIVE_FILE scope to access visible files/folders in user's Drive
            // This allows creating the "IReader" folder visible to the user
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()
            
            _googleSignInClient = GoogleSignIn.getClient(context, gso)
        }
        return _googleSignInClient!!
    }
    
    override suspend fun authenticate(): Result<String> {
        val context = _context ?: return Result.failure(
            GoogleDriveAuthException("Context not initialized. Call initialize() first.")
        )
        
        // Check if already signed in
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null && hasRequiredScopes(context, account)) {
            val email = account.email ?: "unknown@gmail.com"
            storeEmail(context, email)
            return Result.success(email)
        }
        
        // Need to sign in - return failure indicating UI interaction needed
        return Result.failure(
            GoogleDriveAuthException(
                "Authentication must be initiated from UI. Call startSignIn() from an Activity.",
                needsUserInteraction = true
            )
        )
    }
    
    /**
     * Check if the account has the required Drive scopes
     */
    private fun hasRequiredScopes(context: Context, account: GoogleSignInAccount): Boolean {
        return GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_FILE))
    }
    
    /**
     * Get the sign-in intent to launch
     */
    fun getSignInIntent(): Intent? {
        val context = _context ?: return null
        return getGoogleSignInClient(context).signInIntent
    }
    
    /**
     * Start the sign-in flow from an Activity
     * 
     * @param activity The activity to launch sign-in from
     * @param requestCode The request code for onActivityResult
     */
    fun startSignIn(activity: Activity, requestCode: Int = REQUEST_CODE_SIGN_IN) {
        val signInIntent = getGoogleSignInClient(activity).signInIntent
        activity.startActivityForResult(signInIntent, requestCode)
    }
    
    /**
     * Handle the sign-in result from onActivityResult
     * 
     * @param data The intent data from onActivityResult
     * @return Result with user email on success
     */
    suspend fun handleSignInResult(data: Intent?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            val email = account?.email ?: "unknown@gmail.com"
            _context?.let { storeEmail(it, email) }
            
            Result.success(email)
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                12501 -> "Sign-in cancelled by user"
                12502 -> "Sign-in currently in progress"
                7 -> "Network error. Please check your connection."
                else -> "Sign-in failed: ${e.statusMessage ?: "Error code ${e.statusCode}"}"
            }
            Result.failure(GoogleDriveAuthException(message, cause = e))
        } catch (e: Exception) {
            Result.failure(GoogleDriveAuthException("Sign-in failed: ${e.message}", cause = e))
        }
    }
    
    override suspend fun refreshToken(): Result<Unit> {
        // Google Sign-In handles token refresh automatically
        val context = _context ?: return Result.failure(Exception("Context not initialized"))
        
        return withContext(Dispatchers.IO) {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account != null) {
                    // Silently sign in to refresh tokens
                    suspendCancellableCoroutine { continuation ->
                        getGoogleSignInClient(context).silentSignIn()
                            .addOnSuccessListener { 
                                continuation.resume(Result.success(Unit))
                            }
                            .addOnFailureListener { e ->
                                continuation.resume(Result.failure(Exception("Token refresh failed: ${e.message}", e)))
                            }
                    }
                } else {
                    Result.failure(Exception("Not signed in"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Token refresh failed: ${e.message}", e))
            }
        }
    }
    
    override suspend fun isAuthenticated(): Boolean {
        val context = _context ?: return false
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && hasRequiredScopes(context, account)
    }
    
    override suspend fun getAccessToken(): String? {
        val context = _context ?: return null
        
        return withContext(Dispatchers.IO) {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null
                
                // Use GoogleAccountCredential to get access token
                // Use DRIVE_FILE scope for visible folder access
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf(DriveScopes.DRIVE_FILE)
                )
                credential.selectedAccount = account.account
                
                // This will automatically refresh the token if needed
                credential.token
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun disconnect() {
        val context = _context ?: return
        
        withContext(Dispatchers.IO) {
            try {
                // Sign out
                suspendCancellableCoroutine { continuation ->
                    getGoogleSignInClient(context).signOut()
                        .addOnCompleteListener { 
                            continuation.resume(Unit)
                        }
                }
                
                // Revoke access
                suspendCancellableCoroutine { continuation ->
                    getGoogleSignInClient(context).revokeAccess()
                        .addOnCompleteListener {
                            continuation.resume(Unit)
                        }
                }
            } catch (e: Exception) {
                // Continue even if revocation fails
            }
        }
        
        // Clear stored data
        getPrefs(context).edit()
            .remove(KEY_USER_EMAIL)
            .apply()
        
        // Reset client to force new configuration on next sign-in
        _googleSignInClient = null
    }
    
    /**
     * Initialize with context - call this before using the authenticator
     * 
     * @param context Application context
     * @param clientId Google OAuth2 client ID (not used with Google Sign-In SDK, but kept for API compatibility)
     */
    fun initialize(context: Context, clientId: String) {
        _context = context.applicationContext
        _clientId = clientId
        getPrefs(context.applicationContext)
    }
    
    /**
     * Check if the authenticator has been initialized
     */
    fun isInitialized(): Boolean = _context != null
    
    /**
     * Get the currently signed-in account
     */
    fun getSignedInAccount(): GoogleSignInAccount? {
        val context = _context ?: return null
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    /**
     * Get GoogleAccountCredential for Drive API access
     */
    fun getCredential(): GoogleAccountCredential? {
        val context = _context ?: return null
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        
        // Use DRIVE_FILE scope for visible folder access
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account
        return credential
    }
    
    private fun storeEmail(context: Context, email: String) {
        getPrefs(context).edit()
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }
    
    fun getStoredEmail(): String? {
        val context = _context ?: return null
        return getPrefs(context).getString(KEY_USER_EMAIL, null)
    }
    
    companion object {
        private const val PREFS_NAME = "google_drive_auth"
        private const val KEY_USER_EMAIL = "user_email"
        
        const val REQUEST_CODE_SIGN_IN = 9001
    }
}

/**
 * Custom exception for Google Drive authentication errors
 */
class GoogleDriveAuthException(
    message: String,
    val needsUserInteraction: Boolean = false,
    cause: Throwable? = null
) : Exception(message, cause)
