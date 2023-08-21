package ireader.presentation.ui.book.viewmodel

import ireader.i18n.UiText


data class ChapterSort(val type: Type, val isAscending: Boolean) {

    enum class Type {
        Default,
        ByName,
        BySource,
        ByChapterNumber,
        DateFetched,
        DateUpload,
        Bookmark,
        Read,
        ;

        companion object {
            fun name(type: Type): UiText {
                return when (type) {
                    Type.Default -> UiText.MStringResource { xml -> xml.systemDefault }
                    Type.ByName -> UiText.MStringResource { xml -> xml.byName }
                    Type.BySource -> UiText.MStringResource { xml -> xml.bySource }
                    Type.ByChapterNumber -> UiText.MStringResource { xml -> xml.byChapterNumber }
                    Type.Bookmark -> UiText.MStringResource { xml -> xml.byBookmark }
                    Type.DateFetched -> UiText.MStringResource { xml -> xml.dateFetched }
                    Type.DateUpload -> UiText.MStringResource { xml -> xml.byDateUploaded }
                    Type.Read -> UiText.MStringResource { xml -> xml.byDateRead }
                }
            }
        }
    }

    companion object {
        val types = Type.values()
        val default = ChapterSort(Type.Default, true)
    }
}

val ChapterSort.parameter: String
    get() {
        val sort = when (type) {
            ChapterSort.Type.Default -> "default"
            ChapterSort.Type.ByName -> "by_name"
            ChapterSort.Type.BySource -> "by_source"
            ChapterSort.Type.ByChapterNumber -> "by_chapter_number"
            ChapterSort.Type.DateFetched -> "date_fetched"
            ChapterSort.Type.DateUpload -> "date_upload"
            ChapterSort.Type.Read -> "read"
            ChapterSort.Type.Bookmark -> "bookmark"
        }
        return if (isAscending) sort else sort + "Desc"
    }

fun ChapterSort.serialize(): String {
    val type = type.name
    val order = if (isAscending) "a" else "d"
    return "$type,$order"
}

fun ChapterSort.Companion.deserialize(serialized: String): ChapterSort {
    if (serialized.isEmpty()) return default

    return try {
        val values = serialized.split(",")
        val type = enumValueOf<ChapterSort.Type>(values[0])
        val ascending = values[1] == "a"
        ChapterSort(type, ascending)
    } catch (e: Exception) {
        default
    }
}
