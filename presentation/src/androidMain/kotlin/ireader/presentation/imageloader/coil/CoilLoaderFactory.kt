package ireader.presentation.imageloader.coil

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import ireader.core.http.HttpClients
import ireader.core.http.okhttp
import ireader.domain.catalogs.CatalogStore
import ireader.domain.image.cache.CoverCache
import ireader.presentation.imageloader.coil.image_loaders.BookCoverFetcher
import ireader.presentation.imageloader.coil.image_loaders.BookCoverKeyer
import ireader.presentation.imageloader.coil.image_loaders.CatalogRemoteKeyer
import ireader.presentation.imageloader.coil.image_loaders.InstalledCatalogKeyer

class CoilLoaderFactory(
    private val context: Application,
    private val client: HttpClients,
    private val catalogStore: CatalogStore,
    private val coverCache: CoverCache,
) : ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(context).apply {
            val diskCacheInit = { CoilDiskCache.get(context) }
            val callFactoryInit = { client.default.okhttp }
            components {
                add(
                    BookCoverFetcher.Factory(
                        callFactoryLazy = lazy(callFactoryInit),
                        diskCacheLazy = lazy(diskCacheInit),
                        coverCache,
                        catalogStore,
                    )
                )
                add(CatalogRemoteMapper())
                add(CatalogInstalledFetcher.Factory())
                add(BookCoverKeyer())
                add(CatalogRemoteKeyer())
                add(InstalledCatalogKeyer())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            crossfade(300)
            diskCache(diskCacheInit)
            allowRgb565(getSystemService<ActivityManager>(context, ActivityManager::class.java)!!.isLowRamDevice)
            callFactory(callFactoryInit)
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
                DiskCache.Builder()
                    .directory(safeCacheDir.resolve(FOLDER_NAME))
                    .build()
                    .also { instance = it }
            }
        }
    }
}
