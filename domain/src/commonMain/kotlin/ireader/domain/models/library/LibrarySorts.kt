package ireader.domain.models.library

import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

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
                    Type.DateFetched -> UiText.MStringResource(Res.string.date_fetched)
                    Type.DateAdded -> UiText.MStringResource(Res.string.date_added)
                    Type.Title -> UiText.MStringResource(Res.string.title)
                    Type.Unread -> UiText.MStringResource(Res.string.unread)
                    Type.Source -> UiText.MStringResource(Res.string.source)
                    Type.TotalChapters -> UiText.MStringResource(Res.string.total_chapter)
                    Type.LastRead -> UiText.MStringResource(Res.string.last_read)
                    Type.LastUpdated -> UiText.MStringResource(Res.string.last_update)
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
