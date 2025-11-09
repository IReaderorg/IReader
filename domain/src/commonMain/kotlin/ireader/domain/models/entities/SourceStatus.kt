package ireader.domain.models.entities

/**
 * Represents the health status of a source
 */
sealed class SourceStatus {
    /**
     * Source is online and functioning normally
     */
    object Online : SourceStatus()
    
    /**
     * Source is offline or unreachable
     */
    object Offline : SourceStatus()
    
    /**
     * Source requires authentication/login
     */
    object LoginRequired : SourceStatus()
    
    /**
     * Source encountered an error
     * @param message The error message
     */
    data class Error(val message: String) : SourceStatus()
}
