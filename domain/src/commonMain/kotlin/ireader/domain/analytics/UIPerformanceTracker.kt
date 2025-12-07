//package ireader.domain.analytics
//
//import ireader.core.log.Log
//import ireader.domain.utils.extensions.currentTimeToLong
//import kotlinx.datetime.Clock
//
///**
// * UI performance tracker for monitoring frame rendering times
// * Can be integrated with Compose metrics or custom frame callbacks
// */
//class UIPerformanceTracker(
//    val analyticsManager: AnalyticsManager
//) {
//    private var frameStartTime: Long = 0
//    private var frameCount = 0
//    private var droppedFrames = 0
//    private val targetFrameTime = 16.67 // 60 FPS
//
//    /**
//     * Mark frame start
//     */
//    fun onFrameStart() {
//        try {
//            frameStartTime = currentTimeToLong()
//        } catch (e: Exception) {
//            Log.error { "Failed to mark frame start: ${e.message}" }
//        }
//    }
//
//    /**
//     * Mark frame end and record metrics
//     */
//    fun onFrameEnd(screenName: String? = null) {
//        try {
//            if (frameStartTime == 0L) return
//
//            val frameTime = (currentTimeToLong() - frameStartTime).toDouble() // Already in ms
//            frameCount++
//
//            val context = buildMap {
//                screenName?.let { put("screen", it) }
//                put("frame_number", frameCount.toString())
//            }
//
//            analyticsManager.performanceMonitor.recordFrameTime(frameTime, context)
//
//            // Track dropped frames (frame time > 16.67ms for 60 FPS)
//            if (frameTime > targetFrameTime) {
//                droppedFrames++
//
//                // Log significant frame drops
//                if (frameTime > 32) { // More than 2 frames
//                    Log.warn { "Significant frame drop: ${frameTime.toInt()}ms on $screenName" }
//                }
//            }
//
//            frameStartTime = 0
//        } catch (e: Exception) {
//            Log.error { "Failed to mark frame end: ${e.message}" }
//        }
//    }
//
//    /**
//     * Get frame statistics
//     */
//    fun getFrameStatistics(): FrameStatistics {
//        return try {
//            val dropRate = if (frameCount > 0) {
//                (droppedFrames.toDouble() / frameCount.toDouble()) * 100
//            } else {
//                0.0
//            }
//
//            FrameStatistics(
//                totalFrames = frameCount,
//                droppedFrames = droppedFrames,
//                dropRate = dropRate
//            )
//        } catch (e: Exception) {
//            Log.error { "Failed to get frame statistics: ${e.message}" }
//            FrameStatistics(0, 0, 0.0)
//        }
//    }
//
//    /**
//     * Reset frame statistics
//     */
//    fun reset() {
//        try {
//            frameCount = 0
//            droppedFrames = 0
//            frameStartTime = 0
//        } catch (e: Exception) {
//            Log.error { "Failed to reset frame statistics: ${e.message}" }
//        }
//    }
//
//    /**
//     * Track composition time
//     */
//    inline fun <T> trackComposition(
//        composableName: String,
//        block: () -> T
//    ): T {
//        val startTime = currentTimeToLong()
//        return try {
//            block()
//        } finally {
//            try {
//                val duration = (currentTimeToLong() - startTime).toDouble() // Already in ms
//
//                val context = mapOf(
//                    "composable" to composableName,
//                    "type" to "composition"
//                )
//
//                analyticsManager.performanceMonitor.recordFrameTime(duration, context)
//
//                // Log slow compositions
//                if (duration > 16) {
//                    Log.warn { "Slow composition: $composableName took ${duration.toInt()}ms" }
//                }
//            } catch (e: Exception) {
//                Log.error { "Failed to track composition: ${e.message}" }
//            }
//        }
//    }
//
//    /**
//     * Track recomposition
//     */
//    fun trackRecomposition(composableName: String) {
//        try {
//            val context = mapOf(
//                "composable" to composableName,
//                "type" to "recomposition"
//            )
//
//            analyticsManager.trackFeature("recomposition", context)
//        } catch (e: Exception) {
//            Log.error { "Failed to track recomposition: ${e.message}" }
//        }
//    }
//}
//
///**
// * Frame statistics
// */
//data class FrameStatistics(
//    val totalFrames: Int,
//    val droppedFrames: Int,
//    val dropRate: Double // Percentage
//)
//
///**
// * Composable performance tracker
// * Use this to track individual composable performance
// */
//class ComposablePerformanceTracker(
//    private val name: String,
//    private val uiTracker: UIPerformanceTracker
//) {
//    private var compositionCount = 0
//
//    /**
//     * Track composition
//     */
//    fun onComposition() {
//        compositionCount++
//
//        // Log excessive recompositions
//        if (compositionCount > 100) {
//            Log.warn { "Excessive recompositions: $name has recomposed $compositionCount times" }
//        }
//    }
//
//    /**
//     * Get composition count
//     */
//    fun getCompositionCount(): Int = compositionCount
//
//    /**
//     * Reset composition count
//     */
//    fun reset() {
//        compositionCount = 0
//    }
//}
