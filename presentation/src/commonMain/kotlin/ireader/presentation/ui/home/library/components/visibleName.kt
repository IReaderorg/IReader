package ireader.presentation.ui.home.library.components

import androidx.compose.runtime.Composable
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryWithCount
import ireader.i18n.localize
import ireader.i18n.resources.MR
val Category.visibleName
    @Composable
    get() = when (id) {
        Category.ALL_ID -> localize(MR.strings.all_category)
        Category.UNCATEGORIZED_ID -> localize(MR.strings.uncategorized)
        else -> name
    }

val CategoryWithCount.visibleName
    @Composable
    get() = category.visibleName
