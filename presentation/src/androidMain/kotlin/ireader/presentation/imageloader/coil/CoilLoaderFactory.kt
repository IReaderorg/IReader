package ireader.presentation.imageloader.coil

import android.content.Context
import android.os.Build
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.ImageLoaderFactory
import com.seiko.imageloader.cache.disk.DiskCache
import com.seiko.imageloader.component.decoder.GifDecoder
import com.seiko.imageloader.component.decoder.ImageDecoderDecoder
import com.seiko.imageloader.component.setupDefaultComponents
import ireader.core.http.HttpClients
import ireader.core.http.okhttp
import ireader.domain.catalogs.CatalogStore
import ireader.domain.image.cache.CoverCache
import ireader.presentation.imageloader.PackageManager
import ireader.presentation.imageloader.coil.imageloader.BookCoverFetcher
import ireader.presentation.imageloader.coil.imageloader.BookCoverKeyer
import ireader.presentation.imageloader.coil.imageloader.BookCoverMapper
import ireader.presentation.imageloader.coil.imageloader.CatalogKeyer
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteKeyer
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteMapper
import ireader.presentation.imageloader.coil.imageloader.InstalledCatalogKeyer
import okio.FileSystem
import okio.Path.Companion.toOkioPath

class CoilLoaderFactory(
        private val context: Context,
        private val client: HttpClients,
        private val catalogStore: CatalogStore,
        private val coverCache: CoverCache,
) : ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader {
            val diskCacheInit = { CoilDiskCache.get(context) }
            val callFactoryInit = { client.default.okhttp }

            components {
                setupDefaultComponents(context)

                add(
                        BookCoverFetcher.Factory(
                                callFactoryLazy = lazy(callFactoryInit),
                                diskCacheLazy = lazy(diskCacheInit),
                                coverCache,
                                catalogStore,
                        )
                )
                add(CatalogRemoteMapper())
                add(BookCoverMapper())
                add(BookCoverKeyer())
                add(CatalogKeyer())
                add(CatalogInstalledFetcher.Factory(PackageManager(context)))
                add(CatalogRemoteKeyer())
                add(InstalledCatalogKeyer())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory(context))
                } else {
                    add(GifDecoder.Factory())
                }
            }
            interceptor {
                diskCache(diskCacheInit)
            }
        }
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
                DiskCache(fileSystem = FileSystem.SYSTEM) {
                    directory(safeCacheDir.resolve(FOLDER_NAME).toOkioPath())
                }
                        .also { instance = it }
            }
        }
    }
}
