package ireader.domain.services.common

/**
 * Base interface for all platform services
 * Provides common lifecycle and state management
 */
interface PlatformService {
    /**
     * Initialize the service
     */
    suspend fun initialize()
    
    /**
     * Start the service
     */
    suspend fun start()
    
    /**
     * Stop the service
     */
    suspend fun stop()
    
    /**
     * Check if service is running
     */
    fun isRunning(): Boolean
    
    /**
     * Clean up resources
     */
    suspend fun cleanup()
}

/**
 * Service state enum
 */
enum class ServiceState {
    IDLE,
    INITIALIZING,
    RUNNING,
    PAUSED,
    STOPPED,
    ERROR
}

/**
 * Service result wrapper
 */
sealed class ServiceResult<out T> {
    data class Success<T>(val data: T) : ServiceResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : ServiceResult<Nothing>()
    data object Loading : ServiceResult<Nothing>()
}
