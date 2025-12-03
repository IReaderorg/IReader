package ireader.presentation.imageloader

import coil3.Extras
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.disk.DiskCache
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.getOrDefault
import coil3.request.Options
import ireader.core.http.okhttp
import ireader.core.io.VirtualFile
import ireader.core.log.Log
import ireader.core.source.HttpSource
import ireader.domain.catalogs.CatalogStore
import ireader.domain.image.CoverCache
import ireader.domain.models.BookCover
import ireader.domain.models.entities.Book
import ireader.presentation.imageloader.coil.imageloader.BookCoverKeyer
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Source
import okio.buffer
import okio.sink
import okio.source

/**
 * A [Fetcher] that fetches cover image for [Manga] object.
 *
 * It uses [Manga.thumbnailUrl] if custom cover is not set by the user.
 * Disk caching for library items is handled by [CoverCache], otherwise
 * handled by Coil's [DiskCache].
 *
 * Available request parameter:
 * - [USE_CUSTOM_COVER_KEY]: Use custom cover if set by user, default is true
 */
class BookCoverFetcher(
    private val url: String?,
    private val isLibraryManga: Boolean,
    private val options: Options,
    private val bookCover: BookCover,
    private val coverCache: CoverCache,
    private val diskCacheKeyLazy: Lazy<String>,
    private val sourceLazy: Lazy<HttpSource?>,
    private val callFactoryLazy: Lazy<Call.Factory>,
    private val diskCacheLazy: Lazy<DiskCache>,
) : Fetcher {

    private val diskCacheKey: String
        get() = diskCacheKeyLazy.value

    override suspend fun fetch(): FetchResult {
        // Use custom cover if exists
        val useCustomCover = options.extras.getOrDefault(USE_CUSTOM_COVER_KEY)
        if (useCustomCover) {
            val customCoverFile = coverCache.getCustomCoverFile(bookCover)
            if (customCoverFile.exists()) {
                return fileLoader(customCoverFile)
            }
        }

        // diskCacheKey is thumbnail_url
        if (url == null) error("No cover specified")
        return when (getResourceType(url)) {
            Type.File -> {
                val path = url.substringAfter("file://")
                // Platform-specific file loading would be needed here
                // For now, use the URL-based approach
                httpLoader()
            }
            Type.URI -> fileUriLoader(url)
            Type.URL -> httpLoader()
            null -> error("Invalid image")
        }
    }

    private suspend fun fileLoader(file: VirtualFile): FetchResult {
        // Convert VirtualFile to okio Path for Coil
        val okioPath = java.io.File(file.path).toOkioPath()
        return SourceFetchResult(
            source = ImageSource(
                file = okioPath,
                fileSystem = FileSystem.SYSTEM,
                diskCacheKey = diskCacheKey
            ),
            mimeType = "image/*",
            dataSource = DataSource.DISK,
        )
    }

    private suspend fun fileUriLoader(uri: String): FetchResult {
        // Platform-specific URI loading
        // This would need to be implemented per-platform
        throw NotImplementedError("URI loading must be implemented platform-specifically")
    }

    private suspend fun httpLoader(): FetchResult {
        // Only cache separately if it's a library item
        val libraryCoverCacheFile = if (isLibraryManga) {
            coverCache.getCoverFile(bookCover)
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
                return SourceFetchResult(
                    source = snapshot.toImageSource(),
                    mimeType = "image/*",
                    dataSource = DataSource.DISK,
                )
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
                snapshot = writeToDiskCache(response)
                if (snapshot != null) {
                    return SourceFetchResult(
                        source = snapshot.toImageSource(),
                        mimeType = "image/*",
                        dataSource = DataSource.NETWORK,
                    )
                }

                // Read from response if cache is unused or unusable
                return SourceFetchResult(
                    source = ImageSource(source = responseBody.source(), fileSystem = FileSystem.SYSTEM),
                    mimeType = "image/*",
                    dataSource = if (response.cacheResponse != null) DataSource.DISK else DataSource.NETWORK,
                )
            } catch (e: Exception) {
                responseBody.close()
                throw e
            }
        } catch (e: Exception) {
            snapshot?.close()
            throw e
        }
    }

    private suspend fun executeNetworkRequest(): Response {
        val client = sourceLazy.value?.client?.okhttp ?: callFactoryLazy.value
        val response = client.newCall(newRequest()).await()
        if (!response.isSuccessful && response.code != 304) { // 304 = HTTP_NOT_MODIFIED
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
        val request = Request.Builder().apply {
            url(url!!)

            val sourceHeaders = sourceLazy.value?.getCoverRequest(url)?.second?.build()?.convertToOkHttpRequest()?.headers ?: DefaultHeader()
            if (sourceHeaders != null) {
                headers(sourceHeaders)
            }
        }

        when {
            options.networkCachePolicy.readEnabled -> {
                // don't take up okhttp cache
                request.cacheControl(CACHE_CONTROL_NO_STORE)
            }
            else -> {
                // This causes the request to fail with a 504 Unsatisfiable Request.
                request.cacheControl(CACHE_CONTROL_NO_NETWORK_NO_CACHE)
            }
        }

        return request.build()
    }

    private suspend fun moveSnapshotToCoverCache(snapshot: DiskCache.Snapshot, cacheFile: VirtualFile?): VirtualFile? {
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

    private suspend fun writeResponseToCoverCache(response: Response, cacheFile: VirtualFile?): VirtualFile? {
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

    private suspend fun writeSourceToCoverCache(input: Source, cacheFile: VirtualFile) {
        cacheFile.parent?.mkdirs()
        cacheFile.delete()
        try {
            // Use okio's efficient copy instead of manual buffering
            val outputStream = cacheFile.outputStream(append = false)
            val buffer = okio.Buffer()
            
            // Read all from input source into buffer
            var totalBytes = 0L
            while (true) {
                val bytesRead = input.read(buffer, 8192L)
                if (bytesRead == -1L) break
                totalBytes += bytesRead
                
                // Write buffer to VirtualOutputStream
                val bytes = buffer.readByteArray()
                outputStream.write(bytes)
            }
            
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            cacheFile.delete()
            throw e
        }
    }

    private fun readFromDiskCache(): DiskCache.Snapshot? {
        return if (options.diskCachePolicy.readEnabled) {
            diskCacheLazy.value.openSnapshot(diskCacheKey)
        } else {
            null
        }
    }

    private fun writeToDiskCache(
        response: Response,
    ): DiskCache.Snapshot? {
        val editor = diskCacheLazy.value.openEditor(diskCacheKey) ?: return null
        try {
            diskCacheLazy.value.fileSystem.write(editor.data) {
                response.body?.source()?.readAll(this)
            }
            return editor.commitAndOpenSnapshot()
        } catch (e: Exception) {
            try {
                editor.abort()
            } catch (ignored: Exception) {
            }
            throw e
        }
    }

    private fun DiskCache.Snapshot.toImageSource(): ImageSource {
        return ImageSource(
            file = data,
            fileSystem = FileSystem.SYSTEM,
            diskCacheKey = diskCacheKey,
            closeable = this,
        )
    }

    private fun getResourceType(cover: String?): Type? {
        return when {
            cover.isNullOrEmpty() -> null
            cover.startsWith("http", true) || cover.startsWith("Custom-", true) -> Type.URL
            cover.startsWith("/") || cover.startsWith("file://") -> Type.File
            cover.startsWith("content") -> Type.URI
            else -> null
        }
    }

    private enum class Type {
        File,
        URI,
        URL,
    }

    class BookCoverFactory(
        private val callFactoryLazy: Lazy<Call.Factory>,
        private val diskCacheLazy: Lazy<DiskCache>,
        private val catalogStore: CatalogStore,
        private val coverCache: CoverCache,
    ) : Fetcher.Factory<BookCover> {


        override fun create(data: BookCover, options: Options, imageLoader: ImageLoader): Fetcher {
            return BookCoverFetcher(
                url = data.cover,
                isLibraryManga = data.favorite,
                options = options,
                bookCover = data,
                coverCache = coverCache,
                diskCacheKeyLazy = lazy { BookCoverKeyer().key(data, options) },
                sourceLazy = lazy { catalogStore.get(data.sourceId)?.source as? HttpSource },
                callFactoryLazy = callFactoryLazy,
                diskCacheLazy = diskCacheLazy,
            )
        }
    }

    class BookFactory(
        private val callFactoryLazy: Lazy<Call.Factory>,
        private val diskCacheLazy: Lazy<DiskCache>,
        private val catalogStore: CatalogStore,
        private val coverCache: CoverCache,
    ) : Fetcher.Factory<Book> {

        override fun create(data: Book, options: Options, imageLoader: ImageLoader): Fetcher {
            return BookCoverFetcher(
                url = data.cover,
                isLibraryManga = data.favorite,
                options = options,
                bookCover = BookCover.from(data),
                coverCache = coverCache,
                diskCacheKeyLazy = lazy { BookCoverKeyer().key(BookCover.from(data), options) },
                sourceLazy = lazy { catalogStore.get(data.sourceId)?.source as? HttpSource },
                callFactoryLazy = callFactoryLazy,
                diskCacheLazy = diskCacheLazy,
            )
        }
    }

    companion object {
        val USE_CUSTOM_COVER_KEY = Extras.Key(true)

        private val CACHE_CONTROL_NO_STORE = CacheControl.Builder().noStore().build()
        private val CACHE_CONTROL_NO_NETWORK_NO_CACHE = CacheControl.Builder().noCache().onlyIfCached().build()
    }
}
