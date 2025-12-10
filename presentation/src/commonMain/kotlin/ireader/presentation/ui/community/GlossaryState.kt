package ireader.presentation.ui.community

import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlossaryTermType

/**
 * UI State for the Community Glossary Screen
 */
data class GlossaryState(
    val isLoading: Boolean = false,
    val glossaryEntries: List<Glossary> = emptyList(),
    val searchQuery: String = "",
    val selectedBookId: Long? = null,
    val selectedBookTitle: String? = null,
    val availableBooks: List<BookInfo> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingEntry: Glossary? = null,
    val filterType: GlossaryTermType? = null,
    val error: String? = null
)

/**
 * Simple book info for the glossary screen
 */
data class BookInfo(
    val id: Long,
    val title: String,
    val glossaryCount: Int = 0
)
