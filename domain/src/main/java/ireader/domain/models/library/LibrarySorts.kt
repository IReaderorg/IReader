package ireader.common.models.library

import ireader.common.resources.UiText
import ireader.domain.R

data class LibrarySort(val type: Type, val isAscending: Boolean) {

    enum class Type {
        Title,
        LastRead,
        LastUpdated,
        Unread,
        DateAdded,
        DateFetched,
        TotalChapters,
        Source;

        companion object {
            fun name(type: Type): UiText {
                return when (type) {
                    Type.DateFetched -> UiText.StringResource(R.string.date_fetched)
                    Type.DateAdded -> UiText.StringResource(R.string.date_added)
                    Type.Title -> UiText.StringResource(R.string.title)
                    Type.Unread -> UiText.StringResource(R.string.unread)
                    Type.Source -> UiText.StringResource(R.string.source)
                    Type.TotalChapters -> UiText.StringResource(R.string.total_chapter)
                    Type.LastRead -> UiText.StringResource(R.string.last_read)
                    Type.LastUpdated -> UiText.StringResource(R.string.last_update)
                }
            }
        }
    }

    companion object {
        val types = Type.values()
        val default = LibrarySort(Type.Title, true)
    }
}

val LibrarySort.parameter: String
    get() {
        val sort = when (type) {
            LibrarySort.Type.Title -> "title"
            LibrarySort.Type.LastRead -> "lastRead"
            LibrarySort.Type.LastUpdated -> "lastUpdated"
            LibrarySort.Type.Unread -> "unread"
            LibrarySort.Type.TotalChapters -> "totalChapters"
            LibrarySort.Type.Source -> "source"
            LibrarySort.Type.DateAdded -> "dateAdded"
            LibrarySort.Type.DateFetched -> "dateFetched"
        }
        return if (isAscending) sort else sort + "Desc"
    }

fun LibrarySort.serialize(): String {
    val type = type.name
    val order = if (isAscending) "a" else "d"
    return "$type,$order"
}

fun LibrarySort.Companion.deserialize(serialized: String): LibrarySort {
    if (serialized.isEmpty()) return default

    return try {
        val values = serialized.split(",")
        val type = enumValueOf<LibrarySort.Type>(values[0])
        val ascending = values[1] == "a"
        LibrarySort(type, ascending)
    } catch (e: Exception) {
        default
    }
}
