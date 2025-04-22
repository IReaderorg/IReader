package ireader.presentation.ui.settings.general

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.presentation.ui.core.viewmodel.BaseViewModel

class TranslationSettingsViewModel(
    private val readerPreferences: ReaderPreferences,
    val translationEnginesManager: TranslationEnginesManager
) : BaseViewModel() {

    val translatorEngine = readerPreferences.translatorEngine().asState()
    val openAIApiKey = readerPreferences.openAIApiKey().asState()
    val deepSeekApiKey = readerPreferences.deepSeekApiKey().asState()
    val translatorContentType = readerPreferences.translatorContentType().asState()
    val translatorToneType = readerPreferences.translatorToneType().asState()
    val translatorPreserveStyle = readerPreferences.translatorPreserveStyle().asState()
    val ollamaUrl = readerPreferences.ollamaServerUrl().asState()
    val ollamaModel = readerPreferences.ollamaModel().asState()
    
    fun updateTranslatorEngine(value: Long) {
        translatorEngine.value = value
    }
    
    fun updateOpenAIApiKey(value: String) {
        openAIApiKey.value = value
    }
    
    fun updateDeepSeekApiKey(value: String) {
        deepSeekApiKey.value = value
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
} 