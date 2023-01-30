package ireader.presentation.core.ui.util

import androidx.navigation.NavType
import androidx.navigation.navArgument
import ireader.i18n.ARG_HAVE_CUSTOMIZED_VARIANT_BOTTOM_BAR
import ireader.i18n.ARG_HAVE_DRAWER
import ireader.i18n.ARG_HAVE_MODAL_SHEET
import ireader.i18n.ARG_HAVE_VARIANT_BOTTOM_BAR
import ireader.i18n.ARG_HIDE_BOTTOM_BAR
import ireader.i18n.ARG_SYSTEM_BAR_PADDING
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
    val exploreType = navArgument("exploreType") {
        type = NavType.IntType
        defaultValue = 0
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
    val showModalSheet = navArgument(ARG_HAVE_MODAL_SHEET) {
        defaultValue = true
    }
    val haveDrawer = navArgument(ARG_HAVE_DRAWER) {
        defaultValue = true
    }
    val transparentStatusBar = navArgument(ARG_TRANSPARENT_STATUS_BAR) {
        defaultValue = true
    }
    val haveBottomBar = navArgument(ARG_HAVE_VARIANT_BOTTOM_BAR) {
        defaultValue = true
    }
    val haveCustomizedBottomBar = navArgument(ARG_HAVE_CUSTOMIZED_VARIANT_BOTTOM_BAR) {
        defaultValue = true
    }
    val systemBarPadding = navArgument(ARG_SYSTEM_BAR_PADDING) {
        defaultValue = true
    }
}
