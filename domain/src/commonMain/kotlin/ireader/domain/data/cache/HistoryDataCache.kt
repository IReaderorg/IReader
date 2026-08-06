package ireader.domain.data.cache

import ireader.domain.models.entities.HistoryWithRelations
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory cache for recent history data to enable instant display on cold boot.
 * This cache is populated during app startup by DatabasePreloader and updated
 * by HistoryViewModel for zero-latency history rendering.
 */
object HistoryDataCache {
    private val _histories = MutableStateFlow<ImmutableMap<Long, ImmutableList<HistoryWithRelations>>>(
        persistentMapOf()
    )
    val histories: StateFlow<ImmutableMap<Long, ImmutableList<HistoryWithRelations>>> = _histories.asStateFlow()

    private val _isPreloaded = MutableStateFlow(false)
    val isPreloaded: StateFlow<Boolean> = _isPreloaded.asStateFlow()

    /**
     * Update the cache with fresh history data.
     * Called by DatabasePreloader during app startup or HistoryViewModel.
     */
    fun updateCache(grouped: Map<Long, List<HistoryWithRelations>>) {
        if (grouped.isNotEmpty()) {
            val immutableGrouped = grouped.mapValues { (_, list) -> list.toImmutableList() }.toImmutableMap()
            _histories.value = immutableGrouped
            _isPreloaded.value = true
        }
    }

    /**
     * Get cached history items if available.
     */
    fun getCachedHistories(): ImmutableMap<Long, ImmutableList<HistoryWithRelations>> = _histories.value

    /**
     * Check if cache has been populated.
     */
    fun hasCache(): Boolean = _isPreloaded.value && _histories.value.isNotEmpty()

    /**
     * Invalidate the cache (e.g. after clear history).
     */
    fun invalidate() {
        _histories.value = persistentMapOf()
        _isPreloaded.value = false
    }
}
