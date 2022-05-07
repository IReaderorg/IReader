
package org.ireader.common_models.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ireader.core_api.source.Source
import java.io.File

@Keep
@Entity(tableName = CATALOG_REMOTE)
data class CatalogRemote(
    @PrimaryKey(autoGenerate = false)
    override val sourceId: Long,
    override val name: String,
    override val description: String,
    val pkgName: String,
    val versionName: String,
    val versionCode: Int,
    val lang: String,
    val pkgUrl: String,
    val iconUrl: String,
    val nsfw: Boolean,
) : Catalog()

sealed class Catalog {
    abstract val name: String
    abstract val description: String
    abstract val sourceId: Long
}

sealed class CatalogLocal : Catalog() {
    abstract val source: Source?
    override val sourceId get() = source?.id?:-1L
    abstract val nsfw: Boolean
    abstract val isPinned: Boolean
    open val hasUpdate: Boolean = false
}

data class CatalogBundled(
    override val source: Source?,
    override val description: String = "",
    override val name: String = source?.name?:"UNKNOWN",
    override val nsfw: Boolean = false,
    override val isPinned: Boolean = false,
) : CatalogLocal()

sealed class CatalogInstalled : CatalogLocal() {
    abstract val pkgName: String
    abstract val versionName: String
    abstract val versionCode: Int

    data class SystemWide(
        override val name: String,
        override val description: String,
        override val source: Source?,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Int,
        override val nsfw: Boolean,
        override val isPinned: Boolean = false,
        override val hasUpdate: Boolean = false,
    ) : CatalogInstalled()

    data class Locally(
        override val name: String,
        override val description: String,
        override val source: Source,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Int,
        override val nsfw: Boolean,
        val installDir: File,
        override val isPinned: Boolean = false,
        override val hasUpdate: Boolean = false,
    ) : CatalogInstalled()
}
