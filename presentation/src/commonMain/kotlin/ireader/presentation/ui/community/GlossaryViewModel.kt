package ireader.presentation.ui.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlobalGlossary
import ireader.domain.models.entities.GlossaryTermType
import ireader.domain.models.entities.GlossarySyncStatus
import ireader.domain.usecases.glossary.DeleteGlossaryEntryUseCase
import ireader.domain.usecases.glossary.ExportGlossaryUseCase
import ireader.domain.usecases.glossary.GetGlossaryByBookIdUseCase
import ireader.domain.usecases.glossary.ImportGlossaryUseCase
import ireader.domain.usecases.glossary.SaveGlossaryEntryUseCase
import ireader.domain.usecases.glossary.SearchGlossaryUseCase
import ireader.domain.usecases.glossary.GlobalGlossaryUseCases
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GlossaryViewModel(
    private val getGlossaryByBookIdUseCase: GetGlossaryByBookIdUseCase,
    private val saveGlossaryEntryUseCase: SaveGlossaryEntryUseCase,
    private val deleteGlossaryEntryUseCase: DeleteGlossaryEntryUseCase,
    private val exportGlossaryUseCase: ExportGlossaryUseCase,
    private val importGlossaryUseCase: ImportGlossaryUseCase,
    private val searchGlossaryUseCase: SearchGlossaryUseCase,
    private val localGetBookUseCases: LocalGetBookUseCases,
    private val globalGlossaryUseCases: GlobalGlossaryUseCases? = null
) : BaseViewModel() {

    var state by mutableStateOf(GlossaryState())
        private set

    init {
        loadBooksWithGlossary()
        loadGlobalBooks()
    }

    private fun loadBooksWithGlossary() {
        scope.launch {
            try {
                state = state.copy(isLoading = true)
                val books = localGetBookUseCases.findAllInLibraryBooks()
                val bookInfoList = books.map { book ->
                    val glossaryCount = try {
                        getGlossaryByBookIdUseCase.execute(book.id).size
                    } catch (e: Exception) {
                        0
                    }
                    BookInfo(
                        id = book.id,
                        title = book.title,
                        glossaryCount = glossaryCount
                    )
                }
                // Show ALL library books, not just those with glossaries
                // Sort by glossary count (books with glossaries first), then by title
                val sortedBooks = bookInfoList.sortedWith(
                    compareByDescending<BookInfo> { it.glossaryCount }
                        .thenBy { it.title }
                )
                
                state = state.copy(
                    availableBooks = sortedBooks,
                    isLoading = false
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun loadGlobalBooks() {
        scope.launch {
            try {
                globalGlossaryUseCases?.let { useCases ->
                    val books = useCases.getBooks.execute()
                    val globalBookInfoList = books.map { (bookKey, bookTitle) ->
                        val entries = useCases.getGlossary.execute(bookKey)
                        GlobalBookInfo(
                            bookKey = bookKey,
                            title = bookTitle,
                            glossaryCount = entries.size,
                            sourceLanguage = entries.firstOrNull()?.sourceLanguage ?: "auto",
                            targetLanguage = entries.firstOrNull()?.targetLanguage ?: "en",
                            lastSynced = entries.maxOfOrNull { it.syncedAt ?: 0L }
                        )
                    }
                    state = state.copy(globalBooks = globalBookInfoList)
                }
            } catch (e: Exception) {
                // Silently fail for global books
            }
        }
    }


    // View mode switching
    fun setViewMode(mode: GlossaryViewMode) {
        state = state.copy(
            viewMode = mode,
            selectedBookId = null,
            selectedBookKey = null,
            selectedBookTitle = null,
            glossaryEntries = emptyList(),
            globalGlossaryEntries = emptyList()
        )
        if (mode == GlossaryViewMode.LOCAL) {
            loadBooksWithGlossary()
        } else {
            loadGlobalBooks()
        }
    }

    // Local book selection
    fun selectBook(bookId: Long, bookTitle: String) {
        state = state.copy(
            selectedBookId = bookId,
            selectedBookTitle = bookTitle,
            viewMode = GlossaryViewMode.LOCAL
        )
        loadGlossaryForBook(bookId)
    }

    // Global book selection
    fun selectGlobalBook(bookKey: String, bookTitle: String) {
        state = state.copy(
            selectedBookKey = bookKey,
            selectedBookTitle = bookTitle,
            viewMode = GlossaryViewMode.GLOBAL
        )
        loadGlobalGlossary(bookKey)
    }

    private fun loadGlossaryForBook(bookId: Long) {
        scope.launch {
            try {
                state = state.copy(isLoading = true)
                getGlossaryByBookIdUseCase.subscribe(bookId).collectLatest { entries ->
                    val filteredEntries = filterEntries(entries)
                    state = state.copy(
                        glossaryEntries = filteredEntries,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun loadGlobalGlossary(bookKey: String) {
        scope.launch {
            try {
                state = state.copy(isLoading = true)
                globalGlossaryUseCases?.getGlossary?.subscribe(bookKey)?.collectLatest { entries ->
                    val filteredEntries = filterGlobalEntries(entries)
                    state = state.copy(
                        globalGlossaryEntries = filteredEntries,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun filterEntries(entries: List<Glossary>): List<Glossary> {
        var result = entries
        if (state.searchQuery.isNotBlank()) {
            result = result.filter {
                it.sourceTerm.contains(state.searchQuery, ignoreCase = true) ||
                it.targetTerm.contains(state.searchQuery, ignoreCase = true)
            }
        }
        if (state.filterType != null) {
            result = result.filter { it.termType == state.filterType }
        }
        return result
    }

    private fun filterGlobalEntries(entries: List<GlobalGlossary>): List<GlobalGlossary> {
        var result = entries
        if (state.searchQuery.isNotBlank()) {
            result = result.filter {
                it.sourceTerm.contains(state.searchQuery, ignoreCase = true) ||
                it.targetTerm.contains(state.searchQuery, ignoreCase = true)
            }
        }
        if (state.filterType != null) {
            result = result.filter { it.termType == state.filterType }
        }
        return result
    }

    fun updateSearchQuery(query: String) {
        state = state.copy(searchQuery = query)
        when {
            state.selectedBookId != null -> loadGlossaryForBook(state.selectedBookId!!)
            state.selectedBookKey != null -> loadGlobalGlossary(state.selectedBookKey!!)
        }
    }

    fun setFilterType(type: GlossaryTermType?) {
        state = state.copy(filterType = type)
        when {
            state.selectedBookId != null -> loadGlossaryForBook(state.selectedBookId!!)
            state.selectedBookKey != null -> loadGlobalGlossary(state.selectedBookKey!!)
        }
    }

    // Dialog controls
    fun showAddDialog() { state = state.copy(showAddDialog = true) }
    fun hideAddDialog() { state = state.copy(showAddDialog = false) }
    fun showAddBookDialog() { state = state.copy(showAddBookDialog = true) }
    fun hideAddBookDialog() { state = state.copy(showAddBookDialog = false) }
    fun showImportDialog() { state = state.copy(showImportDialog = true) }
    fun hideImportDialog() { state = state.copy(showImportDialog = false) }
    fun showExportDialog() { state = state.copy(showExportDialog = true) }
    fun hideExportDialog() { state = state.copy(showExportDialog = false, exportedJson = null) }

    fun setEditingEntry(entry: Glossary?) { state = state.copy(editingEntry = entry) }
    fun setEditingGlobalEntry(entry: GlobalGlossary?) { state = state.copy(editingGlobalEntry = entry) }


    // Local glossary CRUD
    fun addGlossaryEntry(source: String, target: String, type: GlossaryTermType, notes: String?) {
        val bookId = state.selectedBookId ?: return
        scope.launch {
            try {
                saveGlossaryEntryUseCase.execute(
                    bookId = bookId,
                    sourceTerm = source,
                    targetTerm = target,
                    termType = type,
                    notes = notes
                )
                state = state.copy(showAddDialog = false, successMessage = "Entry added")
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun updateGlossaryEntry(entry: Glossary) {
        scope.launch {
            try {
                saveGlossaryEntryUseCase.execute(
                    bookId = entry.bookId,
                    sourceTerm = entry.sourceTerm,
                    targetTerm = entry.targetTerm,
                    termType = entry.termType,
                    notes = entry.notes,
                    entryId = entry.id
                )
                state = state.copy(editingEntry = null, successMessage = "Entry updated")
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun deleteGlossaryEntry(id: Long) {
        scope.launch {
            try {
                deleteGlossaryEntryUseCase.execute(id)
                state = state.copy(successMessage = "Entry deleted")
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    // Global glossary CRUD
    fun addGlobalGlossaryEntry(source: String, target: String, type: GlossaryTermType, notes: String?) {
        val bookKey = state.selectedBookKey ?: return
        val bookTitle = state.selectedBookTitle ?: return
        scope.launch {
            try {
                globalGlossaryUseCases?.saveGlossary?.execute(
                    bookKey = bookKey,
                    bookTitle = bookTitle,
                    sourceTerm = source,
                    targetTerm = target,
                    termType = type,
                    notes = notes,
                    sourceLanguage = state.sourceLanguage,
                    targetLanguage = state.targetLanguage
                )
                state = state.copy(showAddDialog = false, successMessage = "Entry added")
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun updateGlobalGlossaryEntry(entry: GlobalGlossary) {
        scope.launch {
            try {
                globalGlossaryUseCases?.saveGlossary?.execute(
                    bookKey = entry.bookKey,
                    bookTitle = entry.bookTitle,
                    sourceTerm = entry.sourceTerm,
                    targetTerm = entry.targetTerm,
                    termType = entry.termType,
                    notes = entry.notes,
                    sourceLanguage = entry.sourceLanguage,
                    targetLanguage = entry.targetLanguage,
                    entryId = entry.id
                )
                state = state.copy(editingGlobalEntry = null, successMessage = "Entry updated")
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun deleteGlobalGlossaryEntry(id: Long) {
        scope.launch {
            try {
                globalGlossaryUseCases?.deleteGlossary?.execute(id)
                state = state.copy(successMessage = "Entry deleted")
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    // Add new global book
    fun addGlobalBook(bookKey: String, bookTitle: String, sourceLanguage: String, targetLanguage: String) {
        scope.launch {
            try {
                state = state.copy(
                    selectedBookKey = bookKey,
                    selectedBookTitle = bookTitle,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    showAddBookDialog = false,
                    viewMode = GlossaryViewMode.GLOBAL
                )
                loadGlobalBooks()
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }


    // Export/Import for local glossary
    fun exportGlossary(onSuccess: (String) -> Unit) {
        val bookId = state.selectedBookId ?: return
        val bookTitle = state.selectedBookTitle ?: "Unknown"
        scope.launch {
            try {
                val json = exportGlossaryUseCase.execute(bookId, bookTitle)
                state = state.copy(exportedJson = json)
                onSuccess(json)
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun importGlossary(json: String) {
        val bookId = state.selectedBookId ?: return
        scope.launch {
            try {
                importGlossaryUseCase.execute(json, bookId)
                loadGlossaryForBook(bookId)
                state = state.copy(showImportDialog = false, successMessage = "Glossary imported")
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    // Export/Import for global glossary
    fun exportGlobalGlossary(onSuccess: (String) -> Unit) {
        val bookKey = state.selectedBookKey ?: return
        scope.launch {
            try {
                globalGlossaryUseCases?.exportGlossary?.execute(bookKey)?.onSuccess { json ->
                    state = state.copy(exportedJson = json)
                    onSuccess(json)
                }?.onFailure { e ->
                    state = state.copy(error = e.message)
                }
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun importGlobalGlossary(json: String) {
        val bookKey = state.selectedBookKey ?: return
        val bookTitle = state.selectedBookTitle ?: "Unknown"
        scope.launch {
            try {
                globalGlossaryUseCases?.importGlossary?.execute(json, bookKey, bookTitle)
                    ?.onSuccess { count ->
                        loadGlobalGlossary(bookKey)
                        state = state.copy(
                            showImportDialog = false,
                            successMessage = "Imported $count entries"
                        )
                    }?.onFailure { e ->
                        state = state.copy(error = e.message)
                    }
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    // Sync operations
    fun syncToRemote() {
        val bookKey = state.selectedBookKey ?: return
        scope.launch {
            try {
                state = state.copy(isSyncing = true, syncStatus = GlossarySyncStatus.SYNCING)
                globalGlossaryUseCases?.syncGlossary?.syncToRemote(bookKey)
                    ?.onSuccess { count ->
                        state = state.copy(
                            isSyncing = false,
                            syncStatus = GlossarySyncStatus.SYNCED,
                            successMessage = "Synced $count entries to cloud"
                        )
                    }?.onFailure { e ->
                        state = state.copy(
                            isSyncing = false,
                            syncStatus = GlossarySyncStatus.SYNC_ERROR,
                            error = e.message
                        )
                    }
            } catch (e: Exception) {
                state = state.copy(
                    isSyncing = false,
                    syncStatus = GlossarySyncStatus.SYNC_ERROR,
                    error = e.message
                )
            }
        }
    }

    fun syncFromRemote() {
        val bookKey = state.selectedBookKey ?: return
        scope.launch {
            try {
                state = state.copy(isSyncing = true, syncStatus = GlossarySyncStatus.SYNCING)
                globalGlossaryUseCases?.syncGlossary?.syncFromRemote(bookKey)
                    ?.onSuccess { count ->
                        loadGlobalGlossary(bookKey)
                        state = state.copy(
                            isSyncing = false,
                            syncStatus = GlossarySyncStatus.SYNCED,
                            successMessage = "Downloaded $count entries from cloud"
                        )
                    }?.onFailure { e ->
                        state = state.copy(
                            isSyncing = false,
                            syncStatus = GlossarySyncStatus.SYNC_ERROR,
                            error = e.message
                        )
                    }
            } catch (e: Exception) {
                state = state.copy(
                    isSyncing = false,
                    syncStatus = GlossarySyncStatus.SYNC_ERROR,
                    error = e.message
                )
            }
        }
    }

    fun syncAllFromRemote() {
        scope.launch {
            try {
                state = state.copy(isSyncing = true, syncStatus = GlossarySyncStatus.SYNCING)
                globalGlossaryUseCases?.syncGlossary?.syncAll()
                    ?.onSuccess { count ->
                        loadGlobalBooks()
                        state = state.copy(
                            isSyncing = false,
                            syncStatus = GlossarySyncStatus.SYNCED,
                            successMessage = "Synced $count entries"
                        )
                    }?.onFailure { e ->
                        state = state.copy(
                            isSyncing = false,
                            syncStatus = GlossarySyncStatus.SYNC_ERROR,
                            error = e.message
                        )
                    }
            } catch (e: Exception) {
                state = state.copy(
                    isSyncing = false,
                    syncStatus = GlossarySyncStatus.SYNC_ERROR,
                    error = e.message
                )
            }
        }
    }

    // Get glossary map for translation engine
    fun getGlossaryMapForTranslation(): Map<String, String> {
        return when (state.viewMode) {
            GlossaryViewMode.LOCAL -> state.glossaryEntries.associate { it.sourceTerm to it.targetTerm }
            GlossaryViewMode.GLOBAL -> state.globalGlossaryEntries.associate { it.sourceTerm to it.targetTerm }
        }
    }

    // Clear selection
    fun clearSelectedBook() {
        state = state.copy(
            selectedBookId = null,
            selectedBookKey = null,
            selectedBookTitle = null,
            glossaryEntries = emptyList(),
            globalGlossaryEntries = emptyList()
        )
        if (state.viewMode == GlossaryViewMode.LOCAL) {
            loadBooksWithGlossary()
        } else {
            loadGlobalBooks()
        }
    }

    fun clearError() { state = state.copy(error = null) }
    fun clearSuccessMessage() { state = state.copy(successMessage = null) }
}
