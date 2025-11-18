package ireader.presentation.ui.migration
import ireader.domain.models.migration.MigrationFlags
import ireader.domain.models.migration.MigrationSource

import ireader.presentation.core.viewmodel.IReaderStateScreenModel
// screenModelScope is provided by IReaderStateScreenModel
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.MigrationRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.migration.*
import ireader.domain.usecases.migration.MigrateBookUseCase
import ireader.domain.usecases.migration.SearchMigrationTargetsUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Screen model for migration list screen following Mihon's pattern
 */
class MigrationListScreenModel(
    private val bookRepository: BookRepository,
    private val migrationRepository: MigrationRepository,
    private val searchMigrationTargetsUseCase: SearchMigrationTargetsUseCase
) : IReaderStateScreenModel<MigrationListScreenModel.State>(State()) {
    
    data class State(
        val books: List<Book> = emptyList(),
        val selectedBooks: Set<Long> = emptySet(),
        val migrationSources: List<MigrationSource> = emptyList(),
        val isLoading: Boolean = true,
        val searchQuery: String = "",
        val sortOrder: MigrationSortOrder = MigrationSortOrder.TITLE,
        val showOnlyMigratableBooks: Boolean = true
    )
    
    enum class MigrationSortOrder {
        TITLE, AUTHOR, SOURCE, LAST_READ
    }
    
    init {
        loadBooks()
        loadMigrationSources()
    }
    
    private fun loadBooks() {
        screenModelScope.launch {
            try {
                val allBooks = bookRepository.findAllInLibraryBooks(
                    sortType = ireader.domain.models.library.LibrarySort.default,
                    isAsc = false,
                    unreadFilter = false
                )
                updateState { it.copy(
                    books = allBooks,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun loadMigrationSources() {
        screenModelScope.launch {
            try {
                val sources = migrationRepository.getMigrationSources()
                updateState { it.copy(
                    migrationSources = sources
                ) }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun selectBook(bookId: Long) {
        val currentSelected = state.value.selectedBooks
        updateState { it.copy(
            selectedBooks = if (bookId in currentSelected) {
                currentSelected - bookId
            } else {
                currentSelected + bookId
            }
        ) }
    }
    
    fun selectAllBooks() {
        val filteredBooks = getFilteredBooks()
        updateState { it.copy(
            selectedBooks = filteredBooks.map { book -> book.id }.toSet()
        ) }
    }
    
    fun clearSelection() {
        updateState { it.copy(
            selectedBooks = emptySet()
        ) }
    }
    
    fun updateSearchQuery(query: String) {
        updateState { it.copy(
            searchQuery = query
        ) }
    }
    
    fun updateSortOrder(order: MigrationSortOrder) {
        updateState { it.copy(
            sortOrder = order
        ) }
    }
    
    fun toggleShowOnlyMigratable() {
        updateState { it.copy(
            showOnlyMigratableBooks = !state.value.showOnlyMigratableBooks
        ) }
    }
    
    fun getFilteredBooks(): List<Book> {
        val currentState = state.value
        var books = currentState.books
        
        // Apply search filter
        if (currentState.searchQuery.isNotBlank()) {
            books = books.filter { book ->
                book.title.contains(currentState.searchQuery, ignoreCase = true) ||
                book.author.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
        
        // Apply sort order
        books = when (currentState.sortOrder) {
            MigrationSortOrder.TITLE -> books.sortedBy { it.title }
            MigrationSortOrder.AUTHOR -> books.sortedBy { it.author }
            MigrationSortOrder.SOURCE -> books.sortedBy { it.sourceId }
            MigrationSortOrder.LAST_READ -> books.sortedByDescending { it.lastUpdate }
        }
        
        return books
    }
    
    fun startMigration(targetSources: List<Long>, flags: MigrationFlags) {
        val selectedBookIds = state.value.selectedBooks
        val selectedBooks = state.value.books.filter { it.id in selectedBookIds }
        
        if (selectedBooks.isEmpty() || targetSources.isEmpty()) return
        
        // Create migration job
        val job = MigrationJob(
            id = "migration_${System.currentTimeMillis()}",
            books = selectedBooks,
            targetSources = targetSources.map { sourceId ->
                MigrationSource(
                    sourceId = sourceId,
                    sourceName = "Source $sourceId", // Get actual name
                    isEnabled = true
                )
            },
            flags = flags
        )
        
        screenModelScope.launch {
            migrationRepository.saveMigrationJob(job)
        }
    }
}

/**
 * Screen model for migration configuration screen
 */
class MigrationConfigScreenModel(
    private val migrationRepository: MigrationRepository
) : IReaderStateScreenModel<MigrationConfigScreenModel.State>(State()) {
    
    data class State(
        val availableSources: List<MigrationSource> = emptyList(),
        val selectedSources: List<MigrationSource> = emptyList(),
        val migrationFlags: MigrationFlags = MigrationFlags(),
        val isLoading: Boolean = true
    )
    
    init {
        loadConfiguration()
    }
    
    private fun loadConfiguration() {
        screenModelScope.launch {
            try {
                val sources = migrationRepository.getMigrationSources()
                val flags = migrationRepository.getMigrationFlags()
                
                updateState { it.copy(
                    availableSources = sources,
                    migrationFlags = flags,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                updateState { it.copy(
                    isLoading = false
                ) }
            }
        }
    }
    
    fun toggleSource(source: MigrationSource) {
        val currentSelected = state.value.selectedSources
        updateState { it.copy(
            selectedSources = if (source in currentSelected) {
                currentSelected - source
            } else {
                currentSelected + source
            }
        ) }
    }
    
    fun reorderSources(sources: List<MigrationSource>) {
        updateState { it.copy(
            selectedSources = sources
        ) }
    }
    
    fun updateMigrationFlags(flags: MigrationFlags) {
        updateState { it.copy(
            migrationFlags = flags
        ) }
    }
    
    fun saveConfiguration() {
        screenModelScope.launch {
            try {
                migrationRepository.saveMigrationSources(state.value.selectedSources)
                migrationRepository.saveMigrationFlags(state.value.migrationFlags)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

/**
 * Screen model for migration progress screen
 */
class MigrationProgressScreenModel(
    private val migrationRepository: MigrationRepository,
    private val migrateBookUseCase: MigrateBookUseCase
) : IReaderStateScreenModel<MigrationProgressScreenModel.State>(State()) {
    
    data class State(
        val migrationJobs: List<MigrationJob> = emptyList(),
        val currentMigrations: Map<Long, MigrationProgress> = emptyMap(),
        val isLoading: Boolean = true
    )
    
    init {
        loadMigrationJobs()
    }
    
    private fun loadMigrationJobs() {
        migrationRepository.getAllMigrationJobs()
            .catch { e ->
                updateState { it.copy(isLoading = false) }
            }
            .onEach { jobs ->
                updateState { it.copy(
                    migrationJobs = jobs,
                    isLoading = false
                ) }
            }
            .launchIn(screenModelScope)
    }
    
    fun pauseMigrationJob(jobId: String) {
        screenModelScope.launch {
            migrationRepository.updateMigrationJobStatus(jobId, MigrationJobStatus.PAUSED)
        }
    }
    
    fun resumeMigrationJob(jobId: String) {
        screenModelScope.launch {
            migrationRepository.updateMigrationJobStatus(jobId, MigrationJobStatus.RUNNING)
        }
    }
    
    fun cancelMigrationJob(jobId: String) {
        screenModelScope.launch {
            migrationRepository.updateMigrationJobStatus(jobId, MigrationJobStatus.CANCELLED)
        }
    }
    
    fun deleteMigrationJob(jobId: String) {
        screenModelScope.launch {
            migrationRepository.deleteMigrationJob(jobId)
        }
    }
}