package ireader.presentation.ui.settings.category

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.usecases.category.CategoriesUseCases
import ireader.domain.usecases.category.CreateCategoryWithName
import ireader.domain.usecases.category.ReorderCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach



@OptIn(ExperimentalCoroutinesApi::class)
class CategoryScreenViewModel(
    val categoriesUseCase: CategoriesUseCases,
    val reorderCategory: ReorderCategory,
    val createCategoryWithName: CreateCategoryWithName,
    private val libraryPreferences: ireader.domain.preferences.prefs.LibraryPreferences,
    private val categoryRepository: ireader.domain.data.repository.CategoryRepository,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    var categories: SnapshotStateList<CategoryWithCount> = mutableStateListOf()
    var showDialog by mutableStateOf(false)
    
    val showEmptyCategories = libraryPreferences.showEmptyCategories().asState()
    
    init {
        libraryPreferences.showEmptyCategories().stateIn(scope)
            .flatMapLatest { showEmpty ->
                categoriesUseCase.subscribe(false, showEmpty)
            }
            .onEach { list ->
                categories.clear()
                categories.addAll(list)
            }.launchIn(scope)
    }
    
    suspend fun renameCategory(categoryId: Long, newName: String) {
        categoryRepository.updatePartial(
            ireader.domain.models.entities.CategoryUpdate(
                id = categoryId,
                name = newName
            )
        )
    }
    
    //  val categories by categoriesUseCase.subscribe(false).asState(emptyList())
}
