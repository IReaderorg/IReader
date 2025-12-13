package ireader.domain.plugins.ai

import ireader.plugin.api.AICapability
import ireader.plugin.api.AIError
import ireader.plugin.api.AIModelInfo
import ireader.plugin.api.AIProviderType
import ireader.plugin.api.AIResourceUsage
import ireader.plugin.api.AIResult
import ireader.plugin.api.CharacterAnalysisOptions
import ireader.plugin.api.AICharacterInfo
import ireader.plugin.api.GenerationOptions
import ireader.plugin.api.QAOptions
import ireader.plugin.api.QAResponse
import ireader.plugin.api.SummarizationOptions
import kotlinx.serialization.Serializable

/**
 * Configuration for local AI models.
 */
@Serializable
data class LocalAIConfig(
    /** Path to the model file */
    val modelPath: String,
    /** Model format (GGUF, ONNX, etc.) */
    val modelFormat: ModelFormat,
    /** Number of threads to use */
    val numThreads: Int = 4,
    /** Context size in tokens */
    val contextSize: Int = 2048,
    /** Batch size for processing */
    val batchSize: Int = 512,
    /** Use GPU acceleration if available */
    val useGpu: Boolean = false,
    /** GPU layers to offload (for GGUF models) */
    val gpuLayers: Int = 0,
    /** Memory map the model file */
    val useMmap: Boolean = true,
    /** Lock model in memory */
    val useMlock: Boolean = false
)

@Serializable
enum class ModelFormat {
    GGUF,       // llama.cpp format
    ONNX,       // ONNX Runtime
    PYTORCH,    // PyTorch (via ONNX conversion)
    TFLITE      // TensorFlow Lite
}

/**
 * Interface for local AI inference engines.
 */
interface LocalInferenceEngine {
    /**
     * Load a model from the given path.
     */
    suspend fun loadModel(config: LocalAIConfig): AIResult<Unit>
    
    /**
     * Unload the current model.
     */
    suspend fun unloadModel()
    
    /**
     * Check if a model is loaded.
     */
    fun isModelLoaded(): Boolean
    
    /**
     * Generate text completion.
     */
    suspend fun generate(
        prompt: String,
        options: GenerationOptions
    ): AIResult<String>
    
    /**
     * Generate text with streaming output.
     */
    suspend fun generateStream(
        prompt: String,
        options: GenerationOptions,
        onToken: (String) -> Unit
    ): AIResult<String>
    
    /**
     * Get embeddings for text.
     */
    suspend fun getEmbeddings(texts: List<String>): AIResult<List<FloatArray>>
    
    /**
     * Cancel ongoing generation.
     */
    fun cancel()
    
    /**
     * Get current resource usage.
     */
    fun getResourceUsage(): AIResourceUsage
    
    /**
     * Get model information.
     */
    fun getModelInfo(): AIModelInfo?
}

/**
 * Prompt templates for different AI tasks.
 */
object AIPromptTemplates {
    
    fun summarization(text: String, options: SummarizationOptions): String {
        val lengthInstruction = when (options.targetLength) {
            ireader.plugin.api.SummaryLength.SHORT -> "in 2-3 sentences"
            ireader.plugin.api.SummaryLength.MEDIUM -> "in a short paragraph"
            ireader.plugin.api.SummaryLength.LONG -> "in detail"
            ireader.plugin.api.SummaryLength.CUSTOM -> ""
        }
        
        val styleInstruction = when (options.style) {
            ireader.plugin.api.SummaryStyle.CONCISE -> "Be concise and to the point."
            ireader.plugin.api.SummaryStyle.DETAILED -> "Include important details."
            ireader.plugin.api.SummaryStyle.BULLET_POINTS -> "Use bullet points."
            ireader.plugin.api.SummaryStyle.NARRATIVE -> "Write in narrative form."
        }
        
        return """
            |Summarize the following text $lengthInstruction. $styleInstruction
            |
            |Text:
            |$text
            |
            |Summary:
        """.trimMargin()
    }
    
    fun characterAnalysis(text: String, options: CharacterAnalysisOptions): String {
        val includeRelationships = if (options.includeRelationships) 
            "Include relationships between characters." else ""
        val includeTraits = if (options.includeTraits) 
            "Describe personality traits." else ""
        val includeArcs = if (options.includeArcs) 
            "Describe character development arcs." else ""
        
        return """
            |Analyze the characters in the following text.
            |$includeRelationships $includeTraits $includeArcs
            |List up to ${options.maxCharacters} main characters.
            |
            |For each character provide:
            |- Name
            |- Description
            |- Role (protagonist, antagonist, supporting, etc.)
            |${if (options.includeTraits) "- Personality traits" else ""}
            |${if (options.includeRelationships) "- Relationships with other characters" else ""}
            |
            |Text:
            |$text
            |
            |Characters:
        """.trimMargin()
    }
    
    fun questionAnswering(context: String, question: String, options: QAOptions): String {
        val citationInstruction = if (options.includeCitations)
            "Quote relevant passages from the text to support your answer." else ""
        
        return """
            |Answer the following question based on the provided context.
            |$citationInstruction
            |If the answer cannot be found in the context, say "I cannot find the answer in the provided text."
            |
            |Context:
            |$context
            |
            |Question: $question
            |
            |Answer:
        """.trimMargin()
    }
}

/**
 * Parser for AI responses.
 */
object AIResponseParser {
    
    fun parseCharacters(response: String): List<AICharacterInfo> {
        val characters = mutableListOf<AICharacterInfo>()
        
        // Simple parsing - in production would use more robust parsing
        val characterBlocks = response.split(Regex("(?=\\d+\\.|\\*|-)"))
            .filter { it.isNotBlank() }
        
        for (block in characterBlocks) {
            val lines = block.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) continue
            
            val name = lines.firstOrNull()
                ?.replace(Regex("^[\\d.*-]+\\s*"), "")
                ?.replace(Regex("^Name:\\s*", RegexOption.IGNORE_CASE), "")
                ?.trim()
                ?: continue
            
            val description = lines.drop(1)
                .find { it.contains("description", ignoreCase = true) }
                ?.replace(Regex("^.*?:\\s*"), "")
                ?: lines.getOrNull(1)
                ?: ""
            
            val role = lines
                .find { it.contains("role", ignoreCase = true) }
                ?.replace(Regex("^.*?:\\s*"), "")
            
            val traits = lines
                .filter { it.contains("trait", ignoreCase = true) }
                .flatMap { it.replace(Regex("^.*?:\\s*"), "").split(",") }
                .map { it.trim() }
                .filter { it.isNotBlank() }
            
            characters.add(AICharacterInfo(
                name = name,
                description = description.trim(),
                traits = traits,
                relationships = emptyList(),
                role = role?.trim(),
                confidence = 0.8f
            ))
        }
        
        return characters
    }
    
    fun parseQAResponse(response: String, includeCitations: Boolean): QAResponse {
        val lines = response.lines()
        val answer = lines.takeWhile { !it.startsWith("Citation") && !it.startsWith("Source") }
            .joinToString("\n").trim()
        
        val citations = if (includeCitations) {
            lines.filter { it.startsWith("\"") || it.startsWith("Citation") }
                .map { 
                    ireader.plugin.api.Citation(
                        text = it.replace(Regex("^Citation:?\\s*"), "").trim('"'),
                        startIndex = 0,
                        endIndex = 0
                    )
                }
        } else emptyList()
        
        return QAResponse(
            answer = answer,
            confidence = 0.8f,
            citations = citations,
            relatedQuestions = emptyList()
        )
    }
}
