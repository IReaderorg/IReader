package ireader.presentation.ui.component

import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview

/**
 * Get display name for a review
 * Returns username or "Reader {id}" as fallback
 */
fun getReviewDisplayName(username: String?, userId: String): String {
    return username?.takeIf { it.isNotBlank() } ?: "Reader ${userId.take(8)}"
}

fun BookReview.getDisplayName(): String {
    return getReviewDisplayName(username, userId)
}

fun ChapterReview.getDisplayName(): String {
    return getReviewDisplayName(username, userId)
}
