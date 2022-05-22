

package org.ireader.common_models.entities

import androidx.room.Entity

@Entity(tableName = HISTORY_TABLE
, primaryKeys = [
    "bookId",
    "chapterId"
])
data class History(
    val bookId: Long,
    val chapterId: Long,
    val readAt: Long,
)
