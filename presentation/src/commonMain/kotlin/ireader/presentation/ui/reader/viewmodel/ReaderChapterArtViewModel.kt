package ireader.presentation.ui.reader.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.data.characterart.ChapterArtPromptGenerator
import ireader.data.characterart.GeneratedPromptResult
import ireader.data.characterart.PromptFocus
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Sub-ViewModel for chapter art generation functionality.
 * Handles generating image prompts from chapter text using Gemini AI.
 */
@Stable
class ReaderChapterArtViewModel(
    private val promptGenerator: ChapterArtPromptGenerator?,
    private val readerPreferences: ReaderPreferences,
    private val scope: CoroutineScope
) {
    // State
    var showFocusDialog by mutableStateOf(false)
        private set
    var isGenerating by mutableStateOf(false)
        private set
    var generatedResult by mutableStateOf<GeneratedPromptResult?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    
    // Cached chapter data for generation
    private var cachedBookTitle: String = ""
    private var cachedChapterTitle: String = ""
    private var cachedChapterText: String = ""
    
    /**
     * Show the focus selection dialog
     */
    fun showChapterArtDialog(
        bookTitle: String,
        chapterTitle: String,
        chapterContent: List<Page>
    ) {
        cachedBookTitle = bookTitle
        cachedChapterTitle = chapterTitle
        cachedChapterText = extractTextFromContent(chapterContent)
        showFocusDialog = true
        error = null
        generatedResult = null
    }
    
    /**
     * Dismiss all dialogs
     */
    fun dismissDialogs() {
        showFocusDialog = false
        isGenerating = false
        error = null
    }
    
    /**
     * Clear the generated result
     */
    fun clearResult() {
        generatedResult = null
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        error = null
    }
    
    /**
     * Generate image prompt with selected focus
     */
    fun generatePrompt(focus: PromptFocus) {
        showFocusDialog = false
        
        val generator = promptGenerator
        if (generator == null) {
            error = "Chapter art generator not available"
            return
        }
        
        val apiKey = readerPreferences.geminiApiKey().get()
        if (apiKey.isBlank()) {
            error = "Please set your Gemini API key in settings"
            return
        }
        
        if (cachedChapterText.length < 100) {
            error = "Chapter text is too short to analyze"
            return
        }
        
        isGenerating = true
        error = null
        
        scope.launch {
            generator.generateImagePrompt(
                apiKey = apiKey,
                chapterText = cachedChapterText,
                bookTitle = cachedBookTitle,
                chapterTitle = cachedChapterTitle,
                preferredFocus = focus
            ).onSuccess { result ->
                generatedResult = result
                isGenerating = false
            }.onFailure { e ->
                error = e.message ?: "Failed to generate prompt"
                isGenerating = false
            }
        }
    }
    
    /**
     * Retry generation with the same focus
     */
    fun retryGeneration() {
        error = null
        showFocusDialog = true
    }
    
    /**
     * Get the generated prompt for navigation
     */
    fun getPromptForUpload(): Triple<String, String, String>? {
        val result = generatedResult ?: return null
        return Triple(result.bookTitle, result.chapterTitle, result.imagePrompt)
    }
    
    /**
     * Extract plain text from chapter content
     */
    private fun extractTextFromContent(content: List<Page>): String {
        return content
            .filterIsInstance<Text>()
            .joinToString("\n\n") { it.text }
    }
}
