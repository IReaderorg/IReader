package ireader.presentation.ui.core.theme

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage

/**
 * Desktop implementation of PlatformImageDecoder
 * Uses Skia to decode image bytes
 */
actual object PlatformImageDecoder {
    /**
     * Decode image bytes to ImageBitmap using Skia
     */
    actual fun decode(bytes: ByteArray): ImageBitmap? {
        return try {
            SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
