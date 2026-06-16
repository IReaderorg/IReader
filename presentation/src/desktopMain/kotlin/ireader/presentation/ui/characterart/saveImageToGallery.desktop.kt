package ireader.presentation.ui.characterart

/**
 * Save image bytes to the device gallery.
 * Desktop: no-op (no gallery concept).
 */
internal actual suspend fun saveImageToGallery(context: Any, imageBytes: ByteArray): Boolean {
    return false
}
