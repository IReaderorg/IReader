package org.ireader.image_loader.coil

import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.disk.DiskCache
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.key.Keyer
import coil.network.HttpException
import coil.request.Options
import coil.request.Parameters
import io.ktor.client.HttpClient
import io.ktor.client.engine.mergeHeaders
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.util.InternalAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okio.Path.Companion.toOkioPath
import okio.Source
import okio.buffer
import okio.sink
import org.ireader.core_api.http.okhttp
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.HttpSource
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.image_loader.BookCover
import org.ireader.image_loader.LibraryCovers
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalCoilApi::class)
internal class LibraryMangaFetcherFactory(
    private val defaultClient: OkHttpClient,
    private val libraryCovers: LibraryCovers,
    private val getLocalCatalog: GetLocalCatalog,
    private val coilCache: Cache,
    private val client: HttpClient,
    val data: BookCover,
) : Fetcher.Factory<BookCover> {
    override fun create(data: BookCover, options: Options, imageLoader: ImageLoader): Fetcher {
        return LibraryMangaFetcher(
            data = data,
            coilCache = coilCache,
            defaultClient = defaultClient,
            getLocalCatalog = getLocalCatalog,
            libraryCovers = libraryCovers,
            options = options,
            client = client,
            diskCache = imageLoader.diskCache
        )
    }
}

internal class LibraryMangaFetcherFactoryKeyer() : Keyer<BookCover> {
    override fun key(data: BookCover, options: Options): String? {
        return data.cover.takeIf { it.isNotBlank() }
    }
}

internal class LibraryMangaFetcher(
    private val defaultClient: OkHttpClient,
    private val libraryCovers: LibraryCovers,
    private val getLocalCatalog: GetLocalCatalog,
    private val coilCache: Cache,
    private val options: Options,
    private val client: HttpClient,
    private val diskCache: DiskCache?,
    val data: BookCover,
) : Fetcher {
    private val diskCacheKey: String? by lazy {
        LibraryMangaFetcherFactoryKeyer().key(
            data,
            options
        )
    }
    private lateinit var url: String
    private var source: org.ireader.core_api.source.Source? = null

    override suspend fun fetch(): FetchResult {
        source = getLocalCatalog.get(data.sourceId)?.source
        url = diskCacheKey ?: error("No Cover specified")

        return when (getResourceType(url)) {
            Type.URL -> httpLoader()
            Type.File -> fileLoader(File(url.substringAfter("file://")))
            null -> error("Invalid image")
        }
    }


    companion object {
        const val USE_CUSTOM_COVER = "use_custom_cover"

        private val CACHE_CONTROL_FORCE_NETWORK_NO_CACHE =
            CacheControl.Builder().noCache().noStore().build()
        private val CACHE_CONTROL_NO_NETWORK_NO_CACHE =
            CacheControl.Builder().noCache().onlyIfCached().build()
    }

    private fun fileLoader(file: File): FetchResult {
        return SourceResult(
            source = ImageSource(file = file.toOkioPath(), diskCacheKey = diskCacheKey),
            mimeType = "image/*",
            dataSource = DataSource.DISK,
        )
    }

    private suspend fun httpLoader(): FetchResult {
        val file = libraryCovers.find(data.id).toFile()
        if (file.exists() && file.lastModified() != 0L) {
            return fileLoader(file)
        }


        var snapshot = readFromDiskCache()
        try {
            // Fetch from disk cache
            if (snapshot != null) {
                val snapshotCoverCache = moveSnapshotToCoverCache(snapshot, file)
                if (snapshotCoverCache != null) {
                    // Read from cover cache after added to library
                    return fileLoader(snapshotCoverCache)
                }

                // Read from snapshot
                return SourceResult(
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
                val responseCoverCache = writeResponseToCoverCache(response, file)
                if (responseCoverCache != null) {
                    return fileLoader(responseCoverCache)
                }

                // Read from disk cache
                snapshot = writeToDiskCache(snapshot, response)
                if (snapshot != null) {
                    return SourceResult(
                        source = snapshot.toImageSource(),
                        mimeType = "image/*",
                        dataSource = DataSource.NETWORK,
                    )
                }

                // Read from response if cache is unused or unusable
                return SourceResult(
                    source = ImageSource(source = responseBody.source(), context = options.context),
                    mimeType = "image/*",
                    dataSource = if (response.cacheResponse != null) DataSource.DISK else DataSource.NETWORK,
                )
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
        val client =
            (source as? HttpSource)?.getCoverRequest(data.cover)?.first?.okhttp ?: defaultClient
        val response = client.newCall(newRequest()).await()
        if (!response.isSuccessful && response.code != HttpURLConnection.HTTP_NOT_MODIFIED) {
            response.body?.closeQuietly()
            throw HttpException(response)
        }
        return response
    }

    private fun newRequest(): Request {

        val request = Request.Builder()
            .url(url)
            .headers(
                (source as? HttpSource)?.getCoverRequest(data.cover)?.second?.build()
                    ?.convertToOkHttpRequest()?.headers ?: options.headers
            )
            // Support attaching custom data to the network request.
            .tag(Parameters::class.java, options.parameters)

        val diskRead = options.diskCachePolicy.readEnabled
        val networkRead = options.networkCachePolicy.readEnabled
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

    @OptIn(ExperimentalCoilApi::class)
    private fun moveSnapshotToCoverCache(snapshot: DiskCache.Snapshot, cacheFile: File?): File? {
        if (cacheFile == null) return null
        return try {
            diskCache?.run {
                fileSystem.source(snapshot.data).use { input ->
                    writeSourceToCoverCache(input, cacheFile)
                }
                remove(diskCacheKey!!)
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

    @OptIn(ExperimentalCoilApi::class)
    private fun readFromDiskCache(): DiskCache.Snapshot? {
        return if (options.diskCachePolicy.readEnabled) diskCache?.get(diskCacheKey!!) else null
    }

    @OptIn(ExperimentalCoilApi::class)
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
            diskCache?.edit(diskCacheKey!!)
        } ?: return null
        try {
            diskCache?.fileSystem?.write(editor.data) {
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

    @OptIn(ExperimentalCoilApi::class)
    private fun DiskCache.Snapshot.toImageSource(): ImageSource {
        return ImageSource(file = data, diskCacheKey = diskCacheKey, closeable = this)
    }
}

/**
 * Converts a ktor request to okhttp. Note that it does not support sending a request body. If we
 * ever need it we could use reflection to call this other method instead:
 * https://github.com/ktorio/ktor/blob/1.6.4/ktor-client/ktor-client-okhttp/jvm/src/io/ktor/client/engine/okhttp/OkHttpEngine.kt#L180
 */
@OptIn(InternalAPI::class)
private fun HttpRequestData.convertToOkHttpRequest(): Request {
    val builder = Request.Builder()

    with(builder) {
        url(url.toString())
        mergeHeaders(headers, body) { key, value ->
            if (key == HttpHeaders.ContentLength) return@mergeHeaders
            addHeader(key, value)
        }

        method(method.value, null)
    }

    return builder.build()
}

private fun getResourceType(cover: String): Type? {
    return when {
        cover.isEmpty() -> null
        cover.startsWith("http") || cover.startsWith("Custom-", true) -> Type.URL
        cover.startsWith("/") || cover.startsWith("file://") -> Type.File
        else -> null
    }
}

enum class Type {
    File, URL;
}

// Based on https://github.com/gildor/kotlin-coroutines-okhttp
suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        enqueue(
            object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        continuation.resumeWithException(HttpException(response))
                        return
                    }

                    continuation.resume(response) {
                        response.body?.closeQuietly()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    // Don't bother with resuming the continuation if it is already cancelled.
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                }
            },
        )

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                // Ignore cancel exception
            }
        }
    }
}