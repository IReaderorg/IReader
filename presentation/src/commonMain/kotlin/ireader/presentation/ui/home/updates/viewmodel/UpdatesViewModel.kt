package ireader.presentation.ui.home.updates.viewmodel

import androidx.compose.runtime.Stable
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.usecases.updates.UpdateUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Updates screen following Mihon's StateScreenModel pattern.
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
    
    init {
        // Subscribe to updates
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            updateUseCases.subscribeUpdates(after.value).collect { updatesMap ->
                _state.update { current ->
                    current.copy(
                        updates = updatesMap.mapValues { (_, list) -> list.toImmutableList() }.toImmutableMap(),
                        isLoading = false
                    )
                }
            }
        }
        
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
            
        scope.launch(Dispatchers.IO) {
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
            _state.update { it.copy(isRefreshing = false) }
        }
    }
    
    fun saveScrollPosition(index: Int, offset: Int) {
        _state.update { it.copy(savedScrollIndex = index, savedScrollOffset = offset) }
    }
}
