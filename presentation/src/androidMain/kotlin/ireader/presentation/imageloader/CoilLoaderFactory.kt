package ireader.presentation.imageloader

import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.request.crossfade
import ireader.core.http.HttpClients
import ireader.core.http.okhttp
import ireader.domain.catalogs.CatalogStore
import ireader.domain.image.CoverCache
import ireader.presentation.imageloader.coil.imageloader.BookCoverKeyer
import ireader.presentation.imageloader.coil.imageloader.BookCoverMapper
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteKeyer
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteMapper
import ireader.presentation.imageloader.coil.imageloader.InstalledCatalogKeyer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okio.FileSystem
import okio.Path.Companion.toOkioPath

class CoilLoaderFactory(
    private val client: HttpClients,
    private val catalogStore: CatalogStore,
    private val coverCache: CoverCache,
) : SingletonImageLoader.Factory {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context).apply {
            val diskCacheInit = { CoilDiskCache.get(context) }
            val callFactoryInit = { client.default.okhttp }
            components {
                add(CatalogInstalledFetcher.Factory(PackageManager(context)))
                add(CatalogRemoteMapper())
//                add(BookCoverMapper())
                add(BookCoverKeyer())
                add(CatalogRemoteKeyer())
                add(InstalledCatalogKeyer())
                add(
                    BookCoverFetcher.Factory(
                        callFactoryLazy = lazy(callFactoryInit),
                        diskCacheLazy = lazy(diskCacheInit),
                        coverCache,
                        catalogStore,
                    )
                )
            }
            diskCache(diskCacheInit)
            crossfade((300).toInt())
            // Coil spawns a new thread for every image load by default
            fetcherDispatcher(Dispatchers.IO.limitedParallelism(8))
            decoderDispatcher(Dispatchers.IO.limitedParallelism(2))

        }.build()
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
                DiskCache.Builder().fileSystem(FileSystem.SYSTEM)
                    .directory(safeCacheDir.resolve(FOLDER_NAME).toOkioPath())
                    .build().also { instance = it }

            }
        }
    }


}
