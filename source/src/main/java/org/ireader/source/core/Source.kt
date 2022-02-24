package org.ireader.source.core

import org.ireader.source.models.BookInfo
import org.ireader.source.models.ChapterInfo

/** Source : tachiyomi**/

interface Source {

    val id: Long

    val lang: String

    val name: String

    suspend fun getBookDetails(book: BookInfo): BookInfo

    suspend fun getChapterList(book: BookInfo): List<ChapterInfo>

    suspend fun getContents(chapter: ChapterInfo): List<String>

    fun getRegex(): Regex {
        return Regex("")
    }
}

