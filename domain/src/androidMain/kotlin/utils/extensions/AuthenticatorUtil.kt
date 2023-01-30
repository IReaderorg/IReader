package ireader.domain.utils.extensions

import android.content.Context
import androidx.annotation.CallSuper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt.AuthenticationError
import androidx.biometric.auth.AuthPromptCallback
import androidx.biometric.auth.startClass2BiometricOrCredentialAuthentication
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object AuthenticatorUtil {

    /**
     * A check to avoid double authentication on older APIs when confirming settings changes since
     * the biometric prompt is launched in a separate activity outside of the app.
     */
    var isAuthenticating = false

    /**
     * Launches biometric prompt.
     *
     * @param title String title that will be shown on the prompt
     * @param subtitle Optional string subtitle that will be shown on the prompt
     * @param confirmationRequired Whether require explicit user confirmation after passive biometric is recognized
     * @param callback Callback object to handle the authentication events
     */
    fun FragmentActivity.startAuthentication(
        title: String,
        subtitle: String? = null,
        confirmationRequired: Boolean = true,
        callback: AuthenticationCallback,
    ) {
        isAuthenticating = true
        startClass2BiometricOrCredentialAuthentication(
            title = title,
            subtitle = subtitle,
            confirmationRequired = confirmationRequired,
            executor = ContextCompat.getMainExecutor(this),
            callback = callback,
        )
    }

    /**
     * Returns true if Class 2 biometric or credential lock is set and available to use
     */
    fun Context.isAuthenticationSupported(): Boolean {
        val authenticators = Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
        return BiometricManager.from(this).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * [AuthPromptCallback] with extra check
     *
     * @see isAuthenticating
     */
    abstract class AuthenticationCallback : AuthPromptCallback() {
        /**
         * Called when an unrecoverable error has been encountered and authentication has stopped.
         *
         *
         * After this method is called, no further events will be sent for the current
         * authentication session.
         *
         * @param activity  The activity that is currently hosting the prompt.
         * @param errorCode An integer ID associated with the error.
         * @param errString A human-readable string that describes the error.
         */
        @CallSuper
        override fun onAuthenticationError(
            activity: FragmentActivity?,
            @AuthenticationError errorCode: Int,
            errString: CharSequence,
        ) {
            isAuthenticating = false
        }

        /**
         * Called when the user has successfully authenticated.
         *
         *
         * After this method is called, no further events will be sent for the current
         * authentication session.
         *
         * @param activity The activity that is currently hosting the prompt.
         * @param result   An object containing authentication-related data.
         */
        @CallSuper
        override fun onAuthenticationSucceeded(
            activity: FragmentActivity?,
            result: androidx.biometric.BiometricPrompt.AuthenticationResult,
        ) {
            isAuthenticating = false
        }

        /**
         * Called when an authentication attempt by the user has been rejected.
         *
         * @param activity The activity that is currently hosting the prompt.
         */
        @CallSuper
        override fun onAuthenticationFailed(activity: FragmentActivity?) {
            isAuthenticating = false
        }
    }
}
