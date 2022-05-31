package org.ireader.settings.setting.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ireader.common_extensions.launchIO
import org.ireader.common_models.entities.CategoryWithCount
import org.ireader.common_resources.R
import org.ireader.components.components.component.PreferenceRow
import org.ireader.components.reorderable.ReorderableLazyListState
import org.ireader.components.reorderable.detectReorderAfterLongPress
import org.ireader.components.reorderable.draggedItem
import org.ireader.components.reorderable.rememberReorderLazyListState
import org.ireader.components.reorderable.reorderable
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.MidSizeTextComposable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(
    vm: CategoryScreenViewModel,
) {

    val scope = rememberCoroutineScope()
    val data = vm.categories

    val state: ReorderableLazyListState = rememberReorderLazyListState(
        onMove = { from, to ->
//            runCatching {
//                data.move(
//                    from.index,
//                    to.index
//                )
//            }
            scope.launchIO {
                vm.reorderCategory.await(data[from.index].id, newPosition = to.index)
                //vm.categoriesUseCase.insertCategory(data[from].category.copy(sort = to))
            }
        },
    )
    CategoryContent(
        state = state,
        data = data,
        onDelete = {
            vm.viewModelScope.launch {
                vm.categoriesUseCase.deleteCategory(it.category)
            }
        }
    )


    CategoryFloatingActionButton(vm)

    ShowEditScreen(vm, onConfirm = {
        vm.viewModelScope.launch {
            vm.createCategoryWithName.await(it)
        }
    })
}

@Composable
fun CategoryFloatingActionButton(
    vm: CategoryScreenViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {

        androidx.compose.material3.ExtendedFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            text = {
                MidSizeTextComposable(
                    text = stringResource(R.string.add),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            onClick = {
                vm.showDialog = true
            },
            icon = {
                Icon(Icons.Filled.Add, "", tint = MaterialTheme.colorScheme.onSecondary)
            },
            contentColor = MaterialTheme.colorScheme.onSecondary,
            containerColor = MaterialTheme.colorScheme.secondary,
            shape = RoundedCornerShape(32.dp)
        )
    }
}

@Composable
private fun CategoryContent(
    state: ReorderableLazyListState,
    data: MutableList<CategoryWithCount>,
    onDelete: (CategoryWithCount) -> Unit
) {
    LazyColumn(
        state = state.listState,
        modifier = Modifier.reorderable(state)
    ) {
        items(
            items = data,
            key = {
                it.id
            }
        ) { item ->
            PreferenceRow(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .draggedItem(state.offsetByKey(item.id))
                    .detectReorderAfterLongPress(state),
                title = item.name,
                icon = Icons.Default.DragHandle,
                clickable = false,
                action = {
                    AppIconButton(imageVector = Icons.Default.DeleteForever, onClick = {
                        onDelete(item)
                    })
                },
            )
        }
    }
}

@Composable
private fun ShowEditScreen(
    vm: CategoryScreenViewModel,
    onConfirm: (String) -> Unit
) {

    var query by remember {
        mutableStateOf("")
    }
    if (vm.showDialog) {
        AlertDialog(
            modifier = Modifier.heightIn(max = 350.dp, min = 200.dp),
            onDismissRequest = {
                query = ""
                vm.showDialog = false
            },
            title = { Text(stringResource(id = R.string.edit_category)) },
            text = {
                AppTextField(
                    query = query,
                    onValueChange = { query = it },
                    onConfirm = {},
                    hint = stringResource(
                        id = R.string.category_hint
                    ),
                    mode = 1,
                    keyboardAction =KeyboardOptions(imeAction = ImeAction.Done),
                )
            },
            confirmButton = {
                Button(onClick = {
                    vm.showDialog = false
                    onConfirm(query)
                    query = ""
                }) {
                    MidSizeTextComposable(text = stringResource(id = R.string.confirm))
                }
            }
        )
    }
}