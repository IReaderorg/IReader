package org.ireader.common_models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = FONT_TABLE)
data class FontEntity(
    @PrimaryKey(autoGenerate = false)
    val fontName:String,
    val category:String,
    val isDownloaded:Boolean = false,
)
