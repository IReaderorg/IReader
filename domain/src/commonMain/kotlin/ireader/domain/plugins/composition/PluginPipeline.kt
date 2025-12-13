package ireader.domain.plugins.composition

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withTimeout
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Executable plugin pipeline that chains multiple plugins together.
 */
class PluginPipeline private constructor(
    val definition: PluginPipelineDefinition,
    private val pluginResolver: PluginResolver
) {
    private val _events = MutableSharedFlow<PipelineEvent>(replay = 0)
    val events: Flow<PipelineEvent> = _events.asSharedFlow()
    
    @Volatile
    private var isCancelled = false
    
    /**
     * Execute the pipeline with the given input.
     */
    suspend fun execute(input: PipelineData): PipelineResult {
        isCancelled = false
        val startTime = currentTimeToLong()
        
        _events.emit(PipelineEvent.Started(definition.id, definition.steps.size))
        
        var currentData = input
        val stepResults = mutableListOf<StepExecutionResult>()
        
        for ((index, stepConfig) in definition.steps.withIndex()) {
            if (isCancelled) {
                _events.emit(PipelineEvent.Cancelled(definition.id, index))
                return PipelineResult.Error(PipelineError.Cancelled(index), currentData)
            }
            
            // Check step condition
            if (!shouldExecuteStep(stepConfig, stepResults.lastOrNull())) {
                stepResults.add(
                    StepExecutionResult(
                        stepIndex = index,
                        pluginId = stepConfig.pluginId,
                        status = StepStatus.SKIPPED,
                        startTime = currentTimeToLong(),
                        endTime = currentTimeToLong()
                    )
                )
                continue
            }
            
            val stepStartTime = currentTimeToLong()
            _events.emit(PipelineEvent.StepStarted(index, stepConfig.pluginId))
            
            val plugin = pluginResolver.resolve(stepConfig.pluginId)
            if (plugin == null) {
                val error = PipelineError.PluginNotFound(stepConfig.pluginId)
                _events.emit(PipelineEvent.Failed(definition.id, error, index))
                return PipelineResult.Error(error, currentData)
            }
            
            // Validate input type
            if (!plugin.canProcess(currentData.type)) {
                val error = PipelineError.TypeMismatch(
                    expected = currentData.type,
                    actual = plugin.getOutputType()
                )
                if (stepConfig.skipOnError) {
                    stepResults.add(
                        StepExecutionResult(
                            stepIndex = index,
                            pluginId = stepConfig.pluginId,
                            status = StepStatus.SKIPPED,
                            startTime = stepStartTime,
                            endTime = currentTimeToLong(),
                            errorMessage = "Type mismatch"
                        )
                    )
                    continue
                }
                _events.emit(PipelineEvent.Failed(definition.id, error, index))
                return PipelineResult.Error(error, currentData)
            }
            
            // Execute step with retry logic
            var lastError: PipelineError? = null
            var result: PipelineResult? = null
            
            for (attempt in 0..stepConfig.retryCount) {
                try {
                    result = withTimeout(stepConfig.timeoutMs) {
                        plugin.process(currentData.copy(metadata = currentData.metadata + stepConfig.config))
                    }
                    
                    if (result.isSuccess()) break
                    
                    lastError = when (result) {
                        is PipelineResult.Error -> result.error
                        else -> null
                    }
                } catch (e: CancellationException) {
                    if (isCancelled) {
                        _events.emit(PipelineEvent.Cancelled(definition.id, index))
                        return PipelineResult.Error(PipelineError.Cancelled(index), currentData)
                    }
                    lastError = PipelineError.Timeout(index, stepConfig.timeoutMs)
                } catch (e: Exception) {
                    lastError = PipelineError.PluginError(stepConfig.pluginId, e.message ?: "Unknown error")
                }
            }
            
            val stepEndTime = currentTimeToLong()
            
            when (result) {
                is PipelineResult.Success -> {
                    currentData = result.data
                    stepResults.add(
                        StepExecutionResult(
                            stepIndex = index,
                            pluginId = stepConfig.pluginId,
                            status = StepStatus.SUCCESS,
                            startTime = stepStartTime,
                            endTime = stepEndTime
                        )
                    )
                    _events.emit(PipelineEvent.StepCompleted(index, stepConfig.pluginId, result))
                }
                is PipelineResult.Skipped -> {
                    currentData = result.data
                    stepResults.add(
                        StepExecutionResult(
                            stepIndex = index,
                            pluginId = stepConfig.pluginId,
                            status = StepStatus.SKIPPED,
                            startTime = stepStartTime,
                            endTime = stepEndTime
                        )
                    )
                    _events.emit(PipelineEvent.StepCompleted(index, stepConfig.pluginId, result))
                }
                is PipelineResult.Error, null -> {
                    val error = lastError ?: PipelineError.PluginError(stepConfig.pluginId, "Unknown error")
                    
                    if (stepConfig.skipOnError) {
                        stepResults.add(
                            StepExecutionResult(
                                stepIndex = index,
                                pluginId = stepConfig.pluginId,
                                status = StepStatus.FAILED,
                                startTime = stepStartTime,
                                endTime = stepEndTime,
                                errorMessage = error.toString()
                            )
                        )
                        continue
                    }
                    
                    _events.emit(PipelineEvent.Failed(definition.id, error, index))
                    return PipelineResult.Error(error, currentData)
                }
            }
        }
        
        val finalResult = PipelineResult.Success(currentData)
        _events.emit(PipelineEvent.Completed(definition.id, finalResult))
        return finalResult
    }
    
    /**
     * Cancel the pipeline execution.
     */
    fun cancel() {
        isCancelled = true
    }
    
    private fun shouldExecuteStep(
        config: PipelineStepConfig,
        previousResult: StepExecutionResult?
    ): Boolean {
        return when (val condition = config.condition) {
            null, StepCondition.Always -> true
            StepCondition.OnSuccess -> previousResult?.status == StepStatus.SUCCESS
            StepCondition.OnError -> previousResult?.status == StepStatus.FAILED
            is StepCondition.MetadataEquals -> true // Would need access to current data
            is StepCondition.DataTypeIs -> true // Would need access to current data
        }
    }
    
    companion object {
        fun builder(pluginResolver: PluginResolver) = Builder(pluginResolver)
    }
    
    class Builder(private val pluginResolver: PluginResolver) {
        private var id: String = ""
        private var name: String = ""
        private var description: String = ""
        private val steps = mutableListOf<PipelineStepConfig>()
        private var inputType: PipelineDataType = PipelineDataType.TEXT
        private var outputType: PipelineDataType = PipelineDataType.TEXT
        private var isPublic: Boolean = false
        private var authorId: String? = null
        private var tags: List<String> = emptyList()
        
        fun id(id: String) = apply { this.id = id }
        fun name(name: String) = apply { this.name = name }
        fun description(description: String) = apply { this.description = description }
        fun inputType(type: PipelineDataType) = apply { this.inputType = type }
        fun outputType(type: PipelineDataType) = apply { this.outputType = type }
        fun isPublic(public: Boolean) = apply { this.isPublic = public }
        fun authorId(id: String?) = apply { this.authorId = id }
        fun tags(tags: List<String>) = apply { this.tags = tags }
        
        fun addStep(
            pluginId: String,
            config: Map<String, String> = emptyMap(),
            timeoutMs: Long = 30000,
            retryCount: Int = 0,
            skipOnError: Boolean = false,
            condition: StepCondition? = null
        ) = apply {
            steps.add(
                PipelineStepConfig(
                    pluginId = pluginId,
                    config = config,
                    timeoutMs = timeoutMs,
                    retryCount = retryCount,
                    skipOnError = skipOnError,
                    condition = condition
                )
            )
        }
        
        fun build(): PluginPipeline {
            require(id.isNotBlank()) { "Pipeline ID is required" }
            require(name.isNotBlank()) { "Pipeline name is required" }
            require(steps.isNotEmpty()) { "Pipeline must have at least one step" }
            
            val now = currentTimeToLong()
            val definition = PluginPipelineDefinition(
                id = id,
                name = name,
                description = description,
                steps = steps.toList(),
                inputType = inputType,
                outputType = outputType,
                createdAt = now,
                updatedAt = now,
                isPublic = isPublic,
                authorId = authorId,
                tags = tags
            )
            
            return PluginPipeline(definition, pluginResolver)
        }
    }
}

/**
 * Interface for resolving plugins by ID.
 */
interface PluginResolver {
    fun resolve(pluginId: String): ComposablePlugin?
    fun getAvailablePlugins(): List<ComposablePlugin>
}
