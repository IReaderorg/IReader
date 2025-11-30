package ireader.presentation.ui.reader.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.source.model.Page
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.TranslatedChapter

data class ChapterTranslationState(
    val isShowingTranslation: Boolean = false,
    val hasTranslation: Boolean = false,
    val translatedChapter: TranslatedChapter? = null,
    val isTranslating: Boolean = false,
    val translationProgress: Float = 0f,
    val translationError: String? = null,
    val glossaryEntries: List<Glossary> = emptyList(),
    val showGlossaryDialog: Boolean = false
)

class TranslationStateHolder {
    var isShowingTranslation by mutableStateOf(false)
    var hasTranslation by mutableStateOf(false)
    var translatedChapter by mutableStateOf<TranslatedChapter?>(null)
    var translatedContent by mutableStateOf<List<Page>>(emptyList())
    var isTranslating by mutableStateOf(false)
    var translationProgress by mutableStateOf(0f)
    var translationError by mutableStateOf<String?>(null)
    var glossaryEntries by mutableStateOf<List<Glossary>>(emptyList())
    var showGlossaryDialog by mutableStateOf(false)
    
    /**
     * Update translation state atomically to prevent race conditions.
     * Sets translatedContent first, then hasTranslation to ensure
     * content is available when hasTranslation becomes true.
     */
    @Synchronized
    fun setTranslation(content: List<Page>, error: String? = null) {
        translationError = error
        translatedContent = content
        hasTranslation = content.isNotEmpty()
    }
    
    /**
     * Clear translation state atomically.
     * Sets hasTranslation to false first to prevent reading stale content.
     */
    @Synchronized
    fun clearTranslation(error: String? = null) {
        hasTranslation = false
        translatedContent = emptyList()
        translationError = error
    }
    
    fun reset() {
        isShowingTranslation = false
        hasTranslation = false
        translatedChapter = null
        translatedContent = emptyList()
        isTranslating = false
        translationProgress = 0f
        translationError = null
    }
}
