package ireader.presentation.ui.home.library.components

import androidx.compose.runtime.Composable
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryWithCount
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
val Category.visibleName
    @Composable
    get() = when (id) {
        Category.ALL_ID -> localize(Res.string.all_category)
        Category.UNCATEGORIZED_ID -> localize(Res.string.uncategorized)
        else -> name
    }

val CategoryWithCount.visibleName
    @Composable
    get() = category.visibleName
