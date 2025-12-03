package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book

/**
 * iOS implementation of ImportEpub
 * 
 * TODO: Implement EPUB parsing using okio and XML parsing
 */
actual class ImportEpub {
    actual suspend fun invoke(uri: Uri): Result<Book> {
        // TODO: Implement EPUB import
        return Result.failure(Exception("EPUB import not yet implemented on iOS"))
    }
}
