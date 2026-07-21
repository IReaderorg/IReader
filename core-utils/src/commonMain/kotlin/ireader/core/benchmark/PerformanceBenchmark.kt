package ireader.core.benchmark

import ireader.core.log.IReaderLog
import ireader.core.system.RuntimeCompat
import ireader.core.system.SystemCompat
import ireader.core.system.ThreadCompat
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Performance benchmarking utilities following Mihon's patterns
 * Provides comprehensive performance testing and memory leak detection
 */
@OptIn(ExperimentalTime::class)
object PerformanceBenchmark {
    
    /**
     * Benchmark database operations
     */
    suspend fun benchmarkDatabaseOperation(
        operationName: String,
        iterations: Int = 100,
        operation: suspend () -> Unit
    ): BenchmarkResult {
        val times = mutableListOf<Duration>()
        var totalMemoryUsed = 0L
        var errors = 0
        
        IReaderLog.benchmark("Starting database benchmark: $operationName ($iterations iterations)")
        
        repeat(iterations) { iteration ->
            try {
                val runtime = RuntimeCompat.getRuntime()
                val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
                
                val duration = measureTime {
                    operation()
                }
                
                val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
                val memoryUsed = memoryAfter - memoryBefore
                
                times.add(duration)
                totalMemoryUsed += memoryUsed
                
                if (iteration % 10 == 0) {
                    IReaderLog.debug("Benchmark progress: $iteration/$iterations iterations completed")
                }
                
            } catch (e: Exception) {
                errors++
                IReaderLog.error("Benchmark iteration $iteration failed", e)
            }
        }
        
        val result = BenchmarkResult(
            operationName = operationName,
            iterations = iterations,
            times = times,
            totalMemoryUsed = totalMemoryUsed,
            errors = errors
        )
        
        IReaderLog.benchmark(
            "Database benchmark completed",
            mapOf(
                "operation" to operationName,
                "avgTime" to "${result.averageTime.inWholeMilliseconds}ms",
                "minTime" to "${result.minTime.inWholeMilliseconds}ms",
                "maxTime" to "${result.maxTime.inWholeMilliseconds}ms",
                "avgMemory" to "${result.averageMemoryUsage / 1024}KB",
                "errors" to errors
            )
        )
        
        return result
    }
    
    /**
     * Benchmark UI operations
     */
    fun benchmarkUIOperation(
        operationName: String,
        iterations: Int = 50,
        operation: () -> Unit
    ): BenchmarkResult {
        val times = mutableListOf<Duration>()
        var totalMemoryUsed = 0L
        var errors = 0
        
        IReaderLog.benchmark("Starting UI benchmark: $operationName ($iterations iterations)")
        
        repeat(iterations) { iteration ->
            try {
                val runtime = RuntimeCompat.getRuntime()
                val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
                
                val duration = measureTime {
                    operation()
                }
                
                val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
                val memoryUsed = memoryAfter - memoryBefore
                
                times.add(duration)
                totalMemoryUsed += memoryUsed
                
            } catch (e: Exception) {
                errors++
                IReaderLog.error("UI benchmark iteration $iteration failed", e)
            }
        }
        
        val result = BenchmarkResult(
            operationName = operationName,
            iterations = iterations,
            times = times,
            totalMemoryUsed = totalMemoryUsed,
            errors = errors
        )
        
        IReaderLog.benchmark(
            "UI benchmark completed",
            mapOf(
                "operation" to operationName,
                "avgTime" to "${result.averageTime.inWholeMilliseconds}ms",
                "60fps_compliant" to (result.averageTime.inWholeMilliseconds <= 16),
                "errors" to errors
            )
        )
        
        return result
    }
    
    /**
     * Memory leak detection
     */
    fun detectMemoryLeaks(
        operationName: String,
        iterations: Int = 10,
        operation: () -> Unit
    ): MemoryLeakReport {
        val memorySnapshots = mutableListOf<Long>()
        
        IReaderLog.benchmark("Starting memory leak detection: $operationName")
        
        // Force garbage collection before starting
        SystemCompat.gc()
        ThreadCompat.sleep(100)
        
        repeat(iterations) { iteration ->
            operation()
            
            // Force garbage collection
            SystemCompat.gc()
            ThreadCompat.sleep(100)
            
            val runtime = RuntimeCompat.getRuntime()
            val memoryUsed = runtime.totalMemory() - runtime.freeMemory()
            memorySnapshots.add(memoryUsed)
            
            IReaderLog.debug("Memory snapshot $iteration: ${memoryUsed / 1024 / 1024}MB")
        }
        
        val report = MemoryLeakReport(
            operationName = operationName,
            iterations = iterations,
            memorySnapshots = memorySnapshots
        )
        
        IReaderLog.benchmark(
            "Memory leak detection completed",
            mapOf(
                "operation" to operationName,
                "initialMemory" to "${report.initialMemory / 1024 / 1024}MB",
                "finalMemory" to "${report.finalMemory / 1024 / 1024}MB",
                "memoryGrowth" to "${report.memoryGrowth / 1024 / 1024}MB",
                "hasLeak" to report.hasMemoryLeak
            )
        )
        
        return report
    }
    
    /**
     * Comprehensive performance test suite
     */
    suspend fun runPerformanceTestSuite(
        suiteName: String,
        tests: List<suspend () -> BenchmarkResult>
    ): PerformanceTestSuite {
        IReaderLog.benchmark("Starting performance test suite: $suiteName")
        
        val results = mutableListOf<BenchmarkResult>()
        val startTime = SystemCompat.currentTimeMillis()
        
        tests.forEach { test ->
            try {
                val result = test()
                results.add(result)
            } catch (e: Exception) {
                IReaderLog.error("Performance test failed", e)
            }
        }
        
        val endTime = SystemCompat.currentTimeMillis()
        val totalDuration = Duration.parse("${endTime - startTime}ms")
        
        val suite = PerformanceTestSuite(
            suiteName = suiteName,
            results = results,
            totalDuration = totalDuration
        )
        
        IReaderLog.benchmark(
            "Performance test suite completed",
            mapOf(
                "suite" to suiteName,
                "totalTests" to results.size,
                "totalDuration" to "${totalDuration.inWholeSeconds}s",
                "avgTestTime" to "${suite.averageTestTime.inWholeMilliseconds}ms"
            )
        )
        
        return suite
    }
}

/**
 * Benchmark result data class
 */
data class BenchmarkResult(
    val operationName: String,
    val iterations: Int,
    val times: List<Duration>,
    val totalMemoryUsed: Long,
    val errors: Int
) {
    val averageTime: Duration = if (times.isNotEmpty()) {
        Duration.parse("${times.sumOf { it.inWholeMilliseconds } / times.size}ms")
    } else Duration.ZERO
    
    val minTime: Duration = times.minOrNull() ?: Duration.ZERO
    val maxTime: Duration = times.maxOrNull() ?: Duration.ZERO
    val averageMemoryUsage: Long = if (iterations > 0) totalMemoryUsed / iterations else 0
    val successRate: Float = if (iterations > 0) (iterations - errors).toFloat() / iterations else 0f
}

/**
 * Memory leak detection report
 */
data class MemoryLeakReport(
    val operationName: String,
    val iterations: Int,
    val memorySnapshots: List<Long>
) {
    val initialMemory: Long = memorySnapshots.firstOrNull() ?: 0
    val finalMemory: Long = memorySnapshots.lastOrNull() ?: 0
    val memoryGrowth: Long = finalMemory - initialMemory
    val hasMemoryLeak: Boolean = memoryGrowth > 10 * 1024 * 1024 // 10MB threshold
}

/**
 * Performance test suite results
 */
data class PerformanceTestSuite(
    val suiteName: String,
    val results: List<BenchmarkResult>,
    val totalDuration: Duration
) {
    val averageTestTime: Duration = if (results.isNotEmpty()) {
        Duration.parse("${totalDuration.inWholeMilliseconds / results.size}ms")
    } else Duration.ZERO
    
    val totalErrors: Int = results.sumOf { it.errors }
    val overallSuccessRate: Float = if (results.isNotEmpty()) {
        results.map { it.successRate }.average().toFloat()
    } else 0f
}
