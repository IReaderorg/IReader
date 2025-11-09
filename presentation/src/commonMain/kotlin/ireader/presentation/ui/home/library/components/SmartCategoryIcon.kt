package ireader.presentation.ui.home.library.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.SmartCategory

/**
 * Get the icon for a smart category based on its ID
 */
@Composable
fun getSmartCategoryIcon(categoryId: Long): ImageVector? {
    return when (categoryId) {
        SmartCategory.RECENTLY_ADDED_ID -> Icons.Default.NewReleases
        SmartCategory.CURRENTLY_READING_ID -> Icons.Default.AutoStories
        SmartCategory.COMPLETED_ID -> Icons.Default.CheckCircle
        SmartCategory.UNREAD_ID -> Icons.Outlined.Circle
        SmartCategory.ARCHIVED_ID -> Icons.Default.Archive
        else -> null
    }
}

/**
 * Check if a category is a smart category
 */
fun Category.isSmartCategory(): Boolean {
    return SmartCategory.isSmartCategory(this.id)
}
