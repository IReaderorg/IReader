package ireader.presentation.ui.home.library.components

import androidx.compose.runtime.Composable
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryWithCount
import ireader.i18n.localize

val Category.visibleName
    @Composable
    get() = when (id) {
        Category.ALL_ID -> localize { xml -> xml.allCategory }
        Category.UNCATEGORIZED_ID -> localize { xml -> xml.uncategorized }
        else -> name
    }

val CategoryWithCount.visibleName
    @Composable
    get() = category.visibleName
