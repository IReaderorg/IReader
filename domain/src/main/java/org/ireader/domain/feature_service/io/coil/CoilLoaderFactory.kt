package org.ireader.domain.feature_service.io.coil


import android.app.Application
import coil.ComponentRegistry
import coil.ImageLoader
import coil.disk.DiskCache
import okhttp3.Cache
import org.ireader.core_api.http.HttpClients
import org.ireader.core_api.http.okhttp
import org.ireader.domain.catalog.interactor.GetLocalCatalog
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.feature_service.io.BookCover
import org.ireader.domain.feature_service.io.LibraryCovers
import javax.inject.Inject

class CoilLoaderFactory @Inject constructor(
    private val context: Application,
    private val libraryCovers: LibraryCovers,
    private val extensions: CatalogStore,
    private val client: HttpClients,
    private val getLocalCatalog: GetLocalCatalog,
)  {

    fun create(): ImageLoader {
        val cache = Cache(context.cacheDir.resolve("image_cache"),15L * 1024 * 1024)

        val okhttpClient = client.default.okhttp
        //Faking a book cover here
        val libraryFetcher = LibraryMangaFetcher(
            okhttpClient,
            libraryCovers,
            getLocalCatalog,
            cache,
            data = BookCover(0,0,"", false),
            context = context
        )

        return ImageLoader.Builder(context)
            .components(fun ComponentRegistry.Builder.() {
                add(libraryFetcher)
                add(CatalogRemoteMapper())
                add(CatalogInstalledFetcher.Factory())
            })
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .build()
            }
            .build()
    }


}