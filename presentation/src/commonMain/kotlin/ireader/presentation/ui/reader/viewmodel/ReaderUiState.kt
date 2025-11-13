package ireader.presentation.ui.reader.viewmodel

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.i18n.UiText

/**
 * Sealed class representing the overall reader screen state
 * Provides clear state management for the reader screen
 */
sealed class ReaderUiState {
    object Idle : ReaderUiState()
    object Loading : ReaderUiState()
    data class Success(
        val book: Book,
        val currentChapter: Chapter,
        val allChapters: List<Chapter>
    ) : ReaderUiState()
    data class Error(val message: UiText) : ReaderUiState()
}

/**
 * State for chapter loading operations
 */
sealed class ChapterLoadState {
    object Idle : ChapterLoadState()
    object Loading : ChapterLoadState()
    data class Success(val chapter: Chapter) : ChapterLoadState()
    data class Error(val message: UiText) : ChapterLoadState()
}

/**
 * State for translation operations
 */
sealed class TranslationState {
    object Idle : TranslationState()
    data class InProgress(val progress: Float) : TranslationState()
    data class Success(val translatedContent: List<ireader.core.source.model.Page>) : TranslationState()
    data class Error(val message: UiText) : TranslationState()
}

/**
 * State for preloading operations
 */
sealed class PreloadState {
    object Idle : PreloadState()
    data class InProgress(val chapterName: String) : PreloadState()
    data class Success(val chapterName: String) : PreloadState()
    data class Error(val message: UiText) : PreloadState()
}

/**
 * State for font operations
 */
sealed class FontLoadState {
    object Idle : FontLoadState()
    object Loading : FontLoadState()
    data class Success(
        val systemFonts: List<ireader.domain.models.fonts.CustomFont>,
        val customFonts: List<ireader.domain.models.fonts.CustomFont>
    ) : FontLoadState()
    data class Error(val message: UiText) : FontLoadState()
}

/**
 * State for glossary operations
 */
sealed class GlossaryState {
    object Idle : GlossaryState()
    object Loading : GlossaryState()
    data class Success(val entries: List<ireader.domain.models.entities.Glossary>) : GlossaryState()
    data class Error(val message: UiText) : GlossaryState()
}
