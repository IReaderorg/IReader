package ireader.domain.models.remote

import kotlinx.serialization.Serializable

/**
 * Domain model representing a synced book
 * All fields are required for proper functionality
 * Only syncs favorite books
 */
@Serializable
data class SyncedBook(
    val userId: String,
    val bookId: String,
    val sourceId: Long,
    val title: String,
    val bookUrl: String,
    val lastRead: Long
)
