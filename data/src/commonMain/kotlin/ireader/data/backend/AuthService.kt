package ireader.data.backend

/**
 * Authentication service abstraction.
 * 
 * Separates auth operations from data operations for better separation of concerns.
 * This allows different backends to implement auth differently (Supabase Auth, Firebase Auth, etc.)
 */
interface AuthService {
    
    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<AuthUser>
    
    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String): Result<AuthUser>
    
    /**
     * Sign out current user
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): Result<AuthUser?>
    
    /**
     * Get current user ID (convenience method)
     */
    suspend fun getCurrentUserId(): String?
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Send password reset email
     */
    suspend fun sendPasswordReset(email: String): Result<Unit>
    
    /**
     * Update user email
     */
    suspend fun updateEmail(newEmail: String): Result<Unit>
    
    /**
     * Update user password
     */
    suspend fun updatePassword(newPassword: String): Result<Unit>
    
    /**
     * Refresh authentication token
     */
    suspend fun refreshToken(): Result<Unit>
}

/**
 * Authenticated user data
 */
data class AuthUser(
    val id: String,
    val email: String?,
    val emailVerified: Boolean = false,
    val metadata: Map<String, Any> = emptyMap()
)
