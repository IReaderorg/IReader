package org.ireader.presentation.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink

@OptIn(ExperimentalMaterialApi::class)
sealed interface ScreenSpec {

    companion object {
        val allScreens = listOf<ScreenSpec>(
            LibraryScreenSpec,
            ExtensionScreenSpec,
            SettingScreenSpec,
            AppearanceScreenSpec,
            AboutInfoScreenSpec,
            DnsOverHttpSettingSpec,
            BookDetailScreenSpec,
            DownloaderScreenSpec,
            ExtensionCreatorSpec,
            ChapterScreenSpec,
            ReaderScreenSpec,
            WebViewScreenSpec,
            ExploreScreenSpec
        ).associateBy { it.navHostRoute }
    }

    val navHostRoute: String

    val arguments: List<NamedNavArgument> get() = emptyList()

    val deepLinks: List<NavDeepLink> get() = emptyList()


    @Composable
    fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {

    }


    @Composable
    fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    )
}