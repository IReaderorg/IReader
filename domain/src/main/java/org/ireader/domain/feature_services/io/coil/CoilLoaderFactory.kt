package org.ireader.domain.feature_services.io.coil


import android.app.Application
import coil.ImageLoader
import coil.util.CoilUtils
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.feature_services.io.LibraryCovers
import org.ireader.domain.feature_services.io.LibraryMangaFetcher
import tachiyomi.core.http.HttpClients
import tachiyomi.core.http.okhttp
import javax.inject.Inject

class CoilLoaderFactory @Inject constructor(
    private val context: Application,
    private val libraryCovers: LibraryCovers,
    private val extensions: CatalogStore,
    private val client: HttpClients,
) {
    fun create(): ImageLoader {
        val coilCache = CoilUtils.createDefaultCache(context)

        val okhttpClient = client.default.okhttp
        val libraryFetcher = LibraryMangaFetcher(
            okhttpClient, libraryCovers, extension = extensions, coilCache = coilCache
        )

        return ImageLoader.Builder(context)
            .componentRegistry {
                add(libraryFetcher)
                add(CatalogRemoteMapper())
                add(CatalogInstalledFetcher(context))
            }
            .okHttpClient(okhttpClient.newBuilder().cache(coilCache).build())
            .build()
    }

}