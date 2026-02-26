package ireader.presentation.ui.quote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.domain.models.quote.QuoteCardStyle

/**
 * Instagram story-style quote style selector screen.
 * Users swipe through 9 visual styles and tap to select.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteStyleSelectorScreen(
    onStyleSelected: (QuoteCardStyle) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val styles = QuoteCardStyle.entries.toTypedArray()
    val pagerState = rememberPagerState(pageCount = { styles.size })
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose a Style") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val style = styles[page]
                
                QuoteStylePreview(
                    style = style,
                    onTap = { onStyleSelected(style) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Style name overlay at bottom
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Text(
                    text = styles[pagerState.currentPage].displayName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
            
            // Page indicator
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(styles.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(width = 32.dp, height = 4.dp)
                            .background(
                                color = if (index == pagerState.currentPage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
        }
    }
}

/**
 * Preview of a quote style with sample text.
 * Tap to select this style.
 */
@Composable
private fun QuoteStylePreview(
    style: QuoteCardStyle,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onTap)
            .background(getStyleBackground(style)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "\"The only way to do great work is to love what you do.\"",
                style = MaterialTheme.typography.headlineSmall,
                color = getStyleTextColor(style),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sample Book Title",
                style = MaterialTheme.typography.titleMedium,
                color = getStyleTextColor(style).copy(alpha = 0.8f)
            )
            
            Text(
                text = "by Sample Author",
                style = MaterialTheme.typography.bodyMedium,
                color = getStyleTextColor(style).copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Tap to select",
                style = MaterialTheme.typography.labelLarge,
                color = getStyleTextColor(style).copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Get background brush for a quote card style
 */
private fun getStyleBackground(style: QuoteCardStyle): Brush {
    return when (style) {
        QuoteCardStyle.GRADIENT_SUNSET -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D))
        )
        QuoteCardStyle.GRADIENT_OCEAN -> Brush.verticalGradient(
            colors = listOf(Color(0xFF4ECDC4), Color(0xFF556270))
        )
        QuoteCardStyle.GRADIENT_FOREST -> Brush.verticalGradient(
            colors = listOf(Color(0xFF134E5E), Color(0xFF71B280))
        )
        QuoteCardStyle.GRADIENT_LAVENDER -> Brush.verticalGradient(
            colors = listOf(Color(0xFFDA22FF), Color(0xFF9733EE))
        )
        QuoteCardStyle.GRADIENT_MIDNIGHT -> Brush.verticalGradient(
            colors = listOf(Color(0xFF232526), Color(0xFF414345))
        )
        QuoteCardStyle.MINIMAL_LIGHT -> Brush.verticalGradient(
            colors = listOf(Color(0xFFF5F5F5), Color(0xFFFFFFFF))
        )
        QuoteCardStyle.MINIMAL_DARK -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))
        )
        QuoteCardStyle.PAPER_TEXTURE -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF8DC), Color(0xFFFAF0E6))
        )
        QuoteCardStyle.BOOK_COVER -> Brush.verticalGradient(
            colors = listOf(Color(0xFF8B4513), Color(0xFFD2691E))
        )
    }
}

/**
 * Get text color for a quote card style
 */
private fun getStyleTextColor(style: QuoteCardStyle): Color {
    return when (style) {
        QuoteCardStyle.MINIMAL_LIGHT,
        QuoteCardStyle.PAPER_TEXTURE -> Color.Black
        else -> Color.White
    }
}
