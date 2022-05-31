package org.ireader.settings.setting.category

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.ireader.common_models.entities.CategoryWithCount
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.category.CategoriesUseCases
import org.ireader.domain.use_cases.category.CreateCategoryWithName
import org.ireader.domain.use_cases.category.ReorderCategory
import javax.inject.Inject

@HiltViewModel
class CategoryScreenViewModel @Inject constructor(
    val categoriesUseCase: CategoriesUseCases,
    val reorderCategory: ReorderCategory,
    val createCategoryWithName: CreateCategoryWithName,
) : BaseViewModel() {
    var categories: SnapshotStateList<CategoryWithCount> = mutableStateListOf()
    var showDialog by mutableStateOf(false)
    init {
        categoriesUseCase.subscribe(false).onEach { list ->
            categories.clear()
            categories.addAll(list)

        }.launchIn(viewModelScope)
    }
    //  val categories by categoriesUseCase.subscribe(false).asState(emptyList())


}


