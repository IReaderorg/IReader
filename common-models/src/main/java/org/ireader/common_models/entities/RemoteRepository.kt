

package org.ireader.common_models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = REMOTE_TABLE)

data class RemoteRepository(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val key: String,
    val lastUpdate: Long,
)
