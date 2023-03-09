package ireader.domain.models.entities

data class CategoryWithCount(val category: Category, val bookCount: Int) {

    val id get() = category.id

    val name get() = category.name
}

data class CategoryWithRelation(
    val id: Long = -1,
    val name: String = "",
    val order: Long = 0,
    val flags: Long = 0,
    val bookCount: Int
) {
    fun toCategoryWithCount(): CategoryWithCount {
        return CategoryWithCount(
            Category(
                name = name,
                flags = flags,
                id = id,
                order = order,
            ),
            bookCount = bookCount
        )
    }
}
