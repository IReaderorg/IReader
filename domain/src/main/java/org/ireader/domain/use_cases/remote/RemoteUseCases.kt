package org.ireader.domain.use_cases.remote

import androidx.annotation.Keep
import javax.inject.Inject

@Keep
data class RemoteUseCases @Inject constructor(
    val getBookDetail: GetBookDetail,
    val getRemoteReadingContent: GetRemoteReadingContent,
    val getRemoteChapters: GetRemoteChapters,
    val getRemoteBooks: GetRemoteBooksUseCase,
)









