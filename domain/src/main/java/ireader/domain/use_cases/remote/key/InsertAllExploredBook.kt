package ireader.domain.use_cases.remote.key

import ireader.common.data.repository.RemoteKeyRepository
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Book
import ireader.common.models.entities.BookItem
import ireader.common.models.entities.RemoteKeys
import org.koin.core.annotation.Factory

@Factory
class InsertAllExploredBook(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(books: List<Book>): List<Long> {
        return ireader.common.extensions.withIOContext {
            return@withIOContext remoteKeyRepository.insertBooks(books)
        }
    }
}
@Factory
class FindAllPagedExploreBooks(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(): List<Book> {
        return remoteKeyRepository.findPagedExploreBooks()
    }
}
@Factory
class SubScribeAllPagedExploreBooks(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(): Flow<List<BookItem>> {
        return remoteKeyRepository.subscribePagedExploreBooks()
    }
}
@Factory
class PrepareExploreMode(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke(reset: Boolean, list: List<Book>, keys: List<RemoteKeys>) {
        return ireader.common.extensions.withIOContext {
            return@withIOContext remoteKeyRepository.prepareExploreMode(reset, list, keys)
        }
    }
}
@Factory
class ClearExploreMode(private val remoteKeyRepository: RemoteKeyRepository) {
    suspend operator fun invoke() {
        return remoteKeyRepository.clearExploreMode()
    }
}
