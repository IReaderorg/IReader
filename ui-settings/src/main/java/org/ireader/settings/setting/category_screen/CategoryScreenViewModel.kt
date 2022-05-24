package org.ireader.settings.setting.category_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.category.GetCategories
import javax.inject.Inject

@HiltViewModel
class CategoryScreenViewModel @Inject constructor(
    val getCategories: GetCategories
) : BaseViewModel() {
    val category by getCategories.subscribe(true).asState(emptyList())
    var showDialog by mutableStateOf(false)
}


