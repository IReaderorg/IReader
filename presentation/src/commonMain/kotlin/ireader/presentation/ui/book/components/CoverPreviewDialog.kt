package ireader.presentation.ui.book.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ireader.domain.models.BookCover
import ireader.domain.models.entities.Book
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Full-screen cover preview dialog with zoom and action options.
 * 
 * Features:
 * - Large cover preview with pinch-to-zoom
 * - Action buttons: Pick local image, Edit URL, Share, Reset to original
 * - Tap outside to dismiss
 * 
 * @param book The book whose cover to preview
 * @param onDismiss Called when dialog is dismissed
 * @param onPickLocalImage Called when user wants to pick a local image
 * @param onEditCoverUrl Called when user wants to edit cover URL manually
 * @param onShareCover Called when user wants to share the cover
 * @param onResetCover Called when user wants to reset to original cover
 */
@Composable
fun CoverPreviewDialog(
    book: Book,
    onDismiss: () -> Unit,
    onPickLocalImage: () -> Unit,
    onEditCoverUrl: () -> Unit,
    onShareCover: () -> Unit,
    onResetCover: () -> Unit,
) {
    val localizeHelper = LocalLocalizeHelper.current
    
    // Check if book has a custom cover
    val hasCustomCover = book.customCover.isNotBlank() && book.customCover != book.cover
    
    // Zoom state
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() }
                    )
                }
        ) {
            // Close button at top right
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = localizeHelper?.localize(Res.string.close),
                    tint = Color.White
                )
            }
            
            // Cover image with zoom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                // Reset zoom on double tap
                                scale = if (scale > 1f) 1f else 2f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .widthIn(max = 300.dp)
                        .aspectRatio(2f / 3f),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    IImageLoader(
                        model = BookCover.from(book),
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Action buttons at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                
                // Custom cover indicator
                if (hasCustomCover) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Text(
                                text = localizeHelper?.localize(Res.string.custom_cover) ?: "Custom Cover",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Pick local image
                    CoverActionButton(
                        icon = Icons.Default.Image,
                        label = localizeHelper?.localize(Res.string.pick_image) ?: "Pick Image",
                        onClick = {
                            onDismiss()
                            onPickLocalImage()
                        }
                    )
                    
                    // Edit URL
                    CoverActionButton(
                        icon = Icons.Default.Link,
                        label = localizeHelper?.localize(Res.string.edit_url) ?: "Edit URL",
                        onClick = {
                            onDismiss()
                            onEditCoverUrl()
                        }
                    )
                    
                    // Share
                    CoverActionButton(
                        icon = Icons.Default.Share,
                        label = localizeHelper?.localize(Res.string.share) ?: "Share",
                        onClick = {
                            onShareCover()
                        }
                    )
                    
                    // Reset (only show if custom cover is set)
                    if (hasCustomCover) {
                        CoverActionButton(
                            icon = Icons.Default.Refresh,
                            label = localizeHelper?.localize(Res.string.reset_cover) ?: "Reset",
                            onClick = {
                                onDismiss()
                                onResetCover()
                            }
                        )
                    }
                }
                
                // Hint text
                Text(
                    text = localizeHelper?.localize(Res.string.double_tap_to_zoom) ?: "Double tap to zoom • Pinch to zoom",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CoverActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
    }
}
