package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ireader.core.utils.Constants.REMOTE_TABLE

@Entity(tableName = REMOTE_TABLE)
data class RemoteRepository(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val key: String,
    val lastUpdate: Long,
)
