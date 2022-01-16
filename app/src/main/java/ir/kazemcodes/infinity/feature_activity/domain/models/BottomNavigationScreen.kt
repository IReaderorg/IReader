package ir.kazemcodes.infinity.feature_activity.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavigationScreen(
    val index: Int,
    val title: String,
    val icon: ImageVector,
) {
    object Library :
        BottomNavigationScreen(
            0,
            "Library",
            icon = Icons.Default.Book
        )

    object ExtensionScreen : BottomNavigationScreen(
        1,
        "Explore",
        Icons.Default.Explore
    )

    object Setting : BottomNavigationScreen(
        2,
        "Setting",
        Icons.Default.Settings
    )
}