package eu.kanade.tachiyomi.source.model

import androidx.compose.runtime.Stable

/**
 * Minimal FilterList class shim for tsundoku extension compatibility.
 */
@Stable
data class FilterList(val list: List<Filter<*>>) : List<Filter<*>> by list {
    constructor(vararg fs: Filter<*>) : this(if (fs.isNotEmpty()) fs.asList() else emptyList())
}
