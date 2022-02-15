package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Updates(
    @PrimaryKey
    val id: Long = 0,
    val bookId: Long,
    val sourceId: Long,
    val bookLink: String,
    val bookTitle: String,
    val cover: String = "",
    val favorite: Boolean = false,
    val dateUploaded: Long = 0,
    val chapterId: Long,
    val chapterTitle: String,
    val read: Boolean = false,
    val number: Float = -1f,
)
