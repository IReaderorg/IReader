package ireader.presentation.ui.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlossaryTermType
import ireader.domain.usecases.glossary.DeleteGlossaryEntryUseCase
import ireader.domain.usecases.glossary.ExportGlossaryUseCase
import ireader.domain.usecases.glossary.GetGlossaryByBookIdUseCase
import ireader.domain.usecases.glossary.ImportGlossaryUseCase
import ireader.domain.usecases.glossary.SaveGlossaryEntryUseCase
import ireader.domain.usecases.glossary.SearchGlossaryUseCase
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
    private val localGetBookUseCases: LocalGetBookUseCases
) : BaseViewModel() {

    var state by mutableStateOf(GlossaryState())
        private set

    init {
        loadBooksWithGlossary()
    }

    private fun loadBooksWithGlossary() {
        scope.launch {
            try {
                state = state.copy(isLoading = true)
                // Get all books from library that might have glossary entries
                val books = localGetBookUseCases.findAllInLibraryBooks()
                val bookInfoList = books.map { book ->
                    val glossaryCount = getGlossaryByBookIdUseCase.execute(book.id).size
                    BookInfo(
                        id = book.id,
                        title = book.title,
                        glossaryCount = glossaryCount
                    )
                }.filter { it.glossaryCount > 0 || state.selectedBookId == it.id }
                
                state = state.copy(
                    availableBooks = bookInfoList,
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


    fun selectBook(bookId: Long, bookTitle: String) {
        state = state.copy(
            selectedBookId = bookId,
            selectedBookTitle = bookTitle
        )
        loadGlossaryForBook(bookId)
    }

    private fun loadGlossaryForBook(bookId: Long) {
        scope.launch {
            try {
                state = state.copy(isLoading = true)
                getGlossaryByBookIdUseCase.subscribe(bookId).collectLatest { entries ->
                    val filteredEntries = if (state.searchQuery.isBlank()) {
                        entries
                    } else {
                        entries.filter {
                            it.sourceTerm.contains(state.searchQuery, ignoreCase = true) ||
                            it.targetTerm.contains(state.searchQuery, ignoreCase = true)
                        }
                    }
                    
                    val typeFilteredEntries = if (state.filterType != null) {
                        filteredEntries.filter { it.termType == state.filterType }
                    } else {
                        filteredEntries
                    }
                    
                    state = state.copy(
                        glossaryEntries = typeFilteredEntries,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        state = state.copy(searchQuery = query)
        state.selectedBookId?.let { loadGlossaryForBook(it) }
    }

    fun setFilterType(type: GlossaryTermType?) {
        state = state.copy(filterType = type)
        state.selectedBookId?.let { loadGlossaryForBook(it) }
    }

    fun showAddDialog() {
        state = state.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        state = state.copy(showAddDialog = false)
    }

    fun setEditingEntry(entry: Glossary?) {
        state = state.copy(editingEntry = entry)
    }

    fun addGlossaryEntry(
        source: String,
        target: String,
        type: GlossaryTermType,
        notes: String?
    ) {
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
                state = state.copy(showAddDialog = false)
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
                state = state.copy(editingEntry = null)
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun deleteGlossaryEntry(id: Long) {
        scope.launch {
            try {
                deleteGlossaryEntryUseCase.execute(id)
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun exportGlossary(onSuccess: (String) -> Unit) {
        val bookId = state.selectedBookId ?: return
        val bookTitle = state.selectedBookTitle ?: "Unknown"
        scope.launch {
            try {
                val json = exportGlossaryUseCase.execute(bookId, bookTitle)
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
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }

    fun clearSelectedBook() {
        state = state.copy(
            selectedBookId = null,
            selectedBookTitle = null,
            glossaryEntries = emptyList()
        )
        loadBooksWithGlossary()
    }
}
