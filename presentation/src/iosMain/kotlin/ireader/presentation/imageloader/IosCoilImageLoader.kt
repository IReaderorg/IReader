@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ireader.presentation.imageloader

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import ireader.core.http.HttpClients
import ireader.presentation.imageloader.coil.imageloader.BookCoverKeyer
import ireader.presentation.imageloader.coil.imageloader.BookCoverMapper
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteKeyer
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteMapper
import ireader.presentation.imageloader.coil.imageloader.InstalledCatalogKeyer
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * Coil on iOS has no BookCover fetcher. Map covers to their URL and download
 * them with the app HTTP client, sending the image host as the referrer.
 */
internal fun iosImageLoader(context: PlatformContext, httpClients: HttpClients): ImageLoader {
    val imageClient = httpClients.default.config { }
    imageClient.plugin(HttpSend).intercept { request ->
        val host = request.url.host
        if (host.isNotEmpty() && request.headers[HttpHeaders.Referrer] == null) {
            request.header(HttpHeaders.Referrer, "https://$host/")
        }
        if (request.headers[HttpHeaders.UserAgent] == null) {
            request.header(
                HttpHeaders.UserAgent,
                "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1",
            )
        }
        execute(request)
    }

    return ImageLoader.Builder(context).apply {
        components {
            add(KtorNetworkFetcherFactory(httpClient = { imageClient }))
            add(BookCoverMapper())
            add(CatalogRemoteMapper())
            add(BookCoverKeyer())
            add(CatalogRemoteKeyer())
            add(InstalledCatalogKeyer())
        }
        memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .strongReferencesEnabled(true)
                .build()
        }
        diskCache {
            DiskCache.Builder()
                .directory(imageCacheDirectory())
                .fileSystem(FileSystem.SYSTEM)
                .maxSizePercent(0.02)
                .build()
        }
        crossfade(150)
    }.build()
}

private fun imageCacheDirectory(): okio.Path {
    val base = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true,
    ).firstOrNull() as? String ?: NSFileManager.defaultManager.currentDirectoryPath
    val directory = "$base/ireader_image_cache"
    NSFileManager.defaultManager.createDirectoryAtPath(
        directory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    return directory.toPath()
}
