package ireader.common.models

import androidx.annotation.Keep
import ireader.common.models.entities.Chapter
import kotlinx.serialization.Serializable
import ireader.common.models.entities.Book


@Keep
@Serializable
data class BackUpBook(
    val book: Book,
    val chapters: List<Chapter>,
)
