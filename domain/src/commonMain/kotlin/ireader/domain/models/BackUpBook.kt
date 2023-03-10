package ireader.domain.models

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlinx.serialization.Serializable


@Serializable
data class BackUpBook(
        val book: Book,
        val chapters: List<Chapter>,
)
