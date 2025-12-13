package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for AI-powered features.
 * AI plugins provide intelligent text processing capabilities like:
 * - Text summarization
 * - Character analysis
 * - Reading comprehension Q&A
 * - Content recommendations
 * 
 * Supports both local (llama.cpp, ONNX) and cloud (OpenAI, Claude, Gemini) providers.
 * 
 * Example:
 * ```kotlin
 * class SummarizationPlugin : AIPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.summarizer",
 *         name = "AI Summarizer",
 *         type = PluginType.AI,
 *         permissions = listOf(PluginPermission.READER_CONTEXT, PluginPermission.NETWORK),
 *         // ... other manifest fields
 *     )
 *     
 *     override val aiCapabilities = listOf(
 *         AICapability.SUMMARIZATION,
 *         AICapability.CHARACTER_ANALYSIS
 *     )
 *     
 *     override val providerType = AIProviderType.CLOUD
 *     
 *     override suspend fun summarize(text: String, options: SummarizationOptions): AIResult<String> {
 *         // Implementation
 *     }
 * }
 * ```
 */
interface AIPlugin : Plugin {
    /**
     * AI capabilities provided by this plugin.
     */
    val aiCapabilities: List<AICapability>
    
    /**
     * Type of AI provider (local or cloud).
     */
    val providerType: AIProviderType
    
    /**
     * AI model information.
     */
    val modelInfo: AIModelInfo
    
    /**
     * Check if the AI model is ready for inference.
     */
    suspend fun isModelReady(): Boolean
    
    /**
     * Initialize/load the AI model.
     * For local models, this loads the model into memory.
     * For cloud models, this validates API credentials.
     */
    suspend fun loadModel(): AIResult<Unit>
    
    /**
     * Unload the AI model to free resources.
     */
    suspend fun unloadModel()
    
    /**
     * Summarize text content.
     */
    suspend fun summarize(
        text: String,
        options: SummarizationOptions = SummarizationOptions()
    ): AIResult<String>
    
    /**
     * Analyze characters in the text.
     */
    suspend fun analyzeCharacters(
        text: String,
        options: CharacterAnalysisOptions = CharacterAnalysisOptions()
    ): AIResult<List<AICharacterInfo>>
    
    /**
     * Answer questions about the text (reading comprehension).
     */
    suspend fun answerQuestion(
        context: String,
        question: String,
        options: QAOptions = QAOptions()
    ): AIResult<QAResponse>
    
    /**
     * Generate text continuation or completion.
     */
    suspend fun generateText(
        prompt: String,
        options: GenerationOptions = GenerationOptions()
    ): AIResult<String>
    
    /**
     * Stream text generation for real-time output.
     */
    suspend fun streamGeneration(
        prompt: String,
        options: GenerationOptions = GenerationOptions(),
        onToken: (String) -> Unit
    ): AIResult<String>
    
    /**
     * Get embeddings for text (for semantic search).
     */
    suspend fun getEmbeddings(
        texts: List<String>
    ): AIResult<List<FloatArray>>
    
    /**
     * Cancel any ongoing AI operation.
     */
    fun cancelOperation()
    
    /**
     * Get current resource usage of the AI model.
     */
    fun getResourceUsage(): AIResourceUsage
}

/**
 * AI capabilities that plugins can provide.
 */
@Serializable
enum class AICapability {
    /** Text summarization */
    SUMMARIZATION,
    /** Character/entity analysis */
    CHARACTER_ANALYSIS,
    /** Question answering / reading comprehension */
    QUESTION_ANSWERING,
    /** Text generation / completion */
    TEXT_GENERATION,
    /** Text embeddings for semantic search */
    EMBEDDINGS,
    /** Sentiment analysis */
    SENTIMENT_ANALYSIS,
    /** Content classification */
    CLASSIFICATION,
    /** Translation (AI-powered) */
    AI_TRANSLATION,
    /** Text-to-speech synthesis */
    AI_TTS,
    /** Image generation */
    IMAGE_GENERATION
}

/**
 * Type of AI provider.
 */
@Serializable
enum class AIProviderType {
    /** Local inference (llama.cpp, ONNX, etc.) */
    LOCAL,
    /** Cloud API (OpenAI, Claude, Gemini, etc.) */
    CLOUD,
    /** Hybrid - can use both local and cloud */
    HYBRID
}

/**
 * Information about the AI model.
 */
@Serializable
data class AIModelInfo(
    /** Model name/identifier */
    val name: String,
    /** Model version */
    val version: String,
    /** Model size in bytes (for local models) */
    val sizeBytes: Long? = null,
    /** Required memory in bytes */
    val requiredMemoryBytes: Long? = null,
    /** Supported context length (tokens) */
    val contextLength: Int,
    /** Model provider (e.g., "OpenAI", "Anthropic", "Local") */
    val provider: String,
    /** Model capabilities */
    val capabilities: List<AICapability>,
    /** Whether the model supports streaming */
    val supportsStreaming: Boolean = true,
    /** Model quantization (for local models) */
    val quantization: String? = null
)

/**
 * Options for text summarization.
 */
@Serializable
data class SummarizationOptions(
    /** Target length of summary (in sentences or percentage) */
    val targetLength: SummaryLength = SummaryLength.MEDIUM,
    /** Summary style */
    val style: SummaryStyle = SummaryStyle.CONCISE,
    /** Focus on specific aspects */
    val focus: List<String> = emptyList(),
    /** Language for output */
    val outputLanguage: String? = null
)

@Serializable
enum class SummaryLength {
    SHORT, MEDIUM, LONG, CUSTOM
}

@Serializable
enum class SummaryStyle {
    CONCISE, DETAILED, BULLET_POINTS, NARRATIVE
}

/**
 * Options for character analysis.
 */
@Serializable
data class CharacterAnalysisOptions(
    /** Include character relationships */
    val includeRelationships: Boolean = true,
    /** Include character traits */
    val includeTraits: Boolean = true,
    /** Include character arcs */
    val includeArcs: Boolean = false,
    /** Maximum characters to analyze */
    val maxCharacters: Int = 10
)

/**
 * Character information from AI analysis.
 */
@Serializable
data class AICharacterInfo(
    /** Character name */
    val name: String,
    /** Character description */
    val description: String,
    /** Character traits */
    val traits: List<String> = emptyList(),
    /** Relationships with other characters */
    val relationships: List<AICharacterRelationship> = emptyList(),
    /** Character role (protagonist, antagonist, etc.) */
    val role: String? = null,
    /** Confidence score (0-1) */
    val confidence: Float = 1.0f
)

@Serializable
data class AICharacterRelationship(
    val targetCharacter: String,
    val relationshipType: String,
    val description: String? = null
)

/**
 * Options for question answering.
 */
@Serializable
data class QAOptions(
    /** Include source citations */
    val includeCitations: Boolean = true,
    /** Maximum answer length */
    val maxAnswerLength: Int = 500,
    /** Confidence threshold */
    val confidenceThreshold: Float = 0.5f
)

/**
 * Response from question answering.
 */
@Serializable
data class QAResponse(
    /** The answer text */
    val answer: String,
    /** Confidence score (0-1) */
    val confidence: Float,
    /** Source citations from the context */
    val citations: List<Citation> = emptyList(),
    /** Related questions */
    val relatedQuestions: List<String> = emptyList()
)

@Serializable
data class Citation(
    val text: String,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Options for text generation.
 */
@Serializable
data class GenerationOptions(
    /** Maximum tokens to generate */
    val maxTokens: Int = 256,
    /** Temperature (0-2, higher = more creative) */
    val temperature: Float = 0.7f,
    /** Top-p sampling */
    val topP: Float = 0.9f,
    /** Top-k sampling */
    val topK: Int = 40,
    /** Repetition penalty */
    val repetitionPenalty: Float = 1.1f,
    /** Stop sequences */
    val stopSequences: List<String> = emptyList()
)

/**
 * Resource usage information for AI models.
 */
@Serializable
data class AIResourceUsage(
    /** Memory usage in bytes */
    val memoryBytes: Long,
    /** GPU memory usage in bytes (if applicable) */
    val gpuMemoryBytes: Long? = null,
    /** CPU usage percentage */
    val cpuPercent: Float? = null,
    /** Tokens processed */
    val tokensProcessed: Long = 0,
    /** Average inference time in milliseconds */
    val avgInferenceTimeMs: Long = 0
)

/**
 * Result wrapper for AI operations.
 */
sealed class AIResult<out T> {
    data class Success<T>(val data: T) : AIResult<T>()
    data class Error(val error: AIError) : AIResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw error.toException()
    }
    
    inline fun <R> map(transform: (T) -> R): AIResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    inline fun onSuccess(action: (T) -> Unit): AIResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (AIError) -> Unit): AIResult<T> {
        if (this is Error) action(error)
        return this
    }
}

/**
 * AI operation errors.
 */
@Serializable
sealed class AIError {
    /** Model not loaded or not ready */
    data object ModelNotReady : AIError()
    /** Model loading failed */
    data class ModelLoadFailed(val reason: String) : AIError()
    /** API authentication failed */
    data class AuthenticationFailed(val reason: String) : AIError()
    /** Rate limit exceeded */
    data class RateLimitExceeded(val retryAfterMs: Long?) : AIError()
    /** Context too long */
    data class ContextTooLong(val maxLength: Int, val actualLength: Int) : AIError()
    /** Network error */
    data class NetworkError(val reason: String) : AIError()
    /** Operation cancelled */
    data object Cancelled : AIError()
    /** Insufficient resources */
    data class InsufficientResources(val reason: String) : AIError()
    /** Generic error */
    data class Unknown(val reason: String) : AIError()
    
    fun toException(): Exception = when (this) {
        is ModelNotReady -> IllegalStateException("AI model not ready")
        is ModelLoadFailed -> IllegalStateException("Model load failed: $reason")
        is AuthenticationFailed -> IllegalStateException("Authentication failed: $reason")
        is RateLimitExceeded -> RuntimeException("Rate limit exceeded")
        is ContextTooLong -> IllegalArgumentException("Context too long: $actualLength > $maxLength")
        is NetworkError -> RuntimeException("Network error: $reason")
        is Cancelled -> RuntimeException("Operation cancelled")
        is InsufficientResources -> RuntimeException("Insufficient resources: $reason")
        is Unknown -> RuntimeException("Unknown error: $reason")
    }
}
