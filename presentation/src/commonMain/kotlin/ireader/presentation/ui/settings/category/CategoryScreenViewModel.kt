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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach



class CategoryScreenViewModel(
    val categoriesUseCase: CategoriesUseCases,
    val reorderCategory: ReorderCategory,
    val createCategoryWithName: CreateCategoryWithName,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    var categories: SnapshotStateList<CategoryWithCount> = mutableStateListOf()
    var showDialog by mutableStateOf(false)
    init {
        categoriesUseCase.subscribe(false).onEach { list ->
            categories.clear()
            categories.addAll(list)
        }.launchIn(scope)
    }
    //  val categories by categoriesUseCase.subscribe(false).asState(emptyList())
}
