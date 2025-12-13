package ireader.domain.plugins.composition

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ireader.core.util.createICoroutineScope
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manager for plugin pipelines.
 * Handles creation, storage, and execution of pipelines.
 */
class PipelineManager(
    private val pluginResolver: PluginResolver,
    private val pipelineRepository: PipelineRepository
) {
    private val scope: CoroutineScope = createICoroutineScope()
    private val _pipelines = MutableStateFlow<List<PluginPipelineDefinition>>(emptyList())
    val pipelines: StateFlow<List<PluginPipelineDefinition>> = _pipelines.asStateFlow()
    
    private val _activePipelines = MutableStateFlow<Map<String, PipelineExecutionState>>(emptyMap())
    val activePipelines: StateFlow<Map<String, PipelineExecutionState>> = _activePipelines.asStateFlow()
    
    /**
     * Load all saved pipelines.
     */
    suspend fun loadPipelines() {
        _pipelines.value = pipelineRepository.getAllPipelines()
    }
    
    /**
     * Create a new pipeline.
     */
    suspend fun createPipeline(definition: PluginPipelineDefinition): Result<PluginPipelineDefinition> {
        return try {
            pipelineRepository.savePipeline(definition)
            _pipelines.value = _pipelines.value + definition
            Result.success(definition)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing pipeline.
     */
    suspend fun updatePipeline(definition: PluginPipelineDefinition): Result<PluginPipelineDefinition> {
        return try {
            val updated = definition.copy(updatedAt = currentTimeToLong())
            pipelineRepository.savePipeline(updated)
            _pipelines.value = _pipelines.value.map { 
                if (it.id == updated.id) updated else it 
            }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a pipeline.
     */
    suspend fun deletePipeline(pipelineId: String): Result<Unit> {
        return try {
            pipelineRepository.deletePipeline(pipelineId)
            _pipelines.value = _pipelines.value.filter { it.id != pipelineId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Execute a pipeline by ID.
     */
    suspend fun executePipeline(
        pipelineId: String,
        input: PipelineData
    ): Flow<PipelineEvent> {
        val definition = _pipelines.value.find { it.id == pipelineId }
            ?: throw IllegalArgumentException("Pipeline not found: $pipelineId")
        
        return executePipeline(definition, input)
    }
    
    /**
     * Execute a pipeline definition.
     */
    suspend fun executePipeline(
        definition: PluginPipelineDefinition,
        input: PipelineData
    ): Flow<PipelineEvent> {
        val pipeline = PluginPipeline.builder(pluginResolver)
            .id(definition.id)
            .name(definition.name)
            .description(definition.description)
            .inputType(definition.inputType)
            .outputType(definition.outputType)
            .apply {
                definition.steps.forEach { step ->
                    addStep(
                        pluginId = step.pluginId,
                        config = step.config,
                        timeoutMs = step.timeoutMs,
                        retryCount = step.retryCount,
                        skipOnError = step.skipOnError,
                        condition = step.condition
                    )
                }
            }
            .build()
        
        // Update active pipelines state
        _activePipelines.value = _activePipelines.value + (definition.id to PipelineExecutionState(
            pipelineId = definition.id,
            currentStepIndex = 0,
            totalSteps = definition.steps.size,
            status = PipelineStatus.RUNNING,
            startTime = currentTimeToLong()
        ))
        
        // Execute in background and return events flow
        scope.launch {
            pipeline.execute(input)
            _activePipelines.value = _activePipelines.value - definition.id
        }
        
        return pipeline.events
    }
    
    /**
     * Cancel a running pipeline.
     */
    fun cancelPipeline(pipelineId: String) {
        // Pipeline cancellation is handled through the pipeline instance
        _activePipelines.value = _activePipelines.value.mapValues { (id, state) ->
            if (id == pipelineId) state.copy(status = PipelineStatus.CANCELLED)
            else state
        }
    }
    
    /**
     * Get predefined pipeline templates.
     */
    fun getPipelineTemplates(): List<PipelineTemplate> = listOf(
        PipelineTemplate(
            id = "translate-tts",
            name = "Translate & Read Aloud",
            description = "Translate text and convert to speech",
            steps = listOf("translation", "tts"),
            inputType = PipelineDataType.TEXT,
            outputType = PipelineDataType.AUDIO
        ),
        PipelineTemplate(
            id = "summarize-translate",
            name = "Summarize & Translate",
            description = "Summarize content and translate to target language",
            steps = listOf("ai-summarize", "translation"),
            inputType = PipelineDataType.TEXT,
            outputType = PipelineDataType.TEXT
        ),
        PipelineTemplate(
            id = "analyze-characters",
            name = "Character Analysis",
            description = "Extract and analyze characters from text",
            steps = listOf("ai-character-analysis"),
            inputType = PipelineDataType.BOOK_CONTENT,
            outputType = PipelineDataType.STRUCTURED_DATA
        )
    )
}

/**
 * Template for creating pipelines.
 */
data class PipelineTemplate(
    val id: String,
    val name: String,
    val description: String,
    val steps: List<String>,
    val inputType: PipelineDataType,
    val outputType: PipelineDataType
)

/**
 * Repository interface for pipeline persistence.
 */
interface PipelineRepository {
    suspend fun getAllPipelines(): List<PluginPipelineDefinition>
    suspend fun getPipeline(id: String): PluginPipelineDefinition?
    suspend fun savePipeline(definition: PluginPipelineDefinition)
    suspend fun deletePipeline(id: String)
    suspend fun getPublicPipelines(): List<PluginPipelineDefinition>
    suspend fun getPipelinesByAuthor(authorId: String): List<PluginPipelineDefinition>
}
