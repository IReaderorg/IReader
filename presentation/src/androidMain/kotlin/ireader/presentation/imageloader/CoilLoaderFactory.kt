package ireader.presentation.imageloader

import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import ireader.core.http.HttpClients
import ireader.core.http.okhttp
import ireader.domain.catalogs.CatalogStore
import ireader.domain.image.CoverCache
import ireader.presentation.imageloader.coil.imageloader.BookCoverKeyer
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
    
    companion object {
        // Disk cache: 50MB for cover images
        private const val DISK_CACHE_SIZE = 50L * 1024 * 1024
        
        // Memory cache: 25% of available app memory
        private const val MEMORY_CACHE_PERCENT = 0.25
    }
    
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context).apply {
            val diskCacheInit = { CoilDiskCache.get(context) }
            val memoryCacheInit = { CoilMemoryCache.get(context) }
            val callFactoryInit = { client.default.okhttp }
            
            // Note: Coil 3 uses default network fetcher for HTTP/HTTPS URLs
            // The OkHttp client is used by BookCoverFetcher for custom fetching
            
            components {
                add(CatalogRemoteMapper())
                // CatalogInstalledFetcher removed - mapper handles all catalog icons via URLs
//                add(CatalogInstalledFetcher.Factory(PackageManager(context)))
//                add(BookCoverMapper())
                add(BookCoverKeyer())
                add(CatalogRemoteKeyer())
                add(InstalledCatalogKeyer())
                add(
                    BookCoverFetcher.BookCoverFactory(
                        callFactoryLazy = lazy(callFactoryInit),
                        diskCacheLazy = lazy(diskCacheInit),
                        catalogStore,
                        coverCache,
                    )
                )
                add(
                    BookCoverFetcher.BookFactory(
                        callFactoryLazy = lazy(callFactoryInit),
                        diskCacheLazy = lazy(diskCacheInit),
                        catalogStore,
                        coverCache,
                    )
                )
            }
            
            // Memory cache for fast in-memory access
            memoryCache(memoryCacheInit)
            
            // Disk cache for persistent storage
            diskCache(diskCacheInit)
            
            // Smooth crossfade animation
            crossfade(300)

        }.build()
    }

    /**
     * Memory cache for fast in-memory image access.
     * Uses 25% of available app memory.
     */
    internal object CoilMemoryCache {
        private var instance: MemoryCache? = null
        
        @Synchronized
        fun get(context: Context): MemoryCache {
            return instance ?: run {
                MemoryCache.Builder()
                    .maxSizePercent(context, MEMORY_CACHE_PERCENT)
                    .build()
                    .also { instance = it }
            }
        }
    }

    /**
     * Direct copy of Coil's internal SingletonDiskCache so that [BookCoverFetcher] can access it.
     * Limited to 50MB to prevent excessive storage usage.
     */
    internal object CoilDiskCache {

        private const val FOLDER_NAME = "image_cache"
        private var instance: DiskCache? = null

        @Synchronized
        fun get(context: Context): DiskCache {
            return instance ?: run {
                val safeCacheDir = context.cacheDir.apply { mkdirs() }
                // Create the singleton disk cache instance with size limit
                DiskCache.Builder()
                    .fileSystem(FileSystem.SYSTEM)
                    .directory(safeCacheDir.resolve(FOLDER_NAME).toOkioPath())
                    .maxSizeBytes(DISK_CACHE_SIZE)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
