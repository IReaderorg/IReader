package ireader.presentation.ui.reader.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.core.source.Source
import ireader.core.source.model.Page
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.i18n.UiText

/**
 * Sealed interface representing the overall reader screen state.
 * 
 * This provides clear Loading/Success/Error states for the reader screen.
 * 
 * Requirements: 5.1 - @Stable annotation for Compose optimization
 */
@Stable
sealed interface ReaderState {
    
    @Immutable
    data object Loading : ReaderState
    
    @Immutable
    data class Success(
        // Book and chapter data
        val book: Book,
        val currentChapter: Chapter,
        val chapters: List<Chapter>,
        val catalog: CatalogLocal?,
        
        // Content
        val content: List<Page> = emptyList(),
        val translatedContent: List<Page> = emptyList(),
        
        // Navigation state
        val currentChapterIndex: Int = 0,
        val chapterShell: List<Chapter> = emptyList(),
        
        // Loading states
        val isLoadingContent: Boolean = false,
        val isRefreshing: Boolean = false,
        val isPreloading: Boolean = false,
        val isNavigating: Boolean = false,
        
        // Translation state
        val isTranslating: Boolean = false,
        val translationProgress: Float = 0f,
        val hasTranslation: Boolean = false,
        val showTranslatedContent: Boolean = false,
        
        // UI state
        val isReaderModeEnabled: Boolean = true,
        val isSettingModeEnabled: Boolean = false,
        val isMainBottomModeEnabled: Boolean = false,
        val showSettingsBottomSheet: Boolean = false,
        val isDrawerAsc: Boolean = true,
        
        // Find in chapter
        val showFindInChapter: Boolean = false,
        val findQuery: String = "",
        val findMatches: List<IntRange> = emptyList(),
        val currentFindMatchIndex: Int = 0,
        
        // Chapter health
        val isChapterBroken: Boolean = false,
        val chapterBreakReason: String? = null,
        val showRepairBanner: Boolean = false,
        val isRepairing: Boolean = false,
        val showRepairSuccess: Boolean = false,
        val repairSuccessSourceName: String? = null,
        
        // Reading time estimation
        val showReadingTime: Boolean = false,
        val estimatedReadingMinutes: Int = 0,
        val wordsRemaining: Int = 0,
        val totalWords: Int = 0,
        
        // Dialogs
        val showReportDialog: Boolean = false,
        val showParagraphTranslationDialog: Boolean = false,
        val paragraphToTranslate: String = "",
        val translatedParagraph: String? = null,
        val isParagraphTranslating: Boolean = false,
        val paragraphTranslationError: String? = null,
        val showTranslationApiKeyPrompt: Boolean = false,
        val showReadingBreakDialog: Boolean = false,
        
        // Auto scroll
        val autoScrollMode: Boolean = false,
        
        // Quick controls
        val showBrightnessControl: Boolean = false,
        val showFontSizeAdjuster: Boolean = false,
        val showFontPicker: Boolean = false,
        
        // Scroll target when chapter changes (null = start, true = end)
        val scrollToEndOnChapterChange: Boolean = false,
    ) : ReaderState {
        
        /**
         * Get the source from catalog
         */
        @Stable
        val source: Source?
            get() = catalog?.source
        
        /**
         * Check if chapter has content
         */
        @Stable
        val isChapterLoaded: Boolean
            get() = content.isNotEmpty()
        
        /**
         * Get drawer chapters (respects sort order)
         */
        @Stable
        val drawerChapters: List<Chapter>
            get() = if (isDrawerAsc) chapters else chapters.reversed()
        
        /**
         * Get current content (original or translated based on preference)
         */
        @Stable
        val currentContent: List<Page>
            get() = if (showTranslatedContent && hasTranslation && translatedContent.isNotEmpty()) {
                translatedContent
            } else {
                content
            }
        
        /**
         * Check if initial loading (no content yet)
         */
        @Stable
        val isInitialLoading: Boolean
            get() = isLoadingContent && content.isEmpty()
    }
    
    @Immutable
    data class Error(
        val message: UiText,
        val bookId: Long? = null,
        val chapterId: Long? = null,
    ) : ReaderState
}

/**
 * Reader preferences state (separate from main state for performance)
 */
@Immutable
data class ReaderPreferencesState(
    val isAsc: Boolean = true,
    val isChaptersReversed: Boolean = false,
    val isChapterReversingInProgress: Boolean = false,
    val initialized: Boolean = false,
    val searchQuery: String = "",
    val currentViewingSearchResultIndex: Int = 0,
    val expandTopMenu: Boolean = false,
    val scrollMode: Boolean = false,
)

/**
 * Sealed interface for reader dialogs
 */
sealed interface ReaderDialog {
    data object None : ReaderDialog
    data object Settings : ReaderDialog
    data object ChapterList : ReaderDialog
    data object Glossary : ReaderDialog
    data object ReportBroken : ReaderDialog
    data class ParagraphTranslation(val paragraph: String) : ReaderDialog
    data object TranslationApiKeyPrompt : ReaderDialog
    data object ReadingBreak : ReaderDialog
    data object ChapterReviews : ReaderDialog
}

/**
 * Sealed class for reader events (one-time events)
 */
sealed class ReaderEvent {
    data class ShowSnackbar(val message: UiText) : ReaderEvent()
    data class NavigateToChapter(val chapterId: Long) : ReaderEvent()
    data object NavigateBack : ReaderEvent()
    data class ScrollToPosition(val position: Int) : ReaderEvent()
    data class ScrollToMatch(val matchIndex: Int) : ReaderEvent()
}
