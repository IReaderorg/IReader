package ir.kazemcodes.infinity.library_feature.data.repository

import ir.kazemcodes.infinity.library_feature.data.BookDao
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import ir.kazemcodes.infinity.library_feature.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalRepositoryImpl @Inject constructor(
    private val dao : BookDao
) : LocalRepository {
    override fun getBooks(): Flow<List<BookEntity>> {
        return dao.getBooks()
    }
    override suspend fun getBookByName(name: String): BookEntity? {
        return dao.getBookByName(name)
    }
    override suspend fun insertBook(bookEntity: BookEntity) {
        return dao.insertBook(bookEntity)
    }

    override suspend fun insertBooks(bookEntities: List<BookEntity>) {
        return dao.insertBooks(bookEntities)
    }

    override suspend fun insertChapters(chapterEntities: List<ChapterEntity>) {
        return dao.insertChapters(chapterEntities = chapterEntities)
    }

    override suspend fun deleteBook(bookEntity: BookEntity) {
        return dao.deleteBook(bookEntity = bookEntity)
    }
    override suspend fun deleteAllBook() {
        return dao.deleteAllBook()
    }
}