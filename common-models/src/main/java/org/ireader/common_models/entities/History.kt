

package org.ireader.common_models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = HISTORY_TABLE)

data class History(
    @PrimaryKey(autoGenerate = false)
    val bookId: Long,
    val chapterId: Long,
    val readAt: Long,
)
