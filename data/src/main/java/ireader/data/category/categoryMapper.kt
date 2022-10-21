package ireader.data.category

import ireader.common.models.entities.Category
import ireader.common.models.entities.CategoryWithCount

val categoryMapper: (Long, String, Long, Long) -> Category = { id, name, order, flags ->
    Category(
        id = id,
        name = name,
        order = order,
        flags = flags,
    )
}

internal val categoryWithCountMapper =
    { id: Long, name: String, order: Long, flags: Long, count: Long ->
        CategoryWithCount(Category(id, name, order, flags), count.toInt())
    }
