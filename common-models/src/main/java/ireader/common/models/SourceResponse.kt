
package ireader.common.models

import ireader.common.models.entities.Book

data class SourceResponse(
    val success: Boolean,
    val message: String? = null,
    val prevPage: Int? = null,
    val nextPage: Int? = null,
    val books: List<Book> = emptyList(),
    val lastUpdate: Long? = null,
)
