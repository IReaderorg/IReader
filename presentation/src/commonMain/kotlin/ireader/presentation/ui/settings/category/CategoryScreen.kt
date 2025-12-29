package ireader.presentation.ui.settings.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import ireader.domain.models.entities.CategoryAutoRule
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
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
    // Collect categories as state - this will automatically update when the flow emits
    val data by vm.categories.collectAsState()
    
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scope = rememberCoroutineScope()
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
                title = localizeHelper.localize(Res.string.show_empty_categories),
                description = "Display categories with no books",
                checked = vm.showEmptyCategories.value,
                onCheckedChange = { vm.showEmptyCategories.value = it },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            
            CategoryContent(
                state = state,
                data = data,
                vm = vm,
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
            title = localize(Res.string.edit_category),
            label = localize(Res.string.category_hint),
            placeholder = "e.g., Fantasy, Romance, Sci-Fi",
            confirmText = localize(Res.string.confirm),
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
                vm.createCategory(text)
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
            title = localizeHelper.localize(Res.string.rename_category),
            initialValue = categoryToRename!!.name,
            label = localizeHelper.localize(Res.string.category_name),
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
            title = localizeHelper.localize(Res.string.delete_category),
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
    
    // Auto-categorization rules dialog
    if (vm.showAutoRulesDialog && vm.selectedCategoryForRules != null) {
        AutoCategorizationRulesDialog(
            category = vm.selectedCategoryForRules!!,
            rules = vm.getRulesForCategory(vm.selectedCategoryForRules!!.id),
            onAddGenreRule = { genre ->
                vm.addGenreRule(vm.selectedCategoryForRules!!.id, genre)
            },
            onAddSourceRule = { sourceId, sourceName ->
                vm.addSourceRule(vm.selectedCategoryForRules!!.id, sourceId, sourceName)
            },
            onToggleRule = { ruleId, enabled -> vm.toggleRule(ruleId, enabled) },
            onDeleteRule = { ruleId -> vm.deleteRule(ruleId) },
            onDismiss = {
                vm.showAutoRulesDialog = false
                vm.selectedCategoryForRules = null
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
                    text = localize(Res.string.add),
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
    data: List<CategoryWithCount>,
    vm: CategoryScreenViewModel,
    onDelete: (CategoryWithCount) -> Unit,
    onRename: (CategoryWithCount) -> Unit
) {
    // Filter out system categories (All, Uncategorized) - they cannot be deleted or reordered
    val userCategories = remember(data) { data.filter { !it.isSystemCategory } }
    
    LazyColumn(
        state = state.listState,
        modifier = Modifier.reorderable(state)
    ) {
        items(
            items = userCategories,
            key = {
                it.id
            }
        ) { item ->
            val rules = vm.getRulesForCategory(item.id)
            EnhancedCategoryItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggedItem(state.offsetByKey(item.id))
                    .detectReorderAfterLongPress(state),
                category = item,
                rules = rules,
                onDelete = { onDelete(item) },
                onRename = { onRename(item) },
                onConfigureAutoRules = {
                    vm.selectedCategoryForRules = item
                    vm.showAutoRulesDialog = true
                },
                onToggleRule = { ruleId, enabled -> vm.toggleRule(ruleId, enabled) },
                onDeleteRule = { ruleId -> vm.deleteRule(ruleId) }
            )
        }
    }
}

@Composable
private fun EnhancedCategoryItem(
    modifier: Modifier = Modifier,
    category: CategoryWithCount,
    rules: List<CategoryAutoRule>,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onConfigureAutoRules: () -> Unit,
    onToggleRule: (Long, Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var expanded by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column {
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
                        contentDescription = localizeHelper.localize(Res.string.drag_to_reorder),
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
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category count badge
                        if (category.bookCount > 0) {
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
                        
                        // Auto-rules indicator
                        if (rules.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "${rules.size} ${if (rules.size == 1) "rule" else "rules"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Auto-rules button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ) {
                        AppIconButton(
                            imageVector = Icons.Default.AutoAwesome,
                            onClick = onConfigureAutoRules,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    
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
                    
                    // Expand/collapse button for rules
                    if (rules.isNotEmpty()) {
                        AppIconButton(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            onClick = { expanded = !expanded },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Expandable rules section
            AnimatedVisibility(
                visible = expanded && rules.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 56.dp, end = 12.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "Auto-categorization Rules",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    rules.forEach { rule ->
                        AutoRuleChip(
                            rule = rule,
                            onToggle = { onToggleRule(rule.id, !rule.isEnabled) },
                            onDelete = { onDeleteRule(rule.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoRuleChip(
    rule: CategoryAutoRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .clickable { onToggle() },
        shape = RoundedCornerShape(8.dp),
        color = if (rule.isEnabled) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when (rule.ruleType) {
                    CategoryAutoRule.RuleType.GENRE -> Icons.Default.Label
                    CategoryAutoRule.RuleType.SOURCE -> Icons.Default.Source
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (rule.isEnabled) 
                    MaterialTheme.colorScheme.onPrimaryContainer
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "${rule.ruleType.name}: ${rule.value}",
                style = MaterialTheme.typography.bodySmall,
                color = if (rule.isEnabled) 
                    MaterialTheme.colorScheme.onPrimaryContainer
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete rule",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoCategorizationRulesDialog(
    category: CategoryWithCount,
    rules: List<CategoryAutoRule>,
    onAddGenreRule: (String) -> Unit,
    onAddSourceRule: (Long, String?) -> Unit,
    onToggleRule: (Long, Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddRuleSection by remember { mutableStateOf(false) }
    var selectedRuleType by remember { mutableStateOf(CategoryAutoRule.RuleType.GENRE) }
    var ruleValue by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Auto-categorization: ${category.name}")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Novels matching these rules will be automatically added to this category.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Existing rules
                if (rules.isNotEmpty()) {
                    Text(
                        text = "Current Rules",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    rules.forEach { rule ->
                        AutoRuleItem(
                            rule = rule,
                            onToggle = { onToggleRule(rule.id, !rule.isEnabled) },
                            onDelete = { onDeleteRule(rule.id) }
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "No rules configured. Add a rule to automatically categorize novels.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Add new rule section
                AnimatedVisibility(visible = showAddRuleSection) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Add New Rule",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Rule type selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedRuleType == CategoryAutoRule.RuleType.GENRE,
                                onClick = { selectedRuleType = CategoryAutoRule.RuleType.GENRE },
                                label = { Text("Genre") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Label,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            FilterChip(
                                selected = selectedRuleType == CategoryAutoRule.RuleType.SOURCE,
                                onClick = { selectedRuleType = CategoryAutoRule.RuleType.SOURCE },
                                label = { Text("Source") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Source,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        
                        // Value input
                        OutlinedTextField(
                            value = ruleValue,
                            onValueChange = { ruleValue = it },
                            label = { 
                                Text(
                                    if (selectedRuleType == CategoryAutoRule.RuleType.GENRE) 
                                        "Genre name" 
                                    else 
                                        "Source ID or name"
                                ) 
                            },
                            placeholder = {
                                Text(
                                    if (selectedRuleType == CategoryAutoRule.RuleType.GENRE)
                                        "e.g., Fantasy, Romance, Action"
                                    else
                                        "e.g., 12345 or NovelUpdates"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { 
                                showAddRuleSection = false
                                ruleValue = ""
                            }) {
                                Text("Cancel")
                            }
                            
                            Button(
                                onClick = {
                                    if (ruleValue.isNotBlank()) {
                                        when (selectedRuleType) {
                                            CategoryAutoRule.RuleType.GENRE -> {
                                                onAddGenreRule(ruleValue.trim())
                                            }
                                            CategoryAutoRule.RuleType.SOURCE -> {
                                                val sourceId = ruleValue.trim().toLongOrNull()
                                                if (sourceId != null) {
                                                    onAddSourceRule(sourceId, null)
                                                } else {
                                                    // Treat as source name - use hash as ID
                                                    onAddSourceRule(ruleValue.hashCode().toLong(), ruleValue.trim())
                                                }
                                            }
                                        }
                                        ruleValue = ""
                                        showAddRuleSection = false
                                    }
                                },
                                enabled = ruleValue.isNotBlank()
                            ) {
                                Text("Add Rule")
                            }
                        }
                    }
                }
                
                // Add rule button
                if (!showAddRuleSection) {
                    OutlinedButton(
                        onClick = { showAddRuleSection = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Rule")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun AutoRuleItem(
    rule: CategoryAutoRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (rule.isEnabled)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (rule.ruleType) {
                    CategoryAutoRule.RuleType.GENRE -> Icons.Default.Label
                    CategoryAutoRule.RuleType.SOURCE -> Icons.Default.Source
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (rule.isEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (rule.isEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = rule.ruleType.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.height(24.dp)
            )
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete rule",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
