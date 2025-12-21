package ireader.presentation.ui.settings.auth

import androidx.compose.runtime.Stable
import ireader.domain.usecases.remote.RemoteBackendUseCases
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.launch

class AuthViewModel(
    private val remoteUseCases: RemoteBackendUseCases?
) : StateViewModel<AuthState>(AuthState()) {
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
    
    fun updateEmail(email: String) {
        updateState { it.copy(email = email, emailError = null, error = null) }
    }
    
    fun updatePassword(password: String) {
        updateState { it.copy(password = password, passwordError = null, error = null) }
    }
    
    fun toggleMode() {
        updateState { 
            it.copy(
                isSignUp = !it.isSignUp,
                emailError = null,
                passwordError = null,
                error = null
            ) 
        }
    }
    
    fun submit() {
        val stateValue = currentState
        
        if (!validateInputs()) return
        
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            
            try {
                val result = if (stateValue.isSignUp) {
                    remoteUseCases?.signUp?.invoke(stateValue.email, stateValue.password)
                } else {
                    remoteUseCases?.signIn?.invoke(stateValue.email, stateValue.password)
                }
                
                result?.fold(
                    onSuccess = {
                        updateState { it.copy(isLoading = false, success = true) }
                    },
                    onFailure = { error ->
                        val authError = mapAuthError(error)
                        updateState { 
                            it.copy(
                                isLoading = false,
                                error = authError.toUserMessage()
                            ) 
                        }
                    }
                ) ?: run {
                    updateState { 
                        it.copy(
                            isLoading = false,
                            error = AuthError.ServiceUnavailable.toUserMessage()
                        ) 
                    }
                }
            } catch (e: Exception) {
                val authError = mapAuthError(e)
                updateState { 
                    it.copy(
                        isLoading = false,
                        error = authError.toUserMessage()
                    ) 
                }
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        val stateValue = currentState
        var isValid = true
        
        if (stateValue.email.isBlank()) {
            updateState { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!isValidEmail(stateValue.email)) {
            updateState { it.copy(emailError = "Invalid email address") }
            isValid = false
        }
        
        if (stateValue.password.isBlank()) {
            updateState { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (currentState.password.length < 6) {
            updateState { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Map exceptions to user-friendly auth errors
     */
    private fun mapAuthError(error: Throwable): AuthError {
        val message = error.message?.lowercase() ?: ""
        
        return when {
            // Network errors
            message.contains("network") || 
            message.contains("connection") ||
            message.contains("timeout") ||
            message.contains("unreachable") ||
            message.contains("failed to connect") ||
            message.contains("no internet") ||
            message.contains("offline") -> AuthError.NetworkError
            
            // User not found - check this BEFORE invalid credentials
            message.contains("user not found") ||
            message.contains("account not found") ||
            message.contains("does not exist") ||
            message.contains("no user") ||
            message.contains("user does not exist") -> AuthError.UserNotFound
            
            // Not authenticated
            message.contains("not authenticated") ||
            message.contains("unauthenticated") ||
            message.contains("sign in required") ||
            message.contains("login required") ||
            message.contains("session expired") ||
            message.contains("token expired") -> AuthError.NotAuthenticated
            
            // Invalid credentials
            message.contains("invalid") && (message.contains("credential") || message.contains("password") || message.contains("email")) ||
            message.contains("wrong password") ||
            message.contains("incorrect") ||
            message.contains("unauthorized") ||
            message.contains("401") -> AuthError.InvalidCredentials
            
            // Email already exists
            message.contains("already exists") ||
            message.contains("already registered") ||
            message.contains("email taken") ||
            message.contains("duplicate") -> AuthError.EmailAlreadyExists
            
            // Server errors
            message.contains("server") ||
            message.contains("500") ||
            message.contains("502") ||
            message.contains("503") ||
            message.contains("internal error") -> AuthError.ServerError
            
            // Service unavailable
            message.contains("service") ||
            message.contains("unavailable") ||
            message.contains("maintenance") -> AuthError.ServiceUnavailable
            
            // Rate limiting
            message.contains("too many") ||
            message.contains("rate limit") ||
            message.contains("throttle") -> AuthError.TooManyAttempts
            
            // Default - show the actual error message for debugging
            else -> AuthError.Unknown(error.message ?: "Something went wrong. Please try again.")
        }
    }
}

/**
 * Sealed class representing authentication errors with user-friendly messages
 */
sealed class AuthError {
    object InvalidCredentials : AuthError()
    object NetworkError : AuthError()
    object ServerError : AuthError()
    object ServiceUnavailable : AuthError()
    object UserNotFound : AuthError()
    object NotAuthenticated : AuthError()
    object EmailAlreadyExists : AuthError()
    object TooManyAttempts : AuthError()
    data class Unknown(val message: String) : AuthError()
    
    /**
     * Convert error to user-friendly message with actionable guidance
     */
    fun toUserMessage(): String = when (this) {
        is InvalidCredentials -> "Invalid email or password. Please check your credentials and try again."
        is NetworkError -> "Unable to connect. Please check your internet connection and try again."
        is ServerError -> "Our servers are having issues. Please try again in a few minutes."
        is ServiceUnavailable -> "Sign-in service is temporarily unavailable. Please try again later."
        is UserNotFound -> "No account found with this email. Please sign up or check your email address."
        is NotAuthenticated -> "You're not signed in. Please sign in to continue."
        is EmailAlreadyExists -> "An account with this email already exists. Please sign in or use a different email."
        is TooManyAttempts -> "Too many attempts. Please wait a few minutes before trying again."
        is Unknown -> message
    }
}

@Stable
data class AuthState(
    val email: String = "",
    val password: String = "",
    val isSignUp: Boolean = false,
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)
