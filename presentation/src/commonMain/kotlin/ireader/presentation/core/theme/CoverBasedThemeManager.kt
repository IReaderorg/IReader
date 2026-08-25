package ireader.presentation.core.theme

import androidx.compose.ui.graphics.Color
import ireader.core.log.Log
import ireader.domain.models.theme.DomainColorScheme
import ireader.domain.utils.cover.CoverColorExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * Extracts a dominant color from the current book cover and exposes the generated
 * scheme via StateFlow.
 *
 * No fake fallback: until real extraction succeeds the flow stays null and the UI
 * keeps the regular app theme — a wrong random color is worse than no color.
 * The cache map is guarded by [mutex]; extraction runs on Dispatchers.IO.
 */
class CoverBasedThemeManager(
    private val coverColorExtractor: CoverColorExtractor,
    private val scope: CoroutineScope
) {
    private val _coverBasedTheme = MutableStateFlow<DomainColorScheme?>(null)
    val coverBasedTheme: StateFlow<DomainColorScheme?> = _coverBasedTheme.asStateFlow()

    // ponytail: cache is unbounded per-session but entries are tiny (one ColorScheme)
    // and keyed by visited covers; swap to LRU if memory ever shows up in profiling.
    private val cache = mutableMapOf<String, DomainColorScheme>()
    private val mutex = Mutex()
    private var extractionJob: Job? = null

    fun applyCoverBasedTheme(coverUrl: String?, sourceId: Long?, isDark: Boolean) {
        // Cancel any in-flight extraction for the previous cover
        extractionJob?.cancel()
        extractionJob = null

        if (coverUrl.isNullOrBlank()) {
            clear()
            return
        }

        val cacheKey = "${sourceId ?: 0}_${coverUrl.hashCode()}_$isDark"
        extractionJob = scope.launch {
            val cached = mutex.withLock { cache[cacheKey] }
            if (cached != null) {
                _coverBasedTheme.value = cached
                return@launch
            }

            try {
                val dominantColor = withContext(Dispatchers.IO) {
                    coverColorExtractor.extractDominantColor(coverUrl, sourceId)
                }
                if (dominantColor != null) {
                    val seedColor = Color(dominantColor.red, dominantColor.green, dominantColor.blue, dominantColor.alpha)
                    val scheme = Material3PaletteGenerator.generate(seedColor, isDark)
                    mutex.withLock { cache[cacheKey] = scheme }
                    // Publish only if this extraction is still the latest request;
                    // a newer applyCoverBasedTheme/clear call has cancelled this job by then.
                    if (coroutineContext[Job] === extractionJob) _coverBasedTheme.value = scheme
                } else {
                    Log.warn { "CoverBasedTheme: no color extracted from $coverUrl" }
                }
            } catch (e: Exception) {
                Log.error { "CoverBasedTheme: extraction failed for $coverUrl: ${e.message}" }
            }
        }
    }

    /** Cancels in-flight extraction and unpublishes the current scheme (cache retained). */
    fun clear() {
        extractionJob?.cancel()
        extractionJob = null
        _coverBasedTheme.value = null
    }
}
