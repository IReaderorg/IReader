package ireader.presentation.ui.community

import androidx.compose.runtime.Stable
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlobalGlossary
import ireader.domain.models.entities.GlossaryTermType
import ireader.domain.models.entities.GlossarySyncStatus

/**
 * UI State for the Community Glossary Screen
 * Supports both local book glossaries and global glossaries
 */
@Stable
data class GlossaryState(
    val isLoading: Boolean = false,
    val glossaryEntries: List<Glossary> = emptyList(),
    val globalGlossaryEntries: List<GlobalGlossary> = emptyList(),
    val searchQuery: String = "",
    val selectedBookId: Long? = null,
    val selectedBookKey: String? = null,
    val selectedBookTitle: String? = null,
    val availableBooks: List<BookInfo> = emptyList(),
    val globalBooks: List<GlobalBookInfo> = emptyList(),
    val showAddDialog: Boolean = false,
    val showAddBookDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val editingEntry: Glossary? = null,
    val editingGlobalEntry: GlobalGlossary? = null,
    val filterType: GlossaryTermType? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val viewMode: GlossaryViewMode = GlossaryViewMode.LOCAL,
    val syncStatus: GlossarySyncStatus = GlossarySyncStatus.NOT_SYNCED,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val exportedJson: String? = null,
    val sourceLanguage: String = "auto",
    val targetLanguage: String = "en"
)

/**
 * View mode for glossary screen
 */
enum class GlossaryViewMode {
    LOCAL,  // Books from user's library
    GLOBAL  // Global glossaries (may not be in library)
}

/**
 * Simple book info for the glossary screen (local books)
 */
@Stable
data class BookInfo(
    val id: Long,
    val title: String,
    val glossaryCount: Int = 0
)

/**
 * Global book info for glossaries not tied to local library
 */
@Stable
data class GlobalBookInfo(
    val bookKey: String,
    val title: String,
    val glossaryCount: Int = 0,
    val sourceLanguage: String = "auto",
    val targetLanguage: String = "en",
    val lastSynced: Long? = null
)
