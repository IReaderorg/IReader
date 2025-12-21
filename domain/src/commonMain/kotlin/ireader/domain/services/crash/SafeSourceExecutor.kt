package ireader.domain.services.crash

import ireader.core.error.GlobalExceptionHandler
import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.Source
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.services.source.SourceHealthChecker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Safe executor for source operations that catches all exceptions
 * and prevents crashes from dynamic sources/plugins.
 * 
 * All source operations should go through this executor to ensure
 * the app remains stable even when sources have bugs or are outdated.
 */
class SafeSourceExecutor(
    private val sourceHealthChecker: SourceHealthChecker
) {
    companion object {
        private const val TAG = "SafeSourceExecutor"
        
        // Default timeout for source operations
        val DEFAULT_TIMEOUT: Duration = 30.seconds
        val SEARCH_TIMEOUT: Duration = 45.seconds
        val CONTENT_TIMEOUT: Duration = 60.seconds
    }
    
    /**
     * Result of a safe source operation.
     */
    sealed class SafeResult<out T> {
        data class Success<T>(val data: T) : SafeResult<T>()
        data class Error(
            val exception: Throwable,
            val isCritical: Boolean,
            val userMessage: String
        ) : SafeResult<Nothing>()
        
        fun getOrNull(): T? = (this as? Success)?.data
        
        fun getOrThrow(): T = when (this) {
            is Success -> data
            is Error -> throw exception
        }
        
        inline fun <R> map(transform: (T) -> R): SafeResult<R> = when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
        
        inline fun onSuccess(action: (T) -> Unit): SafeResult<T> {
            if (this is Success) action(data)
            return this
        }
        
        inline fun onError(action: (Error) -> Unit): SafeResult<T> {
            if (this is Error) action(this)
            return this
        }
    }
    
    /**
     * Execute a source operation safely with timeout and error handling.
     */
    suspend fun <T> execute(
        sourceId: Long,
        operationName: String,
        timeout: Duration = DEFAULT_TIMEOUT,
        block: suspend () -> T
    ): SafeResult<T> {
        return try {
            val result = withTimeout(timeout) {
                block()
            }
            
            // Clear any previous errors on success
            sourceHealthChecker.clearSourceError(sourceId)
            
            SafeResult.Success(result)
        } catch (e: CancellationException) {
            // Don't catch cancellation - let it propagate
            throw e
        } catch (e: Exception) {
            handleException(sourceId, operationName, e)
        }
    }
    
    /**
     * Safely get manga list from a source.
     */
    suspend fun getMangaList(
        source: CatalogSource,
        sourceId: Long,
        sort: Listing?,
        page: Int
    ): SafeResult<MangasPageInfo> {
        return execute(sourceId, "getMangaList", SEARCH_TIMEOUT) {
            source.getMangaList(sort, page)
        }
    }
    
    /**
     * Safely get manga details.
     */
    suspend fun getMangaDetails(
        source: Source,
        sourceId: Long,
        manga: MangaInfo,
        commands: List<Command<*>> = emptyList()
    ): SafeResult<MangaInfo> {
        return execute(sourceId, "getMangaDetails", DEFAULT_TIMEOUT) {
            source.getMangaDetails(manga, commands)
        }
    }
    
    /**
     * Safely get chapter list.
     */
    suspend fun getChapterList(
        source: Source,
        sourceId: Long,
        manga: MangaInfo,
        commands: List<Command<*>> = emptyList()
    ): SafeResult<List<ChapterInfo>> {
        return execute(sourceId, "getChapterList", DEFAULT_TIMEOUT) {
            source.getChapterList(manga, commands)
        }
    }
    
    /**
     * Safely get chapter content.
     */
    suspend fun getPageList(
        source: Source,
        sourceId: Long,
        chapter: ChapterInfo,
        commands: List<Command<*>> = emptyList()
    ): SafeResult<List<ireader.core.source.model.Page>> {
        return execute(sourceId, "getPageList", CONTENT_TIMEOUT) {
            source.getPageList(chapter, commands)
        }
    }
    
    /**
     * Handle an exception from a source operation.
     */
    private fun <T> handleException(
        sourceId: Long,
        operationName: String,
        exception: Exception
    ): SafeResult<T> {
        Log.error("$TAG: Error in $operationName for source $sourceId", exception)
        
        // Record the error
        sourceHealthChecker.recordSourceError(sourceId, exception)
        
        // Log to global handler for crash reporting
        GlobalExceptionHandler.handleSourceError(sourceId, exception, "$TAG.$operationName")
        
        // Determine if this is a critical error
        val isCritical = isCriticalError(exception)
        
        // Generate user-friendly message
        val userMessage = generateUserMessage(exception, operationName)
        
        return SafeResult.Error(
            exception = exception,
            isCritical = isCritical,
            userMessage = userMessage
        )
    }
    
    /**
     * Check if an error is critical (source is broken).
     */
    private fun isCriticalError(exception: Exception): Boolean {
        return when {
            exception is ClassNotFoundException -> true
            exception is NoClassDefFoundError -> true
            exception is LinkageError -> true
            exception is UnsatisfiedLinkError -> true
            exception is ExceptionInInitializerError -> true
            exception.message?.contains("class not found", ignoreCase = true) == true -> true
            exception.message?.contains("no class def found", ignoreCase = true) == true -> true
            exception.message?.contains("native library", ignoreCase = true) == true -> true
            exception.message?.contains("plugin not loaded", ignoreCase = true) == true -> true
            else -> false
        }
    }
    
    /**
     * Generate a user-friendly error message.
     */
    private fun generateUserMessage(exception: Exception, operationName: String): String {
        val message = exception.message?.lowercase() ?: ""
        
        return when {
            // Network errors
            message.contains("timeout") -> "Request timed out. Please check your connection and try again."
            message.contains("unable to resolve") || message.contains("unknown host") -> 
                "No internet connection. Please check your network."
            message.contains("connection refused") -> "Server is not responding. Try again later."
            message.contains("ssl") || message.contains("certificate") -> 
                "Security error. The source may have certificate issues."
            
            // Source errors
            message.contains("class not found") || message.contains("no class def") ->
                "This source is broken or incompatible. Consider uninstalling it."
            message.contains("native library") || message.contains("unsatisfied link") ->
                "This source requires a plugin that is not installed."
            message.contains("parse") || message.contains("json") ->
                "Failed to parse response. The source may be outdated."
            message.contains("404") -> "Content not found. It may have been removed."
            message.contains("403") || message.contains("forbidden") ->
                "Access denied. The source may require login or is blocked."
            message.contains("429") || message.contains("rate limit") ->
                "Too many requests. Please wait a moment and try again."
            
            // Generic errors
            else -> "An error occurred: ${exception.message ?: "Unknown error"}"
        }
    }
}
