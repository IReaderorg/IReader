package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.*

/**
 * Performance tests for Piper TTS system.
 * 
 * These tests measure and validate:
 * - Synthesis latency
 * - Memory usage
 * - Throughput
 * - Performance targets from requirements
 * 
 * Performance Targets (from Requirements 5.1, 5.2, 5.3):
 * - Short text synthesis: < 200ms for texts under 100 characters
 * - Memory usage: < 500 MB per loaded voice model
 * - Throughput: Efficient processing of long documents
 * 
 * Note: These tests require native libraries and voice models to be present.
 */
class PiperPerformanceTest {
    
    companion object {
        private var librariesAvailable = false
        private var testModelPath: String? = null
        private var testConfigPath: String? = null
        
        // Performance thresholds
        private const val SHORT_TEXT_LATENCY_MS = 200L
        private const val MEMORY_PER_MODEL_MB = 500L
        private const val MIN_THROUGHPUT_CHARS_PER_SEC = 1000
        
        init {
            try {
                PiperInitializer.resetForTesting()
                val result = kotlinx.coroutines.runBlocking {
                    PiperInitializer.initialize()
                }
                librariesAvailable = result.isSuccess
                
                testModelPath = System.getProperty("test.model.path")
                testConfigPath = System.getProperty("test.config.path")
            } catch (e: Exception) {
                println("Native libraries not available for performance testing: ${e.message}")
                librariesAvailable = false
            }
        }
    }
    
    @BeforeTest
    fun setup() {
        if (!librariesAvailable) {
            println("Skipping performance test - native libraries not available")
        }
    }
    
    // ========== Synthesis Latency Tests ==========
    
    @Test
    fun `test short text synthesis latency meets target`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        // Test with various short texts (under 100 characters)
        val shortTexts = listOf(
            "Hello, world!",
            "This is a test.",
            "The quick brown fox jumps over the lazy dog.",
            "Testing synthesis performance with a slightly longer sentence that is still under limit."
        )
        
        val results = mutableListOf<PerformanceResult>()
        
        // Warm up
        repeat(3) {
            PiperNative.synthesize(instance, shortTexts[0])
        }
        
        shortTexts.forEach { text ->
            assertTrue(text.length < 100, "Test text should be under 100 characters")
            
            val durations = mutableListOf<Long>()
            
            // Run multiple iterations for statistical significance
            repeat(10) {
                val start = System.nanoTime()
                val audio = PiperNative.synthesize(instance, text)
                val duration = (System.nanoTime() - start) / 1_000_000 // Convert to ms
                
                durations.add(duration)
                assertNotNull(audio)
            }
            
            val avgDuration = durations.average()
            val p95Duration = durations.sorted()[9] // 95th percentile
            val minDuration = durations.minOrNull() ?: 0
            val maxDuration = durations.maxOrNull() ?: 0
            
            results.add(PerformanceResult(
                testName = "Short text (${text.length} chars)",
                avgLatencyMs = avgDuration,
                p95LatencyMs = p95Duration.toDouble(),
                minLatencyMs = minDuration,
                maxLatencyMs = maxDuration
            ))
            
            println("Text length ${text.length}: avg=${avgDuration.toInt()}ms, " +
                    "p95=${p95Duration}ms, min=${minDuration}ms, max=${maxDuration}ms")
        }
        
        // Verify performance targets
        results.forEach { result ->
            assertTrue(
                result.avgLatencyMs < SHORT_TEXT_LATENCY_MS,
                "${result.testName}: Average latency ${result.avgLatencyMs}ms exceeds target ${SHORT_TEXT_LATENCY_MS}ms"
            )
        }
        
        PiperNative.shutdown(instance)
        
        println("\n✓ All short text synthesis tests meet latency target of ${SHORT_TEXT_LATENCY_MS}ms")
    }
    
    @Test
    fun `test synthesis latency scales linearly with text length`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        // Test with increasing text lengths
        val textLengths = listOf(50, 100, 200, 500, 1000)
        val results = mutableMapOf<Int, Double>()
        
        textLengths.forEach { length ->
            val text = "Test sentence. ".repeat(length / 15) // Approximate target length
            val actualLength = text.length
            
            // Warm up
            repeat(2) {
                PiperNative.synthesize(instance, text)
            }
            
            val durations = mutableListOf<Long>()
            repeat(5) {
                val start = System.nanoTime()
                PiperNative.synthesize(instance, text)
                val duration = (System.nanoTime() - start) / 1_000_000
                durations.add(duration)
            }
            
            val avgDuration = durations.average()
            results[actualLength] = avgDuration
            
            println("Length $actualLength chars: ${avgDuration.toInt()}ms " +
                    "(${(actualLength / avgDuration * 1000).toInt()} chars/sec)")
        }
        
        // Verify roughly linear scaling
        val sortedResults = results.toList().sortedBy { it.first }
        for (i in 1 until sortedResults.size) {
            val (prevLength, prevDuration) = sortedResults[i - 1]
            val (currLength, currDuration) = sortedResults[i]
            
            val lengthRatio = currLength.toDouble() / prevLength
            val durationRatio = currDuration / prevDuration
            
            // Duration ratio should be roughly proportional to length ratio
            // Allow for some variance (within 2x)
            assertTrue(
                durationRatio < lengthRatio * 2,
                "Latency scaling appears non-linear: ${lengthRatio}x length -> ${durationRatio}x duration"
            )
        }
        
        PiperNative.shutdown(instance)
        
        println("\n✓ Synthesis latency scales approximately linearly with text length")
    }
    
    @Test
    fun `test first synthesis latency vs subsequent`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val text = "Performance test for first vs subsequent synthesis."
        
        // Measure first synthesis (cold)
        val firstStart = System.nanoTime()
        val firstAudio = PiperNative.synthesize(instance, text)
        val firstDuration = (System.nanoTime() - firstStart) / 1_000_000
        
        assertNotNull(firstAudio)
        
        // Measure subsequent syntheses (warm)
        val subsequentDurations = mutableListOf<Long>()
        repeat(10) {
            val start = System.nanoTime()
            PiperNative.synthesize(instance, text)
            val duration = (System.nanoTime() - start) / 1_000_000
            subsequentDurations.add(duration)
        }
        
        val avgSubsequent = subsequentDurations.average()
        
        println("First synthesis: ${firstDuration}ms")
        println("Subsequent average: ${avgSubsequent.toInt()}ms")
        println("Speedup: ${(firstDuration / avgSubsequent).format(2)}x")
        
        // First synthesis may be slower due to initialization, but not excessively
        assertTrue(
            firstDuration < avgSubsequent * 5,
            "First synthesis should not be more than 5x slower than subsequent"
        )
        
        PiperNative.shutdown(instance)
    }
    
    // ========== Memory Usage Tests ==========
    
    @Test
    fun `test memory usage per voice model`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val runtime = Runtime.getRuntime()
        
        // Force garbage collection and measure baseline
        System.gc()
        Thread.sleep(200)
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        // Load voice model
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        assertTrue(instance > 0)
        
        // Perform some synthesis to ensure everything is loaded
        repeat(5) {
            PiperNative.synthesize(instance, "Memory test")
        }
        
        // Measure memory after loading
        System.gc()
        Thread.sleep(200)
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        
        val memoryUsedMB = (memoryAfter - memoryBefore) / (1024 * 1024)
        
        println("Memory used by voice model: ${memoryUsedMB}MB")
        
        // Verify memory usage is within target
        assertTrue(
            memoryUsedMB < MEMORY_PER_MODEL_MB,
            "Memory usage ${memoryUsedMB}MB exceeds target ${MEMORY_PER_MODEL_MB}MB per model"
        )
        
        PiperNative.shutdown(instance)
        
        // Verify memory is released after shutdown
        System.gc()
        Thread.sleep(200)
        val memoryAfterShutdown = runtime.totalMemory() - runtime.freeMemory()
        val memoryReleased = memoryAfter - memoryAfterShutdown
        
        println("Memory released after shutdown: ${memoryReleased / (1024 * 1024)}MB")
        
        println("\n✓ Memory usage per model is within target of ${MEMORY_PER_MODEL_MB}MB")
    }
    
    @Test
    fun `test memory usage with multiple voice models`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val runtime = Runtime.getRuntime()
        System.gc()
        Thread.sleep(200)
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        val instances = mutableListOf<Long>()
        val modelCount = 3
        
        // Load multiple instances of the same model
        repeat(modelCount) {
            val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
            instances.add(instance)
            
            // Perform synthesis with each
            PiperNative.synthesize(instance, "Multi-model test")
        }
        
        System.gc()
        Thread.sleep(200)
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        
        val totalMemoryMB = (memoryAfter - memoryBefore) / (1024 * 1024)
        val memoryPerModelMB = totalMemoryMB / modelCount
        
        println("Total memory for $modelCount models: ${totalMemoryMB}MB")
        println("Average memory per model: ${memoryPerModelMB}MB")
        
        // Each model should still be within target
        assertTrue(
            memoryPerModelMB < MEMORY_PER_MODEL_MB,
            "Average memory per model ${memoryPerModelMB}MB exceeds target ${MEMORY_PER_MODEL_MB}MB"
        )
        
        // Cleanup
        instances.forEach { PiperNative.shutdown(it) }
        
        println("\n✓ Multiple voice models stay within memory targets")
    }
    
    @Test
    fun `test memory stability during continuous synthesis`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val runtime = Runtime.getRuntime()
        
        val text = "Memory stability test. "
        val iterations = 50
        val memorySnapshots = mutableListOf<Long>()
        
        // Perform many synthesis operations and track memory
        repeat(iterations) { i ->
            PiperNative.synthesize(instance, text)
            
            if (i % 10 == 0) {
                System.gc()
                Thread.sleep(50)
                val memory = runtime.totalMemory() - runtime.freeMemory()
                memorySnapshots.add(memory)
            }
        }
        
        // Calculate memory growth
        val firstSnapshot = memorySnapshots.first()
        val lastSnapshot = memorySnapshots.last()
        val memoryGrowthMB = (lastSnapshot - firstSnapshot) / (1024 * 1024)
        
        println("Memory snapshots (MB): ${memorySnapshots.map { it / (1024 * 1024) }}")
        println("Memory growth over $iterations iterations: ${memoryGrowthMB}MB")
        
        // Memory growth should be minimal (less than 50MB)
        assertTrue(
            memoryGrowthMB < 50,
            "Memory growth ${memoryGrowthMB}MB indicates potential memory leak"
        )
        
        PiperNative.shutdown(instance)
        
        println("\n✓ Memory remains stable during continuous synthesis")
    }
    
    // ========== Throughput Tests ==========
    
    @Test
    fun `test synthesis throughput meets target`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        // Test with various text lengths
        val testTexts = listOf(
            "Short text. " * 10,  // ~120 chars
            "Medium text. " * 50, // ~650 chars
            "Long text. " * 100   // ~1100 chars
        )
        
        testTexts.forEach { text ->
            val textLength = text.length
            
            // Warm up
            repeat(2) {
                PiperNative.synthesize(instance, text)
            }
            
            // Measure throughput
            val durations = mutableListOf<Long>()
            repeat(5) {
                val start = System.nanoTime()
                PiperNative.synthesize(instance, text)
                val duration = (System.nanoTime() - start) / 1_000_000
                durations.add(duration)
            }
            
            val avgDuration = durations.average()
            val throughput = (textLength / avgDuration * 1000).toInt() // chars/sec
            
            println("Text length $textLength: ${avgDuration.toInt()}ms, " +
                    "throughput: $throughput chars/sec")
            
            // Verify throughput meets minimum target
            assertTrue(
                throughput >= MIN_THROUGHPUT_CHARS_PER_SEC,
                "Throughput ${throughput} chars/sec below target ${MIN_THROUGHPUT_CHARS_PER_SEC} chars/sec"
            )
        }
        
        PiperNative.shutdown(instance)
        
        println("\n✓ Synthesis throughput meets target of ${MIN_THROUGHPUT_CHARS_PER_SEC} chars/sec")
    }
    
    @Test
    fun `test throughput with different speech rates`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val text = "Testing throughput with different speech rates. " * 10
        
        val speechRates = listOf(0.5f, 1.0f, 1.5f, 2.0f)
        
        speechRates.forEach { rate ->
            PiperNative.setSpeechRate(instance, rate)
            
            // Warm up
            PiperNative.synthesize(instance, text)
            
            val durations = mutableListOf<Long>()
            repeat(5) {
                val start = System.nanoTime()
                val audio = PiperNative.synthesize(instance, text)
                val duration = (System.nanoTime() - start) / 1_000_000
                durations.add(duration)
                assertNotNull(audio)
            }
            
            val avgDuration = durations.average()
            val throughput = (text.length / avgDuration * 1000).toInt()
            
            println("Speech rate ${rate}x: ${avgDuration.toInt()}ms, " +
                    "throughput: $throughput chars/sec")
        }
        
        PiperNative.shutdown(instance)
    }
    
    // ========== Performance Regression Tests ==========
    
    @Test
    fun `test performance consistency across multiple runs`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val text = "Performance consistency test."
        
        val runs = 20
        val durations = mutableListOf<Long>()
        
        // Warm up
        repeat(5) {
            PiperNative.synthesize(instance, text)
        }
        
        // Measure multiple runs
        repeat(runs) {
            val start = System.nanoTime()
            PiperNative.synthesize(instance, text)
            val duration = (System.nanoTime() - start) / 1_000_000
            durations.add(duration)
        }
        
        val avgDuration = durations.average()
        val stdDev = calculateStdDev(durations, avgDuration)
        val coefficientOfVariation = (stdDev / avgDuration) * 100
        
        println("Average: ${avgDuration.toInt()}ms")
        println("Std Dev: ${stdDev.toInt()}ms")
        println("Coefficient of Variation: ${coefficientOfVariation.format(2)}%")
        
        // Performance should be consistent (CV < 20%)
        assertTrue(
            coefficientOfVariation < 20,
            "Performance is inconsistent (CV: ${coefficientOfVariation.format(2)}%)"
        )
        
        PiperNative.shutdown(instance)
        
        println("\n✓ Performance is consistent across multiple runs")
    }
    
    // ========== Performance Summary ==========
    
    @Test
    fun `generate performance summary report`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        println("\n" + "=".repeat(60))
        println("PIPER TTS PERFORMANCE SUMMARY")
        println("=".repeat(60))
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val runtime = Runtime.getRuntime()
        
        // Test 1: Short text latency
        val shortText = "Hello, world!"
        val shortDurations = mutableListOf<Long>()
        repeat(10) {
            val start = System.nanoTime()
            PiperNative.synthesize(instance, shortText)
            shortDurations.add((System.nanoTime() - start) / 1_000_000)
        }
        val shortAvg = shortDurations.average()
        
        // Test 2: Medium text latency
        val mediumText = "This is a test. " * 20
        val mediumDurations = mutableListOf<Long>()
        repeat(10) {
            val start = System.nanoTime()
            PiperNative.synthesize(instance, mediumText)
            mediumDurations.add((System.nanoTime() - start) / 1_000_000)
        }
        val mediumAvg = mediumDurations.average()
        
        // Test 3: Throughput
        val throughput = (mediumText.length / mediumAvg * 1000).toInt()
        
        // Test 4: Memory usage
        System.gc()
        Thread.sleep(100)
        val memory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        
        println("\nLatency Metrics:")
        println("  Short text (${shortText.length} chars): ${shortAvg.toInt()}ms " +
                "(Target: < ${SHORT_TEXT_LATENCY_MS}ms) " +
                if (shortAvg < SHORT_TEXT_LATENCY_MS) "✓" else "✗")
        println("  Medium text (${mediumText.length} chars): ${mediumAvg.toInt()}ms")
        
        println("\nThroughput Metrics:")
        println("  Characters/second: $throughput " +
                "(Target: > ${MIN_THROUGHPUT_CHARS_PER_SEC}) " +
                if (throughput >= MIN_THROUGHPUT_CHARS_PER_SEC) "✓" else "✗")
        
        println("\nMemory Metrics:")
        println("  Current usage: ${memory}MB " +
                "(Target: < ${MEMORY_PER_MODEL_MB}MB per model) " +
                if (memory < MEMORY_PER_MODEL_MB) "✓" else "✗")
        
        println("\n" + "=".repeat(60))
        
        PiperNative.shutdown(instance)
    }
    
    // ========== Helper Classes and Functions ==========
    
    private data class PerformanceResult(
        val testName: String,
        val avgLatencyMs: Double,
        val p95LatencyMs: Double,
        val minLatencyMs: Long,
        val maxLatencyMs: Long
    )
    
    private fun calculateStdDev(values: List<Long>, mean: Double): Double {
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }
    
    private fun Double.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}
