package ireader.data.sync

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

/**
 * Manages concurrency for sync operations.
 * 
 * Phase 10.4.2: Implements parallel processing with controlled concurrency.
 * - Limits concurrent transfers to avoid overwhelming the network
 * - Provides semaphore-based throttling
 * - Tracks active operations for monitoring
 * 
 * @property maxConcurrentTransfers Maximum number of concurrent book transfers (default: 3)
 */
class ConcurrencyManager(
    private val maxConcurrentTransfers: Int = 3
) {
    // Semaphore to limit concurrent transfers
    private val transferSemaphore = Semaphore(maxConcurrentTransfers)
    
    // Mutex for thread-safe state updates
    private val stateMutex = Mutex()
    
    // Track active operations
    private var activeOperations = 0
    
    /**
     * Execute an operation with concurrency control.
     * Limits the number of concurrent operations to maxConcurrentTransfers.
     * 
     * @param operation The suspend function to execute
     * @return Result of the operation
     */
    suspend fun <T> withConcurrencyControl(operation: suspend () -> T): T {
        return transferSemaphore.withPermit {
            incrementActiveOperations()
            try {
                operation()
            } finally {
                decrementActiveOperations()
            }
        }
    }
    
    /**
     * Execute an operation with mutex protection for shared state.
     * Ensures thread-safe access to shared resources.
     * 
     * @param operation The suspend function to execute
     * @return Result of the operation
     */
    suspend fun <T> withMutex(operation: suspend () -> T): T {
        return stateMutex.withLock {
            operation()
        }
    }
    
    /**
     * Get the current number of active operations.
     * Thread-safe read of active operation count.
     * 
     * @return Number of currently active operations
     */
    suspend fun getActiveOperationCount(): Int {
        return stateMutex.withLock {
            activeOperations
        }
    }
    
    /**
     * Reset the concurrency manager state.
     * Useful for testing and cleanup.
     */
    suspend fun reset() {
        stateMutex.withLock {
            activeOperations = 0
        }
    }
    
    // Private helper methods
    
    private suspend fun incrementActiveOperations() {
        stateMutex.withLock {
            activeOperations++
        }
    }
    
    private suspend fun decrementActiveOperations() {
        stateMutex.withLock {
            activeOperations--
        }
    }
}
