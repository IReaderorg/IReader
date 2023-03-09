package ireader.presentation.ui.home.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryWithCount
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel

@Composable
fun EditCategoriesDialog(
        vm: LibraryViewModel,
        modifier: Modifier = Modifier,
        onConfirm: () -> Unit,
        dismissDialog: () -> Unit,
        onAddToInsertQueue: (Category) -> Unit,
        onRemoteInInsertQueue: (Category) -> Unit,
        onRemoteInDeleteQueue: (Category) -> Unit,
        onAddDeleteQueue: (Category) -> Unit,
        categories: List<CategoryWithCount>
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    if (vm.showDialog) {
        AlertDialog(
            modifier = modifier.heightIn(max = 350.dp, min = 200.dp),
            onDismissRequest = { vm.showDialog = false },
            title = { Text(localizeHelper.localize(MR.strings.edit_category)) },
            text = {
                LazyColumn {
                    items(items = categories) { category ->
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
                    MidSizeTextComposable(text = localizeHelper.localize(MR.strings.add))
                }
            }
        )
    }
}
