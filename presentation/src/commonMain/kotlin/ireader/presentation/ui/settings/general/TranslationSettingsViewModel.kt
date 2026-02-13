package ireader.presentation.ui.settings.general

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.http.HttpClients
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslationContext
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.i18n.LocalizeHelper
import ireader.i18n.asString
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

sealed class TestConnectionState {
    object Idle : TestConnectionState()
    object Testing : TestConnectionState()
    data class Success(val message: String) : TestConnectionState()
    data class Error(val message: String) : TestConnectionState()
}

sealed class MlKitInitState {
    object Idle : MlKitInitState()
    object Initializing : MlKitInitState()
    data class Success(val message: String) : MlKitInitState()
    data class Error(val message: String) : MlKitInitState()
}

class TranslationSettingsViewModel(
    private val readerPreferences: ReaderPreferences,
    val translationEnginesManager: TranslationEnginesManager,
    private val communityPreferences: ireader.domain.community.CommunityPreferences? = null,
    private val localizeHelper: LocalizeHelper,
    private val httpClient: HttpClients
) : BaseViewModel() {

    val translatorEngine = readerPreferences.translatorEngine().asState()
    val openAIApiKey = readerPreferences.openAIApiKey().asState()
    val deepSeekApiKey = readerPreferences.deepSeekApiKey().asState()
    val geminiApiKey = readerPreferences.geminiApiKey().asState()
    val geminiModel = readerPreferences.geminiModel().asState()
    val openRouterApiKey = readerPreferences.openRouterApiKey().asState()
    val openRouterModel = readerPreferences.openRouterModel().asState()
    val nvidiaApiKey = readerPreferences.nvidiaApiKey().asState()
    val nvidiaModel = readerPreferences.nvidiaModel().asState()
    val translatorContentType = readerPreferences.translatorContentType().asState()
    val translatorToneType = readerPreferences.translatorToneType().asState()
    val translatorPreserveStyle = readerPreferences.translatorPreserveStyle().asState()
    val ollamaUrl = readerPreferences.ollamaServerUrl().asState()
    val ollamaModel = readerPreferences.ollamaModel().asState()
    val translationCustomPrompt = readerPreferences.translationCustomPrompt().asState()
    
    // Community sharing preferences
    val autoShareTranslations = communityPreferences?.autoShareTranslations()?.asState() 
        ?: mutableStateOf(false)
    val contributorName = communityPreferences?.contributorName()?.asState()
        ?: mutableStateOf("")
    
    fun setContributorName(name: String) {
        contributorName.value = name
    }
    
    var testConnectionState by mutableStateOf<TestConnectionState>(TestConnectionState.Idle)
        private set
    
    // Google ML Kit initialization state
    var mlKitInitState by mutableStateOf<MlKitInitState>(MlKitInitState.Idle)
        private set
    
    var mlKitInitProgress by mutableStateOf(0)
        private set
    
    // Gemini model refresh state
    var geminiModels by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set
    
    var isRefreshingModels by mutableStateOf(false)
        private set
    
    var modelRefreshMessage by mutableStateOf<String?>(null)
        private set
    
    // OpenRouter model state
    var openRouterModels by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set
    
    var isLoadingGeminiModels by mutableStateOf(false)
        private set
    
    var isLoadingOpenRouterModels by mutableStateOf(false)
        private set
    
    // NVIDIA model state
    var nvidiaModels by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set
    
    var isLoadingNvidiaModels by mutableStateOf(false)
        private set
    
    fun updateTranslatorEngine(value: Long) {
        translatorEngine.value = value
    }
    
    fun updateOpenAIApiKey(value: String) {
        openAIApiKey.value = value
    }
    
    fun updateDeepSeekApiKey(value: String) {
        deepSeekApiKey.value = value
    }
    
    fun updateGeminiApiKey(value: String) {
        geminiApiKey.value = value
    }
    
    fun updateGeminiModel(value: String) {
        geminiModel.value = value
    }
    
    fun updateOpenRouterApiKey(value: String) {
        openRouterApiKey.value = value
    }
    
    fun updateOpenRouterModel(value: String) {
        openRouterModel.value = value
    }
    
    fun updateNvidiaApiKey(value: String) {
        nvidiaApiKey.value = value
    }
    
    fun updateNvidiaModel(value: String) {
        nvidiaModel.value = value
    }
    
    fun updateTranslatorContentType(value: Int) {
        translatorContentType.value = value
    }
    
    fun updateTranslatorToneType(value: Int) {
        translatorToneType.value = value
    }
    
    fun updateTranslatorPreserveStyle(value: Boolean) {
        translatorPreserveStyle.value = value
    }
    
    fun updateOllamaUrl(value: String) {
        ollamaUrl.value = value
    }
    
    fun updateOllamaModel(value: String) {
        ollamaModel.value = value
    }
    
    fun updateTranslationCustomPrompt(value: String) {
        translationCustomPrompt.value = value
    }
    
    /**
     * Test the connection to the currently selected translation API
     */
    fun testConnection() {
        scope.launch {
            testConnectionState = TestConnectionState.Testing
            
            try {
                val engine = translationEnginesManager.get()
                
                // Validate API key is set if required
                if (engine.requiresApiKey) {
                    val apiKey = when (engine.id) {
                        2L -> openAIApiKey.value // OpenAI
                        3L -> deepSeekApiKey.value // DeepSeek
                        8L -> geminiApiKey.value // Gemini
                        9L -> openRouterApiKey.value // OpenRouter
                        else -> ""
                    }
                    
                    if (apiKey.isBlank()) {
                        testConnectionState = TestConnectionState.Error(
                            "API key is required for ${engine.engineName}. Please enter your API key first."
                        )
                        return@launch
                    }
                }
                
                // For Ollama, validate URL and model
                if (engine.id == 5L) {
                    if (ollamaUrl.value.isBlank()) {
                        testConnectionState = TestConnectionState.Error(
                            "Ollama URL is required. Please enter the server URL."
                        )
                        return@launch
                    }
                    if (ollamaModel.value.isBlank()) {
                        testConnectionState = TestConnectionState.Error(
                            "Ollama model is required. Please enter the model name."
                        )
                        return@launch
                    }
                }
                
                // Perform a simple test translation
                val testText = listOf("Hello")
                val context = TranslationContext(
                    contentType = ContentType.GENERAL,
                    toneType = ToneType.NEUTRAL,
                    preserveStyle = false
                )
                
                var testSuccess = false
                var errorMessage = ""
                
                translationEnginesManager.translateWithContext(
                    texts = testText,
                    source = "en",
                    target = "es",
                    contentType = context.contentType,
                    toneType = context.toneType,
                    preserveStyle = context.preserveStyle,
                    onProgress = { },
                    onSuccess = { result ->
                        if (result.isNotEmpty()) {
                            testSuccess = true
                            testConnectionState = TestConnectionState.Success(
                                "Connection successful! Translation engine is working correctly."
                            )
                        } else {
                            testConnectionState = TestConnectionState.Error(
                                "Connection test failed: Empty response received."
                            )
                        }
                    },
                    onError = { error ->
                        val message = error.asString(localizeHelper)
                        testConnectionState = TestConnectionState.Error(
                            "Connection test failed: $message"
                        )
                    }
                )
                
                // If no callback was triggered, it's an error
                if (!testSuccess && testConnectionState is TestConnectionState.Testing) {
                    testConnectionState = TestConnectionState.Error(
                        "Connection test failed: No response received."
                    )
                }
                
            } catch (e: Exception) {
                testConnectionState = TestConnectionState.Error(
                    "Connection test failed: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun resetTestConnectionState() {
        testConnectionState = TestConnectionState.Idle
    }
    
    /**
     * Initialize Google ML Kit translation engine by downloading language models
     * @param sourceLanguage Source language code (e.g., "en")
     * @param targetLanguage Target language code (e.g., "es")
     */
    fun initializeGoogleMlKit(sourceLanguage: String, targetLanguage: String) {
        scope.launch {
            mlKitInitState = MlKitInitState.Initializing
            mlKitInitProgress = 0
            
            try {
                // Get the Google ML Kit engine (id = 0)
                val engines = translationEnginesManager.getAvailableEngines()
                val mlKitEngineSource = engines.find { source ->
                    when (source) {
                        is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn -> 
                            source.engine.id == 0L // Google ML Kit engine ID
                        else -> false
                    }
                }
                
                if (mlKitEngineSource == null) {
                    mlKitInitState = MlKitInitState.Error("Google ML Kit engine not found")
                    return@launch
                }
                
                val mlKitEngine = when (mlKitEngineSource) {
                    is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn -> 
                        mlKitEngineSource.engine
                    else -> null
                }
                
                if (mlKitEngine == null) {
                    mlKitInitState = MlKitInitState.Error("Unable to access Google ML Kit engine")
                    return@launch
                }
                
                mlKitEngine.initialize(
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    onProgress = { progress ->
                        mlKitInitProgress = progress
                    },
                    onSuccess = { message ->
                        mlKitInitState = MlKitInitState.Success(message)
                    },
                    onError = { error ->
                        val message = error.asString(localizeHelper)
                        mlKitInitState = MlKitInitState.Error(message)
                    }
                )
            } catch (e: Exception) {
                mlKitInitState = MlKitInitState.Error("Initialization failed: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    fun resetMlKitInitState() {
        mlKitInitState = MlKitInitState.Idle
        mlKitInitProgress = 0
    }
    
    /**
     * Refresh available Gemini models from the API
     * Requirements: 6.2, 6.3, 6.4
     */
    fun refreshGeminiModels() {
        scope.launch {
            isRefreshingModels = true
            modelRefreshMessage = null
            
            try {
                // Get the Gemini engine
                val engines = translationEnginesManager.getAvailableEngines()
                val geminiEngineSource = engines.find { source ->
                    when (source) {
                        is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn -> 
                            source.engine.id == 8L // Gemini engine ID
                        else -> false
                    }
                }
                
                if (geminiEngineSource == null) {
                    modelRefreshMessage = "Gemini engine not found. Please restart the app."
                    isRefreshingModels = false
                    return@launch
                }
                
                // Validate API key
                if (geminiApiKey.value.isBlank()) {
                    modelRefreshMessage = "Please enter your Gemini API key first"
                    isRefreshingModels = false
                    return@launch
                }
                
                // Cast to get the engine
                val geminiEngine = when (geminiEngineSource) {
                    is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn -> 
                        geminiEngineSource.engine as? ireader.domain.usecases.translate.WebscrapingTranslateEngine
                    else -> null
                }
                
                if (geminiEngine == null) {
                    modelRefreshMessage = "Unable to access Gemini engine"
                    isRefreshingModels = false
                    return@launch
                }
                
                // Fetch models from API
                val result = geminiEngine.fetchAvailableGeminiModels(geminiApiKey.value)
                
                if (result.isSuccess) {
                    val models = result.getOrNull()
                    if (models.isNullOrEmpty()) {
                        modelRefreshMessage = "No models found. Please check your API key."
                        geminiModels = emptyList()
                    } else {
                        geminiModels = models
                        modelRefreshMessage = "Successfully loaded ${models.size} model(s)"
                    }
                } else {
                    val error = result.exceptionOrNull()
                    modelRefreshMessage = when {
                        error?.message?.contains("401") == true || 
                        error?.message?.contains("API key") == true -> 
                            "Invalid API key. Please check your key and try again."
                        error?.message?.contains("403") == true -> 
                            "Access forbidden. Please check your API key permissions."
                        error?.message?.contains("429") == true -> 
                            "Rate limit exceeded. Please try again later."
                        error?.message?.contains("timeout") == true -> 
                            "Request timed out. Please check your internet connection."
                        else -> 
                            "Failed to fetch models: ${error?.message ?: "Unknown error"}"
                    }
                    geminiModels = emptyList()
                }
            } catch (e: Exception) {
                modelRefreshMessage = "Error: ${e.message ?: "Unknown error occurred"}"
                geminiModels = emptyList()
            } finally {
                isRefreshingModels = false
            }
        }
    }
    
    /**
     * Load cached Gemini models
     * Requirements: 6.4
     */
    fun loadCachedGeminiModels() {
        geminiModels = ireader.domain.usecases.translate.WebscrapingTranslateEngine.AVAILABLE_GEMINI_MODELS
    }
    
    /**
     * Load Gemini models - wrapper for refreshGeminiModels
     */
    fun loadGeminiModels() {
        refreshGeminiModels()
    }
    
    /**
     * Load OpenRouter models from API
     */
    fun loadOpenRouterModels() {
        scope.launch {
            isLoadingOpenRouterModels = true
            modelRefreshMessage = null
            
            try {
                // Get the OpenRouter engine
                val engines = translationEnginesManager.getAvailableEngines()
                val openRouterEngineSource = engines.find { source ->
                    when (source) {
                        is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn ->
                            source.engine.id == 9L // OpenRouter engine ID
                        else -> false
                    }
                }
                
                val openRouterEngine = when (openRouterEngineSource) {
                    is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn ->
                        openRouterEngineSource.engine as? ireader.domain.usecases.translate.OpenRouterTranslateEngine
                    else -> null
                }
                
                if (openRouterEngine != null) {
                    val result = openRouterEngine.fetchAvailableModels()
                    
                    if (result.isSuccess) {
                        val models = result.getOrNull() ?: emptyList()
                        openRouterModels = models
                        modelRefreshMessage = if (models.isNotEmpty()) {
                            "Loaded ${models.size} models successfully"
                        } else {
                            "No models available"
                        }
                    } else {
                        val error = result.exceptionOrNull()
                        modelRefreshMessage = when {
                            error?.message?.contains("401") == true || 
                            error?.message?.contains("API key") == true -> 
                                "Invalid API key. Please check your key and try again."
                            error?.message?.contains("403") == true -> 
                                "Access forbidden. Please check your API key permissions."
                            error?.message?.contains("429") == true -> 
                                "Rate limit exceeded. Please try again later."
                            error?.message?.contains("timeout") == true -> 
                                "Request timed out. Please check your internet connection."
                            else -> 
                                "Failed to fetch models: ${error?.message ?: "Unknown error"}"
                        }
                        openRouterModels = emptyList()
                    }
                } else {
                    // Use static models if engine not available
                    openRouterModels = ireader.domain.usecases.translate.OpenRouterTranslateEngine(
                        httpClient,
                        readerPreferences
                    ).availableModels
                    modelRefreshMessage = "Using default model list"
                }
            } catch (e: Exception) {
                modelRefreshMessage = "Error: ${e.message ?: "Unknown error occurred"}"
                openRouterModels = emptyList()
            } finally {
                isLoadingOpenRouterModels = false
            }
        }
    }
    
    /**
     * Load cached OpenRouter models (static list)
     */
    fun loadCachedOpenRouterModels() {
        openRouterModels = ireader.domain.usecases.translate.OpenRouterTranslateEngine(
            httpClient,
            readerPreferences
        ).availableModels
    }
    
    /**
     * Load NVIDIA models dynamically from API
     */
    fun loadNvidiaModels() {
        scope.launch {
            isLoadingNvidiaModels = true
            modelRefreshMessage = null
            
            try {
                // Get the NVIDIA engine
                val engines = translationEnginesManager.getAvailableEngines()
                val nvidiaEngineSource = engines.find { source ->
                    when (source) {
                        is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn -> 
                            source.engine.id == 10L // NVIDIA engine ID
                        else -> false
                    }
                }
                
                if (nvidiaEngineSource == null) {
                    modelRefreshMessage = "NVIDIA engine not found. Please restart the app."
                    isLoadingNvidiaModels = false
                    return@launch
                }
                
                // Validate API key
                if (nvidiaApiKey.value.isBlank()) {
                    modelRefreshMessage = "Please enter your NVIDIA API key first"
                    isLoadingNvidiaModels = false
                    return@launch
                }
                
                // Cast to get the engine
                val nvidiaEngine = when (nvidiaEngineSource) {
                    is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn -> 
                        nvidiaEngineSource.engine as? ireader.domain.usecases.translate.NvidiaTranslateEngine
                    else -> null
                }
                
                if (nvidiaEngine == null) {
                    modelRefreshMessage = "Unable to access NVIDIA engine"
                    isLoadingNvidiaModels = false
                    return@launch
                }
                
                // Fetch models from API
                val result = nvidiaEngine.fetchAvailableModels()
                
                if (result.isSuccess) {
                    val models = result.getOrNull()
                    if (models.isNullOrEmpty()) {
                        modelRefreshMessage = "No models found. Please check your API key."
                        nvidiaModels = emptyList()
                    } else {
                        nvidiaModels = models
                        modelRefreshMessage = "Successfully loaded ${models.size} model(s)"
                    }
                } else {
                    val error = result.exceptionOrNull()
                    modelRefreshMessage = when {
                        error?.message?.contains("401") == true || 
                        error?.message?.contains("API key") == true -> 
                            "Invalid API key. Please check your key and try again."
                        error?.message?.contains("403") == true -> 
                            "Access forbidden. Please check your API key permissions."
                        error?.message?.contains("429") == true -> 
                            "Rate limit exceeded. Please try again later."
                        error?.message?.contains("timeout") == true -> 
                            "Request timed out. Please check your internet connection."
                        else -> 
                            "Failed to fetch models: ${error?.message ?: "Unknown error"}"
                    }
                    nvidiaModels = emptyList()
                }
            } catch (e: Exception) {
                modelRefreshMessage = "Error: ${e.message ?: "Unknown error occurred"}"
                nvidiaModels = emptyList()
            } finally {
                isLoadingNvidiaModels = false
            }
        }
    }
    
    /**
     * Load cached NVIDIA models (static list)
     */
    fun loadCachedNvidiaModels() {
        nvidiaModels = ireader.domain.usecases.translate.NvidiaTranslateEngine(
            httpClient,
            readerPreferences
        ).availableModels
    }
    
    fun resetModelRefreshMessage() {
        modelRefreshMessage = null
    }
    
    /**
     * Refresh available translation engines
     * This reloads the list of available engines including plugins
     */
    fun refreshEngines() {
        scope.launch {
            // Trigger a refresh of the translation engines manager
            // This will reload built-in engines and discover any new plugins
            translationEnginesManager.refreshEngines()
        }
    }
} 
