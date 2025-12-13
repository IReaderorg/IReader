package ireader.plugin.api.util

/**
 * Result utility extensions for plugin development.
 * Provides convenient methods for handling success/failure cases.
 */

/**
 * Map a successful result, keeping failures unchanged.
 */
inline fun <T, R> Result<T>.mapSuccess(transform: (T) -> R): Result<R> {
    return when {
        isSuccess -> Result.success(transform(getOrThrow()))
        else -> Result.failure(exceptionOrNull()!!)
    }
}

/**
 * Flat map a successful result.
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return when {
        isSuccess -> transform(getOrThrow())
        else -> Result.failure(exceptionOrNull()!!)
    }
}

/**
 * Recover from a failure with a fallback value.
 */
inline fun <T> Result<T>.recover(fallback: (Throwable) -> T): Result<T> {
    return when {
        isSuccess -> this
        else -> Result.success(fallback(exceptionOrNull()!!))
    }
}

/**
 * Recover from a failure with another Result.
 */
inline fun <T> Result<T>.recoverWith(fallback: (Throwable) -> Result<T>): Result<T> {
    return when {
        isSuccess -> this
        else -> fallback(exceptionOrNull()!!)
    }
}

/**
 * Execute an action on success.
 */
inline fun <T> Result<T>.onSuccessAction(action: (T) -> Unit): Result<T> {
    if (isSuccess) action(getOrThrow())
    return this
}

/**
 * Execute an action on failure.
 */
inline fun <T> Result<T>.onFailureAction(action: (Throwable) -> Unit): Result<T> {
    exceptionOrNull()?.let { action(it) }
    return this
}

/**
 * Convert Result to a nullable value.
 */
fun <T> Result<T>.toNullable(): T? = getOrNull()

/**
 * Combine two results.
 */
inline fun <T, U, R> Result<T>.combine(
    other: Result<U>,
    transform: (T, U) -> R
): Result<R> {
    return when {
        this.isFailure -> Result.failure(this.exceptionOrNull()!!)
        other.isFailure -> Result.failure(other.exceptionOrNull()!!)
        else -> Result.success(transform(this.getOrThrow(), other.getOrThrow()))
    }
}

/**
 * Run a suspending block and wrap in Result.
 */
suspend inline fun <T> runCatchingSuspend(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
