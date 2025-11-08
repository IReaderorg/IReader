package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import ireader.domain.models.BookCover
import ireader.domain.models.entities.BookItem

/**
 * Performance optimizations for image loading in lists.
 * 
 * This file contains utilities to optimize image loading performance by:
 * - Using appropriate image sizes based on display dimensions
 * - Implementing proper caching strategies
 * - Lazy loading images in lists
 */

/**
 * Creates an optimized image request for book covers in lists.
 * 
 * @param book The book item to load the cover for
 * @param targetWidth The target width for the image in Dp
 * @param targetHeight The target height for the image in Dp
 * @return An optimized ImageRequest configured for list performance
 */
@Composable
fun rememberOptimizedBookCoverRequest(
    book: BookItem,
    targetWidth: Dp? = null,
    targetHeight: Dp? = null
): ImageRequest {
    val context = LocalPlatformContext.current
    val density = LocalDensity.current
    
    // Convert Dp to pixels for proper sizing
    val widthPx = targetWidth?.let { with(density) { it.toPx().toInt() } }
    val heightPx = targetHeight?.let { with(density) { it.toPx().toInt() } }
    
    return remember(book.id, book.cover, widthPx, heightPx) {
        val bookCover = BookCover.from(book)
        
        ImageRequest.Builder(context)
            .data(bookCover)
            // Use explicit cache keys for better cache hit rates
            .memoryCacheKey(bookCover.cover)
            .diskCacheKey("${bookCover.cover};${bookCover.lastModified}")
            // Set appropriate size to avoid loading full-resolution images
            .apply {
                if (widthPx != null && heightPx != null) {
                    size(Size(widthPx, heightPx))
                } else if (widthPx != null) {
                    size(Size(widthPx, widthPx * 4 / 3)) // 3:4 aspect ratio
                }
            }
            // Enable crossfade for smooth transitions
            .crossfade(true)
            .build()
    }
}

/**
 * Creates an optimized image request for book covers with custom sizing.
 * 
 * @param bookCover The BookCover object
 * @param size The target size for the image
 * @return An optimized ImageRequest
 */
@Composable
fun rememberOptimizedImageRequest(
    bookCover: BookCover,
    size: Size = Size.ORIGINAL
): ImageRequest {
    val context = LocalPlatformContext.current
    
    return remember(bookCover.cover, bookCover.lastModified, size) {
        ImageRequest.Builder(context)
            .data(bookCover)
            .memoryCacheKey(bookCover.cover)
            .diskCacheKey("${bookCover.cover};${bookCover.lastModified}")
            .size(size)
            .crossfade(true)
            .build()
    }
}

/**
 * Calculates optimal image dimensions for grid items based on column count.
 * 
 * @param columns Number of columns in the grid
 * @param screenWidthDp Screen width in Dp
 * @return Pair of (width, height) in Dp for optimal image sizing
 */
fun calculateOptimalImageSize(columns: Int, screenWidthDp: Dp): Pair<Dp, Dp> {
    // Account for padding and spacing
    val horizontalPadding = 24.dp // Total horizontal padding
    val spacing = (columns - 1) * 12.dp // Spacing between items
    
    val availableWidth = screenWidthDp - horizontalPadding - spacing
    val itemWidth = availableWidth / columns
    
    // Use 3:4 aspect ratio for book covers
    val itemHeight = itemWidth * 4f / 3f
    
    return Pair(itemWidth, itemHeight)
}
