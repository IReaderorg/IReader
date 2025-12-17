package ireader.presentation.ui.book.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ireader.domain.models.BookCover
import ireader.domain.models.entities.Book
import ireader.presentation.imageloader.IImageLoader

@Composable
fun ModernBookBackdrop(
    book: Book,
    hideBackdrop: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    
    if (!hideBackdrop) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            // Placeholder background - shows immediately before image loads
            // This prevents UI jump when the image loads
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                surfaceColor.copy(alpha = 0.3f),
                                surfaceColor.copy(alpha = 0.2f),
                                backgroundColor.copy(alpha = 0.6f),
                                backgroundColor,
                            ),
                            startY = 0f,
                            endY = 600f
                        )
                    )
            )
            
            // Blurred book cover - loads on top of placeholder
            IImageLoader(
                model = BookCover.from(book),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(30.dp)
                    .drawWithContent {
                        drawContent()
                        // Gradient overlay that blends with background
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.3f),
                                    backgroundColor.copy(alpha = 0.6f),
                                    backgroundColor.copy(alpha = 0.85f),
                                    backgroundColor,
                                ),
                                startY = 0f,
                                endY = size.height
                            )
                        )
                    }
            )
            
            // Color tint overlay that matches book cover theme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                                Color.Transparent,
                            ),
                            startY = 0f,
                            endY = 600f
                        )
                    )
            )
        }
    } else {
        // Even when backdrop is hidden, reserve the space with a subtle background
        // to prevent layout jump
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            surfaceColor.copy(alpha = 0.15f),
                            backgroundColor,
                        ),
                        startY = 0f,
                        endY = 400f
                    )
                )
        )
    }
}
