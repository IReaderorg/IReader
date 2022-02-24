package org.ireader.source.core

import org.ireader.source.models.ChapterInfo
import org.ireader.source.models.MangaInfo

/** Source : tachiyomi**/

interface Source {

    val id: Long

    val lang: String

    val name: String

    suspend fun getMangaDetails(manga: MangaInfo): MangaInfo

    suspend fun getChapterList(manga: MangaInfo): List<ChapterInfo>

    suspend fun getContents(chapter: ChapterInfo): List<String>

    fun getRegex(): Regex {
        return Regex("")
    }
}

