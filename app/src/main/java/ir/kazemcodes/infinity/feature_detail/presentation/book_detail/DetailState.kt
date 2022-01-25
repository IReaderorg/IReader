package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.utils.UiText

data class DetailState(
    val source: Source,
    val book: Book = Book.create(),
    val inLibrary : Boolean = false,
    val isLoading: Boolean = false,
    val isLoaded : Boolean = false,
    val error: String = UiText.noError(),
    val isExploreMode: Boolean = false
    )