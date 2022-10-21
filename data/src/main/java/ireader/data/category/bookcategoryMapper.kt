package ireader.data.category

import ireader.common.models.entities.BookCategory
import ireader.common.models.entities.Category

val bookCategoryMapper: (Long, Long, Long) -> BookCategory = { _,bookId, categoryId ->
    BookCategory(
        bookId, categoryId
    )
}