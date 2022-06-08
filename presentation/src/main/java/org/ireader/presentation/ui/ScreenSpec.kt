package org.ireader.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink

@OptIn(ExperimentalMaterialApi::class)
sealed interface ScreenSpec {

    companion object {
        @OptIn(ExperimentalMaterial3Api::class) val allScreens = listOf<ScreenSpec>(
            LibraryScreenSpec,
            UpdateScreenSpec,
            HistoryScreenSpec,
            ExtensionScreenSpec,
            MoreScreenSpec,
            AppearanceScreenSpec,
            AboutSettingSpec,
            AdvanceSettingSpec,
            BookDetailScreenSpec,
            DownloaderScreenSpec,
            ChapterScreenSpec,
            ReaderScreenSpec,
            WebViewScreenSpec,
            ExploreScreenSpec,
            GlobalSearchScreenSpec,
            TTSScreenSpec,
            BackupAndRestoreScreenSpec,
            SettingScreenSpec,
            ReaderSettingSpec,
            FontScreenSpec,
            CategoryScreenSpec,
            GeneralScreenSpec,
            BrowseSettingSpec,
            DownloadSettingSpec,
            LibraryScreenSpec,
            SecuritySettingSpec,
            TrackingSettingSpec
        ).associateBy { it.navHostRoute }
    }

    val navHostRoute: String

    val arguments: List<NamedNavArgument> get() = emptyList()

    val deepLinks: List<NavDeepLink> get() = emptyList()

    data class Controller @OptIn(ExperimentalMaterial3Api::class) constructor(
        val navController: NavController,
        val navBackStackEntry: NavBackStackEntry,
        val snackBarHostState: SnackbarHostState,
        val sheetState: ModalBottomSheetState,
        val drawerState: DrawerState,
        val scaffoldPadding: PaddingValues = PaddingValues(0.dp),
        val requestHideNavigator: (Boolean) -> Unit = {},
        val requestHideTopAppbar: (Boolean) -> Unit = {},
    )


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BottomModalSheet(
        controller: Controller
    ) {

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BottomAppBar(
        controller: Controller
    ) {

    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ModalDrawer(
        controller: Controller
    ) {

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(
        controller: Controller
    ) {

    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun  Content(
        controller: Controller
    )
}

