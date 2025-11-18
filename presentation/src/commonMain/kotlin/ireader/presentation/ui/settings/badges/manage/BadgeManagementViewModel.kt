package ireader.presentation.ui.settings.badges.manage

import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.UserBadge
import ireader.domain.usecases.badge.GetUserBadgesUseCase
import ireader.domain.usecases.badge.SetFeaturedBadgesUseCase
import ireader.domain.usecases.badge.SetPrimaryBadgeUseCase
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.settings.badges.BadgeErrorMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BadgeManagementState(
    val ownedBadges: List<Badge> = emptyList(),
    val userBadges: List<UserBadge> = emptyList(),
    val primaryBadgeId: String? = null,
    val featuredBadgeIds: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val saveError: String? = null,
    val hasChanges: Boolean = false
)

class BadgeManagementViewModel(
    private val getUserBadgesUseCase: GetUserBadgesUseCase,
    private val setPrimaryBadgeUseCase: SetPrimaryBadgeUseCase,
    private val setFeaturedBadgesUseCase: SetFeaturedBadgesUseCase
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(BadgeManagementState())
    val state: StateFlow<BadgeManagementState> = _state.asStateFlow()
    
    // Store original values to detect changes
    private var originalPrimaryBadgeId: String? = null
    private var originalFeaturedBadgeIds: List<String> = emptyList()
    
    init {
        loadUserBadges()
    }
    
    fun loadUserBadges() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            getUserBadgesUseCase()
                .onSuccess { userBadges ->
                    // Extract badge information from UserBadge
                    val badges = userBadges.map { userBadge ->
                        Badge(
                            id = userBadge.badgeId,
                            name = userBadge.badgeName,
                            description = userBadge.badgeDescription,
                            icon = userBadge.badgeIcon,
                            category = userBadge.badgeCategory,
                            rarity = userBadge.badgeRarity,
                            imageUrl = userBadge.badgeIcon
                        )
                    }
                    
                    // Get current primary and featured badges
                    val primaryBadge = userBadges.find { it.isPrimary }
                    val featuredBadges = userBadges.filter { it.isFeatured }
                    
                    // Store original values
                    originalPrimaryBadgeId = primaryBadge?.badgeId
                    originalFeaturedBadgeIds = featuredBadges.map { it.badgeId }
                    
                    _state.update { 
                        it.copy(
                            ownedBadges = badges,
                            userBadges = userBadges,
                            primaryBadgeId = primaryBadge?.badgeId,
                            featuredBadgeIds = featuredBadges.map { it.badgeId },
                            isLoading = false,
                            error = null,
                            hasChanges = false
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = BadgeErrorMapper.toUserMessage(exception)
                        )
                    }
                }
        }
    }
    
    fun onSelectPrimaryBadge(badgeId: String?) {
        _state.update { 
            it.copy(
                primaryBadgeId = badgeId,
                hasChanges = checkForChanges(badgeId, it.featuredBadgeIds)
            )
        }
    }
    
    fun onToggleFeaturedBadge(badgeId: String) {
        val currentFeatured = _state.value.featuredBadgeIds
        val newFeatured = if (badgeId in currentFeatured) {
            // Remove badge
            currentFeatured - badgeId
        } else {
            // Add badge if under limit
            if (currentFeatured.size < 3) {
                currentFeatured + badgeId
            } else {
                // Already at max, don't add
                currentFeatured
            }
        }
        
        _state.update { 
            it.copy(
                featuredBadgeIds = newFeatured,
                hasChanges = checkForChanges(it.primaryBadgeId, newFeatured)
            )
        }
    }
    
    private fun checkForChanges(primaryId: String?, featuredIds: List<String>): Boolean {
        return primaryId != originalPrimaryBadgeId || 
               featuredIds.toSet() != originalFeaturedBadgeIds.toSet()
    }
    
    fun onSaveChanges() {
        scope.launch {
            _state.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            
            var hasError = false
            var errorMessage: String? = null
            
            // Save primary badge
            val primaryBadgeId = _state.value.primaryBadgeId
            if (primaryBadgeId != null && primaryBadgeId != originalPrimaryBadgeId) {
                setPrimaryBadgeUseCase(primaryBadgeId)
                    .onFailure { exception ->
                        hasError = true
                        errorMessage = BadgeErrorMapper.toUserMessage(exception)
                    }
            }
            
            // Save featured badges if no error yet
            if (!hasError) {
                val featuredBadgeIds = _state.value.featuredBadgeIds
                if (featuredBadgeIds.toSet() != originalFeaturedBadgeIds.toSet()) {
                    setFeaturedBadgesUseCase(featuredBadgeIds)
                        .onFailure { exception ->
                            hasError = true
                            errorMessage = BadgeErrorMapper.toUserMessage(exception)
                        }
                }
            }
            
            if (hasError) {
                _state.update { 
                    it.copy(
                        isSaving = false,
                        saveError = errorMessage,
                        saveSuccess = false
                    )
                }
            } else {
                // Update original values after successful save
                originalPrimaryBadgeId = _state.value.primaryBadgeId
                originalFeaturedBadgeIds = _state.value.featuredBadgeIds
                
                _state.update { 
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        saveError = null,
                        hasChanges = false
                    )
                }
            }
        }
    }
    
    fun clearSaveStatus() {
        _state.update { 
            it.copy(
                saveSuccess = false,
                saveError = null
            )
        }
    }
}
