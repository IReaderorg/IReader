package ireader.data.catalog.impl.tsundoku

import eu.kanade.tachiyomi.source.model.FilterList
import ireader.core.source.model.Filter
import eu.kanade.tachiyomi.source.model.Filter as TFilter

/**
 * Bridge between Tachiyomi/Tsundoku filter models and IReader filter models.
 * 
 * Provides:
 * 1. Safe, loss-less conversion of TFilter -> Filter
 * 2. High-performance, zero-loss synchronization from modified Filter -> fresh FilterList
 * 3. Native TriState mapping (to IReader's Filter.Check with allowsExclusion = true)
 * 4. Safe array bounds on Select and Sort filters
 */
object TsundokuFilterBridge {

    /**
     * Convert Tsundoku FilterList to IReader Filter list.
     * Prepends Filter.Title("Search") to allow text search in IReader's UI.
     */
    fun toIReaderFilters(tsundokuFilters: List<TFilter<*>>): List<Filter<*>> {
        val result = mutableListOf<Filter<*>>(Filter.Title("Search"))
        for (tf in tsundokuFilters) {
            convertFilter(tf)?.let { result.add(it) }
        }
        return result
    }

    /**
     * Convert a single Tsundoku filter to an IReader filter.
     */
    fun convertFilter(tf: TFilter<*>): Filter<*>? {
        return when (tf) {
            is TFilter.Header -> Filter.Note(tf.name)
            is TFilter.Separator -> null // Visual separator only
            is TFilter.Text -> Filter.Text(tf.name, tf.state)
            is TFilter.CheckBox -> Filter.Check(tf.name, allowsExclusion = false, value = tf.state)
            is TFilter.TriState -> {
                val value = when (tf.state) {
                    TFilter.TriState.STATE_INCLUDE -> true
                    TFilter.TriState.STATE_EXCLUDE -> false
                    else -> null
                }
                Filter.Check(tf.name, allowsExclusion = true, value = value)
            }
            is TFilter.Select<*> -> Filter.Select(
                name = tf.name,
                options = tf.values.map { it.toString() }.toTypedArray(),
                value = tf.state.coerceIn(0, (tf.values.size - 1).coerceAtLeast(0))
            )
            is TFilter.Sort -> Filter.Sort(
                name = tf.name,
                options = tf.values,
                value = tf.state?.let { Filter.Sort.Selection(it.index, it.ascending) }
            )
            is TFilter.Group<*> -> {
                @Suppress("UNCHECKED_CAST")
                val groupFilters = tf.state as? List<TFilter<*>> ?: emptyList()
                val converted = groupFilters.mapNotNull { convertFilter(it) }
                if (converted.isNotEmpty()) {
                    Filter.Group(name = tf.name, filters = converted)
                } else null
            }
        }
    }

    /**
     * Synchronize modified IReader filter values onto a fresh Tsundoku FilterList instance.
     * Returns the same fresh FilterList instance with mutated state.
     */
    fun syncToTsundoku(
        freshTsundokuFilterList: FilterList,
        ireaderFilters: List<Filter<*>>
    ): FilterList {
        if (freshTsundokuFilterList.isEmpty() || ireaderFilters.isEmpty()) {
            return freshTsundokuFilterList
        }

        // Filter out synthetic Search/Title filter so it doesn't collide or offset content filters
        val contentFilters = ireaderFilters.filter { filter ->
            filter !is Filter.Title &&
            !(filter is Filter.Text && (filter.name.equals("Search", ignoreCase = true) || filter.name.equals("Title", ignoreCase = true)))
        }

        if (contentFilters.isEmpty()) {
            return freshTsundokuFilterList
        }

        syncFilterNodes(freshTsundokuFilterList, contentFilters)
        return freshTsundokuFilterList
    }

    private fun syncFilterNodes(
        tsundokuFilters: List<TFilter<*>>,
        ireaderFilters: List<Filter<*>>
    ) {
        for (tf in tsundokuFilters) {
            when (tf) {
                is TFilter.Separator, is TFilter.Header -> continue
                is TFilter.Group<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val subTsundoku = tf.state as? List<TFilter<*>> ?: emptyList()
                    val matchingGroup = ireaderFilters.filterIsInstance<Filter.Group>()
                        .find { it.name.equals(tf.name, ignoreCase = true) }
                    if (matchingGroup != null) {
                        syncFilterNodes(subTsundoku, matchingGroup.filters)
                    } else {
                        // Fallback: match inside ireaderFilters directly
                        syncFilterNodes(subTsundoku, ireaderFilters)
                    }
                }
                else -> {
                    val matchingIReader = findMatchingIReaderFilter(tf, ireaderFilters)
                    if (matchingIReader != null) {
                        applyState(tf, matchingIReader)
                    }
                }
            }
        }
    }

    private fun findMatchingIReaderFilter(tf: TFilter<*>, ireaderFilters: List<Filter<*>>): Filter<*>? {
        // Direct match at current level
        val direct = ireaderFilters.find { it.name.equals(tf.name, ignoreCase = true) }
        if (direct != null) return direct

        // Search recursively inside groups in ireaderFilters
        for (group in ireaderFilters.filterIsInstance<Filter.Group>()) {
            val inside = findMatchingIReaderFilter(tf, group.filters)
            if (inside != null) return inside
        }

        return null
    }

    private fun applyState(tsundoku: TFilter<*>, ireader: Filter<*>) {
        when {
            tsundoku is TFilter.Text && ireader is Filter.Text -> {
                tsundoku.state = ireader.value
            }
            tsundoku is TFilter.CheckBox && ireader is Filter.Check -> {
                tsundoku.state = ireader.value == true
            }
            tsundoku is TFilter.TriState && ireader is Filter.Check -> {
                tsundoku.state = when (ireader.value) {
                    true -> TFilter.TriState.STATE_INCLUDE
                    false -> TFilter.TriState.STATE_EXCLUDE
                    null -> TFilter.TriState.STATE_IGNORE
                }
            }
            tsundoku is TFilter.TriState && ireader is Filter.Select -> {
                tsundoku.state = ireader.value.coerceIn(0, 2)
            }
            tsundoku is TFilter.Select<*> && ireader is Filter.Select -> {
                if (tsundoku.values.isNotEmpty()) {
                    tsundoku.state = ireader.value.coerceIn(0, tsundoku.values.lastIndex)
                }
            }
            tsundoku is TFilter.Sort && ireader is Filter.Sort -> {
                ireader.value?.let { sel ->
                    if (tsundoku.values.isNotEmpty()) {
                        tsundoku.state = TFilter.Sort.Selection(
                            sel.index.coerceIn(0, tsundoku.values.lastIndex),
                            sel.ascending
                        )
                    }
                }
            }
            tsundoku is TFilter.Sort && ireader is Filter.Select -> {
                if (tsundoku.values.isNotEmpty()) {
                    tsundoku.state = TFilter.Sort.Selection(
                        ireader.value.coerceIn(0, tsundoku.values.lastIndex),
                        false
                    )
                }
            }
        }
    }

    /**
     * Check if a Tsundoku FilterList has any active/non-default filter applied.
     */
    fun isFilterListDefault(filters: FilterList): Boolean {
        return filters.all { isFilterDefault(it) }
    }

    private fun isFilterDefault(tf: TFilter<*>): Boolean {
        return when (tf) {
            is TFilter.Header, is TFilter.Separator -> true
            is TFilter.Text -> tf.state.isBlank()
            is TFilter.CheckBox -> !tf.state
            is TFilter.TriState -> tf.state == TFilter.TriState.STATE_IGNORE
            is TFilter.Select<*> -> tf.state == 0
            is TFilter.Sort -> tf.state == null || (tf.state?.index == 0 && tf.state?.ascending == false)
            is TFilter.Group<*> -> {
                @Suppress("UNCHECKED_CAST")
                (tf.state as? List<TFilter<*>>).orEmpty().all { isFilterDefault(it) }
            }
        }
    }
}
