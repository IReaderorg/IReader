package org.ireader.domain.repository

import org.ireader.domain.local.BookDatabase
import org.ireader.infinity.core.domain.repository.RemoteRepository

interface Repository {

    val localBookRepository: LocalBookRepository

    val localChapterRepository: LocalChapterRepository

    val remoteRepository: RemoteRepository
    val database: BookDatabase
}