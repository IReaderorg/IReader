package ireader.presentation.ui.settings.auth

import androidx.compose.runtime.Stable
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.ConnectionStatus
import ireader.domain.models.remote.User
import ireader.domain.usecases.remote.RemoteBackendUseCases
import ireader.domain.data.repository.BadgeRepository
import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val remoteUseCases: RemoteBackendUseCases?,
    private val badgeRepository: BadgeRepository?,
    private val readingStatisticsRepository: ReadingStatisticsRepository?
) : StateViewModel<ProfileState>(ProfileState()) {
    
    init {
        loadCurrentUser()
        observeConnectionStatus()
        loadFeaturedBadges()
        loadAchievementBadges()
        loadReadingStatistics()
    }
    
    private fun loadCurrentUser() {
        scope.launch {
            updateState { it.copy(isLoading = true) }
            
            remoteUseCases?.getCurrentUser?.invoke()?.fold(
                onSuccess = { user ->
                    updateState { it.copy(currentUser = user, isLoading = false) }
                },
                onFailure = { error ->
                    updateState { it.copy(error = error.message, isLoading = false) }
                }
            ) ?: run {
                updateState { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun observeConnectionStatus() {
        remoteUseCases?.observeConnectionStatus?.invoke()?.onEach { status ->
            updateState { it.copy(connectionStatus = status) }
        }?.launchIn(scope)
    }
    
    fun signOut() {
        scope.launch {
            remoteUseCases?.signOut?.invoke()
            updateState { 
                it.copy(
                    currentUser = null,
                    lastSyncTime = null
                ) 
            }
        }
    }
    
    fun showUsernameDialog() {
        updateState { it.copy(showUsernameDialog = true) }
    }
    
    fun hideUsernameDialog() {
        updateState { it.copy(showUsernameDialog = false) }
    }
    
    fun showWalletDialog() {
        updateState { it.copy(showWalletDialog = true) }
    }
    
    fun hideWalletDialog() {
        updateState { it.copy(showWalletDialog = false) }
    }
    
    fun showPasswordDialog() {
        updateState { it.copy(showPasswordDialog = true) }
    }
    
    fun hidePasswordDialog() {
        updateState { it.copy(showPasswordDialog = false) }
    }
    
    fun updateUsername(username: String) {
        scope.launch {
            updateState { it.copy(isLoading = true, showUsernameDialog = false) }
            
            val userId = currentState.currentUser?.id
            if (userId == null) {
                updateState { 
                    it.copy(
                        isLoading = false, 
                        error = "user not found",  // Will be parsed by UserError.fromMessage
                        requiresSignIn = true
                    ) 
                }
                return@launch
            }
            
            remoteUseCases?.updateUsername?.invoke(userId, username)?.fold(
                onSuccess = {
                    loadCurrentUser()
                },
                onFailure = { error ->
                    updateState { 
                        it.copy(
                            error = error.message ?: "Failed to update username", 
                            isLoading = false,
                            requiresSignIn = error.message?.contains("not found", ignoreCase = true) == true ||
                                           error.message?.contains("unauthorized", ignoreCase = true) == true
                        ) 
                    }
                }
            )
        }
    }
    
    fun updateWallet(wallet: String) {
        scope.launch {
            updateState { it.copy(isLoading = true, showWalletDialog = false) }
            
            val userId = currentState.currentUser?.id
            if (userId == null) {
                updateState { 
                    it.copy(
                        isLoading = false, 
                        error = "user not found",  // Will be parsed by UserError.fromMessage
                        requiresSignIn = true
                    ) 
                }
                return@launch
            }
            
            remoteUseCases?.updateEthWalletAddress?.invoke(userId, wallet)?.fold(
                onSuccess = {
                    loadCurrentUser()
                },
                onFailure = { error ->
                    updateState { 
                        it.copy(
                            error = error.message ?: "Failed to update wallet", 
                            isLoading = false,
                            requiresSignIn = error.message?.contains("not found", ignoreCase = true) == true ||
                                           error.message?.contains("unauthorized", ignoreCase = true) == true
                        ) 
                    }
                }
            )
        }
    }
    
    fun updatePassword(newPassword: String) {
        scope.launch {
            updateState { it.copy(isLoading = true, showPasswordDialog = false) }
            
            if (currentState.currentUser == null) {
                updateState { 
                    it.copy(
                        isLoading = false, 
                        error = "user not found",
                        requiresSignIn = true
                    ) 
                }
                return@launch
            }
            
            remoteUseCases?.updatePassword?.invoke(newPassword)?.fold(
                onSuccess = {
                    updateState { 
                        it.copy(
                            isLoading = false,
                            error = null,
                            passwordUpdateSuccess = true
                        ) 
                    }
                },
                onFailure = { error ->
                    updateState { 
                        it.copy(
                            error = error.message ?: "Failed to update password", 
                            isLoading = false,
                            requiresSignIn = error.message?.contains("not found", ignoreCase = true) == true ||
                                           error.message?.contains("unauthorized", ignoreCase = true) == true ||
                                           error.message?.contains("session", ignoreCase = true) == true
                        ) 
                    }
                }
            )
        }
    }
    
    fun clearPasswordUpdateSuccess() {
        updateState { it.copy(passwordUpdateSuccess = false) }
    }
    
    fun clearError() {
        updateState { it.copy(error = null, requiresSignIn = false) }
    }
    
    private fun loadFeaturedBadges() {
        scope.launch {
            updateState { it.copy(isBadgesLoading = true) }
            
            badgeRepository?.getFeaturedBadges()?.fold(
                onSuccess = { badges ->
                    updateState { it.copy(featuredBadges = badges, isBadgesLoading = false, badgesError = null) }
                },
                onFailure = { error ->
                    updateState { it.copy(badgesError = error.message, isBadgesLoading = false) }
                }
            ) ?: run {
                updateState { it.copy(isBadgesLoading = false) }
            }
        }
    }
    
    private fun loadAchievementBadges() {
        scope.launch {
            updateState { it.copy(isBadgesLoading = true) }
            
            badgeRepository?.getUserBadges()?.fold(
                onSuccess = { userBadges ->
                    // Filter only achievement badges
                    val achievementBadges = userBadges
                        .filter { badge -> 
                            badge.badgeType?.uppercase() == "ACHIEVEMENT" 
                        }
                        .map { userBadge ->
                            Badge(
                                id = userBadge.badgeId,
                                name = userBadge.badgeName,
                                description = userBadge.badgeDescription,
                                icon = userBadge.badgeIcon,
                                category = userBadge.badgeCategory,
                                rarity = userBadge.badgeRarity,
                                imageUrl = userBadge.imageUrl ?: userBadge.badgeIcon,
                                type = ireader.domain.models.remote.BadgeType.ACHIEVEMENT,
                                badgeRarity = when (userBadge.badgeRarity.lowercase()) {
                                    "common" -> ireader.domain.models.remote.BadgeRarity.COMMON
                                    "rare" -> ireader.domain.models.remote.BadgeRarity.RARE
                                    "epic" -> ireader.domain.models.remote.BadgeRarity.EPIC
                                    "legendary" -> ireader.domain.models.remote.BadgeRarity.LEGENDARY
                                    else -> ireader.domain.models.remote.BadgeRarity.COMMON
                                }
                            )
                        }
                    updateState { it.copy(achievementBadges = achievementBadges, isBadgesLoading = false, badgesError = null) }
                },
                onFailure = { error ->
                    updateState { it.copy(badgesError = error.message, isBadgesLoading = false) }
                }
            ) ?: run {
                updateState { it.copy(isBadgesLoading = false) }
            }
        }
    }
    
    private fun loadReadingStatistics() {
        scope.launch {
            updateState { it.copy(isStatsLoading = true) }
            
            try {
                val stats = readingStatisticsRepository?.getStatistics()
                if (stats != null) {
                    updateState { 
                        it.copy(
                            chaptersRead = stats.totalChaptersRead,
                            booksCompleted = stats.booksCompleted,
                            reviewsWritten = 0, // Reviews are tracked separately
                            readingStreak = stats.readingStreak,
                            isStatsLoading = false
                        ) 
                    }
                } else {
                    updateState { 
                        it.copy(
                            chaptersRead = 0,
                            booksCompleted = 0,
                            reviewsWritten = 0,
                            readingStreak = 0,
                            isStatsLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                updateState { 
                    it.copy(
                        chaptersRead = 0,
                        booksCompleted = 0,
                        reviewsWritten = 0,
                        readingStreak = 0,
                        isStatsLoading = false
                    ) 
                }
            }
        }
    }
    
    fun retryLoadBadges() {
        loadFeaturedBadges()
        loadAchievementBadges()
    }
}

@Stable
data class ProfileState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val requiresSignIn: Boolean = false,  // Indicates if error requires user to sign in again
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val lastSyncTime: Long? = null,
    val showUsernameDialog: Boolean = false,
    val showWalletDialog: Boolean = false,
    val showPasswordDialog: Boolean = false,
    val passwordUpdateSuccess: Boolean = false,
    val featuredBadges: List<Badge> = emptyList(),
    val achievementBadges: List<Badge> = emptyList(),
    val isBadgesLoading: Boolean = false,
    val badgesError: String? = null,
    val chaptersRead: Int = 0,
    val booksCompleted: Int = 0,
    val reviewsWritten: Int = 0,
    val readingStreak: Int = 0,
    val isStatsLoading: Boolean = false
)
