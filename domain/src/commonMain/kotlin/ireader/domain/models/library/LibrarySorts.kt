package ireader.domain.models.library

import ireader.i18n.UiText

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
                    Type.DateFetched -> UiText.MStringResource() { xml ->
                        xml.dateFetched
                    }

                    Type.DateAdded -> UiText.MStringResource() { xml ->
                        xml.dateAdded
                    }

                    Type.Title -> UiText.MStringResource() { xml ->
                        xml.title
                    }

                    Type.Unread -> UiText.MStringResource() { xml ->
                        xml.unread
                    }

                    Type.Source -> UiText.MStringResource() { xml ->
                        xml.source
                    }

                    Type.TotalChapters -> UiText.MStringResource() { xml ->
                        xml.totalChapter
                    }

                    Type.LastRead -> UiText.MStringResource() { xml ->
                        xml.lastRead
                    }

                    Type.LastUpdated -> UiText.MStringResource() { xml ->
                        xml.lastUpdate
                    }
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
