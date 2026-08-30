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
    private val lock = Any()
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
        
        if (key != null) {
            synchronized(lock) {
                jobs[key] = job
            }
        }
        return job
    }
    
    /**
     * Cancel a specific job by key
     */
    fun cancelJob(key: String) {
        val job = synchronized(lock) {
            jobs.remove(key)
        }
        job?.cancel()
    }
    
    /**
     * Cancel all jobs
     */
    fun cancelAllJobs() {
        val allJobs = synchronized(lock) {
            val list = jobs.values.toList()
            jobs.clear()
            list
        }
        allJobs.forEach { it.cancel() }
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
