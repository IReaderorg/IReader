package ireader.data.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for ConcurrencyManager.
 * 
 * Verifies:
 * - Semaphore-based concurrency limiting
 * - Mutex-based state protection
 * - Active operation tracking
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConcurrencyManagerTest {
    
    private lateinit var concurrencyManager: ConcurrencyManager
    
    @BeforeTest
    fun setup() {
        concurrencyManager = ConcurrencyManager(maxConcurrentTransfers = 3)
    }
    
    @Test
    fun `withConcurrencyControl should limit concurrent operations`() = runTest {
        // Arrange
        val maxConcurrent = 3
        val manager = ConcurrencyManager(maxConcurrentTransfers = maxConcurrent)
        var maxObservedConcurrent = 0
        var currentConcurrent = 0
        
        // Act - Start 10 operations that should be limited to 3 concurrent
        val jobs = List(10) {
            async {
                manager.withConcurrencyControl {
                    currentConcurrent++
                    if (currentConcurrent > maxObservedConcurrent) {
                        maxObservedConcurrent = currentConcurrent
                    }
                    delay(10.milliseconds)
                    currentConcurrent--
                }
            }
        }
        
        jobs.forEach { it.await() }
        
        // Assert - Should never exceed max concurrent
        assertTrue(maxObservedConcurrent <= maxConcurrent)
    }
    
    @Test
    fun `withMutex should serialize access to shared state`() = runTest {
        // Arrange
        var sharedCounter = 0
        
        // Act - Concurrent increments with mutex protection
        val jobs = List(100) {
            async {
                concurrencyManager.withMutex {
                    val temp = sharedCounter
                    delay(1.milliseconds) // Simulate work
                    sharedCounter = temp + 1
                }
            }
        }
        
        jobs.forEach { it.await() }
        
        // Assert - All increments should be counted (no race conditions)
        assertEquals(100, sharedCounter)
    }
    
    @Test
    fun `getActiveOperationCount should track operations correctly`() = runTest {
        // Arrange
        val manager = ConcurrencyManager(maxConcurrentTransfers = 2)
        
        // Act - Start operations and check count
        val job1 = async {
            manager.withConcurrencyControl {
                delay(50.milliseconds)
            }
        }
        
        val job2 = async {
            manager.withConcurrencyControl {
                delay(50.milliseconds)
            }
        }
        
        // Wait a bit for operations to start
        delay(10.milliseconds)
        val activeCount = manager.getActiveOperationCount()
        
        // Assert - Should have 2 active operations
        assertEquals(2, activeCount)
        
        // Cleanup
        job1.await()
        job2.await()
        
        // After completion, should be 0
        assertEquals(0, manager.getActiveOperationCount())
    }
    
    @Test
    fun `reset should clear active operation count`() = runTest {
        // Arrange
        var operationStarted = false
        val job = async {
            concurrencyManager.withConcurrencyControl {
                operationStarted = true
                delay(100.milliseconds)
            }
        }
        
        // Wait for operation to start
        while (!operationStarted) {
            delay(5.milliseconds)
        }
        
        // Act
        concurrencyManager.reset()
        
        // Assert
        assertEquals(0, concurrencyManager.getActiveOperationCount())
        
        // Cleanup
        job.cancel()
    }
    
    @Test
    fun `concurrent withMutex calls should not deadlock`() = runTest {
        // Arrange & Act
        val results = List(10) {
            async {
                concurrencyManager.withMutex {
                    delay(5.milliseconds)
                    "result-$it"
                }
            }
        }.map { it.await() }
        
        // Assert - All operations should complete
        assertEquals(10, results.size)
        assertTrue(results.all { it.startsWith("result-") })
    }
    
    @Test
    fun `withConcurrencyControl should handle exceptions properly`() = runTest {
        // Arrange
        val manager = ConcurrencyManager(maxConcurrentTransfers = 2)
        
        // Act - Operation that throws exception
        val result = runCatching {
            manager.withConcurrencyControl {
                throw IllegalStateException("Test exception")
            }
        }
        
        // Assert - Exception should propagate
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        
        // Active count should be decremented even after exception
        assertEquals(0, manager.getActiveOperationCount())
    }
}
