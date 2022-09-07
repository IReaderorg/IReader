package ireader.ui.library.components

import androidx.compose.runtime.Composable
import ireader.common.models.entities.Category
import ireader.common.models.entities.CategoryWithCount
import ireader.core.ui.ui.string
import ireader.ui.library.R

val Category.visibleName
    @Composable
    get() = when (id) {
        Category.ALL_ID -> string(R.string.all_category)
        Category.UNCATEGORIZED_ID -> string(R.string.uncategorized)
        else -> name
    }

val CategoryWithCount.visibleName
    @Composable
    get() = category.visibleName
