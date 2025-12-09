package ireader.data.repository.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Abstract base class for stateless NoOp repository implementations.
 * Provides common helper methods for returning empty/default values consistently.
 * 
 * NoOp repositories are used when optional features are disabled or unavailable
 * (e.g., when Supabase is not configured).
 * 
 * All NoOp repositories should extend this base class and be implemented as
 * Kotlin objects (singletons) since they are stateless.
 * 
 * @see Requirements 2.1, 2.2, 2.3, 2.4
 */
abstract class NoOpRepositoryBase {
    
    /**
     * Returns a successful Result containing null.
     * Use for methods that return a single optional item.
     */
    protected fun <T> emptyResult(): Result<T?> = Result.success(null)
    
    /**
     * Returns a successful Result containing an empty list.
     * Use for methods that return a list of items.
     */
    protected fun <T> emptyListResult(): Result<List<T>> = Result.success(emptyList())
    
    /**
     * Returns a successful Result containing Unit.
     * Use for methods that perform an action without returning data.
     */
    protected fun unitResult(): Result<Unit> = Result.success(Unit)
    
    /**
     * Returns a Flow that emits null once and completes.
     * Use for methods that observe a single optional item.
     */
    protected fun <T> emptyFlow(): Flow<T?> = flowOf(null)
    
    /**
     * Returns a Flow that emits an empty list once and completes.
     * Use for methods that observe a list of items.
     */
    protected fun <T> emptyListFlow(): Flow<List<T>> = flowOf(emptyList())
    
    /**
     * Returns a failure Result with an UnsupportedOperationException.
     * Use for methods that require configuration to function.
     * 
     * @param featureName The name of the feature that requires configuration
     */
    protected fun <T> unavailableResult(featureName: String): Result<T> = 
        Result.failure(UnsupportedOperationException(
            "$featureName requires Supabase configuration. " +
            "Please configure Supabase credentials in Settings → Supabase Configuration."
        ))
    
    /**
     * Returns a failure Result with a generic "feature not available" message.
     * Use for methods that are not supported in NoOp mode.
     * 
     * @param featureName The name of the feature that is not available
     */
    protected fun <T> featureNotAvailable(featureName: String): Result<T> =
        Result.failure(Exception("$featureName feature not available"))
    
    /**
     * Returns a successful Result containing a default value.
     * Use for methods that should return a sensible default.
     */
    protected fun <T> defaultResult(value: T): Result<T> = Result.success(value)
}
