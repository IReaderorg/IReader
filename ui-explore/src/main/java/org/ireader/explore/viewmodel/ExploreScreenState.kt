package org.ireader.explore.viewmodel

import org.ireader.common_models.LayoutType
import org.ireader.common_models.entities.Book
import org.ireader.core.LatestListing

import org.ireader.core.utils.UiText
import org.ireader.core.utils.emptyMangaInfoPage
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.model.Listing
import org.ireader.core_api.source.model.MangasPageInfo



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

