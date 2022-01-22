package ir.kazemcodes.infinity.feature_library.presentation.components

import ir.kazemcodes.infinity.feature_library.presentation.LibraryViewModel
import ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator.ExtensionCreatorLogTab
import ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator.ExtensionCreatorTab
import ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator.ExtensionCreatorViewModel
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionViewModel
import ir.kazemcodes.infinity.feature_sources.presentation.extension.composables.UserSourcesScreen

sealed class TabItem(var title: String, var screen: ComposableFun) {
    data class Filter(val viewModel: LibraryViewModel) :
        TabItem("Filter", { FilterScreen(viewModel) })

    data class Sort(val viewModel: LibraryViewModel) : TabItem("Sort", { SortScreen(viewModel) })
    data class Display(val viewModel: LibraryViewModel) :
        TabItem("Display", { DisplayScreen(viewModel) })

    data class Sources(val viewModel: ExtensionViewModel) :
        TabItem("Sources", { UserSourcesScreen(viewModel = viewModel)})

    data class CommunitySources(val viewModel: ExtensionViewModel) :
        TabItem("Community Sources", { UserSourcesScreen(viewModel) })
    data class ExtensionCreator(val viewModel: ExtensionCreatorViewModel) :
        TabItem("Creator", { ExtensionCreatorTab(viewModel) })
    data class ExtensionCreatorLog(val viewModel: ExtensionCreatorViewModel) :
        TabItem("Log", { ExtensionCreatorLogTab(viewModel) })
}