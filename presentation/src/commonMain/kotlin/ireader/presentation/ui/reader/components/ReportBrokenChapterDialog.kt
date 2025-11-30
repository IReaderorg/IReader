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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var selectedCategory by remember { mutableStateOf<IssueCategory?>(null) }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(localizeHelper.localize(Res.string.report_broken_chapter_1))
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
                    text = localizeHelper.localize(Res.string.select_issue_type),
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
                    label = { Text(localizeHelper.localize(Res.string.additional_details_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text(localizeHelper.localize(Res.string.describe_the_issue)) }
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
                Text(localizeHelper.localize(Res.string.report))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
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
