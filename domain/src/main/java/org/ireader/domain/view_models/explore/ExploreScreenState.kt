package org.ireader.domain.view_models.explore

import org.ireader.core.utils.UiText
import org.ireader.domain.models.ExploreType
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.entities.Book
import org.ireader.source.core.CatalogSource
import org.ireader.source.models.BookPageInfo

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
    val searchedBook: BookPageInfo = BookPageInfo(),
    val isLatestUpdateMode: Boolean = true,
    val hasNextPage: Boolean = true,
    val exploreType: ExploreType = ExploreType.Latest,
    val source: CatalogSource? = null,
)

