

package org.ireader.common_models.entities

data class HistoryWithRelations(
    val bookId: Long,
    val chapterId: Long,
    val readAt: Long,
    val bookTitle: String,
    val sourceId: Long,
    val cover: String,
    val favorite: Boolean,
    val chapterTitle: String,
    val chapterNumber: Int,
    val date: String,
)
