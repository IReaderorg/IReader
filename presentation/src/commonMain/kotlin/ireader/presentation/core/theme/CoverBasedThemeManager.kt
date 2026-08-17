package ireader.presentation.core.theme

import androidx.compose.ui.graphics.Color
import ireader.domain.models.common.DomainColor
import ireader.domain.models.theme.DomainColorScheme
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.utils.cover.CoverColorExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class CoverBasedThemeManager(
    private val coverColorExtractor: CoverColorExtractor,
    private val scope: CoroutineScope
) {
    private val _coverBasedTheme = MutableStateFlow<DomainColorScheme?>(null)
    val coverBasedTheme: StateFlow<DomainColorScheme?> = _coverBasedTheme.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val cache = mutableMapOf<String, DomainColorScheme>()
    private val pendingExtractions = mutableSetOf<String>()
    
    private fun generateInstantFallback(coverUrl: String, style: PreferenceValues.CoverBasedThemeStyle, isDark: Boolean): DomainColorScheme {
        val hash = coverUrl.hashCode()
        val hue = (hash and 0x7FFFFFFF) % 360
        val sat = 0.65f
        val light = if (isDark) 0.5f else 0.45f
        val seed = Color.hsv(hue / 360f, sat, light)
        return Material3PaletteGenerator.generate(seed, style, isDark)
    }
    
    fun applyCoverBasedTheme(
        coverUrl: String?,
        sourceId: Long?,
        style: PreferenceValues.CoverBasedThemeStyle,
        isDark: Boolean
    ) {
        if (coverUrl.isNullOrBlank()) {
            _coverBasedTheme.value = null
            return
        }
        
        val cacheKey = "${sourceId ?: 0}_${coverUrl.hashCode()}_${style.name}_$isDark"
        cache[cacheKey]?.let {
            _coverBasedTheme.value = it
            return
        }
        
        val instantScheme = generateInstantFallback(coverUrl, style, isDark)
        cache[cacheKey] = instantScheme
        _coverBasedTheme.value = instantScheme
        
        if (pendingExtractions.add(cacheKey)) {
            scope.launch {
                _isLoading.value = true
                _error.value = null
                try {
                    val dominantColor = withContext(Dispatchers.IO) {
                        coverColorExtractor.extractDominantColor(coverUrl, sourceId)
                    }
                    if (dominantColor != null) {
                        val seedColor = Color(dominantColor.red, dominantColor.green, dominantColor.blue, dominantColor.alpha)
                        val scheme = Material3PaletteGenerator.generate(seedColor, style, isDark)
                        cache[cacheKey] = scheme
                        _coverBasedTheme.value = scheme
                    }
                } catch (e: Exception) {
                    _error.value = e.message
                } finally {
                    _isLoading.value = false
                    pendingExtractions.remove(cacheKey)
                }
            }
        }
    }
    
    fun clearCache() {
        cache.clear()
        pendingExtractions.clear()
        _coverBasedTheme.value = null
        _error.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
}
