package ireader.data.backend

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlin.time.ExperimentalTime

/**
 * Supabase implementation of AuthService
 */
@OptIn(ExperimentalTime::class)
class SupabaseAuthService(
    private val client: SupabaseClient
) : AuthService {
    
    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = client.auth.currentUserOrNull()
                ?: return Result.failure(Exception("Sign in succeeded but no user found"))
            
            Result.success(AuthUser(
                id = user.id,
                email = user.email,
                emailVerified = user.emailConfirmedAt != null,
                metadata = user.userMetadata ?: emptyMap()
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signUp(email: String, password: String): Result<AuthUser> {
        return try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = client.auth.currentUserOrNull()
                ?: return Result.failure(Exception("Sign up succeeded but no user found"))
            
            Result.success(AuthUser(
                id = user.id,
                email = user.email,
                emailVerified = user.emailConfirmedAt != null,
                metadata = user.userMetadata ?: emptyMap()
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): Result<AuthUser?> {
        return try {
            val user = client.auth.currentUserOrNull()
            if (user != null) {
                Result.success(AuthUser(
                    id = user.id,
                    email = user.email,
                    emailVerified = user.emailConfirmedAt != null,
                    metadata = user.userMetadata ?: emptyMap()
                ))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUserId(): String? {
        return try {
            client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return try {
            client.auth.currentUserOrNull() != null
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            client.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            client.auth.updateUser {
                email = newEmail
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            client.auth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun refreshToken(): Result<Unit> {
        return try {
            client.auth.refreshCurrentSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
