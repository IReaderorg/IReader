package org.ireader.settings.setting.category_screen

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Category
import org.ireader.common_resources.R
import org.ireader.components.components.component.PreferenceRow
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.MidSizeTextComposable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    vm: CategoryScreenViewModel,
) {

    androidx.compose.material3.Scaffold(
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
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
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            items(count = vm.category.size) { index ->
                PreferenceRow(
                    title = vm.category[index].name,
                    onClick = {},
                    action = {
                        AppIconButton(imageVector = Icons.Default.DeleteForever, onClick = {
//                            vm.viewModelScope.launch {
//                                vm.getCategories.repo.delete(vm.category[index].category)
//                            }
                        })
                    },
                )
            }
        }
    }
    ShowEditScreen(vm, onConfirm = {
        vm.viewModelScope.launch {
            vm.getCategories.insertCategory(
                Category(
                    name = it,
                )
            )
        }
    })
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
                        id = R.string.hint
                    )
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