package ireader.domain.models.library

data class LibraryFilter(val type: Type, val value: Value) {

    enum class Type {
        Unread,
        Completed,
        Downloaded,
        InProgress;
    }

    enum class Value {
        Included,
        Excluded,
        Missing;
    }

    companion object {
        val types = Type.values()

        fun getDefault(includeAll: Boolean): List<LibraryFilter> {
            return if (includeAll) {
                types.map { LibraryFilter(it, Value.Missing) }
            } else {
                emptyList()
            }
        }
    }
}

private fun LibraryFilter.serialize(): String? {
    val value = when (value) {
        LibraryFilter.Value.Included -> "i"
        LibraryFilter.Value.Excluded -> "e"
        LibraryFilter.Value.Missing -> return null // Missing filters are not saved
    }
    val type = type.name
    return "$type,$value"
}

private fun LibraryFilter.Companion.deserialize(serialized: String): LibraryFilter? {
    return try {
        val parts = serialized.split(",")
        val type = enumValueOf<LibraryFilter.Type>(parts[0])
        val state = when (parts[1]) {
            "i" -> LibraryFilter.Value.Included
            "e" -> LibraryFilter.Value.Excluded
            else -> return null
        }
        LibraryFilter(type, state)
    } catch (e: Exception) {
        null
    }
}

fun List<LibraryFilter>.serialize(): String {
    return mapNotNull { it.serialize() }.joinToString(";")
}

fun LibraryFilter.Companion.deserializeList(
    serialized: String,
    includeAll: Boolean
): List<LibraryFilter> {
    val savedFilters = serialized.split(";").mapNotNull { LibraryFilter.deserialize(it) }
    return if (!includeAll) {
        savedFilters
    } else {
        types.map { type ->
            savedFilters.find { it.type == type } ?: LibraryFilter(
                type,
                LibraryFilter.Value.Missing
            )
        }
    }
}

/**
 * Comprehensive state for library filtering and sorting
 */
data class LibraryFilterState(
    val filters: Set<LibraryFilter.Type> = emptySet(),
    val sortOption: SortOption = SortOption.TITLE,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
    val displayMode: DisplayMode = DisplayMode.COMPACT_GRID,
    val columnCount: Int = 3
) {
    companion object {
        val default = LibraryFilterState()
    }
}

/**
 * Available sort options for library
 */
enum class SortOption {
    TITLE,
    AUTHOR,
    LAST_READ,
    DATE_ADDED,
    UNREAD_COUNT;
}

/**
 * Sort direction
 */
enum class SortDirection {
    ASCENDING,
    DESCENDING;
    
    fun toggle(): SortDirection {
        return when (this) {
            ASCENDING -> DESCENDING
            DESCENDING -> ASCENDING
        }
    }
}

/**
 * Display mode for library items
 */
enum class DisplayMode {
    COMPACT_GRID,
    COMFORTABLE_GRID,
    LIST,
    COVER_ONLY;
}
