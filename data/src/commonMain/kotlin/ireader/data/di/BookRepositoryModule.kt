package ireader.data.di

import ireader.data.book.BookRepositoryImpl
import ireader.data.category.BookCategoryRepositoryImpl
import ireader.data.explorebook.ExploreBookRepositoryImpl
import ireader.data.repository.ConsolidatedBookRepositoryImpl
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ExploreBookRepository
import org.koin.dsl.module

/**
 * DI module for book-related repositories.
 * Contains BookRepository, BookCategoryRepository, ExploreBookRepository, and ConsolidatedBookRepository.
 */
val bookRepositoryModule = module {
    single<BookCategoryRepository> { BookCategoryRepositoryImpl(get()) }
    single<BookRepository> { BookRepositoryImpl(get(), get<BookCategoryRepository>(), getOrNull()) }
    single<ExploreBookRepository> { ExploreBookRepositoryImpl(get()) }
    single<ireader.domain.data.repository.consolidated.BookRepository> { 
        ConsolidatedBookRepositoryImpl(get<BookRepository>(), get<BookCategoryRepository>()) 
    }
}
