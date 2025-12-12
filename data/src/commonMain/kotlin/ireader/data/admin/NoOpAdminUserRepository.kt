package ireader.data.admin

import ireader.domain.data.repository.AdminUserRepository
import ireader.domain.models.remote.AdminUser
import ireader.domain.models.remote.Badge

/**
 * No-op implementation of AdminUserRepository for when Supabase is not configured
 */
class NoOpAdminUserRepository : AdminUserRepository {
    
    override suspend fun getAllUsers(
        limit: Int,
        offset: Int,
        searchQuery: String?
    ): Result<List<AdminUser>> {
        return Result.failure(Exception("Admin user management is not available. Please configure Supabase."))
    }
    
    override suspend fun getUserById(userId: String): Result<AdminUser?> {
        return Result.failure(Exception("Admin user management is not available. Please configure Supabase."))
    }
    
    override suspend fun assignBadgeToUser(userId: String, badgeId: String): Result<Unit> {
        return Result.failure(Exception("Admin user management is not available. Please configure Supabase."))
    }
    
    override suspend fun removeBadgeFromUser(userId: String, badgeId: String): Result<Unit> {
        return Result.failure(Exception("Admin user management is not available. Please configure Supabase."))
    }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return Result.failure(Exception("Admin user management is not available. Please configure Supabase."))
    }
    
    override suspend fun getAvailableBadgesForAssignment(): Result<List<Badge>> {
        return Result.failure(Exception("Admin user management is not available. Please configure Supabase."))
    }
    
    override suspend fun getUserBadges(userId: String): Result<List<Badge>> {
        return Result.failure(Exception("Admin user management is not available. Please configure Supabase."))
    }
    
    override suspend fun isCurrentUserAdmin(): Result<Boolean> {
        return Result.success(false)
    }
}
