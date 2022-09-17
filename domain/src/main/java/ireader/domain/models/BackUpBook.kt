package ireader.domain.models

import androidx.annotation.Keep
import ireader.common.models.entities.Book
import ireader.common.models.entities.Chapter
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class BackUpBook(
    val book: Book,
    val chapters: List<Chapter>,
)
