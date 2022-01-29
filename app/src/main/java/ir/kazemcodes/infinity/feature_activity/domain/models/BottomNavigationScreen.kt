package ir.kazemcodes.infinity.feature_activity.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import ir.kazemcodes.infinity.feature_activity.presentation.Screen

sealed class BottomNavigationScreen(
    val index: Int,
    val name: String,
    val icon: ImageVector,
    val route : String,
    val badgeCount : Int = 0
) {
    object Library :
        BottomNavigationScreen(
            0,
            "Library",
            icon = Icons.Default.Book,
            route = Screen.Library.route
        )

    object ExtensionScreen : BottomNavigationScreen(
        1,
        "Explore",
        Icons.Default.Explore,
        route = Screen.Extension.route
    )

    object Setting : BottomNavigationScreen(
        2,
        "Setting",
        Icons.Default.Settings,
        route = Screen.Setting.route
    )
}