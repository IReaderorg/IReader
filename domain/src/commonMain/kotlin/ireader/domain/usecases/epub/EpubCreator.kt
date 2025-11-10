package ireader.domain.usecases.epub

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book

expect class EpubCreator {
    suspend operator fun invoke(book: Book, uri: Uri,currentEvent: (String) -> Unit)
    @Composable
    fun onEpubCreateRequested(book: Book, onStart: @Composable (Any) -> Unit)

}