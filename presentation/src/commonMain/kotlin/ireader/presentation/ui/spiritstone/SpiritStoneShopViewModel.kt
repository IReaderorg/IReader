package ireader.presentation.ui.spiritstone

import androidx.compose.runtime.Stable
import ireader.domain.data.repository.BadgeRepository
import ireader.domain.data.repository.GamificationRepository
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopTitle(
    val id: String,
    val name: String,
    val rarity: String,
    val cost: Int,
    val isOwned: Boolean,
    val isActive: Boolean
)

data class ShopBadge(
    val id: String,
    val name: String,
    val description: String,
    val rarity: String,
    val cost: Int,
    val isOwned: Boolean,
    val imageUrl: String? = null,
    val category: String = ""
)

@Stable
data class SpiritStoneShopState(
    val spiritStones: Long = 0,
    val availableTitles: List<ShopTitle> = emptyList(),
    val availableBadges: List<ShopBadge> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@Stable
class SpiritStoneShopViewModel(
    private val gamificationRepository: GamificationRepository,
    private val badgeRepository: BadgeRepository,
    private val getCurrentUser: suspend () -> ireader.domain.models.remote.User?
) : BaseViewModel() {

    private val _state = MutableStateFlow(SpiritStoneShopState())
    val state: StateFlow<SpiritStoneShopState> = _state.asStateFlow()

    init {
        loadShopData()
    }

    private fun loadShopData() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val user = getCurrentUser()
                val userId = user?.id ?: ""

                if (userId.isEmpty()) {
                    _state.update { it.copy(isLoading = false, error = "Please sign in to use the shop") }
                    return@launch
                }

                // Load profile for spirit stones
                val profile = gamificationRepository.getProfile(userId).getOrNull()

                // Load owned titles
                val ownedTitles = gamificationRepository.getOwnedTitles(userId).getOrDefault(emptyList())

                // Available titles for purchase
                val titles = listOf(
                    ShopTitle("title_bookworm", "Bookworm", "RARE", 100, ownedTitles.any { it.titleId == "title_bookworm" }, ownedTitles.any { it.titleId == "title_bookworm" && it.isActive }),
                    ShopTitle("title_night_owl", "Night Owl", "RARE", 150, ownedTitles.any { it.titleId == "title_night_owl" }, ownedTitles.any { it.titleId == "title_night_owl" && it.isActive }),
                    ShopTitle("title_speed_reader", "Speed Reader", "EPIC", 300, ownedTitles.any { it.titleId == "title_speed_reader" }, ownedTitles.any { it.titleId == "title_speed_reader" && it.isActive }),
                    ShopTitle("titleLiterary_legend", "Literary Legend", "EPIC", 500, ownedTitles.any { it.titleId == "title_literary_legend" }, ownedTitles.any { it.titleId == "title_literary_legend" && it.isActive }),
                    ShopTitle("title_reading_deity", "Reading Deity", "LEGENDARY", 1000, ownedTitles.any { it.titleId == "title_reading_deity" }, ownedTitles.any { it.titleId == "title_reading_deity" && it.isActive }),
                    ShopTitle("title_streak_master", "Streak Master", "RARE", 200, ownedTitles.any { it.titleId == "title_streak_master" }, ownedTitles.any { it.titleId == "title_streak_master" && it.isActive }),
                    ShopTitle("title_chapter_hunter", "Chapter Hunter", "COMMON", 50, ownedTitles.any { it.titleId == "title_chapter_hunter" }, ownedTitles.any { it.titleId == "title_chapter_hunter" && it.isActive }),
                    ShopTitle("title_genre_explorer", "Genre Explorer", "COMMON", 75, ownedTitles.any { it.titleId == "title_genre_explorer" }, ownedTitles.any { it.titleId == "title_genre_explorer" && it.isActive }),
                )

                // Load available badges from Supabase (achievement badges with spirit stone costs)
                val allBadges = badgeRepository.getAvailableBadges().getOrDefault(emptyList())
                val userBadges = badgeRepository.getUserBadges(userId).getOrDefault(emptyList())
                val ownedBadgeIds = userBadges.map { it.badgeId }.toSet()

                // Filter to ACHIEVEMENT type badges that have spirit stone costs
                val badges = allBadges
                    .filter { it.type == ireader.domain.models.remote.BadgeType.ACHIEVEMENT }
                    .map { badge ->
                        val cost = when (badge.rarity) {
                            "COMMON" -> 50
                            "RARE" -> 150
                            "EPIC" -> 300
                            "LEGENDARY" -> 800
                            else -> 100
                        }
                        ShopBadge(
                            id = badge.id,
                            name = badge.name,
                            description = badge.description,
                            rarity = badge.rarity,
                            cost = cost,
                            isOwned = badge.id in ownedBadgeIds,
                            imageUrl = badge.imageUrl,
                            category = badge.category
                        )
                    }
                    .sortedWith(compareByDescending<ShopBadge> { it.rarity == "LEGENDARY" }
                        .thenByDescending { it.rarity == "EPIC" }
                        .thenByDescending { it.rarity == "RARE" })

                _state.update {
                    it.copy(
                        spiritStones = profile?.spiritStones ?: 0,
                        availableTitles = titles,
                        availableBadges = badges,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load shop: ${e.message}"
                    )
                }
            }
        }
    }

    fun purchaseItem(itemId: String) {
        scope.launch {
            try {
                val currentState = _state.value
                val title = currentState.availableTitles.find { it.id == itemId }
                val badge = currentState.availableBadges.find { it.id == itemId }

                val cost = title?.cost ?: badge?.cost ?: return@launch
                if (currentState.spiritStones < cost) {
                    _state.update { it.copy(error = "Not enough Spirit Stones!") }
                    return@launch
                }

                val result = gamificationRepository.spendStones(
                    itemType = if (title != null) "TITLE" else "BADGE",
                    itemId = itemId,
                    cost = cost
                )

                if (result.isSuccess) {
                    _state.update {
                        it.copy(
                            spiritStones = it.spiritStones - cost,
                            availableTitles = it.availableTitles.map { t ->
                                if (t.id == itemId) t.copy(isOwned = true) else t
                            },
                            availableBadges = it.availableBadges.map { b ->
                                if (b.id == itemId) b.copy(isOwned = true) else b
                            },
                            successMessage = "Purchase successful! 🎉"
                        )
                    }
                } else {
                    _state.update { it.copy(error = "Purchase failed: ${result.exceptionOrNull()?.message}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Purchase failed: ${e.message}") }
            }
        }
    }

    fun equipTitle(titleId: String) {
        scope.launch {
            try {
                gamificationRepository.setActiveTitle(titleId)
                _state.update {
                    it.copy(
                        availableTitles = it.availableTitles.map { t ->
                            t.copy(isActive = t.id == titleId)
                        },
                        successMessage = "Title equipped! ✨"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to equip title: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
}
