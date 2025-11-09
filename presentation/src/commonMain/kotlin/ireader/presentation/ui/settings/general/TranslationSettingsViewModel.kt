package ireader.presentation.ui.settings.general

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslationContext
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

sealed class TestConnectionState {
    object Idle : TestConnectionState()
    object Testing : TestConnectionState()
    data class Success(val message: String) : TestConnectionState()
    data class Error(val message: String) : TestConnectionState()
}

class TranslationSettingsViewModel(
    private val readerPreferences: ReaderPreferences,
    val translationEnginesManager: TranslationEnginesManager
) : BaseViewModel() {

    val translatorEngine = readerPreferences.translatorEngine().asState()
    val openAIApiKey = readerPreferences.openAIApiKey().asState()
    val deepSeekApiKey = readerPreferences.deepSeekApiKey().asState()
    val geminiApiKey = readerPreferences.geminiApiKey().asState()
    val geminiModel = readerPreferences.geminiModel().asState()
    val translatorContentType = readerPreferences.translatorContentType().asState()
    val translatorToneType = readerPreferences.translatorToneType().asState()
    val translatorPreserveStyle = readerPreferences.translatorPreserveStyle().asState()
    val ollamaUrl = readerPreferences.ollamaServerUrl().asState()
    val ollamaModel = readerPreferences.ollamaModel().asState()
    
    var testConnectionState by mutableStateOf<TestConnectionState>(TestConnectionState.Idle)
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
                        val message = when (error) {
                            is UiText.ExceptionString -> error.e.message ?: "Unknown error"
                            is UiText.MStringResource -> error.toString()
                            else -> error.toString()
                        }
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
} 