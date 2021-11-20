package ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen

data class ReadingScreenState(
    val isLoading : Boolean = false,
    val readingContent: String = "",
    val error: String = ""
)
