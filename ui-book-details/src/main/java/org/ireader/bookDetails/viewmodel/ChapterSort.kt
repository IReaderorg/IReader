package org.ireader.bookDetails.viewmodel

import org.ireader.common_models.R
import org.ireader.common_resources.UiText

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
                    Type.Default -> UiText.StringResource(R.string.system_default)
                    Type.ByName -> UiText.StringResource(R.string.by_name)
                    Type.BySource -> UiText.StringResource(R.string.by_source)
                    Type.ByChapterNumber -> UiText.StringResource(R.string.by_chapter_number)
                    Type.Bookmark -> UiText.StringResource(R.string.by_bookmark)
                    Type.DateFetched -> UiText.StringResource(R.string.date_fetched)
                    Type.DateUpload -> UiText.StringResource(R.string.by_date_uploaded)
                    Type.Read -> UiText.StringResource(R.string.by_date_read)
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
