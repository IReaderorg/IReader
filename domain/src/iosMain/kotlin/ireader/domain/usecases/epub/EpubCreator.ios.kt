package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book

/**
 * iOS implementation of EpubCreator
 * 
 * TODO: Implement EPUB creation
 */
actual class EpubCreator {
    actual suspend operator fun invoke(book: Book, uri: Uri, currentEvent: (String) -> Unit) {
        currentEvent("EPUB creation not yet implemented on iOS")
        // TODO: Implement EPUB creation
    }
}
