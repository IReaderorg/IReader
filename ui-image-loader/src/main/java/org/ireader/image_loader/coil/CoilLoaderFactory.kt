package org.ireader.image_loader.coil

import android.app.Application
import coil.ComponentRegistry
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import okhttp3.Cache
import org.ireader.core_api.http.HttpClients
import org.ireader.core_api.http.okhttp
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.image_loader.BookCover
import org.ireader.image_loader.LibraryCovers
import javax.inject.Inject

class CoilLoaderFactory @Inject constructor(
    private val context: Application,
    private val libraryCovers: LibraryCovers,
    private val client: HttpClients,
    private val getLocalCatalog: GetLocalCatalog,
) : ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        val cache = Cache(context.cacheDir.resolve("image_cache"), 15L * 1024 * 1024)

        val okhttpClient = client.default.okhttp
        // Faking a book cover here
        val factory = LibraryMangaFetcherFactory(
            okhttpClient,
            libraryCovers = libraryCovers,
            getLocalCatalog,
            cache,
            data = BookCover(0, 0, "", false),
            client = client.default
        )

        return ImageLoader.Builder(context)
            .components(fun ComponentRegistry.Builder.() {
                add(factory)
                add(CatalogRemoteMapper())
                add(org.ireader.image_loader.coil.CatalogInstalledFetcher.Factory())
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
