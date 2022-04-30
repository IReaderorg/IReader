package org.ireader.common_models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter

@Keep
@Serializable
data class BackUpBook(
    val book: Book,
    val chapters: List<Chapter>,
)