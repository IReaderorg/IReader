package org.ireader.app.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_models.entities.Category
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.ui_library.R

@Composable
fun EditCategoriesDialog(
    vm: LibraryViewModel,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    dismissDialog:() -> Unit,
    onAddToInsertQueue:(Category)-> Unit,
    onRemoteInInsertQueue:(Category)-> Unit,
    onRemoteInDeleteQueue:(Category)-> Unit,
    onAddDeleteQueue:(Category)-> Unit,
) {

    if (vm.showDialog) {
        AlertDialog(
            modifier = modifier.heightIn(max = 350.dp, min = 200.dp),
            onDismissRequest = { vm.showDialog = false },
            title = { Text(stringResource(id = R.string.edit_category)) },
            text = {
                LazyColumn {
                    items(items = vm.categories) { category ->
                        Row(
                            modifier = Modifier
                                .requiredHeight(48.dp)
                                .fillMaxWidth()
                                .clickable(onClick = dismissDialog),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val defaultValue by remember {
                                derivedStateOf { vm.getDefaultValue(category.category) }
                            }
                            var state: ToggleableState by remember {
                                mutableStateOf(defaultValue)
                            }
                            TriStateCheckbox(state = state, onClick = {
                                state = when (defaultValue) {
                                    state -> {
                                        when (state) {
                                            ToggleableState.On -> {
                                              onAddDeleteQueue(category.category)
                                                ToggleableState.Indeterminate
                                            }
                                            ToggleableState.Indeterminate -> {
                                                ToggleableState.On
                                            }
                                            ToggleableState.Off -> {
                                                onAddToInsertQueue(category.category)
                                                ToggleableState.On
                                            }
                                        }
                                    }
                                    else -> {
                                        when (state) {
                                            ToggleableState.On -> {
                                                onRemoteInInsertQueue(category.category)
                                            }
                                            ToggleableState.Indeterminate -> {
                                                onRemoteInDeleteQueue(category.category)

                                            }
                                            else -> {}
                                        }
                                        defaultValue
                                    }
                                }
                            })
                            Text(
                                text = category.name,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onConfirm) {
                    MidSizeTextComposable(text = stringResource(id = R.string.add))
                }
            }
        )
    }
}