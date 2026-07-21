package ireader.core.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build

/**
 * Utility for detecting device performance capabilities
 * and adjusting app behavior for older/low-end devices.
 */
object DevicePerformanceUtil {
    
    private var cachedTier: PerformanceTier? = null
    
    enum class PerformanceTier {
        LOW,      // < 3GB RAM or API < 26
        MEDIUM,   // 3-6GB RAM
        HIGH      // > 6GB RAM
    }
    
    fun getPerformanceTier(context: Context): PerformanceTier {
        cachedTier?.let { return it }
        
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        
        val tier = when {
            Build.VERSION.SDK_INT < 28 || totalRamGb < 3.0 -> PerformanceTier.LOW
            totalRamGb < 6.0 -> PerformanceTier.MEDIUM
            else -> PerformanceTier.HIGH
        }
        
        cachedTier = tier
        return tier
    }
    
    fun isLowEndDevice(context: Context): Boolean {
        return getPerformanceTier(context) == PerformanceTier.LOW
    }
    
    /**
     * Get recommended image cache size based on device capabilities
     */
    fun getRecommendedImageCacheSize(context: Context): Long {
        return when (getPerformanceTier(context)) {
            PerformanceTier.LOW -> 50L * 1024 * 1024      // 50MB
            PerformanceTier.MEDIUM -> 100L * 1024 * 1024  // 100MB
            PerformanceTier.HIGH -> 250L * 1024 * 1024    // 250MB
        }
    }
    
    /**
     * Get recommended animation duration multiplier
     */
    fun getAnimationMultiplier(context: Context): Float {
        return when (getPerformanceTier(context)) {
            PerformanceTier.LOW -> 0.5f
            PerformanceTier.MEDIUM -> 0.75f
            PerformanceTier.HIGH -> 1.0f
        }
    }
    
    /**
     * Get recommended prefetch distance for lazy lists
     */
    fun getPrefetchDistance(context: Context): Int {
        return when (getPerformanceTier(context)) {
            PerformanceTier.LOW -> 2
            PerformanceTier.MEDIUM -> 4
            PerformanceTier.HIGH -> 6
        }
    }
}
