package ireader.domain.models.donation

/**
 * Sealed class representing different donation trigger events
 * Each trigger represents a moment of user satisfaction where donation prompts are appropriate
 */
sealed class DonationTrigger {
    /**
     * Triggered when user completes a book with 500+ chapters
     * @param chapterCount Total number of chapters in the completed book
     * @param bookTitle Title of the completed book
     */
    data class BookCompleted(
        val chapterCount: Int,
        val bookTitle: String
    ) : DonationTrigger()
    
    /**
     * Triggered when user successfully migrates to a better source for the first time
     * @param sourceName Name of the source migrated to
     * @param chapterDifference Number of additional chapters available
     */
    data class FirstMigrationSuccess(
        val sourceName: String,
        val chapterDifference: Int
    ) : DonationTrigger()
    
    /**
     * Triggered when user reaches chapter reading milestones (every 1,000 chapters)
     * @param totalChaptersRead Total number of chapters read
     */
    data class ChapterMilestone(
        val totalChaptersRead: Int
    ) : DonationTrigger()
}

/**
 * Data class representing the contextual message for each trigger type
 */
data class DonationPromptMessage(
    val title: String,
    val message: String,
    val trigger: DonationTrigger
)

/**
 * Extension function to get contextual message for each trigger
 */
fun DonationTrigger.toPromptMessage(): DonationPromptMessage {
    return when (this) {
        is DonationTrigger.BookCompleted -> DonationPromptMessage(
            title = "Congratulations! 🎉",
            message = "You've finished \"$bookTitle\" with $chapterCount chapters! If IReader made this journey better, please consider a small donation to support development.",
            trigger = this
        )
        is DonationTrigger.FirstMigrationSuccess -> DonationPromptMessage(
            title = "Migration Complete! ✨",
            message = "Saved you a headache, right? 😉 Found $chapterDifference more chapters on $sourceName. If you find these power-features useful, please consider supporting the app.",
            trigger = this
        )
        is DonationTrigger.ChapterMilestone -> DonationPromptMessage(
            title = "Amazing Progress! 📚",
            message = "You've read ${totalChaptersRead.formatWithCommas()} chapters! That's incredible. To help us build the app for the next ${(totalChaptersRead / 1000 + 1) * 1000} chapters, please consider donating.",
            trigger = this
        )
    }
}

/**
 * Format number with commas for readability
 */
private fun Int.formatWithCommas(): String {
    return this.toString().reversed().chunked(3).joinToString(",").reversed()
}
