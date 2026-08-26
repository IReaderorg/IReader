package ireader.presentation.core.theme

import androidx.compose.ui.graphics.Color
import ireader.core.log.Log
import ireader.domain.models.theme.DomainColorScheme
import ireader.domain.utils.cover.CoverColorExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
 * Claims are owner-tagged: each screen passes a composition-scoped token, so a
 * stale screen disposing late can't wipe the theme a newly-resumed screen just
 * claimed. No fake fallback: until real extraction succeeds the flow stays null.
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
    private var owner: Any? = null
    private var lastRequestKey: String? = null

    fun applyCoverBasedTheme(coverUrl: String?, sourceId: Long?, isDark: Boolean, requestedBy: Any) {
        if (coverUrl.isNullOrBlank()) {
            clear(requestedBy)
            return
        }
        val cacheKey = "${sourceId ?: 0}_${coverUrl.hashCode()}_$isDark"
        // Repeated ON_RESUME with identical args: don't cancel/restart the job
        if (owner === requestedBy && cacheKey == lastRequestKey && extractionJob?.isActive == true) return

        extractionJob?.cancel()
        owner = requestedBy
        lastRequestKey = cacheKey
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
                    // a newer apply/clear call has cancelled this job by then.
                    if (coroutineContext[Job] === extractionJob) _coverBasedTheme.value = scheme
                } else {
                    Log.warn { "CoverBasedTheme: no color extracted from $coverUrl" }
                }
            } catch (e: Exception) {
                Log.error { "CoverBasedTheme: extraction failed for $coverUrl: ${e.message}" }
            }
        }
    }

    /**
     * Clears only if [requestedBy] owns the current theme — stale disposes are
     * no-ops. A short grace delay covers the back-nav gap where the destination
     * screen hasn't re-claimed yet, avoiding a theme flash.
     */
    fun clear(requestedBy: Any) {
        if (owner !== requestedBy) return
        scope.launch {
            delay(300)
            // Re-check: a screen that resumed in the meantime has re-claimed ownership
            if (owner === requestedBy) clearAll()
        }
    }

    /** Unconditional clear (feature switched off). */
    fun clearAll() {
        extractionJob?.cancel()
        extractionJob = null
        owner = null
        lastRequestKey = null
        _coverBasedTheme.value = null
    }
}
