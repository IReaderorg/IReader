package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.feature_sources.sources.models.SourceTower

data class DetailState(
    val source: Source = SourceTower.create(),
    val book: Book = Book.create(),
    val inLibrary : Boolean = false,
    val isLocalLoading: Boolean = false,
    val isLocalLoaded : Boolean = false,
    val error: UiText = UiText.DynamicString(""),
    val isExploreMode: Boolean = false,
    val isRemoteLoaded: Boolean = false,
    val isRemoteLoading: Boolean = false
    )