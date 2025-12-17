package ireader.presentation.ui.plugins.marketplace.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.PluginType
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Category tabs for filtering plugins by type
 * Requirements: 16.2
 */
@Composable
fun PluginCategoryTabs(
    selectedCategory: PluginType?,
    onCategorySelected: (PluginType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    ScrollableTabRow(
        selectedTabIndex = getCategoryIndex(selectedCategory),
        modifier = modifier.fillMaxWidth(),
        edgePadding = 16.dp,
        divider = {}
    ) {
        // All tab
        Tab(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            text = {
                Text(
                    text = localizeHelper.localize(Res.string.all),
                    fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
        
        // Theme tab
        Tab(
            selected = selectedCategory == PluginType.THEME,
            onClick = { onCategorySelected(PluginType.THEME) },
            text = {
                Text(
                    text = localizeHelper.localize(Res.string.themes),
                    fontWeight = if (selectedCategory == PluginType.THEME) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
        
        // Translation tab
        Tab(
            selected = selectedCategory == PluginType.TRANSLATION,
            onClick = { onCategorySelected(PluginType.TRANSLATION) },
            text = {
                Text(
                    text = localizeHelper.localize(Res.string.translation),
                    fontWeight = if (selectedCategory == PluginType.TRANSLATION) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
        
        // TTS tab
        Tab(
            selected = selectedCategory == PluginType.TTS,
            onClick = { onCategorySelected(PluginType.TTS) },
            text = {
                Text(
                    text = localizeHelper.localize(Res.string.tts),
                    fontWeight = if (selectedCategory == PluginType.TTS) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
        
        // Feature tab
        Tab(
            selected = selectedCategory == PluginType.FEATURE,
            onClick = { onCategorySelected(PluginType.FEATURE) },
            text = {
                Text(
                    text = localizeHelper.localize(Res.string.features),
                    fontWeight = if (selectedCategory == PluginType.FEATURE) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
    }
}

/**
 * Get tab index for selected category
 */
private fun getCategoryIndex(category: PluginType?): Int {
    return when (category) {
        null -> 0
        PluginType.THEME -> 1
        PluginType.TRANSLATION -> 2
        PluginType.TTS -> 3
        PluginType.FEATURE -> 4
        PluginType.AI -> 5
        PluginType.CATALOG -> 6
        PluginType.IMAGE_PROCESSING -> 7
        PluginType.SYNC -> 8
        PluginType.COMMUNITY_SCREEN -> 9
        PluginType.GLOSSARY -> 10
        PluginType.GRADIO_TTS -> 11
    }
}
