package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for AI image generation.
 * Specifically designed for character portrait generation and book cover creation.
 * 
 * Example:
 * ```kotlin
 * class StableDiffusionPlugin : AIImageGeneratorPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.stable-diffusion",
 *         name = "Stable Diffusion",
 *         type = PluginType.AI,
 *         permissions = listOf(
 *             PluginPermission.NETWORK,
 *             PluginPermission.LOCAL_SERVER,
 *             PluginPermission.CHARACTER_DATABASE
 *         ),
 *         // ... other manifest fields
 *     )
 *     
 *     override suspend fun generateCharacterPortrait(
 *         character: CharacterDescription,
 *         style: ImageStyle
 *     ): ImageGenResult<GeneratedImage> {
 *         // Generate character portrait using SD
 *     }
 * }
 * ```
 */
interface AIImageGeneratorPlugin : Plugin {
    /**
     * Image generation capabilities.
     */
    val generationCapabilities: List<ImageGenCapability>
    
    /**
     * Server configuration.
     */
    val serverConfig: ImageGenServerConfig
    
    /**
     * Available generation models.
     */
    val availableModels: List<ImageGenModel>
    
    /**
     * Available image styles.
     */
    val availableStyles: List<ImageStyle>
    
    /**
     * Check if server is reachable.
     */
    suspend fun checkConnection(): ImageGenResult<ImageGenServerStatus>
    
    /**
     * Generate character portrait from description.
     */
    suspend fun generateCharacterPortrait(
        character: CharacterDescription,
        style: ImageStyle,
        options: ImageGenOptions = ImageGenOptions()
    ): ImageGenResult<GeneratedImage>
    
    /**
     * Generate character portrait from text analysis.
     */
    suspend fun generateFromText(
        text: String,
        characterName: String,
        style: ImageStyle,
        options: ImageGenOptions = ImageGenOptions()
    ): ImageGenResult<GeneratedImage>
    
    /**
     * Generate book cover.
     */
    suspend fun generateBookCover(
        title: String,
        author: String?,
        genre: String?,
        description: String?,
        style: ImageStyle,
        options: ImageGenOptions = ImageGenOptions()
    ): ImageGenResult<GeneratedImage>
    
    /**
     * Generate scene illustration.
     */
    suspend fun generateSceneIllustration(
        sceneDescription: String,
        characters: List<CharacterDescription> = emptyList(),
        style: ImageStyle,
        options: ImageGenOptions = ImageGenOptions()
    ): ImageGenResult<GeneratedImage>
    
    /**
     * Generate variations of an existing image.
     */
    suspend fun generateVariations(
        sourceImage: ByteArray,
        count: Int = 4,
        variationStrength: Float = 0.5f,
        options: ImageGenOptions = ImageGenOptions()
    ): ImageGenResult<List<GeneratedImage>>
    
    /**
     * Enhance/upscale generated image.
     */
    suspend fun enhanceImage(
        image: ByteArray,
        enhancementType: ImageEnhancementType
    ): ImageGenResult<GeneratedImage>
    
    /**
     * Get generation progress.
     */
    fun getProgress(): ImageGenProgress?
    
    /**
     * Cancel ongoing generation.
     */
    fun cancelGeneration()
    
    /**
     * Configure server endpoint.
     */
    fun setServerEndpoint(endpoint: String)
    
    /**
     * Build prompt from character description.
     */
    fun buildCharacterPrompt(character: CharacterDescription, style: ImageStyle): String
}

/**
 * Image generation capabilities.
 */
@Serializable
enum class ImageGenCapability {
    /** Character portrait generation */
    CHARACTER_PORTRAIT,
    /** Book cover generation */
    BOOK_COVER,
    /** Scene illustration */
    SCENE_ILLUSTRATION,
    /** Image variations */
    VARIATIONS,
    /** Image enhancement */
    ENHANCEMENT,
    /** Inpainting */
    INPAINTING,
    /** ControlNet support */
    CONTROLNET,
    /** LoRA support */
    LORA
}

/**
 * Image generation server configuration.
 */
@Serializable
data class ImageGenServerConfig(
    /** Default server endpoint */
    val defaultEndpoint: String = "http://localhost:7860",
    /** Whether custom endpoints are supported */
    val supportsCustomEndpoint: Boolean = true,
    /** API type */
    val apiType: ImageGenApiType = ImageGenApiType.AUTOMATIC1111,
    /** Connection timeout in milliseconds */
    val connectionTimeoutMs: Long = 30000,
    /** Generation timeout in milliseconds */
    val generationTimeoutMs: Long = 300000,
    /** Maximum resolution */
    val maxResolution: Int = 2048,
    /** Default resolution */
    val defaultResolution: Int = 512,
    /** Whether batch generation is supported */
    val supportsBatch: Boolean = true,
    /** Maximum batch size */
    val maxBatchSize: Int = 4
)

/**
 * Image generation API types.
 */
@Serializable
enum class ImageGenApiType {
    /** Automatic1111 WebUI API */
    AUTOMATIC1111,
    /** ComfyUI API */
    COMFYUI,
    /** Gradio API */
    GRADIO,
    /** OpenAI DALL-E API */
    DALLE,
    /** Stability AI API */
    STABILITY_AI,
    /** Custom API */
    CUSTOM
}

/**
 * Image generation model.
 */
@Serializable
data class ImageGenModel(
    /** Model identifier */
    val id: String,
    /** Model display name */
    val name: String,
    /** Model description */
    val description: String? = null,
    /** Model type (checkpoint, LoRA, etc.) */
    val type: ModelType,
    /** Supported capabilities */
    val capabilities: List<ImageGenCapability>,
    /** Recommended for character portraits */
    val recommendedForCharacters: Boolean = false,
    /** Recommended for anime style */
    val recommendedForAnime: Boolean = false,
    /** Recommended for realistic style */
    val recommendedForRealistic: Boolean = false,
    /** Base model (SD 1.5, SDXL, etc.) */
    val baseModel: String? = null,
    /** VRAM requirement in MB */
    val vramRequirementMb: Int? = null
)

@Serializable
enum class ModelType {
    CHECKPOINT,
    LORA,
    EMBEDDING,
    VAE,
    CONTROLNET
}

/**
 * Image style for generation.
 */
@Serializable
data class ImageStyle(
    /** Style identifier */
    val id: String,
    /** Style display name */
    val name: String,
    /** Style description */
    val description: String? = null,
    /** Positive prompt additions */
    val positivePrompt: String = "",
    /** Negative prompt additions */
    val negativePrompt: String = "",
    /** Recommended model ID */
    val recommendedModelId: String? = null,
    /** Recommended sampler */
    val recommendedSampler: String? = null,
    /** Recommended CFG scale */
    val recommendedCfgScale: Float? = null,
    /** Recommended steps */
    val recommendedSteps: Int? = null,
    /** Style category */
    val category: StyleCategory = StyleCategory.GENERAL
)

@Serializable
enum class StyleCategory {
    GENERAL,
    ANIME,
    REALISTIC,
    FANTASY,
    SCI_FI,
    MANGA,
    WATERCOLOR,
    OIL_PAINTING,
    DIGITAL_ART,
    PIXEL_ART
}

/**
 * Character description for portrait generation.
 */
@Serializable
data class CharacterDescription(
    /** Character name */
    val name: String,
    /** Gender */
    val gender: CharacterGender? = null,
    /** Approximate age */
    val age: String? = null,
    /** Hair description */
    val hair: HairDescription? = null,
    /** Eye description */
    val eyes: EyeDescription? = null,
    /** Skin tone */
    val skinTone: String? = null,
    /** Body type */
    val bodyType: String? = null,
    /** Clothing description */
    val clothing: String? = null,
    /** Accessories */
    val accessories: List<String> = emptyList(),
    /** Personality traits (for expression) */
    val personality: List<String> = emptyList(),
    /** Expression */
    val expression: String? = null,
    /** Pose */
    val pose: String? = null,
    /** Background preference */
    val background: String? = null,
    /** Additional description */
    val additionalDescription: String? = null,
    /** Reference image (optional) */
    val referenceImage: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CharacterDescription
        return name == other.name &&
                gender == other.gender &&
                age == other.age
    }
    
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (gender?.hashCode() ?: 0)
        result = 31 * result + (age?.hashCode() ?: 0)
        return result
    }
}

@Serializable
enum class CharacterGender {
    MALE,
    FEMALE,
    NON_BINARY,
    UNKNOWN
}

@Serializable
data class HairDescription(
    val color: String? = null,
    val length: String? = null,
    val style: String? = null,
    val texture: String? = null
)

@Serializable
data class EyeDescription(
    val color: String? = null,
    val shape: String? = null,
    val features: String? = null
)

/**
 * Image generation options.
 */
@Serializable
data class ImageGenOptions(
    /** Model to use */
    val modelId: String? = null,
    /** Image width */
    val width: Int = 512,
    /** Image height */
    val height: Int = 512,
    /** Number of images to generate */
    val count: Int = 1,
    /** CFG scale (guidance) */
    val cfgScale: Float = 7.0f,
    /** Sampling steps */
    val steps: Int = 20,
    /** Sampler name */
    val sampler: String = "Euler a",
    /** Random seed (-1 for random) */
    val seed: Long = -1,
    /** Custom positive prompt additions */
    val customPositivePrompt: String? = null,
    /** Custom negative prompt additions */
    val customNegativePrompt: String? = null,
    /** LoRA models to apply */
    val loraModels: List<LoraConfig> = emptyList(),
    /** ControlNet configuration */
    val controlNet: ControlNetConfig? = null,
    /** Output format */
    val outputFormat: ImageFormat = ImageFormat.PNG,
    /** High-res fix */
    val enableHiResFix: Boolean = false,
    /** Upscale factor for hi-res fix */
    val hiResUpscaleFactor: Float = 2.0f
)

@Serializable
data class LoraConfig(
    val modelId: String,
    val weight: Float = 1.0f
)

@Serializable
data class ControlNetConfig(
    val modelId: String,
    val inputImage: ByteArray,
    val weight: Float = 1.0f,
    val guidanceStart: Float = 0.0f,
    val guidanceEnd: Float = 1.0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ControlNetConfig
        return modelId == other.modelId && inputImage.contentEquals(other.inputImage)
    }
    
    override fun hashCode(): Int {
        var result = modelId.hashCode()
        result = 31 * result + inputImage.contentHashCode()
        return result
    }
}

/**
 * Image enhancement types.
 */
@Serializable
enum class ImageEnhancementType {
    UPSCALE_2X,
    UPSCALE_4X,
    FACE_RESTORE,
    DENOISE,
    SHARPEN
}

/**
 * Generated image result.
 */
@Serializable
data class GeneratedImage(
    /** Image data */
    val imageData: ByteArray,
    /** Image format */
    val format: ImageFormat,
    /** Image width */
    val width: Int,
    /** Image height */
    val height: Int,
    /** Seed used */
    val seed: Long,
    /** Prompt used */
    val prompt: String,
    /** Negative prompt used */
    val negativePrompt: String,
    /** Model used */
    val modelId: String,
    /** Generation time in milliseconds */
    val generationTimeMs: Long,
    /** Generation parameters */
    val parameters: Map<String, String> = emptyMap()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as GeneratedImage
        return imageData.contentEquals(other.imageData) &&
                format == other.format &&
                seed == other.seed
    }
    
    override fun hashCode(): Int {
        var result = imageData.contentHashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + seed.hashCode()
        return result
    }
}

/**
 * Image generation progress.
 */
@Serializable
data class ImageGenProgress(
    /** Current step */
    val currentStep: Int,
    /** Total steps */
    val totalSteps: Int,
    /** Progress percentage (0-100) */
    val percentage: Float,
    /** Current status */
    val status: String,
    /** Preview image (if available) */
    val previewImage: ByteArray? = null,
    /** Estimated time remaining in milliseconds */
    val estimatedTimeRemainingMs: Long?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ImageGenProgress
        return currentStep == other.currentStep &&
                totalSteps == other.totalSteps
    }
    
    override fun hashCode(): Int {
        var result = currentStep
        result = 31 * result + totalSteps
        return result
    }
}

/**
 * Image generation server status.
 */
@Serializable
data class ImageGenServerStatus(
    /** Whether server is online */
    val isOnline: Boolean,
    /** Server endpoint */
    val endpoint: String,
    /** Available models */
    val availableModels: List<String> = emptyList(),
    /** Available LoRAs */
    val availableLoRAs: List<String> = emptyList(),
    /** Available samplers */
    val availableSamplers: List<String> = emptyList(),
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
 * Result wrapper for image generation operations.
 */
sealed class ImageGenResult<out T> {
    data class Success<T>(val data: T) : ImageGenResult<T>()
    data class Error(val error: ImageGenError) : ImageGenResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    inline fun <R> map(transform: (T) -> R): ImageGenResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

/**
 * Image generation errors.
 */
@Serializable
sealed class ImageGenError {
    data class ConnectionFailed(val endpoint: String, val reason: String) : ImageGenError()
    data class Timeout(val timeoutMs: Long) : ImageGenError()
    data class ModelNotFound(val modelId: String) : ImageGenError()
    data class InvalidPrompt(val reason: String) : ImageGenError()
    data class ResolutionTooHigh(val maxResolution: Int, val requested: Int) : ImageGenError()
    data class OutOfMemory(val requiredMb: Int, val availableMb: Int) : ImageGenError()
    data class NsfwContentDetected(val message: String) : ImageGenError()
    data class ServerError(val statusCode: Int, val message: String) : ImageGenError()
    data class QueueFull(val queueSize: Int) : ImageGenError()
    data object Cancelled : ImageGenError()
    data class Unknown(val message: String) : ImageGenError()
}
