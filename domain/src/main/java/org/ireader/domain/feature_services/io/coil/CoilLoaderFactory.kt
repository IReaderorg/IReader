package org.ireader.domain.feature_services.io.coil


import android.app.Application
import coil.ImageLoader
import coil.util.CoilUtils
import okhttp3.OkHttpClient
import org.ireader.domain.feature_services.io.LibraryCovers
import org.ireader.domain.feature_services.io.LibraryMangaFetcher
import org.ireader.domain.source.Extensions
import javax.inject.Inject

class CoilLoaderFactory @Inject constructor(
    private val context: Application,
    private val libraryCovers: LibraryCovers,
    private val extensions: Extensions,
    private val client: OkHttpClient,
) {
    fun create(): ImageLoader {
        val coilCache = CoilUtils.createDefaultCache(context)

        val okhttpClient = client
        val libraryFetcher = LibraryMangaFetcher(
            okhttpClient, libraryCovers, extension = extensions, coilCache = coilCache
        )

        return ImageLoader.Builder(context)
            .componentRegistry {
                add(libraryFetcher)
            }
            .okHttpClient(okhttpClient.newBuilder().cache(coilCache).build())
            .build()
    }

}