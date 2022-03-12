package org.ireader.domain.use_cases.fetchers

import javax.inject.Inject

data class FetchUseCase @Inject constructor(
    val fetchBookDetailAndChapterDetailFromWebView: FetchBookDetailAndChapterDetailFromWebView,
)





