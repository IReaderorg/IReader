package ireader.data.characterart

/**
 * Image compression utility for reducing file size before upload.
 * Platform-specific implementations handle actual compression.
 */
expect class ImageCompressor {
    /**
     * Compress image to target size
     * 
     * @param imageBytes Original image bytes
     * @param maxWidth Maximum width (maintains aspect ratio)
     * @param maxHeight Maximum height (maintains aspect ratio)
     * @param quality JPEG quality (0-100)
     * @param maxSizeKb Target max file size in KB
     * @return Compressed image bytes
     */
    suspend fun compress(
        imageBytes: ByteArray,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 85,
        maxSizeKb: Int = 500
    ): ByteArray
    
    /**
     * Get image dimensions
     */
    fun getImageDimensions(imageBytes: ByteArray): Pair<Int, Int>
}

/**
 * Compression settings
 */
data class CompressionSettings(
    val maxWidth: Int = 1024,
    val maxHeight: Int = 1024,
    val quality: Int = 85,
    val maxSizeKb: Int = 500,
    val format: ImageFormat = ImageFormat.JPEG
)

enum class ImageFormat {
    JPEG,
    PNG,
    WEBP
}
