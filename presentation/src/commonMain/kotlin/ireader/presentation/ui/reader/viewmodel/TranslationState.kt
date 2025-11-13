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

class TranslationStateImpl {
    var isShowingTranslation by mutableStateOf(false)
    var hasTranslation by mutableStateOf(false)
    var translatedChapter by mutableStateOf<TranslatedChapter?>(null)
    var isTranslating by mutableStateOf(false)
    var translationProgress by mutableStateOf(0f)
    var translationError by mutableStateOf<String?>(null)
    var glossaryEntries by mutableStateOf<List<Glossary>>(emptyList())
    var showGlossaryDialog by mutableStateOf(false)
    
    fun reset() {
        isShowingTranslation = false
        hasTranslation = false
        translatedChapter = null
        isTranslating = false
        translationProgress = 0f
        translationError = null
    }
}
