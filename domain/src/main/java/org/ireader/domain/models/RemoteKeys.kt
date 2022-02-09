package org.ireader.infinity.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ireader.core.utils.Constants

@Entity(tableName = Constants.PAGE_KET_TABLE)
data class RemoteKeys(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val prevPage: Int?,
    val nextPage: Int?,
)