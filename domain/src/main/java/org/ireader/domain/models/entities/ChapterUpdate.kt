package org.ireader.domain.models.entities

data class ChapterUpdate(
    val id: Long,
    val bookId: Long? = null,
    val link: String? = null,
    val name: String? = null,
    val read: Boolean? = null,
    val bookmark: Boolean? = null,
    val progress: Int? = null,
    val dateUpload: Long? = null,
    val dateFetch: Long? = null,
    val sourceOrder: Int? = null,
    val number: Float? = null,
    val translator: String? = null,
)
