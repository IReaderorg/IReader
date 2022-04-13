package org.ireader.domain.use_cases.fetchers

import androidx.annotation.Keep
import javax.inject.Inject

@Keep
data class FetchUseCase @Inject constructor(
    val fetchBookDetailAndChapterDetailFromWebView: FetchBookDetailAndChapterDetailFromWebView,
)





