package ireader.presentation.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import org.koin.core.Koin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/**
 * A store that caches ViewModel instances per navigation route.
 * This ensures ViewModels survive navigation and are only cleared when
 * the navigation entry is removed from the backstack.
 */
class NavigationViewModelStore {
    @PublishedApi
    internal val viewModels = mutableMapOf<String, Any>()
    
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> getOrCreate(
        key: String,
        koin: Koin,
        qualifier: Qualifier?,
        noinline parameters: ParametersDefinition?
    ): T {
        val existing = viewModels[key]
        if (existing != null) {
            return existing as T
        }
        
        val newInstance = koin.get<T>(qualifier, parameters)
        viewModels[key] = newInstance
        return newInstance
    }
    
    fun clear(key: String) {
        viewModels.remove(key)?.let { vm ->
            // Call onCleared if it's a ViewModel
            if (vm is ViewModel) {
                try {
                    // Use reflection to call onCleared since it's protected
                    val method = ViewModel::class.java.getDeclaredMethod("onCleared")
                    method.isAccessible = true
                    method.invoke(vm)
                } catch (e: Exception) {
                    // Ignore if method not found or not accessible
                }
            }
        }
    }
    
    fun clearAll() {
        viewModels.keys.toList().forEach { clear(it) }
    }
}

/**
 * CompositionLocal for providing the NavigationViewModelStore
 */
val LocalNavigationViewModelStore = staticCompositionLocalOf<NavigationViewModelStore?> { null }
