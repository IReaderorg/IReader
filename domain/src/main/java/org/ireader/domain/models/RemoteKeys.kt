package org.ireader.domain.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ireader.core.utils.Constants

@Entity(tableName = Constants.PAGE_KET_TABLE)
@Keep
data class RemoteKeys(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sourceId: Long,
    val prevPage: Int?,
    val nextPage: Int?,
)