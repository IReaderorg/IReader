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

    val state: ReorderableLazyListState = rememberReorderLazyListState(
        onMove = { from, to ->
            scope.launchIO {
                vm.reorderCategory.await(data[from.index].id, newPosition = to.index)
            }
        },
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        CategoryContent(
            state = state,
            data = data,
            onDelete = { category ->
                categoryToDelete = category
                showDeleteConfirmation = true
            }
        )

        CategoryFloatingActionButton(vm)
        
        // Snackbar host for undo functionality
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }

    ShowEditScreen(vm, onConfirm = {
        vm.scope.launch {
            vm.createCategoryWithName.await(it)
        }
    })
    
    // Enhanced delete confirmation dialog
    if (showDeleteConfirmation && categoryToDelete != null) {
        DeleteConfirmationDialog(
            category = categoryToDelete!!,
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
            EnhancedCategoryItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggedItem(state.offsetByKey(item.id))
                    .detectReorderAfterLongPress(state),
                category = item,
                onDelete = { onDelete(item) }
            )
        }
    }
}

@Composable
private fun EnhancedCategoryItem(
    modifier: Modifier = Modifier,
    category: CategoryWithCount,
    onDelete: () -> Unit
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
            
            // Delete button with enhanced styling
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                modifier = Modifier.padding(start = 8.dp)
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

@Composable
private fun DeleteConfirmationDialog(
    category: CategoryWithCount,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    IAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "Delete Category?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Are you sure you want to delete \"${category.name}\"?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (category.bookCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "This category contains ${category.bookCount} ${if (category.bookCount == 1) "book" else "books"}. ${if (category.bookCount == 1) "It" else "They"} will be moved to the default category.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You can undo this action immediately after deletion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Delete",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
private fun ShowEditScreen(
    vm: CategoryScreenViewModel,
    onConfirm: (String) -> Unit
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    var query by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    if (vm.showDialog) {
        IAlertDialog(
            modifier = Modifier
                .heightIn(max = 400.dp, min = 250.dp)
                .widthIn(min = 280.dp),
            onDismissRequest = {
                query = ""
                errorMessage = null
                vm.showDialog = false
            },
            title = { 
                Text(
                    text = localizeHelper.localize(MR.strings.edit_category),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Enhanced input field with better styling
                    OutlinedTextField(
                        value = query,
                        onValueChange = { 
                            query = it
                            // Clear error when user starts typing
                            if (errorMessage != null && it.isNotBlank()) {
                                errorMessage = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { 
                            Text(
                                text = localizeHelper.localize(MR.strings.category_hint),
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        placeholder = {
                            Text(
                                text = "e.g., Fantasy, Romance, Sci-Fi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        isError = errorMessage != null,
                        supportingText = if (errorMessage != null) {
                            {
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = {
                                val trimmedQuery = query.trim()
                                when {
                                    trimmedQuery.isEmpty() -> {
                                        errorMessage = "Category name cannot be empty"
                                    }
                                    trimmedQuery.length < 2 -> {
                                        errorMessage = "Category name must be at least 2 characters"
                                    }
                                    vm.categories.any { it.name.equals(trimmedQuery, ignoreCase = true) } -> {
                                        errorMessage = "A category with this name already exists"
                                    }
                                    else -> {
                                        vm.showDialog = false
                                        onConfirm(trimmedQuery)
                                        query = ""
                                        errorMessage = null
                                    }
                                }
                            }
                        )
                    )
                    
                    // Helper text
                    if (errorMessage == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enter a unique name for your category",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedQuery = query.trim()
                        when {
                            trimmedQuery.isEmpty() -> {
                                errorMessage = "Category name cannot be empty"
                            }
                            trimmedQuery.length < 2 -> {
                                errorMessage = "Category name must be at least 2 characters"
                            }
                            vm.categories.any { it.name.equals(trimmedQuery, ignoreCase = true) } -> {
                                errorMessage = "A category with this name already exists"
                            }
                            else -> {
                                vm.showDialog = false
                                onConfirm(trimmedQuery)
                                query = ""
                                errorMessage = null
                            }
                        }
                    },
                    enabled = query.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = localizeHelper.localize(MR.strings.confirm),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        query = ""
                        errorMessage = null
                        vm.showDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}
