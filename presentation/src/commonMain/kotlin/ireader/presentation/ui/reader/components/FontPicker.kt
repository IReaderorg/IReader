package ireader.presentation.ui.reader.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ireader.domain.models.fonts.CustomFont
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
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
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var fontToDelete by remember { mutableStateOf<CustomFont?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Define 8 most popular fonts
    val popularFontNames = listOf(
        "Roboto",
        "Open Sans",
        "Lato",
        "Montserrat",
        "Oswald",
        "Raleway",
        "PT Sans",
        "Merriweather"
    )
    
    // Filter fonts based on search query and separate popular fonts
    val (popularFonts, otherFonts) = remember(systemFonts, searchQuery) {
        if (searchQuery.isBlank()) {
            val popular = systemFonts.filter { font -> 
                popularFontNames.any { it.equals(font.name, ignoreCase = true) }
            }.sortedBy { font -> 
                popularFontNames.indexOfFirst { it.equals(font.name, ignoreCase = true) }
            }
            val others = systemFonts.filter { font -> 
                popularFontNames.none { it.equals(font.name, ignoreCase = true) }
            }
            Pair(popular, others)
        } else {
            val filtered = systemFonts.filter { it.name.contains(searchQuery, ignoreCase = true) }
            val popular = filtered.filter { font -> 
                popularFontNames.any { it.equals(font.name, ignoreCase = true) }
            }.sortedBy { font -> 
                popularFontNames.indexOfFirst { it.equals(font.name, ignoreCase = true) }
            }
            val others = filtered.filter { font -> 
                popularFontNames.none { it.equals(font.name, ignoreCase = true) }
            }
            Pair(popular, others)
        }
    }
    
    val filteredSystemFonts = remember(popularFonts, otherFonts) {
        popularFonts + otherFonts
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Search bar
        if (systemFonts.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(localizeHelper.localize(Res.string.search_fonts)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = localizeHelper.localize(Res.string.search)
                    )
                },
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.loading_fonts),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Popular Fonts Section
                if (popularFonts.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizeHelper.localize(Res.string.popular_fonts),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${popularFonts.size} fonts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    items(popularFonts) { font ->
                        FontItem(
                            font = font,
                            isSelected = selectedFontId == font.id,
                            onClick = { onFontSelected(font.id) },
                            onDelete = null // Google Fonts cannot be deleted
                        )
                    }
                }
                
                // Other Google Fonts Section
                if (otherFonts.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizeHelper.localize(Res.string.all_google_fonts),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${otherFonts.size} fonts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    items(otherFonts) { font ->
                        FontItem(
                            font = font,
                            isSelected = selectedFontId == font.id,
                            onClick = { onFontSelected(font.id) },
                            onDelete = null // Google Fonts cannot be deleted
                        )
                    }
                }
                
                // Custom Fonts Section
                if (customFonts.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizeHelper.localize(Res.string.custom_fonts),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${customFonts.size} fonts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    items(customFonts) { font ->
                        FontItem(
                            font = font,
                            isSelected = selectedFontId == font.id,
                            onClick = { onFontSelected(font.id) },
                            onDelete = { fontToDelete = font }
                        )
                    }
                }
            }
        }
    }
    
    // Confirmation dialog for font deletion
    fontToDelete?.let { font ->
        AlertDialog(
            onDismissRequest = { fontToDelete = null },
            title = { Text(localizeHelper.localize(Res.string.delete_font)) },
            text = { Text("Are you sure you want to delete \"${font.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteFont(font.id)
                        fontToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { fontToDelete = null }) {
                    Text(localizeHelper?.localize(Res.string.cancel) ?: "Cancel")
                }
            }
        )
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    text = localizeHelper.localize(Res.string.the_quick_brown_fox_jumps_over_the_lazy_dog),
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
                        contentDescription = localizeHelper.localize(Res.string.selected),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = localizeHelper.localize(Res.string.delete_font_1),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" } ?: return
    var fontName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(localizeHelper.localize(Res.string.import_font))
        },
        text = {
            Column {
                Text(localizeHelper.localize(Res.string.enter_font_name))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fontName,
                    onValueChange = { fontName = it },
                    label = { Text(localizeHelper.localize(Res.string.font_name)) },
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
                Text(localizeHelper.localize(Res.string.import_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        },
        modifier = modifier
    )
}
