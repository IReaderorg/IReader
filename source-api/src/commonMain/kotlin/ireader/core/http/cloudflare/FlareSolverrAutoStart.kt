package ireader.core.http.cloudflare

/**
 * Interface for auto-starting FlareSolverr when needed.
 * Platform-specific implementations handle the actual process management.
 */
interface FlareSolverrAutoStarter {
    /**
     * Check if FlareSolverr is downloaded and available to start.
     */
    fun isDownloaded(): Boolean
    
    /**
     * Start the FlareSolverr server.
     * @return true if start was initiated successfully
     */
    fun startServer(): Boolean
    
    /**
     * Check if the server process is running (not necessarily ready to accept requests).
     */
    fun isProcessRunning(): Boolean
}

/**
 * No-op implementation for platforms that don't support auto-start.
 */
object NoOpFlareSolverrAutoStarter : FlareSolverrAutoStarter {
    override fun isDownloaded(): Boolean = false
    override fun startServer(): Boolean = false
    override fun isProcessRunning(): Boolean = false
}
