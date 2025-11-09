package ireader.domain.usecases.donation

import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.models.donation.DonationTrigger
import ireader.domain.preferences.prefs.AppPreferences
import kotlinx.coroutines.flow.first

/**
 * Manager for checking donation trigger conditions and determining when to show donation prompts
 * Implements 30-day cooldown between prompts to avoid spam
 */
class DonationTriggerManager(
    private val statisticsRepository: ReadingStatisticsRepository,
    private val appPreferences: AppPreferences
) {
    companion object {
        private const val COOLDOWN_DAYS = 30
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
        private const val BOOK_COMPLETION_CHAPTER_THRESHOLD = 500
        private const val CHAPTER_MILESTONE_INTERVAL = 1000
    }
    
    /**
     * Check if a book completion should trigger a donation prompt
     * @param chapterCount Total chapters in the completed book
     * @param bookTitle Title of the completed book
     * @return DonationTrigger if conditions are met, null otherwise
     */
    suspend fun checkBookCompletion(chapterCount: Int, bookTitle: String): DonationTrigger? {
        if (chapterCount < BOOK_COMPLETION_CHAPTER_THRESHOLD) {
            return null
        }
        
        if (!shouldShowPrompt()) {
            return null
        }
        
        return DonationTrigger.BookCompleted(
            chapterCount = chapterCount,
            bookTitle = bookTitle
        )
    }
    
    /**
     * Check if a source migration should trigger a donation prompt
     * Only triggers on the first successful migration
     * @param sourceName Name of the source migrated to
     * @param chapterDifference Number of additional chapters available
     * @return DonationTrigger if conditions are met, null otherwise
     */
    suspend fun checkSourceMigration(sourceName: String, chapterDifference: Int): DonationTrigger? {
        val isFirstMigration = !appPreferences.hasCompletedMigration().get()
        
        if (!isFirstMigration) {
            return null
        }
        
        if (!shouldShowPrompt()) {
            return null
        }
        
        // Mark that user has completed their first migration
        appPreferences.hasCompletedMigration().set(true)
        
        return DonationTrigger.FirstMigrationSuccess(
            sourceName = sourceName,
            chapterDifference = chapterDifference
        )
    }
    
    /**
     * Check if a chapter milestone should trigger a donation prompt
     * Triggers every 1,000 chapters read
     * @return DonationTrigger if conditions are met, null otherwise
     */
    suspend fun checkChapterMilestone(): DonationTrigger? {
        val totalChaptersRead = statisticsRepository.getStatistics().totalChaptersRead
        
        // Check if we've hit a milestone (every 1,000 chapters)
        if (totalChaptersRead % CHAPTER_MILESTONE_INTERVAL != 0 || totalChaptersRead == 0) {
            return null
        }
        
        // Check if we've already shown a prompt for this milestone
        val lastMilestoneShown = appPreferences.lastDonationMilestone().get()
        if (lastMilestoneShown >= totalChaptersRead) {
            return null
        }
        
        if (!shouldShowPrompt()) {
            return null
        }
        
        // Update the last milestone shown
        appPreferences.lastDonationMilestone().set(totalChaptersRead)
        
        return DonationTrigger.ChapterMilestone(
            totalChaptersRead = totalChaptersRead
        )
    }
    
    /**
     * Check if enough time has passed since the last donation prompt
     * Implements 30-day cooldown to avoid spam
     * @return true if prompt should be shown, false otherwise
     */
    suspend fun shouldShowPrompt(): Boolean {
        val lastPromptTime = appPreferences.lastDonationPromptTime().get()
        val currentTime = System.currentTimeMillis()
        
        if (lastPromptTime == 0L) {
            // Never shown a prompt before
            return true
        }
        
        val daysSinceLastPrompt = (currentTime - lastPromptTime) / MILLIS_PER_DAY
        return daysSinceLastPrompt >= COOLDOWN_DAYS
    }
    
    /**
     * Record that a donation prompt was shown
     * Updates the last prompt time to enforce cooldown
     */
    suspend fun recordPromptShown() {
        val currentTime = System.currentTimeMillis()
        appPreferences.lastDonationPromptTime().set(currentTime)
    }
    
    /**
     * Get days remaining until next prompt can be shown
     * @return Number of days remaining, or 0 if prompt can be shown now
     */
    suspend fun getDaysUntilNextPrompt(): Int {
        val lastPromptTime = appPreferences.lastDonationPromptTime().get()
        
        if (lastPromptTime == 0L) {
            return 0
        }
        
        val currentTime = System.currentTimeMillis()
        val daysSinceLastPrompt = (currentTime - lastPromptTime) / MILLIS_PER_DAY
        val daysRemaining = COOLDOWN_DAYS - daysSinceLastPrompt.toInt()
        
        return maxOf(0, daysRemaining)
    }
}
