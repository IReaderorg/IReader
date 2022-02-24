package org.ireader.source.core

import okhttp3.Headers
import org.ireader.source.models.FilterList
import org.ireader.source.models.Listing
import org.ireader.source.models.MangasPageInfo

interface CatalogSource : Source {

    val baseUrl: String

    val headers: Headers

    override val lang: String

    suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo

    suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo

    fun getListings(): List<Listing>

    fun getFilters(): FilterList


}
