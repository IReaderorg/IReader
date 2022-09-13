package ireader.presentation.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import ireader.ui.component.Controller

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
    fun Controller.getSourceId(): Long {
        return this.navBackStackEntry.arguments!!.getLong("sourceId")
    }

    fun Controller.getQuery(): String? {
        return this.navBackStackEntry.arguments!!.getString("query")
    }



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
    fun Content(
        controller: Controller,
    )
}
