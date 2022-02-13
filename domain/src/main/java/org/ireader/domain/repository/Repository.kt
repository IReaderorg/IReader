package org.ireader.domain.repository

import org.ireader.domain.local.BookDatabase

interface Repository {

    val localBookRepository: LocalBookRepository

    val localChapterRepository: LocalChapterRepository

    val remoteRepository: RemoteRepository
    val database: BookDatabase
}