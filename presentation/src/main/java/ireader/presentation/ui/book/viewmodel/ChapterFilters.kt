package ireader.presentation.ui.book.viewmodel

data class ChaptersFilters(val type: Type, val value: Value) {

    enum class Type {
        Unread,
        Downloaded,
        Bookmarked;
    }

    enum class Value {
        Included,
        Excluded,
        Missing;
    }

    companion object {
        val types = Type.values()

        fun getDefault(includeAll: Boolean): List<ChaptersFilters> {
            return if (includeAll) {
                types.map { ChaptersFilters(it, Value.Missing) }
            } else {
                emptyList()
            }
        }
    }
}

private fun ChaptersFilters.serialize(): String? {
    val value = when (value) {
        ChaptersFilters.Value.Included -> "i"
        ChaptersFilters.Value.Excluded -> "e"
        ChaptersFilters.Value.Missing -> return null // Missing filters are not saved
    }
    val type = type.name
    return "$type,$value"
}

private fun ChaptersFilters.Companion.deserialize(serialized: String): ChaptersFilters? {
    return try {
        val parts = serialized.split(",")
        val type = enumValueOf<ChaptersFilters.Type>(parts[0])
        val state = when (parts[1]) {
            "i" -> ChaptersFilters.Value.Included
            "e" -> ChaptersFilters.Value.Excluded
            else -> return null
        }
        ChaptersFilters(type, state)
    } catch (e: Exception) {
        null
    }
}

fun List<ChaptersFilters>.serialize(): String {
    return mapNotNull { it.serialize() }.joinToString(";")
}

fun ChaptersFilters.Companion.deserializeList(
    serialized: String,
    includeAll: Boolean
): List<ChaptersFilters> {
    val savedFilters = serialized.split(";").mapNotNull { ChaptersFilters.deserialize(it) }
    return if (!includeAll) {
        savedFilters
    } else {
        types.map { type ->
            savedFilters.find { it.type == type } ?: ChaptersFilters(
                type,
                ChaptersFilters.Value.Missing
            )
        }
    }
}
