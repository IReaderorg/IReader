package ireader.presentation.ui.featurestore

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.PluginType
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Horizontal scrolling category tabs for Feature Store
 */
@Composable
fun FeatureCategoryTabs(
    selectedCategory: PluginType?,
    onCategorySelected: (PluginType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All category
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text(localizeHelper.localize(Res.string.all)) }
        )
        
        // TTS plugins
        FilterChip(
            selected = selectedCategory == PluginType.TTS,
            onClick = { onCategorySelected(PluginType.TTS) },
            label = { Text(localizeHelper.localize(Res.string.tts)) }
        )
        
        // Theme plugins
        FilterChip(
            selected = selectedCategory == PluginType.THEME,
            onClick = { onCategorySelected(PluginType.THEME) },
            label = { Text(localizeHelper.localize(Res.string.themes)) }
        )
        
        // Translation plugins
        FilterChip(
            selected = selectedCategory == PluginType.TRANSLATION,
            onClick = { onCategorySelected(PluginType.TRANSLATION) },
            label = { Text(localizeHelper.localize(Res.string.translation)) }
        )
        
        // Feature plugins
        FilterChip(
            selected = selectedCategory == PluginType.FEATURE,
            onClick = { onCategorySelected(PluginType.FEATURE) },
            label = { Text(localizeHelper.localize(Res.string.features)) }
        )
    }
}
