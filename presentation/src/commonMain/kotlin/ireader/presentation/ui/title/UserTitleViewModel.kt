package ireader.presentation.ui.title

import androidx.compose.runtime.Immutable
import ireader.domain.models.entities.TitleRarity
import ireader.domain.models.entities.TitleEffect
import ireader.domain.models.entities.UserTitle
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class UserTitleScreenState(
    val isLoading: Boolean = false,
    val titles: List<UserTitle> = emptyList(),
    val activeTitleId: String? = null,
    val error: String? = null
)

class UserTitleViewModel : BaseViewModel() {

    private val _state = MutableStateFlow(UserTitleScreenState())
    val state: StateFlow<UserTitleScreenState> = _state.asStateFlow()

    init {
        loadTitles()
    }

    private fun loadTitles() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // TODO: Load from repository when implemented
                _state.update { it.copy(isLoading = false, titles = getDefaultTitles()) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    fun activateTitle(title: UserTitle) {
        _state.update { it.copy(activeTitleId = title.id) }
    }

    fun refresh() = loadTitles()
    fun clearError() { _state.update { it.copy(error = null) } }

    private fun getDefaultTitles(): List<UserTitle> = listOf(
        UserTitle("novice", "Novice Reader", "Just getting started", "🌱", TitleRarity.COMMON, TitleEffect.ReadingTimeBonus(1.5, 24), true),
        UserTitle("chapter_master", "Chapter Master", "Read 100 chapters", "📖", TitleRarity.RARE, TitleEffect.ChapterBonus(2.0, 12), false),
        UserTitle("bookworm", "Bookworm", "Read for 100 hours", "🐛", TitleRarity.EPIC, TitleEffect.BookCompletionBonus(3.0, 24), false),
        UserTitle("legend", "Reading Legend", "Read for 1000 hours", "👑", TitleRarity.LEGENDARY, TitleEffect.StreakBonus(2.0, 48), false)
    )
}
