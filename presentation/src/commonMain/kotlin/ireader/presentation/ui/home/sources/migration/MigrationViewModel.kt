package ireader.presentation.ui.home.sources.migration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.toBookItem
import ireader.domain.models.migration.MigrationMatch
import ireader.domain.models.migration.MigrationProgress
import ireader.domain.models.migration.MigrationRequest
import ireader.domain.models.migration.MigrationResult
import ireader.domain.models.migration.MigrationStatus
import ireader.domain.usecases.migration.MigrateNovelUseCase
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MigrationViewModel(
    private val bookRepository: BookRepository,
    private val catalogStore: CatalogStore,
    private val migrateNovelUseCase: MigrateNovelUseCase
) : BaseViewModel() {
    
    // Source being migrated from
    var sourceId by mutableStateOf<Long?>(null)
        private set
    
    // Novels from the source
    private val _novels = MutableStateFlow<List<BookItem>>(emptyList())
    val novels: StateFlow<List<BookItem>> = _novels.asStateFlow()
    
    // Selected novels for migration
    private val _selectedNovels = MutableStateFlow<Set<Long>>(emptySet())
    val selectedNovels: StateFlow<Set<Long>> = _selectedNovels.asStateFlow()
    
    // Available target sources
    private val _targetSources = MutableStateFlow<List<CatalogLocal>>(emptyList())
    val targetSources: StateFlow<List<CatalogLocal>> = _targetSources.asStateFlow()
    
    // Selected target source
    var targetSourceId by mutableStateOf<Long?>(null)
        private set
    
    // Migration progress
    private val _migrationProgress = MutableStateFlow<Map<Long, MigrationProgress>>(emptyMap())
    val migrationProgress: StateFlow<Map<Long, MigrationProgress>> = _migrationProgress.asStateFlow()
    
    // Current novel being matched
    private val _currentMatchingNovel = MutableStateFlow<Book?>(null)
    val currentMatchingNovel: StateFlow<Book?> = _currentMatchingNovel.asStateFlow()
    
    // Matches for current novel
    private val _matches = MutableStateFlow<List<MigrationMatch>>(emptyList())
    val matches: StateFlow<List<MigrationMatch>> = _matches.asStateFlow()
    
    // Migration results
    private val _migrationResults = MutableStateFlow<List<MigrationResult>>(emptyList())
    val migrationResults: StateFlow<List<MigrationResult>> = _migrationResults.asStateFlow()
    
    // Loading states
    var isLoadingNovels by mutableStateOf(false)
        private set
    
    var isSearchingMatches by mutableStateOf(false)
        private set
    
    var isMigrating by mutableStateOf(false)
        private set
    
    /**
     * Load novels from a specific source
     */
    fun loadNovelsFromSource(sourceId: Long) {
        this.sourceId = sourceId
        scope.launch {
            isLoadingNovels = true
            try {
                // Query books from the source that are in the library
                val books = bookRepository.findAllInLibraryBooks(
                    sortType = ireader.domain.models.library.LibrarySort.default,
                    isAsc = true
                ).filter { it.sourceId == sourceId }
                
                _novels.value = books.map { it.toBookItem() }
                
                // Load available target sources (all except current source)
                val allSources = catalogStore.catalogs.filter { 
                    it.sourceId != sourceId && it.source != null 
                }
                _targetSources.value = allSources
                
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Failed to load novels: ${e.message ?: "Unknown error"}"))
            } finally {
                isLoadingNovels = false
            }
        }
    }
    
    /**
     * Toggle novel selection
     */
    fun toggleNovelSelection(novelId: Long) {
        val current = _selectedNovels.value.toMutableSet()
        if (current.contains(novelId)) {
            current.remove(novelId)
        } else {
            current.add(novelId)
        }
        _selectedNovels.value = current
    }
    
    /**
     * Select all novels
     */
    fun selectAll() {
        _selectedNovels.value = _novels.value.map { it.id }.toSet()
    }
    
    /**
     * Deselect all novels
     */
    fun deselectAll() {
        _selectedNovels.value = emptySet()
    }
    
    /**
     * Set target source
     */
    fun setTargetSource(sourceId: Long) {
        targetSourceId = sourceId
    }
    
    /**
     * Start migration process
     */
    fun startMigration() {
        val target = targetSourceId
        val source = sourceId
        
        if (target == null || source == null) {
            showSnackBar(UiText.DynamicString("Please select target source"))
            return
        }
        
        if (_selectedNovels.value.isEmpty()) {
            showSnackBar(UiText.DynamicString("Please select at least one novel"))
            return
        }
        
        scope.launch {
            isMigrating = true
            val results = mutableListOf<MigrationResult>()
            
            try {
                for (novelId in _selectedNovels.value) {
                    // Get the novel
                    val novel = bookRepository.findBookById(novelId)
                    if (novel == null) {
                        results.add(MigrationResult(novelId, false, null, "Novel not found"))
                        continue
                    }
                    
                    // Set as current matching novel
                    _currentMatchingNovel.value = novel
                    
                    // Search for matches
                    isSearchingMatches = true
                    migrateNovelUseCase.searchMatches(novel, target).collect { matches ->
                        _matches.value = matches
                        isSearchingMatches = false
                    }
                    
                    // Wait for user to select a match (this will be handled by the UI)
                    // For now, we'll skip if no matches found
                    if (_matches.value.isEmpty()) {
                        results.add(MigrationResult(novelId, false, null, "No matches found"))
                        _currentMatchingNovel.value = null
                        continue
                    }
                    
                    // The actual migration will be triggered by user selection in the UI
                    // This is just the preparation phase
                }
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Migration failed: ${e.message ?: "Unknown error"}"))
            } finally {
                isMigrating = false
                _migrationResults.value = results
            }
        }
    }
    
    /**
     * Perform migration for a specific novel with selected match
     */
    fun migrateNovel(novelId: Long, selectedMatch: BookItem) {
        val target = targetSourceId
        val source = sourceId
        
        if (target == null || source == null) return
        
        scope.launch {
            val request = MigrationRequest(
                novelId = novelId,
                sourceId = source,
                targetSourceId = target,
                preserveProgress = true
            )
            
            migrateNovelUseCase.migrate(request, selectedMatch).collect { progress ->
                val currentProgress = _migrationProgress.value.toMutableMap()
                currentProgress[novelId] = progress
                _migrationProgress.value = currentProgress
                
                if (progress.status == MigrationStatus.COMPLETED) {
                    val results = _migrationResults.value.toMutableList()
                    results.add(MigrationResult(novelId, true, null, null))
                    _migrationResults.value = results
                    
                    // Clear current matching novel
                    _currentMatchingNovel.value = null
                    _matches.value = emptyList()
                    
                    showSnackBar(UiText.DynamicString("Migration completed successfully"))
                } else if (progress.status == MigrationStatus.FAILED) {
                    val results = _migrationResults.value.toMutableList()
                    results.add(MigrationResult(novelId, false, null, progress.error))
                    _migrationResults.value = results
                    
                    showSnackBar(UiText.DynamicString("Migration failed: ${progress.error}"))
                }
            }
        }
    }
    
    /**
     * Skip current novel
     */
    fun skipCurrentNovel() {
        val current = _currentMatchingNovel.value
        if (current != null) {
            val results = _migrationResults.value.toMutableList()
            results.add(MigrationResult(current.id, false, null, "Skipped by user"))
            _migrationResults.value = results
        }
        
        _currentMatchingNovel.value = null
        _matches.value = emptyList()
    }
    
    /**
     * Cancel migration
     */
    fun cancelMigration() {
        isMigrating = false
        _currentMatchingNovel.value = null
        _matches.value = emptyList()
        _migrationProgress.value = emptyMap()
    }
    
    /**
     * Reset migration state
     */
    fun reset() {
        sourceId = null
        targetSourceId = null
        _novels.value = emptyList()
        _selectedNovels.value = emptySet()
        _targetSources.value = emptyList()
        _migrationProgress.value = emptyMap()
        _currentMatchingNovel.value = null
        _matches.value = emptyList()
        _migrationResults.value = emptyList()
        isLoadingNovels = false
        isSearchingMatches = false
        isMigrating = false
    }
}
