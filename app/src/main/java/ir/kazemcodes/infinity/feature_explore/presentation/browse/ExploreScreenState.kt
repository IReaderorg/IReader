package ir.kazemcodes.infinity.feature_explore.presentation.browse

import ir.kazemcodes.infinity.core.data.network.models.BooksPage
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.presentation.layouts.LayoutType
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.feature_sources.sources.models.SourceTower

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
    val searchedBook: BooksPage = BooksPage(),
    val isLatestUpdateMode: Boolean = true,
    val hasNextPage : Boolean = true,
    val exploreType: ExploreType = ExploreType.Latest,
    val source: Source = SourceTower.create()
)

