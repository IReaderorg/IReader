package ireader.presentation.ui.home.updates.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.usecases.updates.UpdateUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UpdatesViewModel(
        private val updateStateImpl: UpdateStateImpl,
        val updateUseCases: UpdateUseCases,
        private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
        private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
        private val serviceUseCases: ServiceUseCases,
        private val uiPreferences: UiPreferences,
        private val categoryUseCases: ireader.domain.usecases.category.CategoriesUseCases
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), UpdateState by updateStateImpl {
    val after = uiPreferences.showUpdatesAfter().asState()
    val relativeFormat by uiPreferences.relativeTime().asState()
    
    var categories by mutableStateOf<List<ireader.domain.models.entities.Category>>(emptyList())
        private set
    
    init {
        scope.launch {
            updateUseCases.subscribeUpdates(after.value).collect {
                updates = it
            }
        }
        
        scope.launch {
            categoryUseCases.subscribe(true,
                showEmptyCategories = true,
                scope
            ).collect { categoriesWithCount ->
                categories = categoriesWithCount.map { it.category }
            }
        }
    }
    
    fun selectCategory(categoryId: Long?) {
        selectedCategoryId = categoryId
    }

    fun addUpdate(update: UpdatesWithRelations) {
        if (update.chapterId in selection) {
            selection.remove(update.chapterId)
        } else {
            selection.add(update.chapterId)
        }
    }

    /**
     * update chapter using updateitem
     */
    fun updateChapters(onChapter: Chapter.() -> Chapter) {
        val updateIds =
            this.updates.values.flatten().map { it.chapterId }.filter { it in selection }
        val chapterIds = this.updates.values.flatten().filter { it.chapterId in updateIds }
            .map { it.chapterId }
        scope.launch(Dispatchers.IO) {

            val chapters = chapterIds.mapNotNull {
                getChapterUseCase.findChapterById(it)
            }.map(onChapter)
            insertUseCases.insertChapters(chapters)
        }
    }

    fun downloadChapters() {
        scope.launch {
            val chapterIds =
                updates.values.flatMap { it }.filter { it.chapterId in selection }.map { it.chapterId }
            serviceUseCases.startDownloadServicesUseCase.start(
                chapterIds = chapterIds.toLongArray()
            )
        }
    }
    fun refreshUpdate() {
        isRefreshing = true
        serviceUseCases.startLibraryUpdateServicesUseCase.start()
        // Reset refreshing state after a delay (the service will handle the actual update)
        scope.launch {
            kotlinx.coroutines.delay(2000)
            isRefreshing = false
        }
    }
}
