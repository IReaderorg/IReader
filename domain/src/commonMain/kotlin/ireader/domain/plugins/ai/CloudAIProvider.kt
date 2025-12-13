package ireader.domain.plugins.ai

import ireader.plugin.api.AICapability
import ireader.plugin.api.AIError
import ireader.plugin.api.AIModelInfo
import ireader.plugin.api.AIProviderType
import ireader.plugin.api.AIResourceUsage
import ireader.plugin.api.AIResult
import ireader.plugin.api.GenerationOptions
import kotlinx.serialization.Serializable

/**
 * Configuration for cloud AI providers.
 */
@Serializable
data class CloudAIConfig(
    /** API provider */
    val provider: CloudProvider,
    /** API key */
    val apiKey: String,
    /** Model identifier */
    val model: String,
    /** API base URL (for custom endpoints) */
    val baseUrl: String? = null,
    /** Organization ID (for OpenAI) */
    val organizationId: String? = null,
    /** Request timeout in milliseconds */
    val timeoutMs: Long = 60000,
    /** Maximum retries on failure */
    val maxRetries: Int = 3,
    /** Rate limit (requests per minute) */
    val rateLimitRpm: Int? = null
)

@Serializable
enum class CloudProvider {
    OPENAI,
    ANTHROPIC,
    GOOGLE,
    COHERE,
    HUGGINGFACE,
    CUSTOM
}

/**
 * Interface for cloud AI API clients.
 */
interface CloudAIClient {
    /**
     * Initialize the client with configuration.
     */
    suspend fun initialize(config: CloudAIConfig): AIResult<Unit>
    
    /**
     * Validate API credentials.
     */
    suspend fun validateCredentials(): AIResult<Unit>
    
    /**
     * Generate text completion.
     */
    suspend fun complete(
        messages: List<ChatMessage>,
        options: GenerationOptions
    ): AIResult<String>
    
    /**
     * Generate text with streaming output.
     */
    suspend fun completeStream(
        messages: List<ChatMessage>,
        options: GenerationOptions,
        onToken: (String) -> Unit
    ): AIResult<String>
    
    /**
     * Get embeddings for text.
     */
    suspend fun getEmbeddings(
        texts: List<String>,
        model: String? = null
    ): AIResult<List<FloatArray>>
    
    /**
     * Cancel ongoing request.
     */
    fun cancel()
    
    /**
     * Get available models.
     */
    suspend fun getAvailableModels(): AIResult<List<AIModelInfo>>
    
    /**
     * Get usage statistics.
     */
    fun getUsageStats(): CloudUsageStats
}

/**
 * Chat message for conversation-based APIs.
 */
@Serializable
data class ChatMessage(
    val role: MessageRole,
    val content: String,
    val name: String? = null
)

@Serializable
enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT
}

/**
 * Usage statistics for cloud API.
 */
@Serializable
data class CloudUsageStats(
    val totalRequests: Long,
    val totalTokensUsed: Long,
    val promptTokens: Long,
    val completionTokens: Long,
    val estimatedCost: Double,
    val rateLimitRemaining: Int?,
    val rateLimitReset: Long?
)

/**
 * Predefined model configurations for popular providers.
 */
object CloudModels {
    
    // OpenAI Models
    object OpenAI {
        val GPT4 = AIModelInfo(
            name = "gpt-4",
            version = "latest",
            contextLength = 8192,
            provider = "OpenAI",
            capabilities = listOf(
                AICapability.SUMMARIZATION,
                AICapability.CHARACTER_ANALYSIS,
                AICapability.QUESTION_ANSWERING,
                AICapability.TEXT_GENERATION
            ),
            supportsStreaming = true
        )
        
        val GPT4_TURBO = AIModelInfo(
            name = "gpt-4-turbo-preview",
            version = "latest",
            contextLength = 128000,
            provider = "OpenAI",
            capabilities = listOf(
                AICapability.SUMMARIZATION,
                AICapability.CHARACTER_ANALYSIS,
                AICapability.QUESTION_ANSWERING,
                AICapability.TEXT_GENERATION
            ),
            supportsStreaming = true
        )
        
        val GPT35_TURBO = AIModelInfo(
            name = "gpt-3.5-turbo",
            version = "latest",
            contextLength = 16385,
            provider = "OpenAI",
            capabilities = listOf(
                AICapability.SUMMARIZATION,
                AICapability.CHARACTER_ANALYSIS,
                AICapability.QUESTION_ANSWERING,
                AICapability.TEXT_GENERATION
            ),
            supportsStreaming = true
        )
        
        val TEXT_EMBEDDING_3_SMALL = AIModelInfo(
            name = "text-embedding-3-small",
            version = "latest",
            contextLength = 8191,
            provider = "OpenAI",
            capabilities = listOf(AICapability.EMBEDDINGS),
            supportsStreaming = false
        )
    }
    
    // Anthropic Models
    object Anthropic {
        val CLAUDE_3_OPUS = AIModelInfo(
            name = "claude-3-opus-20240229",
            version = "latest",
            contextLength = 200000,
            provider = "Anthropic",
            capabilities = listOf(
                AICapability.SUMMARIZATION,
                AICapability.CHARACTER_ANALYSIS,
                AICapability.QUESTION_ANSWERING,
                AICapability.TEXT_GENERATION
            ),
            supportsStreaming = true
        )
        
        val CLAUDE_3_SONNET = AIModelInfo(
            name = "claude-3-sonnet-20240229",
            version = "latest",
            contextLength = 200000,
            provider = "Anthropic",
            capabilities = listOf(
                AICapability.SUMMARIZATION,
                AICapability.CHARACTER_ANALYSIS,
                AICapability.QUESTION_ANSWERING,
                AICapability.TEXT_GENERATION
            ),
            supportsStreaming = true
        )
        
        val CLAUDE_3_HAIKU = AIModelInfo(
            name = "claude-3-haiku-20240307",
            version = "latest",
            contextLength = 200000,
            provider = "Anthropic",
            capabilities = listOf(
                AICapability.SUMMARIZATION,
                AICapability.CHARACTER_ANALYSIS,
                AICapability.QUESTION_ANSWERING,
                AICapability.TEXT_GENERATION
            ),
            supportsStreaming = true
        )
    }
    
    // Google Models
    object Google {
        val GEMINI_PRO = AIModelInfo(
            name = "gemini-pro",
            version = "latest",
            contextLength = 32000,
            provider = "Google",
            capabilities = listOf(
                AICapability.SUMMARIZATION,
                AICapability.CHARACTER_ANALYSIS,
                AICapability.QUESTION_ANSWERING,
                AICapability.TEXT_GENERATION
            ),
            supportsStreaming = true
        )
        
        val GEMINI_PRO_VISION = AIModelInfo(
            name = "gemini-pro-vision",
            version = "latest",
            contextLength = 16000,
            provider = "Google",
            capabilities = listOf(
                AICapability.SUMMARIZATION,
                AICapability.QUESTION_ANSWERING,
                AICapability.TEXT_GENERATION
            ),
            supportsStreaming = true
        )
    }
}

/**
 * Rate limiter for API calls.
 */
class RateLimiter(
    private val requestsPerMinute: Int
) {
    private val requestTimes = mutableListOf<Long>()
    
    suspend fun acquire() {
        val now = ireader.domain.utils.extensions.currentTimeToLong()
        val windowStart = now - 60_000
        
        // Remove old requests
        requestTimes.removeAll { it < windowStart }
        
        if (requestTimes.size >= requestsPerMinute) {
            // Wait until oldest request expires
            val waitTime = requestTimes.first() + 60_000 - now
            if (waitTime > 0) {
                kotlinx.coroutines.delay(waitTime)
            }
            requestTimes.removeAt(0)
        }
        
        requestTimes.add(now)
    }
    
    fun getRemainingRequests(): Int {
        val now = ireader.domain.utils.extensions.currentTimeToLong()
        val windowStart = now - 60_000
        requestTimes.removeAll { it < windowStart }
        return (requestsPerMinute - requestTimes.size).coerceAtLeast(0)
    }
}
