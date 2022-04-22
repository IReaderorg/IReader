package org.ireader.domain.use_cases.remote.key

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.BookItem
import org.ireader.domain.repository.RemoteKeyRepository
import org.ireader.domain.utils.withIOContext
import javax.inject.Inject

class InsertAllExploredBook @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(books: List<Book>): List<Long> {
        return withIOContext {
            return@withIOContext remoteKeyRepository.insertBooks(books)
        }
    }
}

class FindAllPagedExploreBooks @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(): List<Book> {
        return remoteKeyRepository.findPagedExploreBooks()
    }
}

class SubScribeAllPagedExploreBooks @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(): Flow<List<BookItem>> {
        return remoteKeyRepository.subscribePagedExploreBooks()
    }
}

class PrepareExploreMode @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(reset: Boolean, list: List<Book>, keys: List<RemoteKeys>) {
        return withIOContext {
            return@withIOContext remoteKeyRepository.prepareExploreMode(reset, list, keys)
        }
    }
}

class ClearExploreMode @Inject constructor(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke() {
        return remoteKeyRepository.clearExploreMode()
    }
}