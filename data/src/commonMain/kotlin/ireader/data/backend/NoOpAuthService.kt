package ireader.data.backend

/**
 * No-op implementation of AuthService for when auth is not configured
 */
class NoOpAuthService : AuthService {
    
    private val notConfiguredError = Exception("Authentication service is not configured")
    
    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return Result.failure(notConfiguredError)
    }
    
    override suspend fun signUp(email: String, password: String): Result<AuthUser> {
        return Result.failure(notConfiguredError)
    }
    
    override suspend fun signOut(): Result<Unit> {
        return Result.success(Unit)  // Allow sign out even if not configured
    }
    
    override suspend fun getCurrentUser(): Result<AuthUser?> {
        return Result.success(null)  // No user when not configured
    }
    
    override suspend fun getCurrentUserId(): String? {
        return null
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return false
    }
    
    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return Result.failure(notConfiguredError)
    }
    
    override suspend fun updateEmail(newEmail: String): Result<Unit> {
        return Result.failure(notConfiguredError)
    }
    
    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return Result.failure(notConfiguredError)
    }
    
    override suspend fun refreshToken(): Result<Unit> {
        return Result.failure(notConfiguredError)
    }
}
