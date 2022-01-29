package ir.kazemcodes.infinity.feature_library.presentation.components

import androidx.navigation.NavController
import ir.kazemcodes.infinity.feature_library.presentation.LibraryViewModel
import ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator.ExtensionCreatorLogTab
import ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator.ExtensionCreatorTab
import ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator.ExtensionCreatorViewModel
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionViewModel
import ir.kazemcodes.infinity.feature_sources.presentation.extension.composables.CommunitySourceScreen
import ir.kazemcodes.infinity.feature_sources.presentation.extension.composables.UserSourcesScreen

sealed class TabItem(
    var title: String,
    var screen: ComposableFun,
) {
    data class Filter(val viewModel: LibraryViewModel) :
        TabItem("Filter", { FilterScreen(viewModel) })

    data class Sort(val viewModel: LibraryViewModel, val navController: NavController) : TabItem("Sort", { SortScreen(viewModel) })
    data class Display(val viewModel: LibraryViewModel) :
        TabItem("Display", { DisplayScreen(viewModel) })

    data class Sources(val viewModel: ExtensionViewModel, val navController: NavController) :
        TabItem("Sources", { UserSourcesScreen(viewModel = viewModel,navController=navController)})

    data class CommunitySources(val viewModel: ExtensionViewModel, val navController: NavController) :
        TabItem("Community Sources", { CommunitySourceScreen(viewModel, navController = navController) })
    data class ExtensionCreator(val viewModel: ExtensionCreatorViewModel) :
        TabItem("Creator", { ExtensionCreatorTab(viewModel) })
    data class ExtensionCreatorLog(val viewModel: ExtensionCreatorViewModel) :
        TabItem("Log", { ExtensionCreatorLogTab(viewModel) })
}