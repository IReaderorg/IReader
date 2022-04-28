package org.ireader.common_models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = ImageKeyTable)

data class BrowseRemoteKey(
    @PrimaryKey(autoGenerate = false)
    var id: String,
    val previousPage: Int?,
    var nextPage: Int?,
    var lastUpdated: Long?,
)
