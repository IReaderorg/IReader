package org.ireader.domain.models

import androidx.annotation.Keep
import org.ireader.domain.models.entities.Book

@Keep
data class SourceResponse(
    val success: Boolean,
    val message: String? = null,
    val prevPage: Int? = null,
    val nextPage: Int? = null,
    val books: List<Book> = emptyList(),
    val lastUpdate: Long? = null,
)
