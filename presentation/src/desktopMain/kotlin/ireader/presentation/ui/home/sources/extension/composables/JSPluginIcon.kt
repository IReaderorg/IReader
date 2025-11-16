package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.JSPluginCatalog
import ireader.domain.js.util.JSPluginIconLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage

/**
 * Composable for displaying JavaScript plugin icons (Desktop version).
 * Loads icons from cache or downloads them if necessary.
 */
@Composable
fun JSPluginIcon(
    catalog: JSPluginCatalog,
    iconLoader: JSPluginIconLoader,
    modifier: Modifier = Modifier
) {
    var iconBitmap by remember(catalog.metadata.id) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(catalog.metadata.id) { mutableStateOf(true) }
    var loadError by remember(catalog.metadata.id) { mutableStateOf(false) }
    
    LaunchedEffect(catalog.metadata.id) {
        isLoading = true
        loadError = false
        
        try {
            val iconData = iconLoader.loadIcon(catalog.metadata.icon, catalog.metadata.id)
            if (iconData != null) {
                // Convert ByteArray to ImageBitmap using Skia
                val bitmap = withContext(Dispatchers.IO) {
                    try {
                        SkiaImage.makeFromEncoded(iconData).toComposeImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                }
                iconBitmap = bitmap
                if (bitmap == null) {
                    loadError = true
                }
            } else {
                loadError = true
            }
        } catch (e: Exception) {
            loadError = true
        } finally {
            isLoading = false
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
            iconBitmap != null -> {
                Image(
                    bitmap = iconBitmap!!,
                    contentDescription = catalog.name,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Fit
                )
            }
            else -> {
                // Show default icon on error
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = catalog.name,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}
