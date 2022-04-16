package org.ireader.domain.ui

import androidx.navigation.NavType
import androidx.navigation.navArgument
import org.ireader.core.utils.Constants.ARG_HIDE_BOTTOM_BAR

object NavigationArgs {
    val bookId = navArgument("bookId") {
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
}