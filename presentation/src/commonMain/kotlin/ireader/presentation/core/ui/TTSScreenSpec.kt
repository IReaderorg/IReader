package ireader.presentation.core.ui

import ireader.presentation.core.VoyagerScreen

expect class TTSScreenSpec(
         bookId: Long,
         chapterId: Long,
         sourceId: Long,
         readingParagraph: Int,
) : VoyagerScreen {

}