package org.ireader.domain.use_cases.remote

import javax.inject.Inject

data class RemoteUseCases @Inject constructor(
    val getBookDetail: GetBookDetail,
    val getRemoteReadingContent: GetRemoteReadingContent,
    val getRemoteChapters: GetRemoteChapters,
    val getRemoteBooks: GetRemoteBooksUseCase,
)
