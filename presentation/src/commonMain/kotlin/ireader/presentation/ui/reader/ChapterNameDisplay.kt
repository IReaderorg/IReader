package ireader.presentation.ui.reader

/**
 * Chapter names in reader navigation UI are shown in full and wrap onto as many lines as needed.
 */
internal data class ChapterNameDisplay(
    val text: String,
    val maxLines: Int,
)

internal fun chapterNameForDisplay(name: String): ChapterNameDisplay = ChapterNameDisplay(
    text = name,
    maxLines = Int.MAX_VALUE,
)

internal fun previousChapterTitle(name: String?): ChapterNameDisplay =
    chapterNameForDisplay(name ?: "Previous Chapter")
