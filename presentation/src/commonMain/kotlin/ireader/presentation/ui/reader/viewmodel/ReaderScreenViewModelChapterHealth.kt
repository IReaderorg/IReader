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
 * Extension functions for ReaderScreenViewModel to handle chapter health checking and repair
 */

/**
 * Check if the current chapter is broken and update UI state accordingly
 */
fun ReaderScreenViewModel.checkChapterHealth(
    chapter: Chapter,
    chapterHealthChecker: ChapterHealthChecker,
    chapterHealthRepository: ChapterHealthRepository
) {
    scope.launch {
        try {
            // First, check if we have a cached health status for this chapter
            val existingHealth = chapterHealthRepository.getChapterHealthById(chapter.id)
            
            // If the chapter was recently checked (within last 5 minutes) and marked healthy,
            // don't re-check to avoid false positives during navigation
            val recentCheckThreshold = 5 * 60 * 1000L // 5 minutes
            val now = System.currentTimeMillis()
            if (existingHealth != null && 
                !existingHealth.isBroken && 
                (now - existingHealth.checkedAt) < recentCheckThreshold) {
                // Chapter was recently verified as healthy, skip check
                prefState.isChapterBroken = false
                prefState.chapterBreakReason = null
                prefState.showRepairBanner = false
                return@launch
            }
            
            // Check if chapter is broken
            val isBroken = chapterHealthChecker.isChapterBroken(chapter.content)
            
            if (isBroken) {
                val breakReason = chapterHealthChecker.getBreakReason(chapter.content)
                
                // Only show broken banner if:
                // 1. Chapter content is not empty (empty means still loading)
                // 2. Chapter has been read before (has content in database)
                // This prevents false positives for chapters that are still loading
                val hasContent = chapter.content.isNotEmpty()
                val contentText = chapter.content
                    .filterIsInstance<ireader.core.source.model.Text>()
                    .joinToString("") { it.text }
                val hasTextContent = contentText.isNotBlank()
                
                if (hasContent && hasTextContent) {
                    // Update UI state
                    prefState.isChapterBroken = true
                    prefState.chapterBreakReason = getBreakReasonMessage(breakReason)
                    prefState.showRepairBanner = true
                    
                    // Save to database
                    chapterHealthRepository.upsertChapterHealth(
                        ChapterHealth(
                            chapterId = chapter.id,
                            isBroken = true,
                            breakReason = breakReason,
                            checkedAt = now
                        )
                    )
                } else {
                    // Content is empty or has no text - likely still loading
                    // Don't mark as broken, just reset the state
                    prefState.isChapterBroken = false
                    prefState.chapterBreakReason = null
                    prefState.showRepairBanner = false
                }
            } else {
                // Chapter is healthy
                prefState.isChapterBroken = false
                prefState.chapterBreakReason = null
                prefState.showRepairBanner = false
                
                // Update database
                chapterHealthRepository.upsertChapterHealth(
                    ChapterHealth(
                        chapterId = chapter.id,
                        isBroken = false,
                        breakReason = null,
                        checkedAt = now
                    )
                )
            }
        } catch (e: Exception) {
            // Silently fail - don't disrupt reading experience
            ireader.core.log.Log.error("Error checking chapter health: ${e.message}")
            // Reset broken state on error to avoid false positives
            prefState.isChapterBroken = false
            prefState.chapterBreakReason = null
            prefState.showRepairBanner = false
        }
    }
}

/**
 * Attempt to repair the current broken chapter
 */
fun ReaderScreenViewModel.repairChapter(
    autoRepairChapterUseCase: AutoRepairChapterUseCase
) {
    val chapter = stateChapter ?: return
    val book = book ?: return
    
    scope.launch {
        try {
            prefState.isRepairing = true
            
            val result = autoRepairChapterUseCase(chapter, book)
            
            result.onSuccess { repairedChapter ->
                // Update the current chapter with repaired content
                state.stateChapter = repairedChapter
                
                // Update UI state
                prefState.isChapterBroken = false
                prefState.showRepairBanner = false
                prefState.isRepairing = false
                prefState.showRepairSuccess = true
                
                // Get source name from catalog
                val sourceName = catalog?.name ?: "alternative source"
                prefState.repairSuccessSourceName = sourceName
                
                // Auto-dismiss success banner after 5 seconds
                kotlinx.coroutines.delay(5000)
                prefState.showRepairSuccess = false
                
                showSnackBar(UiText.DynamicString("Chapter repaired successfully from $sourceName"))
            }
            
            result.onFailure { error ->
                prefState.isRepairing = false
                
                val message = when {
                    error.message?.contains("already attempted") == true -> 
                        "Repair was recently attempted. Please try again later."
                    error.message?.contains("No working chapter") == true -> 
                        "Unable to find working chapter from other sources"
                    else -> 
                        "Repair failed: ${error.message}"
                }
                
                showSnackBar(UiText.DynamicString(message))
            }
        } catch (e: Exception) {
            prefState.isRepairing = false
            showSnackBar(UiText.DynamicString("Repair failed: ${e.message}"))
        }
    }
}

/**
 * Dismiss the repair banner
 */
fun ReaderScreenViewModel.dismissRepairBanner() {
    prefState.showRepairBanner = false
}

/**
 * Dismiss the repair success banner
 */
fun ReaderScreenViewModel.dismissRepairSuccessBanner() {
    prefState.showRepairSuccess = false
}

/**
 * Convert BreakReason enum to user-friendly message
 */
private fun getBreakReasonMessage(reason: BreakReason?): String {
    return when (reason) {
        BreakReason.LOW_WORD_COUNT -> "This chapter has very few words"
        BreakReason.EMPTY_CONTENT -> "This chapter appears to be empty"
        BreakReason.SCRAMBLED_TEXT -> "This chapter contains scrambled or corrupted text"
        BreakReason.HTTP_ERROR -> "Failed to load this chapter"
        null -> "This chapter appears to be broken"
    }
}
