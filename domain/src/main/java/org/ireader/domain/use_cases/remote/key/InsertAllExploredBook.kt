package org.ireader.domain.use_cases.remote.key

import kotlinx.coroutines.flow.Flow
import org.ireader.common_extensions.withIOContext
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.BookItem
import org.ireader.common_models.entities.RemoteKeys
import javax.inject.Inject

class InsertAllExploredBook @Inject constructor(private val remoteKeyRepository: org.ireader.common_data.repository.RemoteKeyRepository) {
    suspend operator fun invoke(books: List<Book>): List<Long> {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext remoteKeyRepository.insertBooks(books)
        }
    }
}

class FindAllPagedExploreBooks @Inject constructor(private val remoteKeyRepository: org.ireader.common_data.repository.RemoteKeyRepository) {
    suspend operator fun invoke(): List<Book> {
        return remoteKeyRepository.findPagedExploreBooks()
    }
}

class SubScribeAllPagedExploreBooks @Inject constructor(private val remoteKeyRepository: org.ireader.common_data.repository.RemoteKeyRepository) {
    suspend operator fun invoke(): Flow<List<BookItem>> {
        return remoteKeyRepository.subscribePagedExploreBooks()
    }
}

class PrepareExploreMode @Inject constructor(private val remoteKeyRepository: org.ireader.common_data.repository.RemoteKeyRepository) {
    suspend operator fun invoke(reset: Boolean, list: List<Book>, keys: List<RemoteKeys>) {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext remoteKeyRepository.prepareExploreMode(reset, list, keys)
        }
    }
}

class ClearExploreMode @Inject constructor(private val remoteKeyRepository: org.ireader.common_data.repository.RemoteKeyRepository) {
    suspend operator fun invoke() {
        return remoteKeyRepository.clearExploreMode()
    }
}
