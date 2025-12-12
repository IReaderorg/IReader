package ireader.domain.data.repository

import ireader.domain.models.remote.AdminUser
import ireader.domain.models.remote.Badge
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for admin user management operations
 */
interface AdminUserRepository {
    
    /**
     * Get all users (admin only)
     */
    suspend fun getAllUsers(
        limit: Int = 50,
        offset: Int = 0,
        searchQuery: String? = null
    ): Result<List<AdminUser>>
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): Result<AdminUser?>
    
    /**
     * Assign a badge to a user
     */
    suspend fun assignBadgeToUser(userId: String, badgeId: String): Result<Unit>
    
    /**
     * Remove a badge from a user
     */
    suspend fun removeBadgeFromUser(userId: String, badgeId: String): Result<Unit>
    
    /**
     * Send password reset email to user
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    
    /**
     * Get all available badges for assignment
     */
    suspend fun getAvailableBadgesForAssignment(): Result<List<Badge>>
    
    /**
     * Get badges assigned to a specific user
     */
    suspend fun getUserBadges(userId: String): Result<List<Badge>>
    
    /**
     * Check if current user is admin
     */
    suspend fun isCurrentUserAdmin(): Result<Boolean>
}
