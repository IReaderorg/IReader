package ireader.presentation.ui.plugins.integration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ireader.domain.plugins.PluginMenuItem
import ireader.domain.plugins.PluginScreen
import ireader.domain.plugins.ReaderContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing feature plugin integration state
 * Requirements: 6.1, 6.2, 6.3
 */
class FeaturePluginViewModel(
    private val featurePluginIntegration: FeaturePluginIntegration
) : ViewModel() {
    
    private val _menuItems = MutableStateFlow<List<PluginMenuItem>>(emptyList())
    val menuItems: StateFlow<List<PluginMenuItem>> = _menuItems.asStateFlow()
    
    private val _screens = MutableStateFlow<List<PluginScreen>>(emptyList())
    val screens: StateFlow<List<PluginScreen>> = _screens.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadPluginData()
    }
    
    /**
     * Load plugin menu items and screens
     */
    fun loadPluginData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Load menu items
                val items = featurePluginIntegration.getPluginMenuItems()
                _menuItems.value = items
                
                // Load screens
                val pluginScreens = featurePluginIntegration.getPluginScreens()
                _screens.value = pluginScreens
                
            } catch (e: Exception) {
                _error.value = "Failed to load plugin data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh plugin data
     */
    fun refresh() {
        loadPluginData()
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
}

/**
 * State holder for feature plugin integration
 */
data class FeaturePluginState(
    val menuItems: List<PluginMenuItem> = emptyList(),
    val screens: List<PluginScreen> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
