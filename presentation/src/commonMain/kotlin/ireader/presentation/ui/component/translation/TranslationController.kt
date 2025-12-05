package ireader.presentation.ui.component.translation

import androidx.compose.runtime.*
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.TranslationPreferences
import ireader.domain.services.common.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Controller for managing translation operations across all screens.
 * Provides a unified interface for translation in detail screen, reader, and TTS.
 */
@Stable
class TranslationController(
    private val translationService: TranslationService?,
    private val readerPreferences: ReaderPreferences,
    private val translationPreferences: TranslationPreferences?,
    private val scope: CoroutineScope,
    private val onShowSnackbar: (String) -> Unit
) {
    val dialogState = TranslationDialogState()
    
    // Available engines
    val availableEngines: List<TranslationEngine> = TranslationEngines.ALL
    
    /**
     * Show translation dialog for single chapter (reader/TTS)
     */
    fun showForSingleChapter(chapterId: Long, bookId: Long) {
        if (translationService == null) {
            onShowSnackbar("Translation service not available")
            return
        }
        
        dialogState.show(
            mode = TranslationMode.SINGLE_CHAPTER,
            chapterIds = listOf(chapterId),
            bookId = bookId,
            defaultEngineId = readerPreferences.translatorEngine().get(),
            defaultSourceLang = readerPreferences.translatorOriginLanguage().get(),
            defaultTargetLang = readerPreferences.translatorTargetLanguage().get()
        )
    }
    
    /**
     * Show translation dialog for TTS chapter
     */
    fun showForTTSChapter(chapterId: Long, bookId: Long) {
        if (translationService == null) {
            onShowSnackbar("Translation service not available")
            return
        }
        
        dialogState.show(
            mode = TranslationMode.TTS_CHAPTER,
            chapterIds = listOf(chapterId),
            bookId = bookId,
            defaultEngineId = readerPreferences.translatorEngine().get(),
            defaultSourceLang = readerPreferences.translatorOriginLanguage().get(),
            defaultTargetLang = readerPreferences.translatorTargetLanguage().get()
        )
    }
    
    /**
     * Show translation dialog for multiple chapters (detail screen)
     */
    fun showForMassTranslation(chapterIds: List<Long>, bookId: Long) {
        if (translationService == null) {
            onShowSnackbar("Translation service not available")
            return
        }
        
        if (chapterIds.isEmpty()) {
            onShowSnackbar("No chapters selected")
            return
        }
        
        dialogState.show(
            mode = TranslationMode.MASS_CHAPTERS,
            chapterIds = chapterIds,
            bookId = bookId,
            defaultEngineId = readerPreferences.translatorEngine().get(),
            defaultSourceLang = readerPreferences.translatorOriginLanguage().get(),
            defaultTargetLang = readerPreferences.translatorTargetLanguage().get()
        )
    }
    
    /**
     * Start translation with current dialog settings
     */
    fun translate(
        engineId: Long,
        sourceLang: String,
        targetLang: String,
        bypassWarning: Boolean
    ) {
        val service = translationService ?: return
        val bookId = dialogState.bookId ?: return
        
        // Save preferences
        readerPreferences.translatorEngine().set(engineId)
        readerPreferences.translatorOriginLanguage().set(sourceLang)
        readerPreferences.translatorTargetLanguage().set(targetLang)
        
        scope.launch {
            val result = service.queueChapters(
                bookId = bookId,
                chapterIds = dialogState.chapterIds,
                sourceLanguage = sourceLang,
                targetLanguage = targetLang,
                engineId = engineId,
                bypassWarning = bypassWarning
            )
            
            when (result) {
                is ServiceResult.Success -> handleQueueResult(result.data, engineId)
                is ServiceResult.Error -> {
                    dialogState.errorMessage = result.message
                    onShowSnackbar("Translation failed: ${result.message ?: "Unknown error"}")
                }
                else -> {}
            }
        }
    }
    
    private fun handleQueueResult(queueResult: TranslationQueueResult, engineId: Long) {
        when (queueResult) {
            is TranslationQueueResult.Success -> {
                dialogState.isTranslating = true
                dialogState.totalChapters = queueResult.queuedCount
                dialogState.completedChapters = 0
                dialogState.showWarning = false
                onShowSnackbar("${queueResult.queuedCount} chapters queued for translation")
                
                // Start observing progress
                observeProgress()
            }
            is TranslationQueueResult.RateLimitWarning -> {
                dialogState.showWarning = true
                dialogState.warningMessage = queueResult.message
                dialogState.estimatedTimeMinutes = queueResult.estimatedTime / 60000
            }
            is TranslationQueueResult.PreviousTranslationCancelled -> {
                onShowSnackbar("Previous translation cancelled. Please try again.")
            }
        }
    }
    
    private fun observeProgress() {
        scope.launch {
            translationService?.translationProgress?.collectLatest { progressMap ->
                val completed = progressMap.values.count { it.status == TranslationStatus.COMPLETED }
                val current = progressMap.values.find { 
                    it.status == TranslationStatus.TRANSLATING || 
                    it.status == TranslationStatus.DOWNLOADING_CONTENT 
                }
                
                dialogState.completedChapters = completed
                dialogState.currentChapterName = current?.chapterName ?: ""
                
                // Check for errors
                val failed = progressMap.values.find { it.status == TranslationStatus.FAILED }
                if (failed != null) {
                    dialogState.errorMessage = failed.errorMessage
                }
                
                // Check if all done
                if (completed >= dialogState.totalChapters && dialogState.totalChapters > 0) {
                    dialogState.isTranslating = false
                    onShowSnackbar("Translation completed!")
                    dialogState.hide()
                }
            }
        }
        
        scope.launch {
            translationService?.state?.collectLatest { state ->
                dialogState.isPaused = state == ServiceState.PAUSED
                if (state == ServiceState.IDLE && dialogState.isTranslating) {
                    dialogState.isTranslating = false
                }
            }
        }
    }
    
    /**
     * Pause translation
     */
    fun pause() {
        scope.launch {
            translationService?.pause()
            dialogState.isPaused = true
        }
    }
    
    /**
     * Resume translation
     */
    fun resume() {
        scope.launch {
            translationService?.resume()
            dialogState.isPaused = false
        }
    }
    
    /**
     * Cancel translation
     */
    fun cancel() {
        scope.launch {
            translationService?.cancelAll()
            dialogState.reset()
            onShowSnackbar("Translation cancelled")
        }
    }
    
    /**
     * Dismiss dialog
     */
    fun dismiss() {
        if (!dialogState.isTranslating) {
            dialogState.hide()
        }
    }
}

/**
 * Remember a TranslationController instance
 */
@Composable
fun rememberTranslationController(
    translationService: TranslationService?,
    readerPreferences: ReaderPreferences,
    translationPreferences: TranslationPreferences? = null,
    scope: CoroutineScope,
    onShowSnackbar: (String) -> Unit
): TranslationController {
    return remember(translationService, readerPreferences, scope) {
        TranslationController(
            translationService = translationService,
            readerPreferences = readerPreferences,
            translationPreferences = translationPreferences,
            scope = scope,
            onShowSnackbar = onShowSnackbar
        )
    }
}
