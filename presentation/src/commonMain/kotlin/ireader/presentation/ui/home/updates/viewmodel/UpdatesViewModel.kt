package ireader.presentation.ui.home.updates.viewmodel

import androidx.compose.runtime.Stable
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.usecases.updates.UpdateUseCases
import ireader.domain.utils.extensions.ioDispatcher
import ireader.domain.utils.extensions.toLocalDate
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Updates screen following Mihon's StateScreenModel pattern.
 * Uses true database pagination for optimal performance with large update lists.
 */
@Stable
class UpdatesViewModel(
    val updateUseCases: UpdateUseCases,
    private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val serviceUseCases: ServiceUseCases,
    private val uiPreferences: UiPreferences,
    private val categoryUseCases: ireader.domain.usecases.category.CategoriesUseCases,
    private val downloadService: ireader.domain.services.common.DownloadService
) : BaseViewModel() {
    
    // Mihon-style immutable state
    private val _state = MutableStateFlow(UpdatesScreenState())
    val state: StateFlow<UpdatesScreenState> = _state.asStateFlow()
    
    // Convenience accessors
    val isLoading get() = _state.value.isLoading
    val isRefreshing get() = _state.value.isRefreshing
    val isEmpty get() = _state.value.isEmpty
    val updates get() = _state.value.updates
    val hasSelection get() = _state.value.hasSelection
    val selection get() = _state.value.selectedChapterIds
    val selectedCategoryId get() = _state.value.selectedCategoryId
    val categories get() = _state.value.categories
    val updateHistory get() = _state.value.updateHistory
    val updateProgress get() = _state.value.updateProgress
    
    val after = uiPreferences.showUpdatesAfter().asState()
    val relativeFormat = uiPreferences.relativeTime().asState()
    
    // Job for loading operations
    private var loadJob: Job? = null
    
    init {
        // Load initial page from database
        loadInitialPage()
        
        // Subscribe to categories
        scope.launch {
            categoryUseCases.subscribe(
                withAllCategory = true,
                showEmptyCategories = true,
                scope = scope
            ).collect { categoriesWithCount ->
                val categoryList = categoriesWithCount.map { it.category }
                _state.update { it.copy(categories = categoryList.toImmutableList()) }
            }
        }
    }
    
    /**
     * Load initial page of updates from database.
     */
    private fun loadInitialPage() {
        loadJob?.cancel()
        loadJob = scope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val (grouped, totalCount) = updateUseCases.findUpdatesPaginated(
                    after = after.value,
                    limit = UpdatesPaginationState.INITIAL_PAGE_SIZE,
                    offset = 0
                )
                
                _state.update { current ->
                    current.copy(
                        updates = grouped.mapValues { (_, list) -> list.toImmutableList() }.toImmutableMap(),
                        isLoading = false,
                        paginationState = UpdatesPaginationState(
                            loadedCount = grouped.values.sumOf { it.size },
                            isLoadingMore = false,
                            hasMoreItems = grouped.values.sumOf { it.size } < totalCount,
                            totalItems = totalCount
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun selectCategory(categoryId: Long?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun toggleSelection(chapterId: Long) {
        _state.update { current ->
            val newSelection = if (chapterId in current.selectedChapterIds) {
                current.selectedChapterIds - chapterId
            } else {
                current.selectedChapterIds + chapterId
            }
            current.copy(selectedChapterIds = newSelection.toImmutableSet())
        }
    }
    
    fun addUpdate(update: UpdatesWithRelations) {
        toggleSelection(update.chapterId)
    }
    
    fun clearSelection() {
        _state.update { it.copy(selectedChapterIds = persistentSetOf()) }
    }
    
    fun selectAll() {
        val allChapterIds = _state.value.updates.values.flatMap { it }.map { it.chapterId }
        _state.update { it.copy(selectedChapterIds = allChapterIds.toImmutableSet()) }
    }

    fun updateChapters(onChapter: Chapter.() -> Chapter) {
        val selectedIds = _state.value.selectedChapterIds
        val chapterIds = _state.value.updates.values.flatten()
            .filter { it.chapterId in selectedIds }
            .map { it.chapterId }
            
        scope.launch(ioDispatcher) {
            val chapters = chapterIds.mapNotNull {
                getChapterUseCase.findChapterById(it)
            }.map(onChapter)
            insertUseCases.insertChapters(chapters)
        }
    }
    
    fun markAsRead() {
        updateChapters { copy(read = true) }
        clearSelection()
    }
    
    fun markAsUnread() {
        updateChapters { copy(read = false) }
        clearSelection()
    }

    fun downloadChapters() {
        scope.launch {
            val chapterIds = _state.value.updates.values.flatMap { it }
                .filter { it.chapterId in _state.value.selectedChapterIds }
                .map { it.chapterId }
                
            when (val result = downloadService.queueChapters(chapterIds)) {
                is ireader.domain.services.common.ServiceResult.Success -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("${chapterIds.size} chapters queued"))
                    clearSelection()
                }
                is ireader.domain.services.common.ServiceResult.Error -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Download failed: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }
    
    fun refreshUpdate() {
        _state.update { it.copy(isRefreshing = true) }
        serviceUseCases.startLibraryUpdateServicesUseCase.start()
        
        scope.launch {
            kotlinx.coroutines.delay(2000)
            // Reload from database after refresh
            loadInitialPage()
            _state.update { it.copy(isRefreshing = false) }
        }
    }
    
    fun saveScrollPosition(index: Int, offset: Int) {
        _state.update { it.copy(savedScrollIndex = index, savedScrollOffset = offset) }
    }
    
    // ==================== Pagination ====================
    
    /**
     * Get pagination state.
     */
    fun getPaginationState(): UpdatesPaginationState {
        return _state.value.paginationState
    }
    
    /**
     * Load more updates from database when user scrolls near the end.
     */
    fun loadMoreUpdates() {
        val currentPagination = getPaginationState()
        
        // Don't load if already loading or no more items
        if (currentPagination.isLoadingMore || !currentPagination.hasMoreItems) return
        
        // Mark as loading
        _state.update { current ->
            current.copy(paginationState = currentPagination.copy(isLoadingMore = true))
        }
        
        scope.launch {
            try {
                val currentLoadedCount = currentPagination.loadedCount
                
                // Load next page from database
                val (newGrouped, totalCount) = updateUseCases.findUpdatesPaginated(
                    after = after.value,
                    limit = UpdatesPaginationState.PAGE_SIZE,
                    offset = currentLoadedCount
                )
                
                // Merge with existing data
                val existingUpdates = _state.value.updates.toMutableMap()
                newGrouped.forEach { (key, newList) ->
                    val existing = existingUpdates[key]?.toMutableList() ?: mutableListOf()
                    existing.addAll(newList)
                    existingUpdates[key] = existing.toImmutableList()
                }
                
                val newLoadedCount = existingUpdates.values.sumOf { it.size }
                
                _state.update { current ->
                    current.copy(
                        updates = existingUpdates.mapValues { (_, list) -> list }.toImmutableMap(),
                        paginationState = UpdatesPaginationState(
                            loadedCount = newLoadedCount,
                            isLoadingMore = false,
                            hasMoreItems = newLoadedCount < totalCount,
                            totalItems = totalCount
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update { current ->
                    current.copy(
                        paginationState = current.paginationState.copy(isLoadingMore = false)
                    )
                }
            }
        }
    }
    
    /**
     * Check if we should load more items based on scroll position.
     */
    fun checkAndLoadMore(lastVisibleIndex: Int, totalVisibleItems: Int) {
        val paginationState = getPaginationState()
        val threshold = 10 // Load more when within 10 items of the end
        
        if (lastVisibleIndex >= paginationState.loadedCount - threshold && 
            paginationState.canLoadMore) {
            loadMoreUpdates()
        }
    }
}
