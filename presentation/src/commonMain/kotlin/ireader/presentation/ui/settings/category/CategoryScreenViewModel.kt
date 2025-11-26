package ireader.presentation.ui.settings.category

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.usecases.category.CategoriesUseCases
import ireader.domain.usecases.category.CategoryUseCases
import ireader.domain.usecases.category.CreateCategoryWithName
import ireader.domain.usecases.category.ReorderCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch



@OptIn(ExperimentalCoroutinesApi::class)
class CategoryScreenViewModel(
    val categoriesUseCase: CategoriesUseCases,
    val reorderCategory: ReorderCategory,
    val createCategoryWithName: CreateCategoryWithName,
    private val libraryPreferences: ireader.domain.preferences.prefs.LibraryPreferences,
    // NEW: Clean architecture use cases
    private val categoryUseCases: CategoryUseCases,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    var categories: SnapshotStateList<CategoryWithCount> = mutableStateListOf()
    var showDialog by mutableStateOf(false)
    
    val showEmptyCategories = libraryPreferences.showEmptyCategories().asState()
    
    init {
        libraryPreferences.showEmptyCategories().stateIn(scope)
            .flatMapLatest { showEmpty ->
                categoriesUseCase.subscribe(false, showEmpty,scope)
            }
            .onEach { list ->
                categories.clear()
                categories.addAll(list)
            }.launchIn(scope)
    }
    
    /**
     * Rename a category using the new use case layer
     * Includes validation and error handling
     */
    suspend fun renameCategory(categoryId: Long, newName: String) {
        scope.launch {
            val result = categoryUseCases.updateCategory.rename(categoryId, newName)
            
            result.onSuccess {
                showSnackBar(ireader.i18n.UiText.DynamicString("Category renamed to '$newName'"))
            }
            
            result.onFailure { error ->
                when {
                    error.message?.contains("blank") == true ->
                        showSnackBar(ireader.i18n.UiText.DynamicString("Category name cannot be empty"))
                    error.message?.contains("already exists") == true ->
                        showSnackBar(ireader.i18n.UiText.DynamicString("Category '$newName' already exists"))
                    error.message?.contains("not found") == true ->
                        showSnackBar(ireader.i18n.UiText.DynamicString("Category not found"))
                    else ->
                        showSnackBar(ireader.i18n.UiText.DynamicString("Failed to rename: ${error.message}"))
                }
            }
        }
    }
    
    //  val categories by categoriesUseCase.subscribe(false).asState(emptyList())
}
