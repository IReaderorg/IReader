package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

@OptIn(ExperimentalMaterialApi::class)
sealed interface ScreenSpec {

    companion object {
        val allScreens = listOf<ScreenSpec>(
            LibraryScreenSpec,
            UpdateScreenSpec,
            HistoryScreenSpec,
            ExtensionScreenSpec,
            SettingScreenSpec,
            AppearanceScreenSpec,
            AboutInfoScreenSpec,
            AdvanceSettingSpec,
            BookDetailScreenSpec,
            DownloaderScreenSpec,
            ChapterScreenSpec,
            ReaderScreenSpec,
            WebViewScreenSpec,
            ExploreScreenSpec,
            GlobalSearchScreenSpec,
            TTSScreenSpec
        ).associateBy { it.navHostRoute }
    }

    val navHostRoute: String

    val arguments: List<NamedNavArgument> get() = emptyList()

    val deepLinks: List<NavDeepLink> get() = emptyList()

    @OptIn(ExperimentalAnimationApi::class)
    fun NavGraphBuilder.AddDestination(
        navController: NavController
    ) {
        composable(
            route = navHostRoute,
            deepLinks = deepLinks,
            arguments = arguments,
        ) { navBackStackEntry ->
            Content(navController, navBackStackEntry)
        }
    }

    @Composable
    fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
    )
}
