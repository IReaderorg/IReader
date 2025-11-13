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
                        mutableState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Authentication failed"
                            ) 
                        }
                    }
                ) ?: run {
                    mutableState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Authentication service not available"
                        ) 
                    }
                }
            } catch (e: Exception) {
                mutableState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
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
