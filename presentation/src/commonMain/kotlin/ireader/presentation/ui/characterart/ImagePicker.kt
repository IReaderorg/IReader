package ireader.presentation.ui.characterart

import androidx.compose.runtime.Composable

/**
 * Platform-specific image picker for character art uploads.
 * Implementations handle Android gallery/camera and Desktop file chooser.
 */
expect class ImagePicker {
    /**
     * Pick an image from the device
     * @param onImagePicked Called with image bytes and file name when successful
     * @param onError Called when picking fails
     */
    suspend fun pickImage(
        onImagePicked: (bytes: ByteArray, fileName: String) -> Unit,
        onError: (String) -> Unit
    )
    
    /**
     * Get the currently selected image path (for preview)
     */
    fun getSelectedImagePath(): String?
    
    /**
     * Clear the selected image
     */
    fun clearSelection()
}

/**
 * Composable to get platform-specific ImagePicker
 */
@Composable
expect fun rememberImagePicker(): ImagePicker

/**
 * Result of image picking operation
 */
data class ImagePickResult(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String = "image/jpeg",
    val width: Int = 0,
    val height: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ImagePickResult
        return fileName == other.fileName && bytes.contentEquals(other.bytes)
    }
    
    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + fileName.hashCode()
        return result
    }
}
