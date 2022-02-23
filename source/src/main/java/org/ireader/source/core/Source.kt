package org.ireader.source.core

import okhttp3.Headers
import org.ireader.source.models.*
import org.jsoup.nodes.Document

/** Source : tachiyomi**/

interface Source {

    val id: Long

    val lang: String

    val name: String

    val baseUrl: String

    val creator: String

    val iconUrl: String

    val headers: Headers

    suspend fun getLatest(page: Int): MangasPageInfo

    suspend fun getPopular(page: Int): MangasPageInfo

    suspend fun getSearch(page: Int, query: String, filters: FilterList): MangasPageInfo

    suspend fun getContents(chapter: ChapterInfo): List<String>

    suspend fun getMangaDetails(manga: MangaInfo): MangaInfo

    suspend fun getChapters(manga: MangaInfo): List<ChapterInfo>

    fun popularParse(document: Document): MangasPageInfo

    fun latestParse(document: Document): MangasPageInfo

    fun detailParse(document: Document): MangaInfo

    fun chaptersParse(document: Document): List<ChapterInfo>

    fun searchParse(document: Document): MangasPageInfo

    fun pageContentParse(document: Document): List<String>

    /**
     * Returns the list of filters for the source.
     */
    fun getFilterList(): FilterList

    fun getListings(): List<Listing>

    fun getRegex(): Regex {
        return Regex("")
    }
}

