package org.ireader.domain.models.updates

data class BookUpdates(
    val id: Long = 0,
    val sourceId: Long? = null,
    val key: String? = null,
    val title: String? = null,
    val translator: String? = null,
    val author: String? = null,
    val description: String? = null,
    val genres: List<String>? = null,
    val status: Int? = null,
    val cover: String? = null,
    val customCover: String? = null,
    val favorite: Boolean? = null,
    val lastUpdate: Long? = null,
    val lastInit: Long? = null,
    val dateAdded: Long? = null,
    val viewer: Int? = null,
    val flags: Int? = null,
)
