package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.LibraryBook
import javax.inject.Inject

class InsertBook @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(book: Book): Long {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext localBookRepository.insertBook(book)
        }
    }
}
class UpdateBook @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend fun update(book: LibraryBook,favorite:Boolean) {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext localBookRepository.updateBook(book, favorite = favorite)
        }
    }
    suspend fun update(book: Book) {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext localBookRepository.updateBook(book)
        }
    }
    suspend fun update(book: List<Book>) {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext localBookRepository.updateBook(book)
        }
    }
}

