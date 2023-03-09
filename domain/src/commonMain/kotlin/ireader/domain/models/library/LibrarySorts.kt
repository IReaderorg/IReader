package ireader.domain.models.library

import ireader.i18n.UiText
import ireader.i18n.resources.MR

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
                    Type.DateFetched -> UiText.MStringResource(MR.strings.date_fetched)
                    Type.DateAdded -> UiText.MStringResource(MR.strings.date_added)
                    Type.Title -> UiText.MStringResource(MR.strings.title)
                    Type.Unread -> UiText.MStringResource(MR.strings.unread)
                    Type.Source -> UiText.MStringResource(MR.strings.source)
                    Type.TotalChapters -> UiText.MStringResource(MR.strings.total_chapter)
                    Type.LastRead -> UiText.MStringResource(MR.strings.last_read)
                    Type.LastUpdated -> UiText.MStringResource(MR.strings.last_update)
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
