package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CatalogRemote(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 0,
    val name: String,
    val description: String,
    val pkgName: String,
    val versionName: String,
    val versionCode: Int,
    val lang: String,
    val apkUrl: String,
    val iconUrl: String,
    val nsfw: Boolean,
)
