package ireader.presentation.ui.tts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.ColorPickerDialog
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * TTS Settings Drawer - Right-side drawer containing all TTS settings
 * Replaces the TTSSettingsPanelCommon overlay
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSSettingsDrawer(
    drawerState: DrawerState,
    useCustomColors: Boolean,
    customBackgroundColor: Color,
    customTextColor: Color,
    currentParagraphColor: Color,
    currentParagraphHighlightColor: Color,
    otherTextColor: Color,
    fontSize: Int,
    textAlignment: TextAlign,
    sleepModeEnabled: Boolean,
    sleepTimeMinutes: Int,
    speechSpeed: Float,
    speechPitch: Float,
    autoNextChapter: Boolean,
    pageMode: Boolean,
    useGradioTTS: Boolean,
    currentEngineName: String,
    readTranslatedText: Boolean,
    hasTranslation: Boolean,
    sentenceHighlightEnabled: Boolean,
    contentFilterEnabled: Boolean,
    contentFilterPatterns: String,
    onUseCustomColorsChange: (Boolean) -> Unit,
    onBackgroundColorChange: (Color) -> Unit,
    onTextColorChange: (Color) -> Unit,
    onCurrentParagraphColorChange: (Color) -> Unit,
    onCurrentParagraphHighlightColorChange: (Color) -> Unit,
    onOtherTextColorChange: (Color) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onTextAlignmentChange: (TextAlign) -> Unit,
    onSleepModeChange: (Boolean) -> Unit,
    onSleepTimeChange: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onAutoNextChange: (Boolean) -> Unit,
    onCoquiTTSChange: (Boolean) -> Unit,
    onReadTranslatedTextChange: (Boolean) -> Unit,
    onSentenceHighlightChange: (Boolean) -> Unit,
    onPageModeChange: (Boolean) -> Unit,
    onContentFilterEnabledChange: (Boolean) -> Unit,
    onContentFilterPatternsChange: (String) -> Unit,
    onOpenEngineSettings: () -> Unit,
    onSelectVoice: () -> Unit,
    onNavigateToTextReplacement: () -> Unit,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.33f)
            ) {
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.tts_settings),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Scrollable settings
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // TTS Engine
                        SettingSection(title = localizeHelper.localize(Res.string.tts_engine)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = localizeHelper.localize(Res.string.current_engine),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = currentEngineName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    OutlinedButton(onClick = onOpenEngineSettings) {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Settings", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                
                                HorizontalDivider()
                                
                                SettingRow(
                                    title = localizeHelper.localize(Res.string.use_coqui_tts),
                                    subtitle = if (useGradioTTS) "High-quality neural TTS" else "System TTS",
                                    checked = useGradioTTS,
                                    onCheckedChange = onCoquiTTSChange
                                )
                            }
                        }
                        
                        // Voice Selection
                        SettingSection(title = localizeHelper.localize(Res.string.voice)) {
                            OutlinedButton(
                                onClick = onSelectVoice,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(localizeHelper.localize(Res.string.select_voice))
                            }
                        }
                        
                        // Speed
                        SettingSection(title = "Speed: ${formatMultiplier(speechSpeed)}") {
                            Slider(
                                value = speechSpeed,
                                onValueChange = onSpeedChange,
                                valueRange = 0.5f..2.0f,
                                steps = 14
                            )
                        }
                        
                        // Pitch
                        SettingSection(title = "Pitch: ${formatMultiplier(speechPitch)}") {
                            Slider(
                                value = speechPitch,
                                onValueChange = onPitchChange,
                                valueRange = 0.5f..2.0f,
                                steps = 14
                            )
                        }
                        
                        // Playback
                        SettingSection(title = localizeHelper.localize(Res.string.playback)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettingRow(
                                    title = localizeHelper.localize(Res.string.auto_next_chapter_1),
                                    subtitle = "Play next chapter automatically",
                                    checked = autoNextChapter,
                                    onCheckedChange = onAutoNextChange
                                )
                                
                                HorizontalDivider()
                                
                                SettingRow(
                                    title = localizeHelper.localize(Res.string.read_translated_text),
                                    subtitle = if (readTranslatedText) "Reading translated" else "Reading original",
                                    checked = readTranslatedText,
                                    onCheckedChange = onReadTranslatedTextChange
                                )
                                
                                HorizontalDivider()
                                
                                SettingRow(
                                    title = "Sentence Highlighting",
                                    subtitle = if (sentenceHighlightEnabled) "Highlights sentences" else "Highlights paragraphs",
                                    checked = sentenceHighlightEnabled,
                                    onCheckedChange = onSentenceHighlightChange
                                )
                                
                                HorizontalDivider()
                                
                                SettingRow(
                                    title = "Page Mode",
                                    subtitle = if (pageMode) "Swipe to turn pages" else "Scroll paragraphs",
                                    checked = pageMode,
                                    onCheckedChange = onPageModeChange
                                )
                            }
                        }
                        
                        // Text Replacement
                        SettingSection(title = "Text Replacement") {
                            OutlinedButton(
                                onClick = onNavigateToTextReplacement,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Manage Text Replacements")
                            }
                            Text(
                                text = "Replace text patterns (e.g., 'khan' → 'khaaan')",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Colors
                        SettingSection(title = localizeHelper.localize(Res.string.color_theme)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettingRow(
                                    title = localizeHelper.localize(Res.string.use_custom_colors),
                                    subtitle = if (useCustomColors) "Custom colors" else "App theme",
                                    checked = useCustomColors,
                                    onCheckedChange = onUseCustomColorsChange
                                )
                                
                                if (useCustomColors) {
                                    HorizontalDivider()
                                    
                                    Text("Background", style = MaterialTheme.typography.bodySmall)
                                    ColorPickerButton(
                                        selectedColor = customBackgroundColor,
                                        onColorSelected = onBackgroundColorChange,
                                        label = "Background"
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text("Current Paragraph Color", style = MaterialTheme.typography.bodySmall)
                                    ColorPickerButton(
                                        selectedColor = currentParagraphColor,
                                        onColorSelected = onCurrentParagraphColorChange,
                                        label = "Current Paragraph"
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text("Current Paragraph Highlight", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "Set to transparent to disable highlighting",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                    ColorPickerButton(
                                        selectedColor = currentParagraphHighlightColor,
                                        onColorSelected = onCurrentParagraphHighlightColorChange,
                                        label = "Highlight",
                                        allowTransparent = true
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text("Other Text Color", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "Color for non-current paragraphs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                    ColorPickerButton(
                                        selectedColor = otherTextColor,
                                        onColorSelected = onOtherTextColorChange,
                                        label = "Other Text"
                                    )
                                }
                            }
                        }
                        
                        // Font Size
                        SettingSection(title = "Font Size: ${fontSize}sp") {
                            Slider(
                                value = fontSize.toFloat(),
                                onValueChange = { onFontSizeChange(it.toInt()) },
                                valueRange = 12f..32f,
                                steps = 19
                            )
                        }
                        
                        // Text Alignment
                        SettingSection(title = localizeHelper.localize(Res.string.text_alignment)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                AlignmentButton(
                                    icon = Icons.Default.FormatAlignLeft,
                                    isSelected = textAlignment == TextAlign.Start,
                                    onClick = { onTextAlignmentChange(TextAlign.Start) }
                                )
                                AlignmentButton(
                                    icon = Icons.Default.FormatAlignCenter,
                                    isSelected = textAlignment == TextAlign.Center,
                                    onClick = { onTextAlignmentChange(TextAlign.Center) }
                                )
                                AlignmentButton(
                                    icon = Icons.Default.FormatAlignRight,
                                    isSelected = textAlignment == TextAlign.End,
                                    onClick = { onTextAlignmentChange(TextAlign.End) }
                                )
                                AlignmentButton(
                                    icon = Icons.Default.FormatAlignJustify,
                                    isSelected = textAlignment == TextAlign.Justify,
                                    onClick = { onTextAlignmentChange(TextAlign.Justify) }
                                )
                            }
                        }
                        
                        // Sleep Timer
                        SettingSection(title = "Sleep Timer") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettingRow(
                                    title = "Enable Timer",
                                    subtitle = "Auto-stop after set time",
                                    checked = sleepModeEnabled,
                                    onCheckedChange = onSleepModeChange
                                )
                                
                                if (sleepModeEnabled) {
                                    Text("Sleep after: $sleepTimeMinutes min", style = MaterialTheme.typography.bodySmall)
                                    Slider(
                                        value = sleepTimeMinutes.toFloat(),
                                        onValueChange = { onSleepTimeChange(it.toInt()) },
                                        valueRange = 5f..120f,
                                        steps = 22
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        },
        content = content
    )
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ColorPickerButton(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    label: String,
    allowTransparent: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Color preview button
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Select $label Color")
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(selectedColor, MaterialTheme.shapes.small)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.small
                    )
            )
        }
    }
    
    // Color picker dialog
    if (showDialog) {
        ColorPickerDialog(
            title = { Text("$label Color") },
            onDismissRequest = { showDialog = false },
            onSelected = { color ->
                onColorSelected(color)
                showDialog = false
            },
            initialColor = selectedColor
        )
    }
}

@Composable
private fun AlignmentButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatMultiplier(value: Float): String {
    return String.format("%.1fx", value)
}
