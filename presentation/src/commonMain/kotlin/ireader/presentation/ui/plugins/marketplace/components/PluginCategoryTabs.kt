package ireader.presentation.ui.plugins.marketplace.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.PluginType

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
                    text = "All",
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
                    text = "Themes",
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
                    text = "Translation",
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
                    text = "TTS",
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
                    text = "Features",
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
    }
}
