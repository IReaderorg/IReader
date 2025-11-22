package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.sources.extension.LocaleHelper

/**
 * Clean and simple section header
 */
@Composable
fun CleanSourceHeader(
    modifier: Modifier = Modifier,
    language: String,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current)
    
    Text(
        text = LocaleHelper.getSourceDisplayName(language, localizeHelper),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
    )
}
