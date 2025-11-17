package ireader.presentation.ui.migration

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.MigrationRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.migration.*
import ireader.domain.usecases.migration.MigrateBookUseCase
import ireader.domain.usecases.migration.SearchMigrationTargetsUseCase
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
) : StateScreenModel<MigrationListScreenModel.State>(State()) {
    
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
                val allBooks = bookRepository.getFavorites() // Only show library books
                mutableState.value = mutableState.value.copy(
                    books = allBooks,
                    isLoading = false
                )
            } catch (e: Exception) {
                mutableState.value = mutableState.value.copy(
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadMigrationSources() {
        screenModelScope.launch {
            try {
                val sources = migrationRepository.getMigrationSources()
                mutableState.value = mutableState.value.copy(
                    migrationSources = sources
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun selectBook(bookId: Long) {
        val currentSelected = mutableState.value.selectedBooks
        mutableState.value = mutableState.value.copy(
            selectedBooks = if (bookId in currentSelected) {
                currentSelected - bookId
            } else {
                currentSelected + bookId
            }
        )
    }
    
    fun selectAllBooks() {
        val filteredBooks = getFilteredBooks()
        mutableState.value = mutableState.value.copy(
            selectedBooks = filteredBooks.map { it.id }.toSet()
        )
    }
    
    fun clearSelection() {
        mutableState.value = mutableState.value.copy(
            selectedBooks = emptySet()
        )
    }
    
    fun updateSearchQuery(query: String) {
        mutableState.value = mutableState.value.copy(
            searchQuery = query
        )
    }
    
    fun updateSortOrder(order: MigrationSortOrder) {
        mutableState.value = mutableState.value.copy(
            sortOrder = order
        )
    }
    
    fun toggleShowOnlyMigratable() {
        mutableState.value = mutableState.value.copy(
            showOnlyMigratableBooks = !mutableState.value.showOnlyMigratableBooks
        )
    }
    
    fun getFilteredBooks(): List<Book> {
        val state = mutableState.value
        var books = state.books
        
        // Apply search filter
        if (state.searchQuery.isNotBlank()) {
            books = books.filter { book ->
                book.title.contains(state.searchQuery, ignoreCase = true) ||
                book.author?.contains(state.searchQuery, ignoreCase = true) == true
            }
        }
        
        // Apply sort order
        books = when (state.sortOrder) {
            MigrationSortOrder.TITLE -> books.sortedBy { it.title }
            MigrationSortOrder.AUTHOR -> books.sortedBy { it.author ?: "" }
            MigrationSortOrder.SOURCE -> books.sortedBy { it.sourceId }
            MigrationSortOrder.LAST_READ -> books.sortedByDescending { it.lastUpdate ?: 0 }
        }
        
        return books
    }
    
    fun startMigration(targetSources: List<Long>, flags: MigrationFlags) {
        val selectedBookIds = mutableState.value.selectedBooks
        val selectedBooks = mutableState.value.books.filter { it.id in selectedBookIds }
        
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
) : StateScreenModel<MigrationConfigScreenModel.State>(State()) {
    
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
                
                mutableState.value = mutableState.value.copy(
                    availableSources = sources,
                    migrationFlags = flags,
                    isLoading = false
                )
            } catch (e: Exception) {
                mutableState.value = mutableState.value.copy(
                    isLoading = false
                )
            }
        }
    }
    
    fun toggleSource(source: MigrationSource) {
        val currentSelected = mutableState.value.selectedSources
        mutableState.value = mutableState.value.copy(
            selectedSources = if (source in currentSelected) {
                currentSelected - source
            } else {
                currentSelected + source
            }
        )
    }
    
    fun reorderSources(sources: List<MigrationSource>) {
        mutableState.value = mutableState.value.copy(
            selectedSources = sources
        )
    }
    
    fun updateMigrationFlags(flags: MigrationFlags) {
        mutableState.value = mutableState.value.copy(
            migrationFlags = flags
        )
    }
    
    fun saveConfiguration() {
        screenModelScope.launch {
            try {
                migrationRepository.saveMigrationSources(mutableState.value.selectedSources)
                migrationRepository.saveMigrationFlags(mutableState.value.migrationFlags)
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
) : StateScreenModel<MigrationProgressScreenModel.State>(State()) {
    
    data class State(
        val migrationJobs: List<MigrationJob> = emptyList(),
        val currentMigrations: Map<Long, MigrationProgress> = emptyMap(),
        val isLoading: Boolean = true
    )
    
    init {
        loadMigrationJobs()
        observeMigrationProgress()
    }
    
    private fun loadMigrationJobs() {
        migrationRepository.getAllMigrationJobs()
            .onEach { jobs ->
                mutableState.value = mutableState.value.copy(
                    migrationJobs = jobs,
                    isLoading = false
                )
            }
            .launchIn(screenModelScope)
    }
    
    private fun observeMigrationProgress() {
        // This would observe individual migration progress
        // Implementation depends on how migration progress is tracked
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