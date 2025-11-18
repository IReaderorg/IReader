package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.getKoin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/**
 * Get a ViewModel instance from Koin with optional parameters.
 * ViewModels are cached in NavigationViewModelStore and survive navigation.
 * 
 * @param key Optional key for caching. Use this to avoid recreating parameter objects.
 * @param qualifier Optional Koin qualifier
 * @param parameters Lambda that provides parameters to Koin
 */
@Composable
inline fun <reified T : Any> getIViewModel(
    key: Any? = null,
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T {
    val koin = getKoin()
    val store = LocalNavigationViewModelStore.current
    
    // Generate cache key based on key or parameters
    // IMPORTANT: The cacheKey must be stable and unique per ViewModel instance
    val cacheKey = if (key != null) {
        "${T::class.simpleName}_${qualifier?.value ?: ""}_$key"
    } else {
        val paramValues = parameters?.invoke()
        val paramKey = paramValues?.values?.joinToString(",") { it.toString() } ?: ""
        "${T::class.simpleName}_${qualifier?.value ?: ""}_$paramKey"
    }
    
    return if (store != null) {
        // Use remember with cacheKey to ensure we get the right ViewModel for this key
        // When the key changes, remember will recompute and get/create a new ViewModel
        remember(cacheKey) {
            store.getOrCreate<T>(cacheKey, koin, qualifier, parameters)
        }
    } else {
        // Without a store, create a new instance per cacheKey
        remember(cacheKey) {
            koin.get(qualifier, parameters)
        }
    }
}
