package ireader.data.catalog.impl.tsundoku

import ireader.core.source.model.Filter
import ireader.core.source.model.FilterList

/**
 * Adapter for converting between Tsundoku and IReader filter systems.
 *
 * Tsundoku: eu.kanade.tachiyomi.source.model.Filter (sealed class with state property)
 * IReader:  ireader.core.source.model.Filter (sealed class with value property)
 *
 * Both systems share similar filter types but different class hierarchies.
 */
object TsundokuFilterAdapter {

    /**
     * Convert a Tsundoku FilterList to an IReader FilterList.
     * Uses reflection since we can't directly reference tsundoku types at compile time.
     */
    fun convertTsundokuFiltersToIReader(tsundokuFilters: List<Any>): FilterList {
        return tsundokuFilters.mapNotNull { convertSingleFilter(it) }
    }

    /**
     * Convert an IReader FilterList to a Tsundoku FilterList.
     * Uses reflection to create tsundoku filter instances.
     */
    fun convertIReaderFiltersToTsundoku(iReaderFilters: FilterList): List<Any> {
        return iReaderFilters.mapNotNull { convertToTsundokuFilter(it) }
    }

    /**
     * Apply IReader filter values to corresponding tsundoku filters.
     * This is used when performing a search: the UI presents IReader filters,
     * user sets values, then we apply those values to tsundoku filters before searching.
     */
    fun applyIReaderValuesToTsundokuFilters(
        iReaderFilters: FilterList,
        tsundokuFilters: List<Any>
    ) {
        if (iReaderFilters.size != tsundokuFilters.size) return

        iReaderFilters.zip(tsundokuFilters).forEach { (iReaderFilter, tsundokuFilter) ->
            applyFilterValue(iReaderFilter, tsundokuFilter)
        }
    }

    // ==================== Tsundoku → IReader ====================

    private fun convertSingleFilter(tsundokuFilter: Any): Filter<*>? {
        val className = tsundokuFilter.javaClass.name
        val name = tsundokuFilter.getField("name") as? String ?: return null

        return when {
            // Filter.Header or Filter.Separator → Filter.Note
            className.contains("Header") || className.contains("Separator") -> {
                Filter.Note(name)
            }

            // Filter.Text → Filter.Text
            className.endsWith("Filter\$Text") || className.endsWith("Filter.Text") ||
                isSubclassOf(tsundokuFilter, "eu.kanade.tachiyomi.source.model.Filter\$Text") -> {
                val state = tsundokuFilter.getField("state") as? String ?: ""
                Filter.Text(name, state)
            }

            // Filter.CheckBox → Filter.Check
            className.endsWith("Filter\$CheckBox") || className.endsWith("Filter.CheckBox") ||
                isSubclassOf(tsundokuFilter, "eu.kanade.tachiyomi.source.model.Filter\$CheckBox") -> {
                val state = tsundokuFilter.getField("state") as? Boolean ?: false
                Filter.Check(name, value = state)
            }

            // Filter.TriState → Filter.Check (with exclusion support)
            className.endsWith("Filter\$TriState") || className.endsWith("Filter.TriState") ||
                isSubclassOf(tsundokuFilter, "eu.kanade.tachiyomi.source.model.Filter\$TriState") -> {
                val state = tsundokuFilter.getField("state") as? Int ?: 0
                // TriState: 0=ignore, 1=include, 2=exclude
                // IReader Check: null=ignore, true=include, false=exclude
                val boolValue: Boolean? = when (state) {
                    1 -> true
                    2 -> false
                    else -> null
                }
                Filter.Check(name, allowsExclusion = true, value = boolValue)
            }

            // Filter.Select → Filter.Select
            className.endsWith("Filter\$Select") || className.endsWith("Filter.Select") ||
                isSubclassOf(tsundokuFilter, "eu.kanade.tachiyomi.source.model.Filter\$Select") -> {
                val values = tsundokuFilter.getField("values") as? Array<*> ?: emptyArray<Any>()
                val state = tsundokuFilter.getField("state") as? Int ?: 0
                val options = values.map { it.toString() }.toTypedArray()
                Filter.Select(name, options, state)
            }

            // Filter.Group → Filter.Group
            className.endsWith("Filter\$Group") || className.endsWith("Filter.Group") ||
                isSubclassOf(tsundokuFilter, "eu.kanade.tachiyomi.source.model.Filter\$Group") -> {
                @Suppress("UNCHECKED_CAST")
                val state = tsundokuFilter.getField("state") as? List<Any> ?: emptyList()
                val convertedFilters = state.mapNotNull { convertSingleFilter(it) }
                Filter.Group(name, convertedFilters)
            }

            // Filter.Sort → Filter.Sort
            className.endsWith("Filter\$Sort") || className.endsWith("Filter.Sort") ||
                isSubclassOf(tsundokuFilter, "eu.kanade.tachiyomi.source.model.Filter\$Sort") -> {
                val values = tsundokuFilter.getField("values") as? Array<*> ?: emptyArray<Any>()
                val state = tsundokuFilter.getField("state")
                val options = values.map { it.toString() }.toTypedArray()

                val selection = if (state != null) {
                    val index = state.getField("index") as? Int ?: 0
                    val ascending = state.getField("ascending") as? Boolean ?: true
                    Filter.Sort.Selection(index, ascending)
                } else null

                Filter.Sort(name, options, selection)
            }

            else -> null
        }
    }

    // ==================== IReader → Tsundoku ====================

    private fun convertToTsundokuFilter(iReaderFilter: Filter<*>): Any? {
        return when (iReaderFilter) {
            is Filter.Note -> createTsundokuFilter("Header", iReaderFilter.name)
            is Filter.Text -> createTsundokuTextFilter(iReaderFilter.name, iReaderFilter.value)
            is Filter.Check -> {
                if (iReaderFilter.allowsExclusion) {
                    createTsundokuTriStateFilter(iReaderFilter.name, iReaderFilter.value)
                } else {
                    createTsundokuCheckBoxFilter(iReaderFilter.name, iReaderFilter.value ?: false)
                }
            }
            is Filter.Select -> createTsundokuSelectFilter(iReaderFilter.name, iReaderFilter.options, iReaderFilter.value)
            is Filter.Group -> createTsundokuGroupFilter(iReaderFilter.name, iReaderFilter.filters)
            is Filter.Sort -> createTsundokuSortFilter(iReaderFilter.name, iReaderFilter.options, iReaderFilter.value)
            else -> null
        }
    }

    // ==================== Apply Values ====================

    private fun applyFilterValue(iReaderFilter: Filter<*>, tsundokuFilter: Any) {
        when (iReaderFilter) {
            is Filter.Text -> {
                tsundokuFilter.setField("state", iReaderFilter.value)
            }
            is Filter.Check -> {
                if (iReaderFilter.allowsExclusion) {
                    // Convert Boolean? to TriState int
                    val triState = when (iReaderFilter.value) {
                        true -> 1  // INCLUDE
                        false -> 2 // EXCLUDE
                        null -> 0  // IGNORE
                    }
                    tsundokuFilter.setField("state", triState)
                } else {
                    tsundokuFilter.setField("state", iReaderFilter.value ?: false)
                }
            }
            is Filter.Select -> {
                tsundokuFilter.setField("state", iReaderFilter.value)
            }
            is Filter.Sort -> {
                val sel = iReaderFilter.value
                if (sel != null) {
                    val selectionClass = try {
                        Class.forName("eu.kanade.tachiyomi.source.model.Filter\$Sort\$Selection")
                    } catch (e: ClassNotFoundException) {
                        null
                    }
                    selectionClass?.let {
                        val selection = it.getDeclaredConstructor(Int::class.java, Boolean::class.java)
                            .newInstance(sel.index, sel.ascending)
                        tsundokuFilter.setField("state", selection)
                    }
                }
            }
            is Filter.Group -> {
                @Suppress("UNCHECKED_CAST")
                val tsundokuState = tsundokuFilter.getField("state") as? List<Any> ?: return
                iReaderFilter.filters.zip(tsundokuState).forEach { (iChild, tChild) ->
                    applyFilterValue(iChild, tChild)
                }
            }
            else -> {} // Note and other types don't have state
        }
    }

    // ==================== Tsundoku Filter Factory ====================

    private fun createTsundokuFilter(type: String, name: String): Any? {
        return try {
            val baseClass = Class.forName("eu.kanade.tachiyomi.source.model.Filter\$$type")
            baseClass.getDeclaredConstructor(String::class.java).newInstance(name)
        } catch (e: Exception) {
            null
        }
    }

    private fun createTsundokuTextFilter(name: String, value: String): Any? {
        return try {
            val clazz = Class.forName("eu.kanade.tachiyomi.source.model.Filter\$Text")
            clazz.getDeclaredConstructor(String::class.java, String::class.java).newInstance(name, value)
        } catch (e: Exception) {
            null
        }
    }

    private fun createTsundokuCheckBoxFilter(name: String, value: Boolean): Any? {
        return try {
            val clazz = Class.forName("eu.kanade.tachiyomi.source.model.Filter\$CheckBox")
            clazz.getDeclaredConstructor(String::class.java, Boolean::class.java).newInstance(name, value)
        } catch (e: Exception) {
            null
        }
    }

    private fun createTsundokuTriStateFilter(name: String, value: Boolean?): Any? {
        return try {
            val clazz = Class.forName("eu.kanade.tachiyomi.source.model.Filter\$TriState")
            val state = when (value) {
                true -> 1
                false -> 2
                null -> 0
            }
            clazz.getDeclaredConstructor(String::class.java, Int::class.java).newInstance(name, state)
        } catch (e: Exception) {
            null
        }
    }

    private fun createTsundokuSelectFilter(name: String, options: Array<String>, value: Int): Any? {
        return try {
            val clazz = Class.forName("eu.kanade.tachiyomi.source.model.Filter\$Select")
            clazz.getDeclaredConstructor(String::class.java, Array::class.java, Int::class.java)
                .newInstance(name, options, value)
        } catch (e: Exception) {
            null
        }
    }

    private fun createTsundokuGroupFilter(name: String, filters: List<Filter<*>>): Any? {
        return try {
            val clazz = Class.forName("eu.kanade.tachiyomi.source.model.Filter\$Group")
            val tsundokuChildren = filters.mapNotNull { convertToTsundokuFilter(it) }
            clazz.getDeclaredConstructor(String::class.java, List::class.java)
                .newInstance(name, tsundokuChildren)
        } catch (e: Exception) {
            null
        }
    }

    private fun createTsundokuSortFilter(name: String, options: Array<String>, value: Filter.Sort.Selection?): Any? {
        return try {
            val clazz = Class.forName("eu.kanade.tachiyomi.source.model.Filter\$Sort")
            val selectionClass = Class.forName("eu.kanade.tachiyomi.source.model.Filter\$Sort\$Selection")
            val selection = value?.let {
                selectionClass.getDeclaredConstructor(Int::class.java, Boolean::class.java)
                    .newInstance(it.index, it.ascending)
            }
            clazz.getDeclaredConstructor(String::class.java, Array::class.java, selectionClass)
                .newInstance(name, options, selection)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== Reflection Helpers ====================

    private fun Any.getField(name: String): Any? {
        return try {
            var clazz: Class<*>? = this.javaClass
            while (clazz != null) {
                try {
                    val field = clazz.getDeclaredField(name)
                    field.isAccessible = true
                    return field.get(this)
                } catch (e: NoSuchFieldException) {
                    clazz = clazz.superclass
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun Any.setField(name: String, value: Any?) {
        try {
            var clazz: Class<*>? = this.javaClass
            while (clazz != null) {
                try {
                    val field = clazz.getDeclaredField(name)
                    field.isAccessible = true
                    field.set(this, value)
                    return
                } catch (e: NoSuchFieldException) {
                    clazz = clazz.superclass
                }
            }
        } catch (e: Exception) {
            // Silently fail
        }
    }

    private fun isSubclassOf(obj: Any, className: String): Boolean {
        return try {
            val targetClass = Class.forName(className)
            targetClass.isInstance(obj)
        } catch (e: Exception) {
            false
        }
    }
}
