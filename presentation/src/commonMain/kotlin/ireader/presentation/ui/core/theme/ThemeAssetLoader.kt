package ireader.presentation.ui.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import ireader.core.io.FileSystem
import ireader.core.io.VirtualFile
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.withContext

/**
 * Utility for loading theme assets (backgrounds, images)
 * Requirements: 3.4
 */
class ThemeAssetLoader(
    private val fileSystem: FileSystem
) {
    private val cache = mutableMapOf<String, ImageBitmap>()
    
    /**
     * Load an image asset from a path
     * Supports both local file paths and URLs
     */
    suspend fun loadImage(path: String): Result<ImageBitmap> {
        return try {
            // Check cache first
            cache[path]?.let {
                return Result.success(it)
            }
            
            val image = withContext(ioDispatcher) {
                when {
                    path.startsWith("http://") || path.startsWith("https://") -> {
                        loadImageFromUrl(path)
                    }
                    else -> {
                        val file = fileSystem.getFile(path)
                        loadImageFromFile(file)
                    }
                }
            }
            
            // Cache the loaded image
            cache[path] = image
            Result.success(image)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Load image from a local file
     */
    private suspend fun loadImageFromFile(file: VirtualFile): ImageBitmap {
        // Platform-specific implementation would go here
        // For now, throw an exception to indicate not implemented
        throw NotImplementedError("File loading not implemented for this platform")
    }
    
    /**
     * Load image from a URL
     */
    private suspend fun loadImageFromUrl(url: String): ImageBitmap {
        // Platform-specific implementation would go here
        // For now, throw an exception to indicate not implemented
        throw NotImplementedError("URL loading not implemented for this platform")
    }
    
    /**
     * Clear the image cache
     */
    fun clearCache() {
        cache.clear()
    }
    
    /**
     * Remove a specific image from cache
     */
    fun removeFromCache(path: String) {
        cache.remove(path)
    }
}

/**
 * Composable for loading and displaying theme background
 * Requirements: 3.4
 * Note: assetLoader must be provided via dependency injection or composition local
 */
@Composable
fun rememberThemeBackground(
    backgroundPath: String?,
    assetLoader: ThemeAssetLoader
): State<ImageBitmap?> {
    val backgroundState = remember { mutableStateOf<ImageBitmap?>(null) }
    
    LaunchedEffect(backgroundPath) {
        if (backgroundPath != null) {
            assetLoader.loadImage(backgroundPath).onSuccess { image ->
                backgroundState.value = image
            }.onFailure {
                // Failed to load, keep null
                backgroundState.value = null
            }
        } else {
            backgroundState.value = null
        }
    }
    
    return backgroundState
}

/**
 * Error handling for theme asset loading
 * Requirements: 3.5
 */
sealed class ThemeAssetError {
    data class LoadFailed(val path: String, val exception: Throwable) : ThemeAssetError()
    data class InvalidFormat(val path: String) : ThemeAssetError()
    data class NotFound(val path: String) : ThemeAssetError()
    
    fun toUserMessage(): String = when (this) {
        is LoadFailed -> "Failed to load theme asset: ${exception.message}"
        is InvalidFormat -> "Invalid theme asset format: $path"
        is NotFound -> "Theme asset not found: $path"
    }
}
