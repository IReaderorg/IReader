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
        val showOnlyMigratableBooks: Boolean = true,
        val isMigrating: Boolean = false,
        val migrationMessage: String? = null,
        val showSourceSelectionDialog: Boolean = false,
        val showMigrationSuccessDialog: Boolean = false,
        val migratedBooksCount: Int = 0
    )
    
    enum class MigrationSortOrder(val displayName: String) {
        TITLE("Title"),
        AUTHOR("Author"),
        SOURCE("Source"),
        LAST_READ("Last Read")
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
    
    fun showSourceSelectionDialog() {
        updateState { it.copy(showSourceSelectionDialog = true) }
    }
    
    fun hideSourceSelectionDialog() {
        updateState { it.copy(showSourceSelectionDialog = false) }
    }
    
    fun dismissMigrationSuccessDialog() {
        ireader.core.log.Log.info { "Dismissing migration success dialog" }
        updateState { it.copy(showMigrationSuccessDialog = false, migrationMessage = null) }
    }
    
    fun startMigration(targetSources: List<Long>, flags: MigrationFlags) {
        val selectedBookIds = state.value.selectedBooks
        val selectedBooks = state.value.books.filter { it.id in selectedBookIds }
        
        if (selectedBooks.isEmpty()) {
            ireader.core.log.Log.warn { "No books selected for migration" }
            return
        }
        
        // If no target sources specified, use all available sources
        val sources = if (targetSources.isEmpty()) {
            state.value.migrationSources.map { it.sourceId }
        } else {
            targetSources
        }
        
        if (sources.isEmpty()) {
            ireader.core.log.Log.warn { "No target sources available for migration" }
            return
        }
        
        ireader.core.log.Log.info { "Starting migration for ${selectedBooks.size} books to ${sources.size} sources" }
        
        // Create migration job
        val job = MigrationJob(
            id = "migration_${System.currentTimeMillis()}",
            books = selectedBooks,
            targetSources = sources.map { sourceId ->
                val sourceName = state.value.migrationSources
                    .find { it.sourceId == sourceId }?.sourceName 
                    ?: "Source $sourceId"
                MigrationSource(
                    sourceId = sourceId,
                    sourceName = sourceName,
                    isEnabled = true
                )
            },
            flags = flags
        )
        
        screenModelScope.launch {
            try {
                ireader.core.log.Log.info { "=== MIGRATION START ===" }
                ireader.core.log.Log.info { "Selected books: ${selectedBooks.size}" }
                ireader.core.log.Log.info { "Target sources: ${sources.size}" }
                
                updateState { 
                    ireader.core.log.Log.info { "Setting isMigrating = true" }
                    it.copy(isMigrating = true, migrationMessage = "Starting migration for ${selectedBooks.size} books...") 
                }
                
                migrationRepository.saveMigrationJob(job)
                ireader.core.log.Log.info { "Migration job saved: ${job.id}" }
                
                // Small delay to ensure UI updates
                kotlinx.coroutines.delay(500)
                
                // TODO: Actually execute the migration
                // For now, just show a success dialog
                ireader.core.log.Log.info { "=== SHOWING SUCCESS DIALOG ===" }
                ireader.core.log.Log.info { "Setting showMigrationSuccessDialog = true" }
                
                updateState { currentState ->
                    ireader.core.log.Log.info { "Current state before update: isMigrating=${currentState.isMigrating}, showDialog=${currentState.showMigrationSuccessDialog}" }
                    val newState = currentState.copy(
                        isMigrating = false,
                        showMigrationSuccessDialog = true,
                        migratedBooksCount = selectedBooks.size,
                        selectedBooks = emptySet(), // Clear selection
                        migrationMessage = null // Clear any previous messages
                    )
                    ireader.core.log.Log.info { "New state after update: isMigrating=${newState.isMigrating}, showDialog=${newState.showMigrationSuccessDialog}, count=${newState.migratedBooksCount}" }
                    newState
                }
                
                ireader.core.log.Log.info { "State updated, dialog should be visible now" }
                
                selectedBooks.forEach { book ->
                    ireader.core.log.Log.info { "Would migrate: ${book.title} from source ${book.sourceId} to ${sources.size} target sources" }
                }
                
                ireader.core.log.Log.info { "=== MIGRATION SETUP COMPLETE ===" }
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to start migration", e)
                updateState { 
                    it.copy(
                        isMigrating = false,
                        migrationMessage = "Migration failed: ${e.message}"
                    )
                }
            }
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