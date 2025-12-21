package ireader.presentation.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.decode.DecodeUtils
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.request.allowRgb565
import coil3.request.bitmapConfig
import coil3.request.allowHardware
import coil3.util.DebugLogger
import ireader.core.http.HttpClients
import ireader.core.http.okhttp
import ireader.core.util.DevicePerformanceUtil
import ireader.domain.catalogs.CatalogStore
import ireader.domain.image.CoverCache
import ireader.domain.utils.extensions.ioDispatcher
import ireader.presentation.imageloader.coil.imageloader.BookCoverKeyer
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteKeyer
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteMapper
import ireader.presentation.imageloader.coil.imageloader.InstalledCatalogKeyer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okio.FileSystem
import okio.Path.Companion.toOkioPath

class CoilLoaderFactory(
    private val client: HttpClients,
    private val catalogStore: CatalogStore,
    private val coverCache: CoverCache,
) : SingletonImageLoader.Factory {
    
    companion object {
        // Increased disk cache for faster subsequent loads
        private const val DISK_CACHE_SIZE_DEFAULT = 100L * 1024 * 1024 // 100MB
        
        // Increased memory cache for instant display
        private const val MEMORY_CACHE_PERCENT_LOW = 0.20
        private const val MEMORY_CACHE_PERCENT_MEDIUM = 0.25
        private const val MEMORY_CACHE_PERCENT_HIGH = 0.30
        
        // ZERO crossfade for native-like instant display
        private const val CROSSFADE_LOW = 0
        private const val CROSSFADE_MEDIUM = 0
        private const val CROSSFADE_HIGH = 0 // Even high-end gets instant display
    }
    
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val performanceTier = DevicePerformanceUtil.getPerformanceTier(context as Context)
        val isLowEnd = performanceTier == DevicePerformanceUtil.PerformanceTier.LOW
        val isHighEnd = performanceTier == DevicePerformanceUtil.PerformanceTier.HIGH
        
        return ImageLoader.Builder(context).apply {
            val diskCacheInit = { CoilDiskCache.get(context, performanceTier) }
            val memoryCacheInit = { CoilMemoryCache.get(context, performanceTier) }
            val callFactoryInit = { client.default.okhttp }
            
            components {
                add(CatalogRemoteMapper())
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
            
            // Aggressive memory caching for instant display
            memoryCache(memoryCacheInit)
            
            // Large disk cache for persistent storage
            diskCache(diskCacheInit)
            
            // ZERO crossfade - native-like instant display
            crossfade(0)
            
            // Bitmap configuration optimized for speed
            when {
                isLowEnd -> {
                    // RGB_565 for memory efficiency on low-end
                    bitmapConfig(Bitmap.Config.RGB_565)
                    allowRgb565(true)
                    allowHardware(false)
                }
                isHighEnd && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    // Hardware bitmaps for GPU acceleration on high-end
                    allowHardware(true)
                    bitmapConfig(Bitmap.Config.ARGB_8888)
                }
                else -> {
                    // Medium tier: hardware bitmaps if supported
                    allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    bitmapConfig(Bitmap.Config.ARGB_8888)
                }
            }
            
            // Increased parallelism for faster loading
            if (isLowEnd) {
                fetcherCoroutineContext(ioDispatcher.limitedParallelism(4))
                decoderCoroutineContext(ioDispatcher.limitedParallelism(4))
            } else if (performanceTier == DevicePerformanceUtil.PerformanceTier.MEDIUM) {
                fetcherCoroutineContext(ioDispatcher.limitedParallelism(6))
                decoderCoroutineContext(ioDispatcher.limitedParallelism(6))
            } else {
                // High-end: maximum parallelism
                fetcherCoroutineContext(ioDispatcher.limitedParallelism(8))
                decoderCoroutineContext(ioDispatcher.limitedParallelism(8))
            }

        }.build()
    }

    /**
     * Memory cache optimized for instant display.
     * Larger cache = more images ready instantly.
     */
    internal object CoilMemoryCache {
        private var instance: MemoryCache? = null
        
        @Synchronized
        fun get(context: Context, performanceTier: DevicePerformanceUtil.PerformanceTier): MemoryCache {
            return instance ?: run {
                val cachePercent = when (performanceTier) {
                    DevicePerformanceUtil.PerformanceTier.LOW -> MEMORY_CACHE_PERCENT_LOW
                    DevicePerformanceUtil.PerformanceTier.MEDIUM -> MEMORY_CACHE_PERCENT_MEDIUM
                    DevicePerformanceUtil.PerformanceTier.HIGH -> MEMORY_CACHE_PERCENT_HIGH
                }
                
                MemoryCache.Builder()
                    .maxSizePercent(context, cachePercent)
                    .strongReferencesEnabled(true)
                    .weakReferencesEnabled(true)
                    .build()
                    .also { instance = it }
            }
        }
    }

    /**
     * Disk cache for persistent image storage.
     * Larger cache = faster cold starts.
     */
    internal object CoilDiskCache {

        private const val FOLDER_NAME = "image_cache"
        private var instance: DiskCache? = null

        @Synchronized
        fun get(context: Context, performanceTier: DevicePerformanceUtil.PerformanceTier): DiskCache {
            return instance ?: run {
                val safeCacheDir = context.cacheDir.apply { mkdirs() }
                // Increased disk cache for better cold start performance
                val diskCacheSize = when (performanceTier) {
                    DevicePerformanceUtil.PerformanceTier.LOW -> 75L * 1024 * 1024   // 75MB
                    DevicePerformanceUtil.PerformanceTier.MEDIUM -> 150L * 1024 * 1024 // 150MB
                    DevicePerformanceUtil.PerformanceTier.HIGH -> 300L * 1024 * 1024   // 300MB
                }
                
                DiskCache.Builder()
                    .fileSystem(FileSystem.SYSTEM)
                    .directory(safeCacheDir.resolve(FOLDER_NAME).toOkioPath())
                    .maxSizeBytes(diskCacheSize)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
