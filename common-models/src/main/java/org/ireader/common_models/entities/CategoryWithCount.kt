package org.ireader.common_models.entities

data class CategoryWithCount(val category: Category, val bookCount: Int) {

    val id get() = category.id

    val name get() = category.name
}

data class CategoryWithRelation(
    val id: Long = 0,
    val name: String = "",
    val sort: Int = 0,
    val updateInterval: Int = 0,
    val flags: Int = 0,
    val bookCount: Int
) {
    fun toCategoryWithCount(): CategoryWithCount {
        return CategoryWithCount(
            Category(
                name = name,
                flags = flags,
                id = id,
                sort = sort,
                updateInterval = updateInterval,
            ),
            bookCount = bookCount
        )
    }
}