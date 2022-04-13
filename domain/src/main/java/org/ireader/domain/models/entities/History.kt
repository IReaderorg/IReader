package org.ireader.domain.models.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ireader.core.utils.Constants


@Entity(tableName = Constants.HISTORY_TABLE)
@Keep
data class History(
    @PrimaryKey(autoGenerate = false)
    val bookId: Long,
    val chapterId: Long,
    val readAt: Long,
)
