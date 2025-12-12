package ireader.domain.usecases.admin

import ireader.domain.data.repository.AdminUserRepository
import ireader.domain.models.remote.AdminUser
import ireader.domain.models.remote.Badge

/**
 * Use case to get all users for admin management
 */
class GetAllUsersUseCase(
    private val adminUserRepository: AdminUserRepository
) {
    suspend operator fun invoke(
        limit: Int = 50,
        offset: Int = 0,
        searchQuery: String? = null
    ): Result<List<AdminUser>> {
        return adminUserRepository.getAllUsers(limit, offset, searchQuery)
    }
}

/**
 * Use case to assign a badge to a user
 */
class AssignBadgeToUserUseCase(
    private val adminUserRepository: AdminUserRepository
) {
    suspend operator fun invoke(userId: String, badgeId: String): Result<Unit> {
        return adminUserRepository.assignBadgeToUser(userId, badgeId)
    }
}

/**
 * Use case to remove a badge from a user
 */
class RemoveBadgeFromUserUseCase(
    private val adminUserRepository: AdminUserRepository
) {
    suspend operator fun invoke(userId: String, badgeId: String): Result<Unit> {
        return adminUserRepository.removeBadgeFromUser(userId, badgeId)
    }
}

/**
 * Use case to send password reset email
 */
class SendPasswordResetUseCase(
    private val adminUserRepository: AdminUserRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return adminUserRepository.sendPasswordResetEmail(email)
    }
}

/**
 * Use case to get available badges for assignment
 */
class GetAvailableBadgesForAssignmentUseCase(
    private val adminUserRepository: AdminUserRepository
) {
    suspend operator fun invoke(): Result<List<Badge>> {
        return adminUserRepository.getAvailableBadgesForAssignment()
    }
}

/**
 * Use case to get badges assigned to a user
 */
class GetUserBadgesForAdminUseCase(
    private val adminUserRepository: AdminUserRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Badge>> {
        return adminUserRepository.getUserBadges(userId)
    }
}

/**
 * Use case to check if current user is admin
 */
class IsCurrentUserAdminUseCase(
    private val adminUserRepository: AdminUserRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return adminUserRepository.isCurrentUserAdmin()
    }
}

/**
 * Aggregate container for admin user use cases
 */
data class AdminUserUseCases(
    val getAllUsers: GetAllUsersUseCase,
    val assignBadgeToUser: AssignBadgeToUserUseCase,
    val removeBadgeFromUser: RemoveBadgeFromUserUseCase,
    val sendPasswordReset: SendPasswordResetUseCase,
    val getAvailableBadges: GetAvailableBadgesForAssignmentUseCase,
    val getUserBadges: GetUserBadgesForAdminUseCase,
    val isCurrentUserAdmin: IsCurrentUserAdminUseCase
)
