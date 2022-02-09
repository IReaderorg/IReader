package org.ireader.use_cases.remote

import org.ireader.domain.use_cases.remote.GetRemoteBooksByRemoteMediator
import org.ireader.infinity.core.domain.use_cases.remote.GetBookDetail
import org.ireader.infinity.core.domain.use_cases.remote.GetRemoteChapters
import org.ireader.infinity.core.domain.use_cases.remote.GetRemoteReadingContent

data class RemoteUseCases(
    val getBookDetail: GetBookDetail,
    val getRemoteReadingContent: GetRemoteReadingContent,
    val getRemoteBooksByRemoteMediator: GetRemoteBooksByRemoteMediator,
    val getRemoteChapters: GetRemoteChapters,
)








