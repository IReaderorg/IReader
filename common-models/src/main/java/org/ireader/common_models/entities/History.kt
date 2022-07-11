package org.ireader.common_models.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = HISTORY_TABLE,
    primaryKeys = [
        "bookId",
        "chapterId"
    ],
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bookId"),
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
data class History(
    val bookId: Long,
    val chapterId: Long,
    val readAt: Long,
    val progress: Int = 0,
)
