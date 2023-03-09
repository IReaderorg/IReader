package ireader.domain.models.entities


data class Category(
    val id: Long = 0,
    val name: String = "",
    val order: Long = 0,
    val flags: Long = 0,
) {

    private val isUncategorized get() = id == UNCATEGORIZED_ID
    private val isAll get() = id == ALL_ID

    val isSystemCategory get() = isUncategorized || isAll

    companion object {
        const val ALL_ID = 0L
        const val UNCATEGORIZED_ID = -1L
        val baseCategories = listOf<Category>(
            Category(
                id = ALL_ID,
                name = "All",
                order = 0,
                flags = 0
            ),
            Category(
                id = UNCATEGORIZED_ID,
                name = "Uncategorized",
                order = 0,
                flags = 0
            ),
        )
    }
}

data class BookCategory(
    val bookId: Long,
    val categoryId: Long,
)

fun Category.toBookCategory(bookId: List<Long>): List<BookCategory> {
    return bookId.map {
        BookCategory(
            categoryId = this.id,
            bookId = it
        )
    }
}
data class CategoryUpdate(
    val id: Long,
    val name: String? = null,
    val order: Long? = null,
    val flags: Long? = null,
)
