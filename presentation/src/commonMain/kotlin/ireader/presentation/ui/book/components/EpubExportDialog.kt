package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * Data class representing EPUB export options
 */
data class ExportOptions(
    val includeCover: Boolean,
    val selectedChapters: List<Long>,
    val formatOptions: FormatOptions
)

/**
 * Data class representing formatting options for EPUB export
 */
data class FormatOptions(
    val paragraphSpacing: Float,
    val chapterHeadingSize: Float,
    val typography: Typography
)

/**
 * Typography options for EPUB export
 */
enum class Typography {
    DEFAULT,
    SERIF,
    SANS_SERIF
}

/**
 * Dialog for configuring EPUB export options.
 * Allows users to select chapters, formatting, and other export settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubExportDialog(
    book: Book,
    chapters: List<Chapter>,
    onExport: (ExportOptions) -> Unit,
    onDismiss: () -> Unit
) {
    var includeCover by remember { mutableStateOf(true) }
    var selectAllChapters by remember { mutableStateOf(true) }
    var selectedChapterIds by remember { mutableStateOf(chapters.map { it.id }.toSet()) }
    var paragraphSpacing by remember { mutableStateOf(1.0f) }
    var chapterHeadingSize by remember { mutableStateOf(2.0f) }
    var typography by remember { mutableStateOf(Typography.DEFAULT) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localize(Res.string.export_as_epub),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = localize(Res.string.close)
                        )
                    }
                }
                
                Divider()
                
                // Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Include cover option
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localize(Res.string.include_cover_image),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = includeCover,
                                onCheckedChange = { includeCover = it }
                            )
                        }
                    }
                    
                    // Chapter selection
                    item {
                        Text(
                            text = localize(Res.string.chapter_selection),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectAllChapters,
                                onClick = {
                                    selectAllChapters = true
                                    selectedChapterIds = chapters.map { it.id }.toSet()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localize(Res.string.all_chapters),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !selectAllChapters,
                                onClick = { selectAllChapters = false }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localize(Res.string.select_chapters),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    // Chapter list (only shown when "Select Chapters" is chosen)
                    if (!selectAllChapters) {
                        items(chapters) { chapter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = chapter.id in selectedChapterIds,
                                    onCheckedChange = { checked ->
                                        selectedChapterIds = if (checked) {
                                            selectedChapterIds + chapter.id
                                        } else {
                                            selectedChapterIds - chapter.id
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = chapter.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Formatting section
                    item {
                        Text(
                            text = localize(Res.string.formatting),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Paragraph spacing
                    item {
                        Column {
                            Text(
                                text = "${localize(Res.string.paragraph_spacing)}: ${String.format("%.1f", paragraphSpacing)}em",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Slider(
                                value = paragraphSpacing,
                                onValueChange = { paragraphSpacing = it },
                                valueRange = 0.5f..2.0f,
                                steps = 14
                            )
                        }
                    }
                    
                    // Chapter heading size
                    item {
                        Column {
                            Text(
                                text = "${localize(Res.string.chapter_heading_size)}: ${String.format("%.1f", chapterHeadingSize)}em",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Slider(
                                value = chapterHeadingSize,
                                onValueChange = { chapterHeadingSize = it },
                                valueRange = 1.5f..3.0f,
                                steps = 14
                            )
                        }
                    }
                    
                    // Typography dropdown
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        
                        Column {
                            Text(
                                text = localize(Res.string.typography),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = typography.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    Typography.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                                            onClick = {
                                                typography = option
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Divider()
                
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(localize(Res.string.cancel))
                    }
                    Button(
                        onClick = {
                            val options = ExportOptions(
                                includeCover = includeCover,
                                selectedChapters = selectedChapterIds.toList(),
                                formatOptions = FormatOptions(
                                    paragraphSpacing = paragraphSpacing,
                                    chapterHeadingSize = chapterHeadingSize,
                                    typography = typography
                                )
                            )
                            onExport(options)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedChapterIds.isNotEmpty()
                    ) {
                        Text(localize(Res.string.export))
                    }
                }
            }
        }
    }
}
