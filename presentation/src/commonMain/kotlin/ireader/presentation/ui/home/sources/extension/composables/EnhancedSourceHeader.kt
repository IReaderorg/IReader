package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.sources.extension.LocaleHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Enhanced section header with custom design
 * - Gradient background
 * - Animated appearance
 * - Better visual hierarchy
 */
@Composable
fun EnhancedSourceHeader(
    modifier: Modifier = Modifier,
    language: String,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current)
    val displayName = LocaleHelper.getSourceDisplayName(language, localizeHelper)
    
    // Animated entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = localizeHelper.localize(Res.string.header_alpha)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Decorative line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.CenterStart)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f * alpha),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f * alpha),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Header content
        Surface(
            modifier = Modifier.wrapContentWidth(),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f * alpha),
            shadowElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .drawBehind {
                        // Accent line on left
                        drawLine(
                            color = Color(0xFF4CAF50).copy(alpha = 0.6f),
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Language emoji if available
                    val emoji = getLanguageEmoji(language)
                    if (emoji != null) {
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                        )
                    }
                    
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

private fun getLanguageEmoji(languageCode: String): String? {
    return when (languageCode.lowercase()) {
        "en" -> "🇬🇧"
        "es" -> "🇪🇸"
        "fr" -> "🇫🇷"
        "de" -> "🇩🇪"
        "it" -> "🇮🇹"
        "pt" -> "🇧🇷"
        "ru" -> "🇷🇺"
        "ja" -> "🇯🇵"
        "ko" -> "🇰🇷"
        "zh" -> "🇨🇳"
        "ar" -> "🇸🇦"
        "hi" -> "🇮🇳"
        "tr" -> "🇹🇷"
        "nl" -> "🇳🇱"
        "pl" -> "🇵🇱"
        "vi" -> "🇻🇳"
        "th" -> "🇹🇭"
        "id" -> "🇮🇩"
        "installed" -> "📦"
        "pinned" -> "📌"
        "all" -> "🌐"
        else -> null
    }
}
