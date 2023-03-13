package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book

actual class EpubCreator {
    actual suspend operator fun invoke(book: Book, uri: Uri) {

    }
    actual fun onEpubCreateRequested(book: Book, onStart: (Any) -> Unit) {

    }


}