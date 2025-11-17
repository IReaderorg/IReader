package ireader.domain.models.updates

import ireader.core.source.model.Page

/**
 * Data class for partial chapter updates following Mihon's pattern.
 * Allows updating specific fields without affecting others.
 */
data class ChapterUpdate(
    val id: Long,
    val bookId: Long? = null,
    val key: String? = null,
    val name: String? = null,
    val read: Boolean? = null,
    val bookmark: Boolean? = null,
    val dateUpload: Long? = null,
    val dateFetch: Long? = null,
    val sourceOrder: Long? = null,
    val content: List<Page>? = null,
    val number: Float? = null,
    val translator: String? = null,
    val lastPageRead: Long? = null,
    val type: Long? = null,
)