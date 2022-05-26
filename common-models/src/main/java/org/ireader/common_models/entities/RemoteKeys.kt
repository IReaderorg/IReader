

package org.ireader.common_models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = PAGE_KET_TABLE)

data class RemoteKeys(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId:Long = 0,
    val title: String,
    val sourceId: Long,
    val prevPage: Int?,
    val nextPage: Int?,
)
