package ireader.presentation.ui.home.sources.browse

import ireader.presentation.ui.component.animations.AnimatedVisibility
import ireader.presentation.ui.component.animations.animateColorAsState
import ireader.presentation.ui.component.animations.animateFloatAsState
import ireader.presentation.ui.component.animations.tween
import ireader.presentation.ui.component.animations.expandVertically
import ireader.presentation.ui.component.animations.fadeIn
import ireader.presentation.ui.component.animations.fadeOut
import ireader.presentation.ui.component.animations.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String
)

@Composable
fun LanguageToggleGroup(
    selectedLanguages: Set<String>,
    onLanguageToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var searchQuery by remember { mutableStateOf("") }
    
    val availableLanguages = listOf(
        Language("en", "English", "English", "🇬🇧"),
        Language("ar", "Arabic", "العربية", "🇸🇦"),
        Language("bn", "Bengali", "বাংলা", "🇧🇩"),
        Language("de", "German", "Deutsch", "🇩🇪"),
        Language("es", "Spanish", "Español", "🇪🇸"),
        Language("fa", "Persian", "فارسی", "🇮🇷"),
        Language("fi", "Finnish", "Suomi", "🇫🇮"),
        Language("fil", "Filipino", "Filipino", "🇵🇭"),
        Language("fr", "French", "Français", "🇫🇷"),
        Language("hi", "Hindi", "हिन्दी", "🇮🇳"),
        Language("hr", "Croatian", "Hrvatski", "🇭🇷"),
        Language("hu", "Hungarian", "Magyar", "🇭🇺"),
        Language("id", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
        Language("in", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
        Language("it", "Italian", "Italiano", "🇮🇹"),
        Language("ja", "Japanese", "日本語", "🇯🇵"),
        Language("ko", "Korean", "한국어", "🇰🇷"),
        Language("ml", "Malayalam", "മലയാളം", "🇮🇳"),
        Language("my", "Burmese", "မြန်မာဘာသာ", "🇲🇲"),
        Language("nl", "Dutch", "Nederlands", "🇳🇱"),
        Language("pl", "Polish", "Polski", "🇵🇱"),
        Language("pt", "Portuguese", "Português", "🇵🇹"),
        Language("ro", "Romanian", "Română", "🇷🇴"),
        Language("ru", "Russian", "Русский", "🇷🇺"),
        Language("si", "Sinhala", "සිංහල", "🇱🇰"),
        Language("sk", "Slovak", "Slovenčina", "🇸🇰"),
        Language("th", "Thai", "ไทย", "🇹🇭"),
        Language("tr", "Turkish", "Türkçe", "🇹🇷"),
        Language("uk", "Ukrainian", "Українська", "🇺🇦"),
        Language("uz", "Uzbek", "Oʻzbekcha", "🇺🇿"),
        Language("vi", "Vietnamese", "Tiếng Việt", "🇻🇳"),
        Language("zh", "Chinese", "中文", "🇨🇳"),
        Language("zh-CN", "Chinese (Simplified)", "简体中文", "🇨🇳"),
        Language("zh-TW", "Chinese (Traditional)", "繁體中文", "🇹🇼")
    )

    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            availableLanguages
        } else {
            availableLanguages.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.nativeName.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with stats
        LanguageSelectionHeader(
            selectedCount = selectedLanguages.size,
            totalCount = availableLanguages.size,
            onSelectAll = {
                availableLanguages.forEach { lang ->
                    if (lang.code !in selectedLanguages) {
                        onLanguageToggle(lang.code)
                    }
                }
            },
            onDeselectAll = {
                selectedLanguages.forEach { code ->
                    if (selectedLanguages.size > 1) {
                        onLanguageToggle(code)
                    }
                }
            }
        )

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(localizeHelper.localize(Res.string.search_languages)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = localizeHelper.localize(Res.string.search)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        // Language chips
        AnimatedVisibility(
            visible = filteredLanguages.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredLanguages.forEach { language ->
                    ModernLanguageChip(
                        language = language,
                        isSelected = language.code in selectedLanguages,
                        onClick = { onLanguageToggle(language.code) }
                    )
                }
            }
        }

        // Empty state
        if (filteredLanguages.isEmpty()) {
            EmptySearchState()
        }
    }
}

@Composable
private fun LanguageSelectionHeader(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = localizeHelper.localize(Res.string.selected_languages),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$selectedCount of $totalCount selected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Select All button
            FilledTonalButton(
                onClick = onSelectAll,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = localizeHelper.localize(Res.string.select_all),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("All", style = MaterialTheme.typography.labelMedium)
            }

            // Deselect All button
            if (selectedCount > 1) {
                OutlinedButton(
                    onClick = onDeselectAll,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Deselect,
                        contentDescription = localizeHelper.localize(Res.string.clear_1),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun ModernLanguageChip(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300)
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(300)
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(200)
    )

    Surface(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Flag emoji
            Text(
                text = language.flag,
                style = MaterialTheme.typography.titleMedium
            )

            // Language name
            Column {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (language.nativeName != language.name) {
                    Text(
                        text = language.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Check icon
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = localizeHelper.localize(Res.string.selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptySearchState(modifier: Modifier = Modifier) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = localizeHelper.localize(Res.string.no_languages_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = localizeHelper.localize(Res.string.try_a_different_search_term),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
