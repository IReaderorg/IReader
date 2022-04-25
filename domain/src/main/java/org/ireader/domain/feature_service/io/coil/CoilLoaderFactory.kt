package org.ireader.domain.feature_service.io.coil


import android.app.Application
import coil.ComponentRegistry
import coil.ImageLoader
import coil.ImageLoaderFactory
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
)  : ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        val cache = Cache(context.cacheDir.resolve("image_cache"),15L * 1024 * 1024)

        val okhttpClient = client.default.okhttp
        //Faking a book cover here
        val factory  = LibraryMangaFetcherFactory(
            okhttpClient,
            libraryCovers = libraryCovers,
            getLocalCatalog,
            cache,
            data = BookCover(0,0,"", false),
        )

        return ImageLoader.Builder(context)
            .components(fun ComponentRegistry.Builder.() {
                add(factory)
                add(CatalogRemoteMapper())
                add(CatalogInstalledFetcher.Factory())
                add(LibraryMangaFetcherFactoryKeyer(libraryCovers))
            })
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .build()
            }
            .build()
    }

}