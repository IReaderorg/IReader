package org.ireader.app.components

import androidx.compose.runtime.Composable
import org.ireader.common_models.entities.Category
import org.ireader.common_models.entities.CategoryWithCount
import org.ireader.core_ui.ui.string
import org.ireader.ui_library.R

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