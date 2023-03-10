package ireader.presentation.ui.home.sources.extension

import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.SourceState

sealed class SourceUiModel {
    data class Item(val source: Catalog, val state: SourceState) : SourceUiModel()
    data class Header(val language: String) : SourceUiModel()
}
object SourceKeys {
    const val PINNED_KEY = "pinned"
    const val INSTALLED_KEY = "installed"
    const val AVAILABLE = "available"
    const val LAST_USED_KEY = "last_used"
}
