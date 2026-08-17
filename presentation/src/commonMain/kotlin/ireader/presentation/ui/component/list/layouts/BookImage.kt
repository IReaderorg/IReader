package ireader.presentation.ui.component.list.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.BookCover
import ireader.domain.models.entities.BaseBook
import ireader.presentation.ui.component.LocalPerformanceConfig
import ireader.presentation.ui.component.PerformanceConfig
import ireader.presentation.ui.component.components.IBookImageComposable
import ireader.presentation.ui.core.theme.LocalCoverBasedThemeForLibrary

/**
 * NATIVE-LIKE BOOK IMAGE COMPOSABLE
 * 
 * Optimizations for 60fps scroll:
 * 1. graphicsLayer for GPU compositing
 * 2. Stable data classes prevent recomposition
 * 3. Pre-computed values cached with remember
 * 4. Minimal composable tree depth
 * 5. Zero crossfade for instant display
 */
@Stable
private data class BookImageState(
    val bookId: Long,
    val cover: String?,
    val title: String,
    val isSelected: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookImage(
    modifier: Modifier = Modifier,
    onClick: (BaseBook) -> Unit = {},
    onLongClick: (BaseBook) -> Unit = {},
    book: BaseBook,
    ratio: Float = 0.70f,
    selected: Boolean = false,
    header: ((url: String) -> Map<String, String>?)? = null,
    onlyCover: Boolean = false,
    comfortableMode: Boolean = false,
    isScrollingFast: Boolean = false,
    performanceConfig: PerformanceConfig = LocalPerformanceConfig.current,
    badge: @Composable BoxScope.() -> Unit,
) {
    val useCoverColors = LocalCoverBasedThemeForLibrary.current
    // Cache BookCover - stable reference prevents recomposition
    val bookCover = remember(book.id, book.cover) { BookCover.from(book) }
    
    // Generate deterministic accent color from cover URL for library items
    val coverAccentColor = if (useCoverColors) remember(book.cover) {
        val hash = book.cover?.hashCode() ?: book.id.hashCode()
        val hue = (hash and 0x7FFFFFFF) % 360
        Color.hsv(hue / 360f, 0.7f, 0.5f)
    } else Color.Gray
    
    // Pre-compute colors - avoid recalculation during scroll
    val borderColor = remember(selected) {
        if (selected) coverAccentColor else Color(0x1A000000)
    }
    
    // Compute high-contrast title color for comfortable mode
    val titleTextColor = remember(coverAccentColor) {
        val luminance = coverAccentColor.luminance()
        if (luminance > 0.5f) Color.Black else Color.White
    }
    
    // Compute semi-transparent badge background
    val badgeBackgroundColor = remember(coverAccentColor) {
        coverAccentColor.copy(alpha = 0.7f)
    }
    
    // ZERO crossfade for native-like instant display
    val crossfadeDuration = 0
    
    // Cache gradient for title overlay - use cover-based accent
    val titleGradient = remember(coverAccentColor) {
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, coverAccentColor.copy(alpha = 0.85f)),
            startY = 0f,
            endY = 80f
        )
    }
    
    Column(
        modifier = modifier
            // GPU layer promotion for smooth scrolling
            .graphicsLayer {
                // Promote to separate layer - reduces recomposition impact
                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Auto
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .combinedClickable(
                    onClick = { onClick(book) },
                    onLongClick = { onLongClick(book) }
                )
                .border(2.dp, borderColor),
        ) {
            // Image - always rendered, no conditional logic
            IBookImageComposable(
                modifier = Modifier
                    .aspectRatio(ratio)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .align(Alignment.Center),
                image = bookCover,
                headers = header,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alignment = Alignment.Center,
                crossfadeDurationMs = crossfadeDuration
            )
            
            // Title overlay - only if not cover-only mode
            if (!onlyCover) {
                Box(
                    Modifier
                        .height(50.dp)
                        .background(titleGradient)
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp, start = 4.dp, end = 4.dp)
                            .fillMaxWidth(),
                        text = book.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
            
            // Badges
            badge()
        }
        
        // Comfortable mode title
        if (comfortableMode) {
            Text(
                modifier = Modifier
                    .padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                    .fillMaxWidth(),
                text = book.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = titleTextColor,
                maxLines = 2,
            )
        }
    }
}
