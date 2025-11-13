package ireader.presentation.ui.settings.auth

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.domain.usecases.remote.RemoteBackendUseCases
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val remoteUseCases: RemoteBackendUseCases?
) : StateScreenModel<AuthState>(AuthState()) {
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
    
    fun updateEmail(email: String) {
        mutableState.update { it.copy(email = email, emailError = null, error = null) }
    }
    
    fun updatePassword(password: String) {
        mutableState.update { it.copy(password = password, passwordError = null, error = null) }
    }
    
    fun toggleMode() {
        mutableState.update { 
            it.copy(
                isSignUp = !it.isSignUp,
                emailError = null,
                passwordError = null,
                error = null
            ) 
        }
    }
    
    fun submit() {
        val currentState = state.value
        
        if (!validateInputs()) return
        
        screenModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = if (currentState.isSignUp) {
                    remoteUseCases?.signUp?.invoke(currentState.email, currentState.password)
                } else {
                    remoteUseCases?.signIn?.invoke(currentState.email, currentState.password)
                }
                
                result?.fold(
                    onSuccess = {
                        mutableState.update { it.copy(isLoading = false, success = true) }
                    },
                    onFailure = { error ->
                        val authError = mapAuthError(error)
                        mutableState.update { 
                            it.copy(
                                isLoading = false,
                                error = authError.toUserMessage()
                            ) 
                        }
                    }
                ) ?: run {
                    mutableState.update { 
                        it.copy(
                            isLoading = false,
                            error = AuthError.ServiceUnavailable.toUserMessage()
                        ) 
                    }
                }
            } catch (e: Exception) {
                val authError = mapAuthError(e)
                mutableState.update { 
                    it.copy(
                        isLoading = false,
                        error = authError.toUserMessage()
                    ) 
                }
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        val currentState = state.value
        var isValid = true
        
        if (currentState.email.isBlank()) {
            mutableState.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!isValidEmail(currentState.email)) {
            mutableState.update { it.copy(emailError = "Invalid email address") }
            isValid = false
        }
        
        if (currentState.password.isBlank()) {
            mutableState.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (currentState.password.length < 6) {
            mutableState.update { it.copy(passwordError = "Password must be at least 6 characters") }
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
            message.contains("unreachable") -> AuthError.NetworkError
            
            // Invalid credentials
            message.contains("invalid") && (message.contains("credential") || message.contains("password") || message.contains("email")) ||
            message.contains("wrong password") ||
            message.contains("incorrect") ||
            message.contains("unauthorized") ||
            message.contains("401") -> AuthError.InvalidCredentials
            
            // User not found
            message.contains("user not found") ||
            message.contains("account not found") ||
            message.contains("does not exist") -> AuthError.UserNotFound
            
            // Email already exists
            message.contains("already exists") ||
            message.contains("already registered") ||
            message.contains("email taken") -> AuthError.EmailAlreadyExists
            
            // Server errors
            message.contains("server") ||
            message.contains("500") ||
            message.contains("502") ||
            message.contains("503") -> AuthError.ServerError
            
            // Rate limiting
            message.contains("too many") ||
            message.contains("rate limit") -> AuthError.TooManyAttempts
            
            // Default to unknown
            else -> AuthError.Unknown(error.message ?: "An unexpected error occurred")
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
    object EmailAlreadyExists : AuthError()
    object TooManyAttempts : AuthError()
    data class Unknown(val message: String) : AuthError()
    
    /**
     * Convert error to user-friendly message with actionable guidance
     */
    fun toUserMessage(): String = when (this) {
        is InvalidCredentials -> "Invalid email or password. Please check your credentials and try again."
        is NetworkError -> "No internet connection. Please check your network and try again."
        is ServerError -> "Server error occurred. Please try again later."
        is ServiceUnavailable -> "Authentication service is not available. Please try again later."
        is UserNotFound -> "No account found with this email. Please sign up or check your email address."
        is EmailAlreadyExists -> "An account with this email already exists. Please sign in or use a different email."
        is TooManyAttempts -> "Too many login attempts. Please wait a few minutes and try again."
        is Unknown -> message
    }
}

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
