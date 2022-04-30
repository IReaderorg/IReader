package org.ireader.domain.use_cases.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class RemoteUseCases @Inject constructor(
    val getBookDetail: GetBookDetail,
    val getRemoteReadingContent: GetRemoteReadingContent,
    val getRemoteChapters: GetRemoteChapters,
    val getRemoteBooks: GetRemoteBooksUseCase,
)
