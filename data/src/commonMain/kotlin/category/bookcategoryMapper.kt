package ireader.data.category

import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.Category

val bookCategoryMapper: (Long, Long, Long) -> BookCategory = { _,bookId, categoryId ->
    BookCategory(
        bookId, categoryId
    )
}