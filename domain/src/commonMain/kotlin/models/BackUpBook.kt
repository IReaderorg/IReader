package ireader.domain.models

import androidx.annotation.Keep
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class BackUpBook(
        val book: Book,
        val chapters: List<Chapter>,
)
