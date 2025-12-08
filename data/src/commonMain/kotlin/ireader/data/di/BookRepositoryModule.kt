package ireader.data.di

import ireader.data.book.BookRepositoryImpl
import ireader.data.category.BookCategoryRepositoryImpl
import ireader.data.repository.ConsolidatedBookRepositoryImpl
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import org.koin.dsl.module

/**
 * DI module for book-related repositories.
 * Contains BookRepository, BookCategoryRepository, and ConsolidatedBookRepository.
 */
val bookRepositoryModule = module {
    single<BookCategoryRepository> { BookCategoryRepositoryImpl(get()) }
    single<BookRepository> { BookRepositoryImpl(get(), get<BookCategoryRepository>(), getOrNull()) }
    single<ireader.domain.data.repository.consolidated.BookRepository> { 
        ConsolidatedBookRepositoryImpl(get<BookRepository>(), get<BookCategoryRepository>()) 
    }
}
