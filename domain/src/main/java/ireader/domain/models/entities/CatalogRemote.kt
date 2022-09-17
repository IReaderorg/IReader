
package ireader.common.models.entities

import androidx.annotation.Keep

import ireader.core.api.source.Source
import java.io.File
import kotlin.random.Random

@Keep
data class CatalogRemote(
    override val sourceId: Long,
    val source: Long,
    override val name: String,
    override val description: String,
    val pkgName: String,
    val versionName: String,
    val versionCode: Int,
    val lang: String,
    val pkgUrl: String,
    val iconUrl: String,
    val nsfw: Boolean,
) : Catalog() {
    companion object {
        const val DEFAULT_ID = -1L

    }
}

sealed class Catalog {
    abstract val name: String
    abstract val description: String
    abstract val sourceId: Long
}

sealed class CatalogLocal : Catalog() {
    abstract val source: Source?
    override val sourceId get() = source?.id ?: -1L
    abstract val nsfw: Boolean
    abstract val isPinned: Boolean
    open val hasUpdate: Boolean = false
}

data class CatalogBundled(
    override val source: Source?,
    override val description: String = "",
    override val name: String = source?.name ?: "UNKNOWN",
    override val nsfw: Boolean = false,
    override val isPinned: Boolean = false,
) : CatalogLocal()

sealed class CatalogInstalled : CatalogLocal() {
    abstract val pkgName: String
    abstract val versionName: String
    abstract val iconUrl: String
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
        override val iconUrl: String
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
        override val iconUrl: String
    ) : CatalogInstalled()
}

enum class SourceState {
    Pinned,
    UnPinned,
    LastUsed,
    Installed,
    Remote,
    Nothing
}

fun Catalog.key(state: SourceState,index: Long): String {
    if (sourceId == -1L) return  "$index-installed"
    return when (state) {
        SourceState.LastUsed -> "$sourceId-lastused"
        SourceState.Pinned -> "$sourceId-pinned"
        SourceState.UnPinned -> "$sourceId-unpinned"
        SourceState.Installed -> "$sourceId-installed"
        SourceState.Remote -> "$sourceId-remote"
        else -> "$sourceId"
    }
}
