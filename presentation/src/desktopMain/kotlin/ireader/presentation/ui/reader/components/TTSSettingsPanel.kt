package ireader.presentation.ui.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun TTSSettingsPanel(
    useCustomColors: Boolean,
    customBackgroundColor: Color,
    customTextColor: Color,
    fontSize: Int,
    textAlignment: TextAlign,
    sleepModeEnabled: Boolean,
    sleepTimeMinutes: Int,
    onUseCustomColorsChange: (Boolean) -> Unit,
    onBackgroundColorChange: (Color) -> Unit,
    onTextColorChange: (Color) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onTextAlignmentChange: (TextAlign) -> Unit,
    onSleepModeChange: (Boolean) -> Unit,
    onSleepTimeChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TTS Settings",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Divider()
                
                // Settings content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Custom Colors Toggle
                    SettingSection(title = "Color Theme") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Custom Colors",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (useCustomColors) "Custom colors enabled" else "Using app theme colors",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = useCustomColors,
                                onCheckedChange = onUseCustomColorsChange
                            )
                        }
                    }
                    
                    // Background Color (only if custom colors enabled)
                    if (useCustomColors) {
                        SettingSection(title = "Background Color") {
                            ColorPicker(
                                selectedColor = customBackgroundColor,
                                onColorSelected = onBackgroundColorChange,
                                colors = listOf(
                                    Color(0xFF1E1E1E), // Dark
                                    Color(0xFF2C2C2C), // Dark Gray
                                    Color(0xFF1A1A2E), // Dark Blue
                                    Color(0xFF16213E), // Navy
                                    Color(0xFF0F3460), // Deep Blue
                                    Color(0xFFFFFBF0), // Cream
                                    Color(0xFFF5F5DC), // Beige
                                    Color(0xFFE8E8E8), // Light Gray
                                )
                            )
                        }
                        
                        // Text Color
                        SettingSection(title = "Text Color") {
                            ColorPicker(
                                selectedColor = customTextColor,
                                onColorSelected = onTextColorChange,
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFE0E0E0),
                                    Color(0xFFFFF8DC),
                                    Color(0xFFFFE4B5),
                                    Color.Black,
                                    Color(0xFF333333),
                                    Color(0xFF4A4A4A),
                                    Color(0xFF2196F3),
                                )
                            )
                        }
                    }
                    
                    // Font Size
                    SettingSection(title = "Font Size: ${fontSize}sp") {
                        Slider(
                            value = fontSize.toFloat(),
                            onValueChange = { onFontSizeChange(it.toInt()) },
                            valueRange = 12f..32f,
                            steps = 19,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Text Alignment
                    SettingSection(title = "Text Alignment") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AlignmentButton(
                                icon = Icons.Default.FormatAlignLeft,
                                label = "Left",
                                isSelected = textAlignment == TextAlign.Start,
                                onClick = { onTextAlignmentChange(TextAlign.Start) }
                            )
                            AlignmentButton(
                                icon = Icons.Default.FormatAlignCenter,
                                label = "Center",
                                isSelected = textAlignment == TextAlign.Center,
                                onClick = { onTextAlignmentChange(TextAlign.Center) }
                            )
                            AlignmentButton(
                                icon = Icons.Default.FormatAlignRight,
                                label = "Right",
                                isSelected = textAlignment == TextAlign.End,
                                onClick = { onTextAlignmentChange(TextAlign.End) }
                            )
                            AlignmentButton(
                                icon = Icons.Default.FormatAlignJustify,
                                label = "Justify",
                                isSelected = textAlignment == TextAlign.Justify,
                                onClick = { onTextAlignmentChange(TextAlign.Justify) }
                            )
                        }
                    }
                    
                    // Sleep Mode
                    SettingSection(title = "Sleep Mode") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Enable Sleep Timer")
                                Switch(
                                    checked = sleepModeEnabled,
                                    onCheckedChange = onSleepModeChange
                                )
                            }
                            
                            if (sleepModeEnabled) {
                                Column {
                                    Text(
                                        text = "Sleep after: $sleepTimeMinutes minutes",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Slider(
                                        value = sleepTimeMinutes.toFloat(),
                                        onValueChange = { onSleepTimeChange(it.toInt()) },
                                        valueRange = 5f..120f,
                                        steps = 22,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (color == selectedColor) 3.dp else 1.dp,
                        color = if (color == selectedColor) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) },
                contentAlignment = Alignment.Center
            ) {
                if (color == selectedColor) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = if (color.luminance() > 0.5f) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AlignmentButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(icon, label)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Extension function to calculate color luminance
private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}
