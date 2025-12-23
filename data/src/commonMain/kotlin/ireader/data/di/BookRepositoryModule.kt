package ireader.data.di

import ireader.data.book.BookRepositoryImpl
import ireader.data.category.BookCategoryRepositoryImpl
import ireader.data.explorebook.ExploreBookRepositoryImpl
import ireader.data.repository.CachedBookRepository
import ireader.data.repository.ConsolidatedBookRepositoryImpl
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ExploreBookRepository
import ireader.domain.services.library.LibraryChangeNotifier
import org.koin.dsl.module

/**
 * DI module for book-related repositories.
 * Contains BookRepository, BookCategoryRepository, ExploreBookRepository, and ConsolidatedBookRepository.
 */
val bookRepositoryModule = module {
    single<BookCategoryRepository> { BookCategoryRepositoryImpl(get()) }
    single<BookRepository> { BookRepositoryImpl(get(), get<BookCategoryRepository>(), getOrNull()) }
    single<ExploreBookRepository> { ExploreBookRepositoryImpl(get()) }
    
    // Consolidated BookRepository with caching and change notifications
    // The CachedBookRepository wraps the base implementation and:
    // 1. Provides in-memory caching for frequently accessed data
    // 2. Notifies LibraryChangeNotifier when books are modified
    // This enables pagination to know when to reload without subscribing to all books
    single<ireader.domain.data.repository.consolidated.BookRepository> { 
        val baseRepo = ireader.data.repository.consolidated.BookRepositoryImpl(get())
        CachedBookRepository(
            delegate = baseRepo,
            optimizedHandler = getOrNull(), // Optional - may not be available
            changeNotifier = getOrNull<LibraryChangeNotifier>() // Optional - may not be available in tests
        )
    }
}
