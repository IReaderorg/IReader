package ireader.domain.usecases.donation

import ireader.domain.models.donation.DonationTrigger
import ireader.domain.models.entities.Book

/**
 * Integration helper functions for donation trigger system
 * These functions should be called at appropriate points in the application
 */

/**
 * Check if book completion should trigger a donation prompt
 * Call this when user marks a book as completed
 * 
 * Example usage in ViewModel:
 * ```
 * suspend fun markBookAsCompleted(book: Book) {
 *     // Update book status
 *     updateBook(book.copy(status = Book.COMPLETED))
 *     
 *     // Check for donation trigger
 *     val trigger = donationUseCases.donationTriggerManager.checkBookCompletion(
 *         chapterCount = book.totalChapters,
 *         bookTitle = book.title
 *     )
 *     
 *     if (trigger != null) {
 *         showDonationPrompt(trigger)
 *     }
 * }
 * ```
 */
suspend fun DonationTriggerManager.checkBookCompletionTrigger(
    book: Book,
    totalChapters: Int
): DonationTrigger? {
    return checkBookCompletion(
        chapterCount = totalChapters,
        bookTitle = book.title
    )
}

/**
 * Check if source migration should trigger a donation prompt
 * Call this after successful source migration
 * 
 * Example usage in ViewModel:
 * ```
 * suspend fun migrateToSource(targetSourceId: Long, targetSourceName: String) {
 *     // Perform migration
 *     val result = migrateToSourceUseCase(novelId, targetSourceId)
 *     
 *     if (result is Result.Success) {
 *         // Check for donation trigger
 *         val trigger = donationUseCases.donationTriggerManager.checkSourceMigration(
 *             sourceName = targetSourceName,
 *             chapterDifference = result.chapterDifference
 *         )
 *         
 *         if (trigger != null) {
 *             showDonationPrompt(trigger)
 *         }
 *     }
 * }
 * ```
 */
suspend fun DonationTriggerManager.checkSourceMigrationTrigger(
    sourceName: String,
    chapterDifference: Int
): DonationTrigger? {
    return checkSourceMigration(
        sourceName = sourceName,
        chapterDifference = chapterDifference
    )
}

/**
 * Check if chapter milestone should trigger a donation prompt
 * Call this after tracking chapter reading progress
 * 
 * Example usage in ViewModel:
 * ```
 * suspend fun onChapterRead() {
 *     // Track reading progress
 *     statisticsUseCases.trackReadingProgress.onChapterProgressUpdate(1.0f, wordCount)
 *     
 *     // Check for donation trigger
 *     val trigger = donationUseCases.donationTriggerManager.checkChapterMilestone()
 *     
 *     if (trigger != null) {
 *         showDonationPrompt(trigger)
 *     }
 * }
 * ```
 */
suspend fun DonationTriggerManager.checkChapterMilestoneTrigger(): DonationTrigger? {
    return checkChapterMilestone()
}
