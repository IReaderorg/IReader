
package ireader.domain.models.entities

import ireader.core.source.Source
import okio.Path


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
    val jarUrl: String,
    val nsfw: Boolean,
    val repositoryId: Long = -1L, // Reference to ExtensionSource
    val repositoryType: String = "IREADER", // "IREADER" or "LNREADER"
) : Catalog() {
    companion object {
        const val DEFAULT_ID = -1L
    }
    
    fun isLNReaderSource(): Boolean {
        return repositoryType.equals("LNREADER", ignoreCase = true)
    }
    
    fun isIReaderSource(): Boolean {
        return repositoryType.equals("IREADER", ignoreCase = true)
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
    abstract val installDir: Path?

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
        override val iconUrl: String,
        override val installDir: Path?
    ) : CatalogInstalled()

    data class Locally(
        override val name: String,
        override val description: String,
        override val source: Source,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Int,
        override val nsfw: Boolean,
        override val installDir: Path,
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

fun Catalog.key(state: SourceState, index: Long, repoId: Long): String {
    if (sourceId == -1L) return  "$index-installed"
    val name = when(this) {
        is CatalogInstalled.SystemWide -> "system-"
        is CatalogInstalled.Locally -> "local-"
        is CatalogRemote -> "remote-"
        else -> ""
    }
    return when (state) {
        SourceState.LastUsed -> "$repoId-$name-$sourceId-lastused"
        SourceState.Pinned -> "$repoId-$name-$sourceId-pinned"
        SourceState.UnPinned -> "$repoId-$name-$sourceId-unpinned"
        SourceState.Installed -> "$repoId-$name-$sourceId-installed"
        SourceState.Remote -> "$repoId-$name-$sourceId-remote"
        else -> "$repoId-$name-$sourceId"
    }
}
