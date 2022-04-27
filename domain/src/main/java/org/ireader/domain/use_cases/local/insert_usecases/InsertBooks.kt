package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.common_data.repository.LocalBookRepository
import org.ireader.domain.utils.withIOContext
import javax.inject.Inject

class InsertBooks @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(books: List<Book>): List<Long> {
        return withIOContext {
            return@withIOContext localBookRepository.insertBooks(books)
        }
    }
}

class InsertBookAndChapters @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(books: List<Book>, chapters: List<Chapter>) {
        return withIOContext {
            return@withIOContext localBookRepository.insertBooksAndChapters(books, chapters)
        }
    }
}