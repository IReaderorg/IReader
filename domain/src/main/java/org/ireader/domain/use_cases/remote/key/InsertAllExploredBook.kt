package org.ireader.domain.use_cases.remote.key

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.RemoteKeyRepository
import javax.inject.Inject

class InsertAllExploredBook @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(books: List<Book>): List<Long> {
        return remoteKeyRepository.insertAllExploredBook(books)
    }
}

class FindAllPagedExploreBooks @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(): List<Book> {
        return remoteKeyRepository.findPagedExploreBooks()
    }
}

class SubScribeAllPagedExploreBooks @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(): Flow<List<Book>> {
        return remoteKeyRepository.subscribePagedExploreBooks()
    }
}