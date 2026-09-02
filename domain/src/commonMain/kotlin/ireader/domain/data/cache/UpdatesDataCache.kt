package ireader.domain.data.cache

import ireader.domain.models.entities.UpdatesWithRelations
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime

/**
 * In-memory cache for recent updates data to enable instant display on cold boot.
 * This cache is populated during app startup by DatabasePreloader and updated
 * by UpdatesViewModel for zero-latency updates rendering.
 */
object UpdatesDataCache {
    private val _updates = MutableStateFlow<ImmutableMap<LocalDateTime, ImmutableList<UpdatesWithRelations>>>(
        persistentMapOf()
    )
    val updates: StateFlow<ImmutableMap<LocalDateTime, ImmutableList<UpdatesWithRelations>>> = _updates.asStateFlow()

    private val _isPreloaded = MutableStateFlow(false)
    val isPreloaded: StateFlow<Boolean> = _isPreloaded.asStateFlow()

    /**
     * Update the cache with fresh updates data.
     * Called by DatabasePreloader during app startup or UpdatesViewModel.
     */
    fun updateCache(grouped: Map<LocalDateTime, List<UpdatesWithRelations>>) {
        if (grouped.isNotEmpty()) {
            val immutableGrouped = grouped.mapValues { (_, list) -> list.toImmutableList() }.toImmutableMap()
            _updates.value = immutableGrouped
            _isPreloaded.value = true
        }
    }

    /**
     * Get cached updates items if available.
     */
    fun getCachedUpdates(): ImmutableMap<LocalDateTime, ImmutableList<UpdatesWithRelations>> = _updates.value

    /**
     * Check if cache has been populated.
     */
    fun hasCache(): Boolean = _isPreloaded.value && _updates.value.isNotEmpty()

    /**
     * Invalidate the cache (e.g. after clear updates or mark as read).
     */
    fun invalidate() {
        _updates.value = persistentMapOf()
        _isPreloaded.value = false
    }
}
