package ireader.presentation.ui.reader.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ireader.domain.models.fonts.CustomFont
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Font picker composable that displays system and custom fonts
 */
@Composable
fun FontPicker(
    selectedFontId: String,
    customFonts: List<CustomFont>,
    systemFonts: List<CustomFont>,
    onFontSelected: (String) -> Unit,
    onImportFont: () -> Unit,
    onDeleteFont: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = LocalLocalizeHelper.current
    
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Import Font Button
        item {
            Button(
                onClick = onImportFont,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Font")
            }
        }
        
        // System Fonts Section
        if (systemFonts.isNotEmpty()) {
            item {
                Text(
                    text = "System Fonts",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            items(systemFonts) { font ->
                FontItem(
                    font = font,
                    isSelected = selectedFontId == font.id,
                    onClick = { onFontSelected(font.id) },
                    onDelete = null // System fonts cannot be deleted
                )
            }
        }
        
        // Custom Fonts Section
        if (customFonts.isNotEmpty()) {
            item {
                Text(
                    text = "Custom Fonts",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            items(customFonts) { font ->
                FontItem(
                    font = font,
                    isSelected = selectedFontId == font.id,
                    onClick = { onFontSelected(font.id) },
                    onDelete = { onDeleteFont(font.id) }
                )
            }
        }
    }
}

/**
 * Individual font item in the list
 */
@Composable
private fun FontItem(
    font: CustomFont,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = font.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                // Font preview
                Text(
                    text = "The quick brown fox jumps over the lazy dog",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete font",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog for importing a font
 */
@Composable
fun ImportFontDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = LocalLocalizeHelper.current ?: return
    var fontName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Import Font")
        },
        text = {
            Column {
                Text("Enter font name")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fontName,
                    onValueChange = { fontName = it },
                    label = { Text("Font Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(fontName) },
                enabled = fontName.isNotBlank()
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(MR.strings.cancel))
            }
        },
        modifier = modifier
    )
}
