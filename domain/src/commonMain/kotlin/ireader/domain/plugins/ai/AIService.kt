package ireader.domain.plugins.ai

import ireader.plugin.api.AICapability
import ireader.plugin.api.AIError
import ireader.plugin.api.AIModelInfo
import ireader.plugin.api.AIPlugin
import ireader.plugin.api.AIProviderType
import ireader.plugin.api.AIResourceUsage
import ireader.plugin.api.AIResult
import ireader.plugin.api.CharacterAnalysisOptions
import ireader.plugin.api.AICharacterInfo
import ireader.plugin.api.GenerationOptions
import ireader.plugin.api.PluginContext
import ireader.plugin.api.PluginManifest
import ireader.plugin.api.QAOptions
import ireader.plugin.api.QAResponse
import ireader.plugin.api.SummarizationOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unified AI service that manages AI plugins and provides a consistent interface.
 */
class AIService(
    private val localEngine: LocalInferenceEngine?,
    private val cloudClient: CloudAIClient?
) {
    private val _currentProvider = MutableStateFlow<AIProviderType?>(null)
    val currentProvider: StateFlow<AIProviderType?> = _currentProvider.asStateFlow()
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    private val _modelInfo = MutableStateFlow<AIModelInfo?>(null)
    val modelInfo: StateFlow<AIModelInfo?> = _modelInfo.asStateFlow()
    
    private var localConfig: LocalAIConfig? = null
    private var cloudConfig: CloudAIConfig? = null
    
    /**
     * Initialize with local AI model.
     */
    suspend fun initializeLocal(config: LocalAIConfig): AIResult<Unit> {
        if (localEngine == null) {
            return AIResult.Error(AIError.InsufficientResources("Local inference engine not available"))
        }
        
        val result = localEngine.loadModel(config)
        if (result is AIResult.Success) {
            localConfig = config
            _currentProvider.value = AIProviderType.LOCAL
            _modelInfo.value = localEngine.getModelInfo()
            _isReady.value = true
        }
        return result
    }
    
    /**
     * Initialize with cloud AI provider.
     */
    suspend fun initializeCloud(config: CloudAIConfig): AIResult<Unit> {
        if (cloudClient == null) {
            return AIResult.Error(AIError.InsufficientResources("Cloud AI client not available"))
        }
        
        val initResult = cloudClient.initialize(config)
        if (initResult is AIResult.Error) return initResult
        
        val validateResult = cloudClient.validateCredentials()
        if (validateResult is AIResult.Success) {
            cloudConfig = config
            _currentProvider.value = AIProviderType.CLOUD
            _modelInfo.value = getCloudModelInfo(config)
            _isReady.value = true
        }
        return validateResult
    }

    /**
     * Summarize text.
     */
    suspend fun summarize(
        text: String,
        options: SummarizationOptions = SummarizationOptions()
    ): AIResult<String> {
        if (!_isReady.value) {
            return AIResult.Error(AIError.ModelNotReady)
        }
        
        val prompt = AIPromptTemplates.summarization(text, options)
        return generate(prompt, GenerationOptions(maxTokens = 500))
    }
    
    /**
     * Analyze characters in text.
     */
    suspend fun analyzeCharacters(
        text: String,
        options: CharacterAnalysisOptions = CharacterAnalysisOptions()
    ): AIResult<List<AICharacterInfo>> {
        if (!_isReady.value) {
            return AIResult.Error(AIError.ModelNotReady)
        }
        
        val prompt = AIPromptTemplates.characterAnalysis(text, options)
        val result = generate(prompt, GenerationOptions(maxTokens = 1000))
        
        return result.map { response ->
            AIResponseParser.parseCharacters(response)
        }
    }
    
    /**
     * Answer a question about text.
     */
    suspend fun answerQuestion(
        context: String,
        question: String,
        options: QAOptions = QAOptions()
    ): AIResult<QAResponse> {
        if (!_isReady.value) {
            return AIResult.Error(AIError.ModelNotReady)
        }
        
        val prompt = AIPromptTemplates.questionAnswering(context, question, options)
        val result = generate(prompt, GenerationOptions(maxTokens = options.maxAnswerLength))
        
        return result.map { response ->
            AIResponseParser.parseQAResponse(response, options.includeCitations)
        }
    }
    
    /**
     * Generate text.
     */
    suspend fun generate(
        prompt: String,
        options: GenerationOptions = GenerationOptions()
    ): AIResult<String> {
        if (!_isReady.value) {
            return AIResult.Error(AIError.ModelNotReady)
        }
        
        return when (_currentProvider.value) {
            AIProviderType.LOCAL -> {
                localEngine?.generate(prompt, options)
                    ?: AIResult.Error(AIError.ModelNotReady)
            }
            AIProviderType.CLOUD -> {
                val messages = listOf(ChatMessage(MessageRole.USER, prompt))
                cloudClient?.complete(messages, options)
                    ?: AIResult.Error(AIError.ModelNotReady)
            }
            else -> AIResult.Error(AIError.ModelNotReady)
        }
    }
    
    /**
     * Generate text with streaming.
     */
    suspend fun generateStream(
        prompt: String,
        options: GenerationOptions = GenerationOptions(),
        onToken: (String) -> Unit
    ): AIResult<String> {
        if (!_isReady.value) {
            return AIResult.Error(AIError.ModelNotReady)
        }
        
        return when (_currentProvider.value) {
            AIProviderType.LOCAL -> {
                localEngine?.generateStream(prompt, options, onToken)
                    ?: AIResult.Error(AIError.ModelNotReady)
            }
            AIProviderType.CLOUD -> {
                val messages = listOf(ChatMessage(MessageRole.USER, prompt))
                cloudClient?.completeStream(messages, options, onToken)
                    ?: AIResult.Error(AIError.ModelNotReady)
            }
            else -> AIResult.Error(AIError.ModelNotReady)
        }
    }
    
    /**
     * Get embeddings for texts.
     */
    suspend fun getEmbeddings(texts: List<String>): AIResult<List<FloatArray>> {
        if (!_isReady.value) {
            return AIResult.Error(AIError.ModelNotReady)
        }
        
        return when (_currentProvider.value) {
            AIProviderType.LOCAL -> {
                localEngine?.getEmbeddings(texts)
                    ?: AIResult.Error(AIError.ModelNotReady)
            }
            AIProviderType.CLOUD -> {
                cloudClient?.getEmbeddings(texts)
                    ?: AIResult.Error(AIError.ModelNotReady)
            }
            else -> AIResult.Error(AIError.ModelNotReady)
        }
    }
    
    /**
     * Cancel ongoing operation.
     */
    fun cancel() {
        when (_currentProvider.value) {
            AIProviderType.LOCAL -> localEngine?.cancel()
            AIProviderType.CLOUD -> cloudClient?.cancel()
            else -> {}
        }
    }
    
    /**
     * Unload the current model.
     */
    suspend fun unload() {
        when (_currentProvider.value) {
            AIProviderType.LOCAL -> localEngine?.unloadModel()
            else -> {}
        }
        _isReady.value = false
        _currentProvider.value = null
        _modelInfo.value = null
    }
    
    /**
     * Get resource usage.
     */
    fun getResourceUsage(): AIResourceUsage {
        return when (_currentProvider.value) {
            AIProviderType.LOCAL -> localEngine?.getResourceUsage() ?: AIResourceUsage(0, null, null, 0, 0)
            AIProviderType.CLOUD -> {
                val stats = cloudClient?.getUsageStats()
                AIResourceUsage(
                    memoryBytes = 0,
                    gpuMemoryBytes = null,
                    cpuPercent = null,
                    tokensProcessed = stats?.totalTokensUsed ?: 0,
                    avgInferenceTimeMs = 0
                )
            }
            else -> AIResourceUsage(0, null, null, 0, 0)
        }
    }
    
    /**
     * Get available capabilities.
     */
    fun getCapabilities(): List<AICapability> {
        return _modelInfo.value?.capabilities ?: emptyList()
    }
    
    /**
     * Check if a capability is available.
     */
    fun hasCapability(capability: AICapability): Boolean {
        return getCapabilities().contains(capability)
    }
    
    private fun getCloudModelInfo(config: CloudAIConfig): AIModelInfo {
        return when (config.provider) {
            CloudProvider.OPENAI -> when {
                config.model.contains("gpt-4-turbo") -> CloudModels.OpenAI.GPT4_TURBO
                config.model.contains("gpt-4") -> CloudModels.OpenAI.GPT4
                config.model.contains("gpt-3.5") -> CloudModels.OpenAI.GPT35_TURBO
                else -> CloudModels.OpenAI.GPT35_TURBO
            }
            CloudProvider.ANTHROPIC -> when {
                config.model.contains("opus") -> CloudModels.Anthropic.CLAUDE_3_OPUS
                config.model.contains("sonnet") -> CloudModels.Anthropic.CLAUDE_3_SONNET
                config.model.contains("haiku") -> CloudModels.Anthropic.CLAUDE_3_HAIKU
                else -> CloudModels.Anthropic.CLAUDE_3_SONNET
            }
            CloudProvider.GOOGLE -> when {
                config.model.contains("vision") -> CloudModels.Google.GEMINI_PRO_VISION
                else -> CloudModels.Google.GEMINI_PRO
            }
            else -> AIModelInfo(
                name = config.model,
                version = "unknown",
                contextLength = 4096,
                provider = config.provider.name,
                capabilities = listOf(
                    AICapability.SUMMARIZATION,
                    AICapability.TEXT_GENERATION
                ),
                supportsStreaming = true
            )
        }
    }
}
