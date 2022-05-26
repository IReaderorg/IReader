package org.ireader.image_loader.coil

import android.app.Application
import android.content.Context
import coil.ComponentRegistry
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import org.ireader.core_api.http.HttpClients
import org.ireader.core_api.http.okhttp
import org.ireader.core_catalogs.CatalogStore
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.image_loader.LibraryCovers
import org.ireader.image_loader.coil.cache.CoverCache
import org.ireader.image_loader.coil.image_loaders.BookCoverFetcher

class CoilLoaderFactory(
    private val context: Application,
    private val libraryCovers: LibraryCovers,
    private val client: HttpClients,
    private val getLocalCatalog: GetLocalCatalog,
    private val catalogStore: CatalogStore,
    private val coverCache: CoverCache,
) : ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        val diskCacheInit = { CoilDiskCache.get(context) }
        val callFactoryInit = { client.default.okhttp }

        return ImageLoader.Builder(context)
            .components(fun ComponentRegistry.Builder.() {
                add(BookCoverFetcher.Factory(callFactoryLazy =lazy(callFactoryInit) ,diskCacheLazy = lazy(diskCacheInit),catalogStore, coverCache))
                add(CatalogRemoteMapper())
                add(CatalogInstalledFetcher.Factory())
            })
            .crossfade(300)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .build()
            }
            .callFactory(callFactoryInit)
            .build()
    }
    /**
     * Direct copy of Coil's internal SingletonDiskCache so that [BookCoverFetcher] can access it.
     */
    internal object CoilDiskCache {

        private const val FOLDER_NAME = "image_cache"
        private var instance: DiskCache? = null

        @Synchronized
        fun get(context: Context): DiskCache {
            return instance ?: run {
                val safeCacheDir = context.cacheDir.apply { mkdirs() }
                // Create the singleton disk cache instance.
                DiskCache.Builder()
                    .directory(safeCacheDir.resolve(FOLDER_NAME))
                    .build()
                    .also { instance = it }
            }
        }
    }
}
