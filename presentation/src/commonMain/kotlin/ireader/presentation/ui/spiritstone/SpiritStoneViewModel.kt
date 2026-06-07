package ireader.presentation.ui.spiritstone

import androidx.compose.runtime.Immutable
import ireader.domain.models.entities.ShopItem
import ireader.domain.models.entities.ShopItemType
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SpiritStoneScreenState(
    val isLoading: Boolean = false,
    val balance: Long = 0,
    val totalEarned: Long = 0,
    val totalSpent: Long = 0,
    val shopItems: List<ShopItem> = emptyList(),
    val ownedItemIds: Set<String> = emptySet(),
    val error: String? = null
)

class SpiritStoneViewModel : BaseViewModel() {

    private val _state = MutableStateFlow(SpiritStoneScreenState())
    val state: StateFlow<SpiritStoneScreenState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // TODO: Load from SpiritStoneRepository when implemented
                _state.update {
                    it.copy(
                        isLoading = false,
                        balance = 0,
                        shopItems = getDefaultShopItems()
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load")
                }
            }
        }
    }

    fun purchaseItem(item: ShopItem) {
        scope.launch {
            try {
                // TODO: Implement purchase via SpiritStoneRepository
                _state.update {
                    it.copy(
                        balance = it.balance - item.price,
                        ownedItemIds = it.ownedItemIds + item.id,
                        totalSpent = it.totalSpent + item.price
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Purchase failed") }
            }
        }
    }

    fun refresh() = loadData()
    fun clearError() { _state.update { it.copy(error = null) } }

    private fun getDefaultShopItems(): List<ShopItem> = listOf(
        ShopItem("badge_1", "Profile Badge", "Show off your reading status", ShopItemType.PROFILE_BADGE, 50, "🏅"),
        ShopItem("frame_1", "Profile Frame", "Circle frame for your avatar", ShopItemType.PROFILE_FRAME, 100, "🖼"),
        ShopItem("bg_1", "Profile Background", "Custom banner for your profile", ShopItemType.PROFILE_BACKGROUND, 150, "🌄"),
        ShopItem("title_1", "Profile Title", "Special title under your name", ShopItemType.PROFILE_TITLE, 200, "📛"),
        ShopItem("special_badge_1", "Special Badge", "Rare exclusive badge", ShopItemType.SPECIAL_BADGE, 300, "💎"),
        ShopItem("effect_1", "Animated Effect", "Animated profile effect", ShopItemType.ANIMATED_EFFECT, 500, "✨")
    )
}
