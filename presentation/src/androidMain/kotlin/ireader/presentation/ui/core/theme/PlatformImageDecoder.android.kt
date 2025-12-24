package ireader.presentation.ui.core.theme

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Android implementation of PlatformImageDecoder
 * Uses BitmapFactory to decode image bytes
 */
actual object PlatformImageDecoder {
    /**
     * Decode image bytes to ImageBitmap using Android's BitmapFactory
     */
    actual fun decode(bytes: ByteArray): ImageBitmap? {
        return try {
            val androidBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            androidBitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
