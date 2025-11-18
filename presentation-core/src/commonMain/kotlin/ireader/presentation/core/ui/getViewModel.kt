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
    
    val cacheKey = remember(key, qualifier) {
        if (key != null) {
            "${T::class.simpleName}_${qualifier?.value ?: ""}_$key"
        } else {
            val paramValues = parameters?.invoke()
            val paramKey = paramValues?.values?.joinToString(",") { it.toString() } ?: ""
            "${T::class.simpleName}_${qualifier?.value ?: ""}_$paramKey"
        }
    }
    
    return if (store != null) {
        remember(cacheKey) {
            store.getOrCreate<T>(cacheKey, koin, qualifier, parameters)
        }
    } else {
        remember(cacheKey) {
            koin.get(qualifier, parameters)
        }
    }
}
