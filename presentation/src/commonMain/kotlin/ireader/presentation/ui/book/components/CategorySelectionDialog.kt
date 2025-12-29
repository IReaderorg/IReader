package ireader.presentation.ui.book.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryWithCount
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * Dialog for selecting categories when adding a book to the library.
 * Allows users to choose which categories to assign the book to.
 */
@Composable
fun CategorySelectionDialog(
    categories: List<CategoryWithCount>,
    preSelectedCategoryIds: Set<Long> = emptySet(),
    onConfirm: (selectedCategoryIds: Set<Long>) -> Unit,
    onDismiss: () -> Unit,
    onSkip: () -> Unit
) {
    var selectedIds by remember { mutableStateOf(preSelectedCategoryIds) }
    
    // Filter out system categories (All, Uncategorized)
    val userCategories = remember(categories) {
        categories.filter { !it.isSystemCategory }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null
            )
        },
        title = {
            Text(text = localize(Res.string.select_categories))
        },
        text = {
            if (userCategories.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = localize(Res.string.no_categories_found),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The book will be added without a category.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(userCategories, key = { it.id }) { category ->
                        CategorySelectionItem(
                            category = category,
                            isSelected = category.id in selectedIds,
                            onClick = {
                                selectedIds = if (category.id in selectedIds) {
                                    selectedIds - category.id
                                } else {
                                    selectedIds + category.id
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedIds) }
            ) {
                Text(
                    if (selectedIds.isEmpty() && userCategories.isNotEmpty()) 
                        localize(Res.string.add_without_category)
                    else 
                        localize(Res.string.confirm)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localize(Res.string.cancel))
            }
        }
    )
}

@Composable
private fun CategorySelectionItem(
    category: CategoryWithCount,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        tonalElevation = if (isSelected) 3.dp else 1.dp,
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${category.bookCount} books",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
