package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for image processing (upscaling, enhancement).
 * Can connect to local servers (Real-ESRGAN, etc.) or cloud services.
 * 
 * Example:
 * ```kotlin
 * class RealESRGANPlugin : ImageProcessingPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.real-esrgan",
 *         name = "Real-ESRGAN Upscaler",
 *         type = PluginType.IMAGE_PROCESSING,
 *         permissions = listOf(PluginPermission.LOCAL_SERVER, PluginPermission.IMAGE_PROCESSING),
 *         // ... other manifest fields
 *     )
 *     
 *     override val capabilities = listOf(ImageCapability.UPSCALE, ImageCapability.DENOISE)
 *     
 *     override suspend fun processImage(request: ImageProcessRequest): ImageResult<ProcessedImage> {
 *         // Call local Real-ESRGAN server
 *     }
 * }
 * ```
 */
interface ImageProcessingPlugin : Plugin {
    /**
     * Image processing capabilities.
     */
    val capabilities: List<ImageCapability>
    
    /**
     * Server configuration.
     */
    val serverConfig: ImageServerConfig
    
    /**
     * Available processing models.
     */
    val availableModels: List<ImageModel>
    
    /**
     * Check if server is reachable.
     */
    suspend fun checkConnection(): ImageResult<ImageServerStatus>
    
    /**
     * Process an image (upscale, enhance, etc.).
     */
    suspend fun processImage(request: ImageProcessRequest): ImageResult<ProcessedImage>
    
    /**
     * Process multiple images in batch.
     */
    suspend fun processBatch(requests: List<ImageProcessRequest>): ImageResult<List<ProcessedImage>>
    
    /**
     * Get processing progress (for long operations).
     */
    fun getProgress(): ImageProcessProgress?
    
    /**
     * Cancel ongoing processing.
     */
    fun cancelProcessing()
    
    /**
     * Configure server endpoint.
     */
    fun setServerEndpoint(endpoint: String)
    
    /**
     * Get estimated processing time.
     */
    fun estimateProcessingTime(request: ImageProcessRequest): Long
}

/**
 * Image processing capabilities.
 */
@Serializable
enum class ImageCapability {
    /** Image upscaling (2x, 4x, etc.) */
    UPSCALE,
    /** Noise reduction */
    DENOISE,
    /** JPEG artifact removal */
    DEJPEG,
    /** Face enhancement */
    FACE_ENHANCE,
    /** Color correction */
    COLOR_CORRECT,
    /** Background removal */
    BACKGROUND_REMOVE,
    /** Style transfer */
    STYLE_TRANSFER,
    /** Inpainting */
    INPAINT,
    /** Super resolution */
    SUPER_RESOLUTION
}

/**
 * Image server configuration.
 */
@Serializable
data class ImageServerConfig(
    /** Default server endpoint */
    val defaultEndpoint: String = "http://localhost:7861",
    /** Whether custom endpoints are supported */
    val supportsCustomEndpoint: Boolean = true,
    /** Connection timeout in milliseconds */
    val connectionTimeoutMs: Long = 30000,
    /** Processing timeout in milliseconds */
    val processingTimeoutMs: Long = 300000,
    /** Maximum image size in bytes */
    val maxImageSizeBytes: Long = 50 * 1024 * 1024,
    /** Maximum resolution (width or height) */
    val maxResolution: Int = 4096,
    /** Whether batch processing is supported */
    val supportsBatch: Boolean = true,
    /** Maximum batch size */
    val maxBatchSize: Int = 10
)

/**
 * Image processing model information.
 */
@Serializable
data class ImageModel(
    /** Model identifier */
    val id: String,
    /** Model display name */
    val name: String,
    /** Model description */
    val description: String? = null,
    /** Supported capabilities */
    val capabilities: List<ImageCapability>,
    /** Upscale factors (if applicable) */
    val upscaleFactors: List<Int> = emptyList(),
    /** Quality rating (1-5) */
    val quality: Int = 3,
    /** Speed rating (1-5, higher = faster) */
    val speed: Int = 3,
    /** VRAM requirement in MB */
    val vramRequirementMb: Int? = null
)

/**
 * Image processing request.
 */
@Serializable
data class ImageProcessRequest(
    /** Image data as bytes */
    val imageData: ByteArray,
    /** Input image format */
    val inputFormat: ImageFormat,
    /** Processing operation */
    val operation: ImageCapability,
    /** Model to use */
    val modelId: String,
    /** Upscale factor (for UPSCALE operation) */
    val upscaleFactor: Int = 2,
    /** Denoise strength (0.0 - 1.0) */
    val denoiseStrength: Float = 0.5f,
    /** Output format */
    val outputFormat: ImageFormat = ImageFormat.PNG,
    /** Output quality (for lossy formats, 1-100) */
    val outputQuality: Int = 95,
    /** Custom endpoint (overrides default) */
    val endpoint: String? = null,
    /** Additional model-specific parameters */
    val extraParams: Map<String, String> = emptyMap()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ImageProcessRequest
        return imageData.contentEquals(other.imageData) &&
                inputFormat == other.inputFormat &&
                operation == other.operation &&
                modelId == other.modelId
    }
    
    override fun hashCode(): Int {
        var result = imageData.contentHashCode()
        result = 31 * result + inputFormat.hashCode()
        result = 31 * result + operation.hashCode()
        result = 31 * result + modelId.hashCode()
        return result
    }
}

/**
 * Image format.
 */
@Serializable
enum class ImageFormat {
    PNG,
    JPEG,
    WEBP,
    GIF,
    BMP,
    TIFF
}

/**
 * Processed image result.
 */
@Serializable
data class ProcessedImage(
    /** Processed image data */
    val imageData: ByteArray,
    /** Output format */
    val format: ImageFormat,
    /** Output width */
    val width: Int,
    /** Output height */
    val height: Int,
    /** Processing time in milliseconds */
    val processingTimeMs: Long,
    /** Model used */
    val modelId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ProcessedImage
        return imageData.contentEquals(other.imageData) &&
                format == other.format &&
                width == other.width &&
                height == other.height
    }
    
    override fun hashCode(): Int {
        var result = imageData.contentHashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}

/**
 * Image processing progress.
 */
@Serializable
data class ImageProcessProgress(
    /** Current step */
    val currentStep: Int,
    /** Total steps */
    val totalSteps: Int,
    /** Progress percentage (0-100) */
    val percentage: Float,
    /** Current status message */
    val statusMessage: String,
    /** Estimated time remaining in milliseconds */
    val estimatedTimeRemainingMs: Long?
)

/**
 * Image server status.
 */
@Serializable
data class ImageServerStatus(
    /** Whether server is online */
    val isOnline: Boolean,
    /** Server endpoint */
    val endpoint: String,
    /** Available models */
    val availableModels: List<String> = emptyList(),
    /** GPU available */
    val gpuAvailable: Boolean = false,
    /** GPU name */
    val gpuName: String? = null,
    /** Available VRAM in MB */
    val availableVramMb: Int? = null,
    /** Current queue size */
    val queueSize: Int = 0
)

/**
 * Result wrapper for image operations.
 */
sealed class ImageResult<out T> {
    data class Success<T>(val data: T) : ImageResult<T>()
    data class Error(val error: ImageError) : ImageResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    inline fun <R> map(transform: (T) -> R): ImageResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

/**
 * Image processing errors.
 */
@Serializable
sealed class ImageError {
    data class ConnectionFailed(val endpoint: String, val reason: String) : ImageError()
    data class Timeout(val timeoutMs: Long) : ImageError()
    data class ModelNotFound(val modelId: String) : ImageError()
    data class ImageTooLarge(val maxSizeBytes: Long, val actualSizeBytes: Long) : ImageError()
    data class ResolutionTooHigh(val maxResolution: Int, val actualResolution: Int) : ImageError()
    data class UnsupportedFormat(val format: String) : ImageError()
    data class ProcessingFailed(val reason: String) : ImageError()
    data class OutOfMemory(val requiredMb: Int, val availableMb: Int) : ImageError()
    data object Cancelled : ImageError()
    data class Unknown(val message: String) : ImageError()
}
