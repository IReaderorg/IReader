package ireader.presentation.ui.characterart

/**
 * Save image bytes to the device gallery.
 * iOS: no-op (would need Photos framework, not implemented yet).
 */
internal actual suspend fun saveImageToGallery(context: Any, imageBytes: ByteArray): Boolean {
    return false
}
