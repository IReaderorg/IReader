package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val chapterId: Long,
    val readAt: Long,
)
