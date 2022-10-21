package ireader.common.models.library

data class LibraryFilter(val type: Type, val value: Value) {

    enum class Type {
        Unread,
        Completed,
        Downloaded;
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
