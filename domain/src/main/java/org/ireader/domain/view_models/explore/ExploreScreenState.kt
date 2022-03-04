package org.ireader.domain.view_models.explore

import org.ireader.core.LatestListing
import org.ireader.core.emptyMangaInfoPage
import org.ireader.core.utils.UiText
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.entities.Book
import sources.CatalogSource
import sources.model.Listing
import sources.model.MangasPageInfo

data class ExploreScreenState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val error: UiText = UiText.DynamicString(""),
    val page: Int = 1,
    val searchPage: Int = 1,
    val layout: LayoutType = LayoutType.GridLayout,
    val isMenuDropDownShown: Boolean = false,
    val isSearchModeEnable: Boolean = false,
    val searchQuery: String = "",
    val searchedBook: MangasPageInfo = emptyMangaInfoPage(),
    val isLatestUpdateMode: Boolean = true,
    val exploreType: Listing = LatestListing(),
    val source: CatalogSource? = null,
    val isFilterEnable: Boolean = false,

    )

