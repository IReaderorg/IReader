package ireader.data.sync.fake

/**
 * Failure modes for testing error handling.
 */
enum class FailureMode {
    /** Transient failures that can be retried */
    TRANSIENT,
    
    /** Persistent failures that always fail */
    PERSISTENT,
    
    /** Network interruption during transfer */
    NETWORK_INTERRUPTION
}
