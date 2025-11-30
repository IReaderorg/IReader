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
        // Default disk cache: 50MB for cover images
        private const val DISK_CACHE_SIZE_DEFAULT = 50L * 1024 * 1024
        
        // Memory cache percentages based on device tier
        private const val MEMORY_CACHE_PERCENT_LOW = 0.15
        private const val MEMORY_CACHE_PERCENT_MEDIUM = 0.20
        private const val MEMORY_CACHE_PERCENT_HIGH = 0.25
        
        // Crossfade durations based on device tier
        private const val CROSSFADE_LOW = 100
        private const val CROSSFADE_MEDIUM = 200
        private const val CROSSFADE_HIGH = 300
    }
    
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        // Get device performance tier for optimizations
        val performanceTier = DevicePerformanceUtil.getPerformanceTier(context as Context)
        val isLowEnd = performanceTier == DevicePerformanceUtil.PerformanceTier.LOW
        val isHighEnd = performanceTier == DevicePerformanceUtil.PerformanceTier.HIGH
        
        return ImageLoader.Builder(context).apply {
            val diskCacheInit = { CoilDiskCache.get(context, performanceTier) }
            val memoryCacheInit = { CoilMemoryCache.get(context, performanceTier) }
            val callFactoryInit = { client.default.okhttp }
            
            // Note: Coil 3 uses default network fetcher for HTTP/HTTPS URLs
            // The OkHttp client is used by BookCoverFetcher for custom fetching
            
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
            
            // Memory cache for fast in-memory access
            memoryCache(memoryCacheInit)
            
            // Disk cache for persistent storage
            diskCache(diskCacheInit)
            
            // Performance-based crossfade animation duration
            val crossfadeDuration = when (performanceTier) {
                DevicePerformanceUtil.PerformanceTier.LOW -> CROSSFADE_LOW
                DevicePerformanceUtil.PerformanceTier.MEDIUM -> CROSSFADE_MEDIUM
                DevicePerformanceUtil.PerformanceTier.HIGH -> CROSSFADE_HIGH
            }
            crossfade(crossfadeDuration)
            
            // Bitmap configuration based on device tier
            when {
                isLowEnd -> {
                    // Use RGB_565 on low-end devices for better memory efficiency
                    // RGB_565 uses 2 bytes per pixel vs 4 bytes for ARGB_8888
                    bitmapConfig(Bitmap.Config.RGB_565)
                    allowRgb565(true)
                    // Disable hardware bitmaps on low-end to avoid GPU memory pressure
                    allowHardware(false)
                }
                isHighEnd && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    // Use hardware bitmaps on high-end devices for better performance
                    // Hardware bitmaps are stored in GPU memory and render faster
                    allowHardware(true)
                    bitmapConfig(Bitmap.Config.ARGB_8888)
                }
                else -> {
                    // Medium tier: standard config with hardware bitmaps if supported
                    allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    bitmapConfig(Bitmap.Config.ARGB_8888)
                }
            }
            
            // Use a limited dispatcher for decoding on low-end devices
            // This prevents too many concurrent decode operations
            if (isLowEnd) {
                fetcherCoroutineContext(Dispatchers.IO.limitedParallelism(2))
                decoderCoroutineContext(Dispatchers.IO.limitedParallelism(2))
            } else if (performanceTier == DevicePerformanceUtil.PerformanceTier.MEDIUM) {
                fetcherCoroutineContext(Dispatchers.IO.limitedParallelism(4))
                decoderCoroutineContext(Dispatchers.IO.limitedParallelism(4))
            }
            // High-end devices use default parallelism

        }.build()
    }

    /**
     * Memory cache for fast in-memory image access.
     * Cache size and reference types are adjusted based on device performance tier.
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
                
                // Configure strong/weak reference ratio based on device tier
                // Strong references are faster but use more memory
                // Weak references can be GC'd under memory pressure
                val (strongPercent, weakPercent) = when (performanceTier) {
                    DevicePerformanceUtil.PerformanceTier.LOW -> 0.25 to 0.10  // More aggressive GC
                    DevicePerformanceUtil.PerformanceTier.MEDIUM -> 0.50 to 0.25
                    DevicePerformanceUtil.PerformanceTier.HIGH -> 0.75 to 0.25  // Keep more in memory
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
     * Direct copy of Coil's internal SingletonDiskCache so that [BookCoverFetcher] can access it.
     * Cache size is adjusted based on device performance tier.
     */
    internal object CoilDiskCache {

        private const val FOLDER_NAME = "image_cache"
        private var instance: DiskCache? = null

        @Synchronized
        fun get(context: Context, performanceTier: DevicePerformanceUtil.PerformanceTier): DiskCache {
            return instance ?: run {
                val safeCacheDir = context.cacheDir.apply { mkdirs() }
                // Adjust disk cache size based on device tier
                val diskCacheSize = DevicePerformanceUtil.getRecommendedImageCacheSize(context)
                
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
