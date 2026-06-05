package ireader.server.di

import ireader.server.api.SourcesApi
import ireader.server.api.BooksApi
import ireader.server.api.ChaptersApi

/**
 * Server dependency injection module.
 *
 * Provides API handlers for the server.
 * 
 * Note: Full integration with IReader's CatalogStore and repositories
 * requires platform-specific database setup. This is a simplified version
 * that provides the API structure.
 */
class ServerModule {
    val sourcesApi = SourcesApi()
    val booksApi = BooksApi()
    val chaptersApi = ChaptersApi()

    companion object {
        fun create(): ServerModule {
            return ServerModule()
        }
    }
}
