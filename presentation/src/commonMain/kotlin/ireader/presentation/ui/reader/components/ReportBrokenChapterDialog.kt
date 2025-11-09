package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.IssueCategory

/**
 * Dialog for reporting broken or problematic chapters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBrokenChapterDialog(
    chapterName: String,
    onDismiss: () -> Unit,
    onReport: (IssueCategory, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<IssueCategory?>(null) }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text("Report Broken Chapter")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Chapter: $chapterName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Select issue type:",
                    style = MaterialTheme.typography.titleSmall
                )
                
                // Issue category selection
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IssueCategory.values().forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = getCategoryDisplayName(category),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = getCategoryDescription(category),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Additional details (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("Describe the issue...") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedCategory?.let { category ->
                        onReport(category, description)
                        onDismiss()
                    }
                },
                enabled = selectedCategory != null
            ) {
                Text("Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Get display name for issue category
 */
private fun getCategoryDisplayName(category: IssueCategory): String {
    return when (category) {
        IssueCategory.MISSING_CONTENT -> "Missing Content"
        IssueCategory.INCORRECT_CONTENT -> "Incorrect Content"
        IssueCategory.FORMATTING_ISSUES -> "Formatting Issues"
        IssueCategory.TRANSLATION_ERRORS -> "Translation Errors"
        IssueCategory.DUPLICATE_CONTENT -> "Duplicate Content"
        IssueCategory.OTHER -> "Other"
    }
}

/**
 * Get description for issue category
 */
private fun getCategoryDescription(category: IssueCategory): String {
    return when (category) {
        IssueCategory.MISSING_CONTENT -> "Chapter is empty or incomplete"
        IssueCategory.INCORRECT_CONTENT -> "Wrong chapter or content doesn't match"
        IssueCategory.FORMATTING_ISSUES -> "Text formatting or layout problems"
        IssueCategory.TRANSLATION_ERRORS -> "Translation quality issues"
        IssueCategory.DUPLICATE_CONTENT -> "Repeated or duplicated text"
        IssueCategory.OTHER -> "Other issues not listed above"
    }
}
