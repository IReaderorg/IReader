package ireader.presentation.imageloader.coil.imageloader

import com.seiko.imageloader.cache.disk.DiskCache
import com.seiko.imageloader.component.fetcher.FetchResult
import com.seiko.imageloader.component.fetcher.Fetcher
import com.seiko.imageloader.option.Options
import io.ktor.http.*
import ireader.core.http.okhttp
import ireader.core.log.Log
import ireader.core.source.HttpSource
import ireader.domain.catalogs.CatalogStore
import ireader.domain.image.cache.CoverCache
import ireader.domain.models.BookCover
import ireader.presentation.imageloader.HttpException
import ireader.presentation.imageloader.await
import ireader.presentation.imageloader.coil.imageloader.BookCoverFetcher.Companion.USE_CUSTOM_COVER
import ireader.presentation.imageloader.convertToOkHttpRequest
import okhttp3.*
import okhttp3.CacheControl
import okhttp3.internal.closeQuietly
import okio.Source
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.net.HttpURLConnection

/**
 * A [Fetcher] that fetches cover image for [Manga] object.
 *
 * It uses [Manga.thumbnail_url] if custom cover is not set by the user.
 * Disk caching for library items is handled by [CoverCache], otherwise
 * handled by Coil's [DiskCache].
 *
 * Available request parameter:
 * - [USE_CUSTOM_COVER]: Use custom cover if set by user, default is true
 */
class BookCoverFetcher(
        private val url: String?,
        private val isLibraryManga: Boolean,
        private val options: Options,
        private val coverFileLazy: Lazy<File?>,
        private val customCoverFileLazy: Lazy<File>,
        private val diskCacheKeyLazy: Lazy<String>,
        private val sourceLazy: Lazy<HttpSource?>,
        private val callFactoryLazy: Lazy<Call.Factory>,
        private val diskCacheLazy: Lazy<DiskCache>,
) : Fetcher {

    private val diskCacheKey: String
        get() = diskCacheKeyLazy.value

    override suspend fun fetch(): FetchResult {
        // diskCacheKey is thumbnail_url
        if (url == null) error("No cover specified")
        return when (getResourceType(url)) {
            Type.URL -> httpLoader()
            Type.File -> fileLoader(File(url.substringAfter("file://")))
            null -> error("Invalid image")
        }
    }

    private fun fileLoader(file: File): FetchResult {
        return FetchResult.Source(file.source().buffer())
    }

    private suspend fun httpLoader(): FetchResult {
        // Only cache separately if it's a library item
        val libraryCoverCacheFile = if (isLibraryManga) {
            coverFileLazy.value ?: error("No cover specified")
        } else {
            null
        }
        if (libraryCoverCacheFile?.exists() == true && options.diskCachePolicy.readEnabled) {
            return fileLoader(libraryCoverCacheFile)
        }

        var snapshot = readFromDiskCache()
        try {
            // Fetch from disk cache
            if (snapshot != null) {
                val snapshotCoverCache = moveSnapshotToCoverCache(snapshot, libraryCoverCacheFile)
                if (snapshotCoverCache != null) {
                    // Read from cover cache after added to library
                    return fileLoader(snapshotCoverCache)
                }

                // Read from snapshot
                return snapshot.toImageSource()
            }

            // Fetch from network
            val response = executeNetworkRequest()
            val responseBody = checkNotNull(response.body) { "Null response source" }
            try {
                // Read from cover cache after library manga cover updated
                val responseCoverCache = writeResponseToCoverCache(response, libraryCoverCacheFile)
                if (responseCoverCache != null) {
                    return fileLoader(responseCoverCache)
                }

                // Read from disk cache
                snapshot = writeToDiskCache(snapshot, response)
                if (snapshot != null) {
                    return snapshot.toImageSource()
                }

                // Read from response if cache is unused or unusable

                return FetchResult.Source(responseBody.source().buffer)
            } catch (e: Exception) {
                responseBody.closeQuietly()
                throw e
            }
        } catch (e: Exception) {
            snapshot?.closeQuietly()
            throw e
        }
    }

    private suspend fun executeNetworkRequest(): Response {
        val client = sourceLazy.value?.client?.okhttp ?: callFactoryLazy.value
        val response = client.newCall(newRequest()).await()
        if (!response.isSuccessful && response.code != HttpURLConnection.HTTP_NOT_MODIFIED) {
            response.body?.closeQuietly()
            throw HttpException(response)
        }
        return response
    }

    fun DefaultHeader() : okhttp3.Headers {
        return okhttp3.Headers.Builder().apply {

        }.build()
    }
    private fun newRequest(): Request {
        val request = Request.Builder()
                .url(url!!)
                .headers(sourceLazy.value?.getCoverRequest(url)?.second?.build()?.convertToOkHttpRequest()?.headers ?: DefaultHeader())


        val diskRead = options.diskCachePolicy.readEnabled
        val networkRead = options.diskCachePolicy.readEnabled
        when {
            !networkRead && diskRead -> {
                request.cacheControl(CacheControl.FORCE_CACHE)
            }
            networkRead && !diskRead -> if (options.diskCachePolicy.writeEnabled) {
                request.cacheControl(CacheControl.FORCE_NETWORK)
            } else {
                request.cacheControl(CACHE_CONTROL_FORCE_NETWORK_NO_CACHE)
            }
            !networkRead && !diskRead -> {
                // This causes the request to fail with a 504 Unsatisfiable Request.
                request.cacheControl(CACHE_CONTROL_NO_NETWORK_NO_CACHE)
            }
        }

        return request.build()
    }

    private fun moveSnapshotToCoverCache(snapshot: DiskCache.Snapshot, cacheFile: File?): File? {
        if (cacheFile == null) return null
        return try {
            diskCacheLazy.value.run {
                fileSystem.source(snapshot.data).use { input ->
                    writeSourceToCoverCache(input, cacheFile)
                }
                remove(diskCacheKey)
            }
            cacheFile.takeIf { it.exists() }
        } catch (e: Exception) {
            Log.error { "Failed to write snapshot data to cover cache ${cacheFile.name}" }
            null
        }
    }

    private fun writeResponseToCoverCache(response: Response, cacheFile: File?): File? {
        if (cacheFile == null || !options.diskCachePolicy.writeEnabled) return null
        return try {
            response.peekBody(Long.MAX_VALUE).source().use { input ->
                writeSourceToCoverCache(input, cacheFile)
            }
            cacheFile.takeIf { it.exists() }
        } catch (e: Exception) {
            Log.error { "Failed to write response data to cover cache ${cacheFile.name}" }
            null
        }
    }

    private fun writeSourceToCoverCache(input: Source, cacheFile: File) {
        cacheFile.parentFile?.mkdirs()
        cacheFile.delete()
        try {
            cacheFile.sink().buffer().use { output ->
                output.writeAll(input)
            }
        } catch (e: Exception) {
            cacheFile.delete()
            throw e
        }
    }

    private fun readFromDiskCache(): DiskCache.Snapshot? {
        return if (options.diskCachePolicy.readEnabled) diskCacheLazy.value[diskCacheKey] else null
    }

    private fun writeToDiskCache(
            snapshot: DiskCache.Snapshot?,
            response: Response,
    ): DiskCache.Snapshot? {
        if (!options.diskCachePolicy.writeEnabled) {
            snapshot?.closeQuietly()
            return null
        }
        val editor = if (snapshot != null) {
            snapshot.closeAndEdit()
        } else {
            diskCacheLazy.value.edit(diskCacheKey)
        } ?: return null
        try {
            diskCacheLazy.value.fileSystem.write(editor.data) {
                response.body!!.source().readAll(this)
            }
            return editor.commitAndGet()
        } catch (e: Exception) {
            try {
                editor.abort()
            } catch (ignored: Exception) {
            }
            throw e
        }
    }

    private fun DiskCache.Snapshot.toImageSource(): FetchResult.Source {
        return FetchResult.Source(data.toFile().source().buffer())
    }

    private fun getResourceType(cover: String?): Type? {
        return when {
            cover.isNullOrEmpty() -> null
            cover.startsWith("ireader/core/http", true) || cover.startsWith("Custom-", true) -> Type.URL
            cover.startsWith("/") || cover.startsWith("file://") -> Type.File
            else -> null
        }
    }

    private enum class Type {
        File, URL
    }

    class Factory(
            private val callFactoryLazy: Lazy<Call.Factory>,
            private val diskCacheLazy: Lazy<DiskCache>,
            private val coverCache: CoverCache,
            private val catalogStore: CatalogStore,
    ) : Fetcher.Factory {

        override fun create(data: Any, options: Options): Fetcher? {
            return if (data is BookCover) {
                BookCoverFetcher(
                        url = data.cover,
                        isLibraryManga = data.favorite,
                        options = options,
                        coverFileLazy = lazy { coverCache.getCoverFile(data) },
                        customCoverFileLazy = lazy { coverCache.getCustomCoverFile(data) },
                        diskCacheKeyLazy = lazy { BookCoverKeyer().key(data, options) ?: "" },
                        sourceLazy = lazy { catalogStore.get(data.sourceId)?.source as? HttpSource },
                        callFactoryLazy = callFactoryLazy,
                        diskCacheLazy = diskCacheLazy,
                )
            } else {
                null
            }
        }
    }

    companion object {
        const val USE_CUSTOM_COVER = "use_custom_cover"

        private val CACHE_CONTROL_FORCE_NETWORK_NO_CACHE = CacheControl.Builder().noCache().noStore().build()
        private val CACHE_CONTROL_NO_NETWORK_NO_CACHE = CacheControl.Builder().noCache().onlyIfCached().build()
    }
}
