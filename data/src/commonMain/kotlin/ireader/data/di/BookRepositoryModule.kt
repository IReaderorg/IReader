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
    // BookCategoryRepositoryImpl now receives LibraryChangeNotifier to emit change events
    // when book categories are added/updated/deleted
    single<BookCategoryRepository> { 
        BookCategoryRepositoryImpl(
            handler = get(),
            changeNotifier = getOrNull<LibraryChangeNotifier>() // Optional - may not be available in tests
        ) 
    }
    // BookRepositoryImpl now receives LibraryChangeNotifier to emit change events
    // when books are added/updated/deleted from the library
    single<BookRepository> { 
        BookRepositoryImpl(
            handler = get(), 
            bookCategoryRepository = get<BookCategoryRepository>(), 
            dbOptimizations = getOrNull(),
            changeNotifier = getOrNull<LibraryChangeNotifier>() // Optional - may not be available in tests
        ) 
    }
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
