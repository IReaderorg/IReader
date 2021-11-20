package ir.kazemcodes.infinity.library_feature.domain.repository

import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import kotlinx.coroutines.flow.Flow

interface LocalRepository {

    fun getBooks() : Flow<List<BookEntity>>

    suspend fun getBookByName(name : String): BookEntity?

    suspend fun insertBook(bookEntity : BookEntity)

    suspend fun insertBooks(bookEntities : List<BookEntity>)

    suspend fun insertChapters(chapterEntity: List<ChapterEntity>)


    suspend fun deleteBook(bookEntity: BookEntity)

    suspend fun deleteAllBook()


}