package ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen

import ir.kazemcodes.infinity.explore_feature.data.model.Chapter

data class ChapterState (
    val isLoading : Boolean = false,
    val chapters : List<Chapter> = emptyList(),
    val error: String = "",
    val loaded : Boolean = false
)