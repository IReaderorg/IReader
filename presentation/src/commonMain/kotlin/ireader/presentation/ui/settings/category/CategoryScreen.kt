package ireader.presentation.ui.settings.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.IAlertDialog
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.reorderable.*
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.components.SettingsConfirmationDialog
import ireader.presentation.ui.settings.components.SettingsTextInputDialog
import ireader.presentation.ui.settings.components.SettingsSwitchItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(
    vm: CategoryScreenViewModel,
) {
    val scope = rememberCoroutineScope()
    val data = vm.categories
    val snackbarHostState = remember { SnackbarHostState() }
    
    var categoryToDelete by remember { mutableStateOf<CategoryWithCount?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var categoryToRename by remember { mutableStateOf<CategoryWithCount?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }

    val state: ReorderableLazyListState = rememberReorderLazyListState(
        onMove = { from, to ->
            scope.launchIO {
                vm.reorderCategory.await(data[from.index].id, newPosition = to.index)
            }
        },
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Show Empty Categories Toggle using unified component
            SettingsSwitchItem(
                title = "Show Empty Categories",
                description = "Display categories with no books",
                checked = vm.showEmptyCategories.value,
                onCheckedChange = { vm.showEmptyCategories.value = it },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            
            CategoryContent(
                state = state,
                data = data,
                onDelete = { category ->
                    categoryToDelete = category
                    showDeleteConfirmation = true
                },
                onRename = { category ->
                    categoryToRename = category
                    showRenameDialog = true
                }
            )
        }

        CategoryFloatingActionButton(vm)
        
        // Snackbar host for undo functionality
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }

    // Create category dialog using unified component
    if (vm.showDialog) {
        SettingsTextInputDialog(
            title = localize(MR.strings.edit_category),
            label = localize(MR.strings.category_hint),
            placeholder = "e.g., Fantasy, Romance, Sci-Fi",
            confirmText = localize(MR.strings.confirm),
            validator = { text ->
                when {
                    text.isEmpty() -> "Category name cannot be empty"
                    text.length < 2 -> "Category name must be at least 2 characters"
                    data.any { it.name.equals(text, ignoreCase = true) } -> 
                        "A category with this name already exists"
                    else -> null
                }
            },
            onConfirm = { text ->
                vm.scope.launch {
                    vm.createCategoryWithName.await(text)
                }
                vm.showDialog = false
            },
            onDismiss = {
                vm.showDialog = false
            }
        )
    }
    
    // Rename dialog using unified component
    if (showRenameDialog && categoryToRename != null) {
        SettingsTextInputDialog(
            title = "Rename Category",
            initialValue = categoryToRename!!.name,
            label = "Category Name",
            placeholder = "Enter new category name",
            confirmText = "Rename",
            icon = Icons.Default.Edit,
            validator = { newName ->
                when {
                    newName.isEmpty() -> "Category name cannot be empty"
                    newName.length < 2 -> "Category name must be at least 2 characters"
                    newName == categoryToRename!!.name -> "Please enter a different name"
                    data.any { it.name.equals(newName, ignoreCase = true) } -> 
                        "A category with this name already exists"
                    else -> null
                }
            },
            onConfirm = { newName ->
                vm.scope.launch {
                    vm.renameCategory(categoryToRename!!.id, newName)
                }
                showRenameDialog = false
                categoryToRename = null
            },
            onDismiss = {
                showRenameDialog = false
                categoryToRename = null
            }
        )
    }
    
    // Delete confirmation dialog using unified component
    if (showDeleteConfirmation && categoryToDelete != null) {
        val category = categoryToDelete!!
        val message = buildString {
            append("Are you sure you want to delete \"${category.name}\"?")
            if (category.bookCount > 0) {
                append("\n\nThis category contains ${category.bookCount} ")
                append(if (category.bookCount == 1) "book" else "books")
                append(". ${if (category.bookCount == 1) "It" else "They"} will be moved to the default category.")
            }
            append("\n\nYou can undo this action immediately after deletion.")
        }
        
        SettingsConfirmationDialog(
            title = "Delete Category?",
            message = message,
            confirmText = "Delete",
            icon = Icons.Default.DeleteForever,
            isDestructive = true,
            onConfirm = {
                val deletedCategory = categoryToDelete!!
                vm.scope.launch {
                    vm.categoriesUseCase.deleteCategory(deletedCategory.category)
                    
                    // Show undo snackbar
                    val result = snackbarHostState.showSnackbar(
                        message = "Deleted \"${deletedCategory.name}\"",
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Short
                    )
                    
                    if (result == SnackbarResult.ActionPerformed) {
                        // Undo the deletion by recreating the category
                        vm.createCategoryWithName.await(deletedCategory.name)
                    }
                }
                showDeleteConfirmation = false
                categoryToDelete = null
            },
            onDismiss = {
                showDeleteConfirmation = false
                categoryToDelete = null
            }
        )
    }
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
                    text = localize(MR.strings.add),
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
    onDelete: (CategoryWithCount) -> Unit,
    onRename: (CategoryWithCount) -> Unit
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
            EnhancedCategoryItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggedItem(state.offsetByKey(item.id))
                    .detectReorderAfterLongPress(state),
                category = item,
                onDelete = { onDelete(item) },
                onRename = { onRename(item) }
            )
        }
    }
}

@Composable
private fun EnhancedCategoryItem(
    modifier: Modifier = Modifier,
    category: CategoryWithCount,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced drag handle with better visibility
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Category name and count
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                // Category count badge
                if (category.bookCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "${category.bookCount} ${if (category.bookCount == 1) "book" else "books"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Rename button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    AppIconButton(
                        imageVector = Icons.Default.Edit,
                        onClick = onRename,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Delete button with enhanced styling
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ) {
                    AppIconButton(
                        imageVector = Icons.Default.DeleteForever,
                        onClick = onDelete,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}