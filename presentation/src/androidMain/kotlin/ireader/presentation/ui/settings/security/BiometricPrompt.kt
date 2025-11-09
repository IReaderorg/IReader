package ireader.presentation.ui.settings.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun rememberBiometricPrompt(
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
): BiometricPrompt {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
        ?: throw IllegalStateException("BiometricPrompt requires FragmentActivity")
    
    val executor = remember { ContextCompat.getMainExecutor(context) }
    
    return remember {
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailure(errString.toString())
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailure("Authentication failed. Please try again.")
                }
            }
        )
    }
}

@Composable
fun LaunchBiometricPrompt(
    biometricPrompt: BiometricPrompt,
    title: String = "Authenticate",
    subtitle: String = "Unlock IReader",
    negativeButtonText: String = "Cancel"
) {
    LaunchedEffect(Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
}
