package org.ireader.domain.use_cases.local.insert_usecases

import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalBookRepository
import javax.inject.Inject

class InsertBooks @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(books: List<Book>): List<Long> {
        return localBookRepository.insertBooks(books)
    }
}

class InsertBookAndChapters @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(books: List<Book>, chapters: List<Chapter>) {
        return localBookRepository.insertBooksAndChapters(books, chapters)
    }
}