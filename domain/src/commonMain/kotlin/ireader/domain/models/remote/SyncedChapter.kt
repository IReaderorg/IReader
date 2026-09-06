package ireader.domain.models.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Domain model representing a synced chapter in remote database (e.g. Supabase synced_chapters).
 * Supports both lightweight metadata-only sync and full self-hosted chapter content storage.
 */
@Serializable
data class SyncedChapter(
    @SerialName("user_id") val userId: String,
    @SerialName("chapter_id") val chapterId: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("chapter_key") val chapterKey: String,
    @SerialName("name") val name: String,
    @SerialName("chapter_number") val chapterNumber: Float = 0f,
    @SerialName("source_order") val sourceOrder: Long = 0L,
    @SerialName("read") val read: Boolean = false,
    @SerialName("bookmark") val bookmark: Boolean = false,
    @SerialName("last_page_read") val lastPageRead: Long = 0L,
    @SerialName("date_upload") val dateUpload: Long = 0L,
    @SerialName("date_fetch") val dateFetch: Long = 0L,
    @SerialName("translator") val translator: String = "",
    @SerialName("content") val content: String = ""
)
