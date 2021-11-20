package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen

import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter

data class DetailState (
    val isLoading : Boolean = false,
    val book: Book = Book.create(),
    val chapters: List<Chapter> = emptyList(),
    val error: String = ""
)