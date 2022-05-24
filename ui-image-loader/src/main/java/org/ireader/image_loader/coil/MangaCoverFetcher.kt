package org.ireader.image_loader.coil

import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.key.Keyer
import coil.request.Options
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mergeHeaders
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.util.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Path.Companion.toOkioPath
import org.ireader.core_api.http.okhttp
import org.ireader.core_api.io.saveTo
import org.ireader.core_api.source.HttpSource
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.image_loader.BookCover
import org.ireader.image_loader.LibraryCovers
import java.io.File
import java.util.Calendar

internal class LibraryMangaFetcherFactory(
    private val defaultClient: OkHttpClient,
    private val libraryCovers: LibraryCovers,
    private val getLocalCatalog: GetLocalCatalog,
    private val coilCache: Cache,
    private val client: HttpClient,
    val data: BookCover,
) : Fetcher.Factory<BookCover> {
    override fun create(data: BookCover, options: Options, imageLoader: ImageLoader): Fetcher? {
        return LibraryMangaFetcher(
            data = data,
            coilCache = coilCache,
            defaultClient = defaultClient,
            getLocalCatalog = getLocalCatalog,
            libraryCovers = libraryCovers,
            options = options,
            client = client
        )
    }
}
internal class LibraryMangaFetcherFactoryKeyer(
    private val libraryCovers: LibraryCovers,
) : Keyer<BookCover> {

    override fun key(data: BookCover, options: Options): String? {
        return when (getResourceType(data.cover)) {
            Type.File -> {
                val cover = File(data.cover.substringAfter("file://"))
                "${data.cover}_${cover.lastModified()}"
            }
            Type.URL -> {
                val cover = libraryCovers.find(data.id).toFile()
                if (data.favorite && (!cover.exists() || cover.lastModified() == 0L)) {
                    null
                } else {
                    "${data.cover}_${cover.lastModified()}"
                }
            }
            null -> null
        }
    }
}

internal class LibraryMangaFetcher(
    private val defaultClient: OkHttpClient,
    private val libraryCovers: LibraryCovers,
    private val getLocalCatalog: GetLocalCatalog,
    private val coilCache: Cache,
    private val options: Options,
    private val client: HttpClient,
    val data: BookCover,
) : Fetcher {
    private val diskCacheKey: String? by lazy { LibraryMangaFetcherFactoryKeyer(libraryCovers).key(data, options) }
    private lateinit var url: String

    override suspend fun fetch(): FetchResult? {
        url = diskCacheKey ?: error("No Cover specified")
        return when (getResourceType(data.cover)) {
            Type.File -> getFileLoader(data)
            Type.URL -> getUrlLoader(data)
            null -> error("Invalid Image")
        }
    }

    private fun getFileLoader(manga: BookCover): SourceResult {
        val file = File(manga.cover.substringAfter("file://"))
        return getFileLoader(file)
    }

    private fun getFileLoader(file: File): SourceResult {
        return SourceResult(
            source = ImageSource(file = file.toOkioPath(), diskCacheKey = diskCacheKey),
            mimeType = "image/*",
            dataSource = DataSource.DISK
        )
    }
    //TODO need to replace this with ktor
    private suspend fun getUrlLoader(manga: BookCover): SourceResult {
        val file = libraryCovers.find(manga.id).toFile()
        if (file.exists() && file.lastModified() != 0L) {
            return getFileLoader(file)
        }

        val call = getCall(manga)

        // TODO this crashes if using suspending call due to a compiler bug
        val response = withContext(Dispatchers.IO) { call.execute() }
        val body = checkNotNull(response.body) { "Response received null body" }

        if (manga.favorite) {
            // If the cover isn't already saved or the size is different, save it
            if (!file.exists() || file.length() != body.contentLength()) {
                val tmpFile = File(file.absolutePath + ".tmp")
                try {
                    body.source().saveTo(tmpFile)
                    tmpFile.renameTo(file)
                } finally {
                    tmpFile.delete()
                }
                return getFileLoader(file)
            }
            // If the cover is already saved but both covers have the same size, use the saved one
            if (file.exists() && file.length() == body.contentLength()) {
                body.close()
                file.setLastModified(Calendar.getInstance().timeInMillis)
                return getFileLoader(file)
            }
        }

        // Fallback to image from source
        return SourceResult(
            source = ImageSource(body.source(), options.context),
            mimeType = "image/*",
            dataSource = if (response.cacheResponse != null) DataSource.DISK else DataSource.NETWORK
        )
    }
    
    private suspend fun getResponseBody(manga: BookCover) : ResponseBody? {
        val catalog = getLocalCatalog.get(manga.sourceId)
        val source = catalog?.source as? HttpSource

        val clientAndRequest = source?.getCoverRequest(manga.cover)
       return clientAndRequest?.first?.get(manga.cover) {
            clientAndRequest.second.build()
        }?.body()
    }

    private fun getCall(manga: BookCover): Call {
        val catalog = getLocalCatalog.get(manga.sourceId)
        val source = catalog?.source as? HttpSource

        val clientAndRequest = source?.getCoverRequest(manga.cover)

        val newClient = (clientAndRequest?.first?.okhttp ?: defaultClient).newBuilder()
            .cache(coilCache)
            .build()

        val request = clientAndRequest?.second?.build()?.convertToOkHttpRequest()
            ?: Request.Builder().url(manga.cover).build()

        return newClient.newCall(request)
    }
    companion object {
        const val USE_CUSTOM_COVER = "use_custom_cover"

        private val CACHE_CONTROL_FORCE_NETWORK_NO_CACHE = CacheControl.Builder().noCache().noStore().build()
        private val CACHE_CONTROL_NO_NETWORK_NO_CACHE = CacheControl.Builder().noCache().onlyIfCached().build()
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
        cover.startsWith("http") -> Type.URL
        cover.startsWith("/") || cover.startsWith("file://") -> Type.File
        else -> null
    }
}

private enum class Type {
    File, URL;
}
