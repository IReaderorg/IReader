package ireader.data.catalog.impl

import kotlinx.serialization.Serializable
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL

internal fun iosJsPluginsDir(): Path {
    val urls = NSFileManager.defaultManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
    val documents = (urls.firstOrNull() as? NSURL)?.path ?: NSTemporaryDirectory()
    return "$documents/js-plugins".toPath()
}

@Serializable
internal data class InstalledJsSourceMeta(
    val id: Long,
    val name: String,
    val lang: String,
    val version: String,
    val code: Int,
    val description: String,
    val icon: String,
    val nsfw: Boolean,
    val fileName: String,
    val initFunction: String = "",
)
