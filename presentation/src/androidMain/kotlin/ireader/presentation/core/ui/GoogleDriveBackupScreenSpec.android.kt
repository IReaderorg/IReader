package ireader.presentation.core.ui

import android.app.Activity
import android.content.Intent
import ireader.data.backup.GoogleDriveAuthenticatorAndroid
import ireader.presentation.ui.settings.backups.GoogleDriveViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android-specific Google Sign-In flow handler
 */
private object GoogleDriveSignInHandler : KoinComponent {
    private val authenticator: GoogleDriveAuthenticatorAndroid by inject()
    
    // Store reference to current activity for sign-in flow
    private var currentActivity: Activity? = null
    private var pendingSuccessCallback: ((String) -> Unit)? = null
    private var pendingErrorCallback: ((String) -> Unit)? = null
    
    fun setActivity(activity: Activity?) {
        currentActivity = activity
    }
    
    fun startSignIn(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val activity = currentActivity
        if (activity != null && authenticator.isInitialized()) {
            pendingSuccessCallback = onSuccess
            pendingErrorCallback = onError
            authenticator.startSignIn(activity, GoogleDriveAuthenticatorAndroid.REQUEST_CODE_SIGN_IN)
        } else {
            onError("Google Drive not initialized. Please restart the app.")
        }
    }
    
    /**
     * Handle the activity result from Google Sign-In
     * Call this from your Activity's onActivityResult
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GoogleDriveAuthenticatorAndroid.REQUEST_CODE_SIGN_IN) {
            val successCb = pendingSuccessCallback
            val errorCb = pendingErrorCallback
            pendingSuccessCallback = null
            pendingErrorCallback = null
            
            CoroutineScope(Dispatchers.Main).launch {
                val result = authenticator.handleSignInResult(data)
                result.onSuccess { email ->
                    successCb?.invoke(email)
                }.onFailure { error ->
                    errorCb?.invoke(error.message ?: "Sign-in failed")
                }
            }
        }
    }
    
    /**
     * Process OAuth callback (for compatibility with existing code)
     * With Google Sign-In SDK, this is handled via onActivityResult instead
     */
    fun processOAuthCallback(viewModel: GoogleDriveViewModel, authCode: String) {
        // With Google Sign-In SDK, we don't use auth codes
        // The sign-in is handled via onActivityResult
        viewModel.onOAuthError("Please use the Sign In button to authenticate with Google Drive.")
    }
}

/**
 * Android implementation of GoogleDriveOAuthLauncher
 */
actual object GoogleDriveOAuthLauncher {
    actual fun launchOAuthFlow(
        onSuccess: (email: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        GoogleDriveSignInHandler.startSignIn(onSuccess, onError)
    }
}

/**
 * Set the current activity for Google Sign-In flow
 * Call this from MainActivity's onResume/onPause
 */
fun setGoogleDriveOAuthActivity(activity: Activity?) {
    GoogleDriveSignInHandler.setActivity(activity)
}

/**
 * Handle activity result for Google Sign-In
 * Call this from MainActivity's onActivityResult
 */
fun handleGoogleDriveActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    GoogleDriveSignInHandler.handleActivityResult(requestCode, resultCode, data)
}

actual fun startOAuthFlow() {
    // No-op - use GoogleDriveOAuthLauncher.launchOAuthFlow instead
}

actual fun startOAuthFlowWithViewModel(viewModel: GoogleDriveViewModel) {
    GoogleDriveOAuthLauncher.launchOAuthFlow(viewModel::onOAuthSuccess, viewModel::onOAuthError)
}

actual fun processOAuthCallback(viewModel: GoogleDriveViewModel, authCode: String) {
    GoogleDriveSignInHandler.processOAuthCallback(viewModel, authCode)
}
