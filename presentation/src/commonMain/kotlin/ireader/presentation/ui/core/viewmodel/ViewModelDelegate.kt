package ireader.presentation.ui.core.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ireader.i18n.UiText

/**
 * Delegate for common ViewModel operations
 * Reduces boilerplate code in ViewModels
 */
class ViewModelDelegate(
    private val scope: CoroutineScope,
    private val showSnackBar: (UiText) -> Unit
) {
    private val jobs = mutableMapOf<String, Job>()
    
    /**
     * Launch a coroutine with automatic error handling
     * @param key Unique key for the job (for cancellation)
     * @param onError Custom error handler
     * @param block The coroutine block to execute
     */
    fun launchWithErrorHandling(
        key: String? = null,
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val job = scope.launch {
            try {
                block()
            } catch (e: Exception) {
                onError?.invoke(e) ?: showSnackBar(UiText.ExceptionString(e))
            }
        }
        
        key?.let { jobs[it] = job }
        return job
    }
    
    /**
     * Cancel a specific job by key
     */
    fun cancelJob(key: String) {
        jobs[key]?.cancel()
        jobs.remove(key)
    }
    
    /**
     * Cancel all jobs
     */
    fun cancelAllJobs() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }
}

/**
 * Extension function to execute a block with loading state management
 */
suspend fun <T> withLoadingState(
    setLoading: (Boolean) -> Unit,
    block: suspend () -> T
): Result<T> {
    return try {
        setLoading(true)
        val result = block()
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    } finally {
        setLoading(false)
    }
}
