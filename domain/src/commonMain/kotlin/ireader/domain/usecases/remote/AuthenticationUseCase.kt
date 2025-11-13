package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.User
import ireader.domain.utils.validation.ValidationResult
import ireader.domain.utils.validation.Validators
import ireader.domain.utils.validation.combineValidationResults

/**
 * Consolidated authentication use case that handles sign in, sign up, and sign out operations
 * 
 * This replaces the separate SignInUseCase, SignUpUseCase, and SignOutUseCase classes
 * to reduce code duplication and provide a unified authentication interface.
 */
class AuthenticationUseCase(
    private val remoteRepository: RemoteRepository
) {
    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<User> {
        val validation = validateCredentials(email, password)
        if (validation.isInvalid) {
            val errors = validation.getAllErrors()
            return Result.failure(
                IllegalArgumentException(errors.joinToString(", ") { it.message })
            )
        }
        
        return remoteRepository.signIn(email, password)
    }
    
    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String): Result<User> {
        val validation = validateCredentials(email, password)
        if (validation.isInvalid) {
            val errors = validation.getAllErrors()
            return Result.failure(
                IllegalArgumentException(errors.joinToString(", ") { it.message })
            )
        }
        
        return remoteRepository.signUp(email, password)
    }
    
    /**
     * Sign out the current user
     */
    suspend fun signOut() {
        remoteRepository.signOut()
    }
    
    /**
     * Validate email and password credentials
     */
    private fun validateCredentials(email: String, password: String): ValidationResult {
        val emailValidation = Validators.validateEmail(email)
        val passwordValidation = Validators.validatePassword(password)
        
        return combineValidationResults(emailValidation, passwordValidation)
    }
}
