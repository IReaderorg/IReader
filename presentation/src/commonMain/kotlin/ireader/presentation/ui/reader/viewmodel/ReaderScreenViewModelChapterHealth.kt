package ireader.presentation.ui.reader.viewmodel

import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.ChapterHealth
import ireader.domain.services.BreakReason
import ireader.domain.services.ChapterHealthChecker
import ireader.domain.usecases.chapter.AutoRepairChapterUseCase
import ireader.domain.data.repository.ChapterHealthRepository
import ireader.i18n.UiText
import kotlinx.coroutines.launch

/**
 * Extension functions for ReaderScreenViewModel to handle chapter health checking and repair.
 * 
 * Note: These functions are now mostly handled internally by ReaderScreenViewModel.
 * This file provides additional utility functions for chapter health management.
 */

/**
 * Convert BreakReason enum to user-friendly message
 */
fun getBreakReasonMessage(reason: BreakReason?): String {
    return when (reason) {
        BreakReason.LOW_WORD_COUNT -> "This chapter has very few words"
        BreakReason.EMPTY_CONTENT -> "This chapter appears to be empty"
        BreakReason.SCRAMBLED_TEXT -> "This chapter contains scrambled or corrupted text"
        BreakReason.HTTP_ERROR -> "Failed to load this chapter"
        null -> "This chapter appears to be broken"
    }
}
