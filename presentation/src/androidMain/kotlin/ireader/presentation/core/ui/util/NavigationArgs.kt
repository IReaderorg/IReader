package ireader.presentation.core.ui.util

import androidx.navigation.NavType
import androidx.navigation.navArgument
import ireader.i18n.ARG_HIDE_BOTTOM_BAR
import ireader.i18n.ARG_TRANSPARENT_STATUS_BAR

object NavigationArgs {
    val bookId = navArgument("bookId") {
        type = NavType.LongType
        defaultValue = 0L
    }
    val readingParagraph = navArgument("readingParagraph") {
        type = NavType.LongType
        defaultValue = 0L
    }

    val sourceId = navArgument("sourceId") {
        type = NavType.LongType
        defaultValue = 0L
    }

    val chapterId = navArgument("chapterId") {
        type = NavType.LongType
        defaultValue = 0L
    }
    val url = navArgument("url") {
        type = NavType.StringType
        nullable = true
    }
    val query = navArgument("query") {
        type = NavType.StringType
        defaultValue = ""
    }
    val showBottomNav = navArgument(ARG_HIDE_BOTTOM_BAR) {
        defaultValue = true
    }
    val transparentStatusBar = navArgument(ARG_TRANSPARENT_STATUS_BAR) {
        defaultValue = true
    }
}
