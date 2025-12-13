package ireader.domain.plugins.composition

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

/**
 * Plugin Composition System
 * Enables chaining plugins together to create powerful workflows.
 * 
 * Example: Translate → TTS pipeline
 * ```kotlin
 * val pipeline = PluginPipeline.builder()
 *     .addStep(translatePlugin, TranslateConfig(targetLang = "en"))
 *     .addStep(ttsPlugin, TTSConfig(voice = "default"))
 *     .build()
 * 
 * pipeline.execute(inputText)
 * ```
 */

/**
 * Represents a composable plugin step in a pipeline.
 */
interface ComposablePlugin {
    /**
     * Unique identifier for this plugin.
     */
    val pluginId: String
    
    /**
     * Process input and produce output.
     */
    suspend fun process(input: PipelineData): PipelineResult
    
    /**
     * Validate if this plugin can process the given input type.
     */
    fun canProcess(inputType: PipelineDataType): Boolean
    
    /**
     * Get the output type this plugin produces.
     */
    fun getOutputType(): PipelineDataType
}

/**
 * Data types that can flow through a pipeline.
 */
@Serializable
enum class PipelineDataType {
    TEXT,
    AUDIO,
    IMAGE,
    STRUCTURED_DATA,
    BINARY,
    BOOK_CONTENT,
    CHAPTER_CONTENT,
    AI_RESPONSE
}

/**
 * Data container for pipeline processing.
 */
@Serializable
data class PipelineData(
    val type: PipelineDataType,
    val textContent: String? = null,
    val binaryContent: ByteArray? = null,
    val metadata: Map<String, String> = emptyMap(),
    val structuredData: Map<String, String>? = null
) {
    companion object {
        fun text(content: String, metadata: Map<String, String> = emptyMap()) =
            PipelineData(PipelineDataType.TEXT, textContent = content, metadata = metadata)
        
        fun audio(data: ByteArray, metadata: Map<String, String> = emptyMap()) =
            PipelineData(PipelineDataType.AUDIO, binaryContent = data, metadata = metadata)
        
        fun bookContent(content: String, bookId: Long, chapterId: Long) =
            PipelineData(
                PipelineDataType.BOOK_CONTENT,
                textContent = content,
                metadata = mapOf("bookId" to bookId.toString(), "chapterId" to chapterId.toString())
            )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PipelineData
        if (type != other.type) return false
        if (textContent != other.textContent) return false
        if (binaryContent != null) {
            if (other.binaryContent == null) return false
            if (!binaryContent.contentEquals(other.binaryContent)) return false
        } else if (other.binaryContent != null) return false
        if (metadata != other.metadata) return false
        return true
    }
    
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (textContent?.hashCode() ?: 0)
        result = 31 * result + (binaryContent?.contentHashCode() ?: 0)
        result = 31 * result + metadata.hashCode()
        return result
    }
}

/**
 * Result of a pipeline step execution.
 */
sealed class PipelineResult {
    data class Success(val data: PipelineData) : PipelineResult()
    data class Error(val error: PipelineError, val partialData: PipelineData? = null) : PipelineResult()
    data class Skipped(val reason: String, val data: PipelineData) : PipelineResult()
    
    fun getDataOrNull(): PipelineData? = when (this) {
        is Success -> data
        is Error -> partialData
        is Skipped -> data
    }
    
    fun isSuccess(): Boolean = this is Success
}

/**
 * Pipeline execution errors.
 */
@Serializable
sealed class PipelineError {
    data class PluginError(val pluginId: String, val message: String) : PipelineError()
    data class TypeMismatch(val expected: PipelineDataType, val actual: PipelineDataType) : PipelineError()
    data class PluginNotFound(val pluginId: String) : PipelineError()
    data class ConfigurationError(val message: String) : PipelineError()
    data class Timeout(val stepIndex: Int, val timeoutMs: Long) : PipelineError()
    data class Cancelled(val stepIndex: Int) : PipelineError()
}

/**
 * Configuration for a pipeline step.
 */
@Serializable
data class PipelineStepConfig(
    val pluginId: String,
    val config: Map<String, String> = emptyMap(),
    val timeoutMs: Long = 30000,
    val retryCount: Int = 0,
    val skipOnError: Boolean = false,
    val condition: StepCondition? = null
)

/**
 * Condition for executing a pipeline step.
 */
@Serializable
sealed class StepCondition {
    /** Always execute */
    data object Always : StepCondition()
    /** Execute only if previous step succeeded */
    data object OnSuccess : StepCondition()
    /** Execute only if previous step failed */
    data object OnError : StepCondition()
    /** Execute based on metadata value */
    data class MetadataEquals(val key: String, val value: String) : StepCondition()
    /** Execute based on data type */
    data class DataTypeIs(val type: PipelineDataType) : StepCondition()
}

/**
 * A complete plugin pipeline definition.
 */
@Serializable
data class PluginPipelineDefinition(
    val id: String,
    val name: String,
    val description: String,
    val steps: List<PipelineStepConfig>,
    val inputType: PipelineDataType,
    val outputType: PipelineDataType,
    val createdAt: Long,
    val updatedAt: Long,
    val isPublic: Boolean = false,
    val authorId: String? = null,
    val tags: List<String> = emptyList()
)

/**
 * Execution state of a pipeline.
 */
@Serializable
data class PipelineExecutionState(
    val pipelineId: String,
    val currentStepIndex: Int,
    val totalSteps: Int,
    val status: PipelineStatus,
    val startTime: Long,
    val endTime: Long? = null,
    val stepResults: List<StepExecutionResult> = emptyList()
)

@Serializable
enum class PipelineStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Serializable
data class StepExecutionResult(
    val stepIndex: Int,
    val pluginId: String,
    val status: StepStatus,
    val startTime: Long,
    val endTime: Long,
    val errorMessage: String? = null
)

@Serializable
enum class StepStatus {
    SUCCESS,
    FAILED,
    SKIPPED,
    TIMEOUT
}

/**
 * Event emitted during pipeline execution.
 */
sealed class PipelineEvent {
    data class Started(val pipelineId: String, val totalSteps: Int) : PipelineEvent()
    data class StepStarted(val stepIndex: Int, val pluginId: String) : PipelineEvent()
    data class StepCompleted(val stepIndex: Int, val pluginId: String, val result: PipelineResult) : PipelineEvent()
    data class StepProgress(val stepIndex: Int, val pluginId: String, val progress: Float) : PipelineEvent()
    data class Completed(val pipelineId: String, val finalResult: PipelineResult) : PipelineEvent()
    data class Failed(val pipelineId: String, val error: PipelineError, val stepIndex: Int) : PipelineEvent()
    data class Cancelled(val pipelineId: String, val stepIndex: Int) : PipelineEvent()
}
