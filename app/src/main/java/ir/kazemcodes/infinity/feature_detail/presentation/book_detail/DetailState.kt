package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book

data class DetailState(
    val source: Source,
    val book: Book = Book.create(),
    val inLibrary : Boolean = false,
    val isLoading: Boolean = false,
    val isLoaded : Boolean = false,
    val error: String = "",
    val isExploreMode: Boolean = false
    )