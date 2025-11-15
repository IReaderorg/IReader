package ireader.presentation.ui.book.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ireader.domain.models.BookCover
import ireader.domain.models.entities.Book
import ireader.presentation.imageloader.IImageLoader

@Composable
fun BoxScope.BookHeaderImage(
    book: Book,
    scrollProgress: Float = 0f,
    hideBackdrop: Boolean = false
) {
    Crossfade(
        targetState = hideBackdrop,
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) { isHidden ->
        if (isHidden) {
            // Show solid background with subtle gradient when backdrop is hidden
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.surface,
                            ),
                            startY = 0f,
                            endY = 1000f
                        )
                    )
            )
        } else {
            // Show backdrop image with parallax effect
            val backdropGradientColors = listOf(
                Color.Transparent,
                MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                MaterialTheme.colorScheme.background,
            )
            
            // Parallax effect: image moves slower than scroll
            val parallaxOffset = scrollProgress * 0.5f
            
            IImageLoader(
                model = BookCover.from(book),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .graphicsLayer {
                        translationY = parallaxOffset
                        alpha = 1f - (scrollProgress / 1000f).coerceIn(0f, 0.5f)
                    }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = backdropGradientColors,
                                startY = size.height * 0.3f,
                                endY = size.height
                            ),
                        )
                    }
                    .alpha(.3f),
            )
        }
    }
}
