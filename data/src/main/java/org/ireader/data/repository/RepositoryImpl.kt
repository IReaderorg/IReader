package org.ireader.data.repository

import org.ireader.domain.local.BookDatabase
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.repository.RemoteRepository
import org.ireader.domain.repository.Repository
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    override val localBookRepository: LocalBookRepository,
    override val localChapterRepository: LocalChapterRepository,
    override val remoteRepository: RemoteRepository,
    override val database: BookDatabase,
) : Repository