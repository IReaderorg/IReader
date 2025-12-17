package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Interface for plugins that support batch operations.
 * Enables efficient bulk processing of items.
 */
interface BatchCapablePlugin {
    /**
     * Maximum batch size supported.
     */
    val maxBatchSize: Int
    
    /**
     * Whether parallel processing is supported.
     */
    val supportsParallelProcessing: Boolean
    
    /**
     * Get optimal batch size based on current conditions.
     */
    fun getOptimalBatchSize(): Int
}

/**
 * Batch operation builder for fluent API.
 */
class BatchOperationBuilder<T, R> {
    private val items = mutableListOf<T>()
    private var onProgress: ((BatchProgress) -> Unit)? = null
    private var onItemComplete: ((T, R) -> Unit)? = null
    private var onItemError: ((T, Throwable) -> Unit)? = null
    private var batchSize: Int = 10
    private var parallelism: Int = 1
    private var retryCount: Int = 0
    private var retryDelayMs: Long = 1000
    private var continueOnError: Boolean = true
    
    fun addItem(item: T): BatchOperationBuilder<T, R> {
        items.add(item)
        return this
    }
    
    fun addItems(items: List<T>): BatchOperationBuilder<T, R> {
        this.items.addAll(items)
        return this
    }
    
    fun batchSize(size: Int): BatchOperationBuilder<T, R> {
        this.batchSize = size
        return this
    }
    
    fun parallelism(count: Int): BatchOperationBuilder<T, R> {
        this.parallelism = count
        return this
    }
    
    fun retry(count: Int, delayMs: Long = 1000): BatchOperationBuilder<T, R> {
        this.retryCount = count
        this.retryDelayMs = delayMs
        return this
    }
    
    fun continueOnError(continue_: Boolean): BatchOperationBuilder<T, R> {
        this.continueOnError = continue_
        return this
    }
    
    fun onProgress(callback: (BatchProgress) -> Unit): BatchOperationBuilder<T, R> {
        this.onProgress = callback
        return this
    }
    
    fun onItemComplete(callback: (T, R) -> Unit): BatchOperationBuilder<T, R> {
        this.onItemComplete = callback
        return this
    }
    
    fun onItemError(callback: (T, Throwable) -> Unit): BatchOperationBuilder<T, R> {
        this.onItemError = callback
        return this
    }
    
    fun build(): BatchOperation<T, R> {
        return BatchOperation(
            items = items.toList(),
            batchSize = batchSize,
            parallelism = parallelism,
            retryCount = retryCount,
            retryDelayMs = retryDelayMs,
            continueOnError = continueOnError,
            onProgress = onProgress,
            onItemComplete = onItemComplete,
            onItemError = onItemError
        )
    }
}

/**
 * Batch operation configuration.
 */
data class BatchOperation<T, R>(
    val items: List<T>,
    val batchSize: Int,
    val parallelism: Int,
    val retryCount: Int,
    val retryDelayMs: Long,
    val continueOnError: Boolean,
    val onProgress: ((BatchProgress) -> Unit)?,
    val onItemComplete: ((T, R) -> Unit)?,
    val onItemError: ((T, Throwable) -> Unit)?
)

/**
 * Batch operation progress.
 */
@Serializable
data class BatchProgress(
    val totalItems: Int,
    val completedItems: Int,
    val failedItems: Int,
    val currentBatch: Int,
    val totalBatches: Int,
    val percentage: Float,
    val estimatedTimeRemainingMs: Long?,
    val currentItem: String? = null
)

/**
 * Batch operation result.
 */
@Serializable
data class BatchResult<R>(
    val totalItems: Int,
    val successfulItems: Int,
    val failedItems: Int,
    val results: List<BatchItemResult<R>>,
    val totalTimeMs: Long,
    val averageTimePerItemMs: Long
)

/**
 * Individual item result in batch.
 */
@Serializable
data class BatchItemResult<R>(
    val index: Int,
    val success: Boolean,
    val result: R? = null,
    val error: String? = null,
    val retryCount: Int = 0,
    val processingTimeMs: Long
)

/**
 * Queue for managing batch operations.
 */
interface BatchQueue {
    /**
     * Add operation to queue.
     */
    suspend fun <T, R> enqueue(
        operation: BatchOperation<T, R>,
        processor: suspend (T) -> R
    ): String
    
    /**
     * Get queue status.
     */
    fun getQueueStatus(): QueueStatus
    
    /**
     * Get operation status by ID.
     */
    fun getOperationStatus(operationId: String): OperationStatus?
    
    /**
     * Cancel operation.
     */
    suspend fun cancelOperation(operationId: String): Boolean
    
    /**
     * Pause queue processing.
     */
    fun pauseQueue()
    
    /**
     * Resume queue processing.
     */
    fun resumeQueue()
    
    /**
     * Clear completed operations from queue.
     */
    fun clearCompleted()
}

/**
 * Queue status.
 */
@Serializable
data class QueueStatus(
    val pendingOperations: Int,
    val runningOperations: Int,
    val completedOperations: Int,
    val failedOperations: Int,
    val isPaused: Boolean,
    val totalItemsInQueue: Int
)

/**
 * Operation status.
 */
@Serializable
data class OperationStatus(
    val operationId: String,
    val state: OperationState,
    val progress: BatchProgress?,
    val startTime: Long?,
    val endTime: Long?,
    val error: String?
)

@Serializable
enum class OperationState {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Extension function to create batch builder.
 */
fun <T, R> batchOperation(): BatchOperationBuilder<T, R> = BatchOperationBuilder()

/**
 * Utility for chunking lists into batches.
 */
object BatchUtils {
    /**
     * Split list into chunks of specified size.
     */
    fun <T> List<T>.chunked(size: Int): List<List<T>> {
        return this.windowed(size, size, partialWindows = true)
    }
    
    /**
     * Process items in batches with delay between batches.
     */
    suspend fun <T, R> processBatched(
        items: List<T>,
        batchSize: Int,
        delayBetweenBatchesMs: Long = 0,
        processor: suspend (T) -> R
    ): List<R> {
        val results = mutableListOf<R>()
        val batches = items.chunked(batchSize)
        
        batches.forEachIndexed { index, batch ->
            batch.forEach { item ->
                results.add(processor(item))
            }
            if (index < batches.size - 1 && delayBetweenBatchesMs > 0) {
                kotlinx.coroutines.delay(delayBetweenBatchesMs)
            }
        }
        
        return results
    }
    
    /**
     * Calculate optimal batch size based on item count and constraints.
     */
    fun calculateOptimalBatchSize(
        totalItems: Int,
        maxBatchSize: Int,
        minBatchSize: Int = 1,
        targetBatches: Int = 10
    ): Int {
        val idealSize = (totalItems / targetBatches).coerceIn(minBatchSize, maxBatchSize)
        return idealSize
    }
}