package org.ireader.domain.repository

interface Repository {

    val localBookRepository: LocalBookRepository
    val localChapterRepository: LocalChapterRepository
    val remoteRepository: RemoteRepository
}