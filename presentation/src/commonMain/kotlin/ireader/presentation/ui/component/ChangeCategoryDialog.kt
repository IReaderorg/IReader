package ireader.presentation.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CheckboxState
import ireader.domain.models.entities.asToggleableState
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * Dialog for selecting categories when adding/editing book categories.
 * Supports both single-select (checkbox) and multi-select (tri-state) modes.
 * 
 * Following Mihon's ChangeCategoryDialog pattern.
 * 
 * @param initialSelection Initial checkbox states for each category
 * @param onDismissRequest Called when dialog should be dismissed
 * @param onEditCategories Called when user wants to edit/create categories
 * @param onConfirm Called with (includedIds, excludedIds) when user confirms selection
 */
@Composable
fun ChangeCategoryDialog(
    initialSelection: List<CheckboxState<Category>>,
    onDismissRequest: () -> Unit,
    onEditCategories: () -> Unit,
    onConfirm: (includedIds: List<Long>, excludedIds: List<Long>) -> Unit,
) {
    // Handle empty categories case
    if (initialSelection.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                        onEditCategories()
                    }
                ) {
                    Text(text = localize(Res.string.edit_category))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = localize(Res.string.cancel))
                }
            },
            title = {
                Text(text = localize(Res.string.set_categories))
            },
            text = {
                Text(text = localize(Res.string.no_categories_create_one))
            }
        )
        return
    }
    
    // Mutable selection state
    var selection by remember { mutableStateOf(initialSelection) }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Edit button on the left
                TextButton(
                    onClick = {
                        onDismissRequest()
                        onEditCategories()
                    }
                ) {
                    Text(text = localize(Res.string.edit))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Cancel and OK buttons on the right
                TextButton(onClick = onDismissRequest) {
                    Text(text = localize(Res.string.cancel))
                }
                TextButton(
                    onClick = {
                        onDismissRequest()
                        val included = selection
                            .filter { 
                                it is CheckboxState.State.Checked || 
                                it is CheckboxState.TriState.Include 
                            }
                            .map { it.value.id }
                        val excluded = selection
                            .filter { 
                                it is CheckboxState.State.None || 
                                it is CheckboxState.TriState.Exclude 
                            }
                            .map { it.value.id }
                        onConfirm(included, excluded)
                    }
                ) {
                    Text(text = localize(Res.string.ok))
                }
            }
        },
        title = {
            Text(text = localize(Res.string.set_categories))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                selection.forEachIndexed { index, checkbox ->
                    CategoryCheckboxRow(
                        checkbox = checkbox,
                        onClick = {
                            val mutableList = selection.toMutableList()
                            mutableList[index] = checkbox.next()
                            selection = mutableList
                        }
                    )
                }
            }
        }
    )
}

/**
 * A single row in the category selection list with checkbox and category name.
 */
@Composable
private fun <T : Category> CategoryCheckboxRow(
    checkbox: CheckboxState<T>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (checkbox) {
            is CheckboxState.TriState -> {
                TriStateCheckbox(
                    state = checkbox.asToggleableState(),
                    onClick = onClick
                )
            }
            is CheckboxState.State -> {
                Checkbox(
                    checked = checkbox.isChecked,
                    onCheckedChange = { onClick() }
                )
            }
        }
        
        Text(
            text = checkbox.value.name.ifEmpty { 
                localize(Res.string.uncategorized) 
            },
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Simplified version of ChangeCategoryDialog for single book addition.
 * Uses simple checkboxes instead of tri-state.
 * 
 * @param categories List of available categories
 * @param preselectedIds IDs of categories that should be pre-selected
 * @param onDismissRequest Called when dialog should be dismissed
 * @param onEditCategories Called when user wants to edit/create categories
 * @param onConfirm Called with selected category IDs when user confirms
 */
@Composable
fun SimpleCategoryDialog(
    categories: List<Category>,
    preselectedIds: Set<Long> = emptySet(),
    onDismissRequest: () -> Unit,
    onEditCategories: () -> Unit,
    onConfirm: (selectedIds: List<Long>) -> Unit,
) {
    // Convert to CheckboxState
    val initialSelection = remember(categories, preselectedIds) {
        categories.map { category ->
            if (category.id in preselectedIds) {
                CheckboxState.State.Checked(category)
            } else {
                CheckboxState.State.None(category)
            }
        }
    }
    
    ChangeCategoryDialog(
        initialSelection = initialSelection,
        onDismissRequest = onDismissRequest,
        onEditCategories = onEditCategories,
        onConfirm = { included, _ -> onConfirm(included) }
    )
}
