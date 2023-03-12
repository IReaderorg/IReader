package ireader.presentation.core.ui

import ireader.presentation.core.VoyagerScreen

expect class WebViewScreenSpec(
         url: String?,
         sourceId: Long?,
         bookId: Long?,
         chapterId: Long?,
         enableBookFetch: Boolean,
         enableChapterFetch: Boolean,
         enableChaptersFetch: Boolean,
) : VoyagerScreen {

}