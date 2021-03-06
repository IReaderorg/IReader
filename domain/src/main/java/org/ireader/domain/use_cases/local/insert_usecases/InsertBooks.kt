package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import javax.inject.Inject

class InsertBooks @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke(books: List<Book>): List<Long> {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext bookRepository.insertBooks(books)
        }
    }
}

class InsertBookAndChapters @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke(books: List<Book>, chapters: List<Chapter>) {
        return org.ireader.common_extensions.withIOContext {
            return@withIOContext bookRepository.insertBooksAndChapters(books, chapters)
        }
    }
}
