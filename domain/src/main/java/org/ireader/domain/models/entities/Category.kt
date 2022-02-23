package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ireader.core.utils.Constants.CATEGORY_TABLE

@Entity(tableName = CATEGORY_TABLE)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val sort: Int = 0,
    val updateInterval: Int = 0,
    val flags: Int = 0,
)
