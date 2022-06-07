package org.ireader.presentation.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import org.ireader.common_extensions.getCacheSize
import org.ireader.common_extensions.launchIO
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.components.components.Components
import org.ireader.components.components.SetupSettingComponents
import org.ireader.components.components.TitleToolbar
import org.ireader.settings.setting.AdvanceSettingViewModel
import org.ireader.ui_settings.R

object AdvanceSettingSpec : ScreenSpec {

    override val navHostRoute: String = "advance_setting_route"
    @ExperimentalMaterial3Api
    @Composable
    override fun TopBar(
        controller: ScreenSpec.Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.advance_setting),
            navController =controller. navController
        )
    }

    @Composable
    override fun Content(
        controller: ScreenSpec.Controller
    ) {
        val vm: AdvanceSettingViewModel = hiltViewModel(   controller.navBackStackEntry)
        val context = LocalContext.current
        val snackBarHostState = controller.snackBarHostState
        LaunchedEffect(key1 = true) {

            vm.eventFlow.collectLatest { event ->
                when (event) {
                    is UiEvent.ShowSnackbar -> {
                        snackBarHostState.showSnackbar(
                            event.uiText.asString(context)
                        )
                    }
                }
            }
        }



        val items = listOf<Components>(
            Components.Header(stringResource(R.string.data)),
            Components.Row(
                title = stringResource(id = R.string.clear_all_database),
                onClick = {
                    vm.deleteAllDatabase()
                    vm.showSnackBar(
                        UiText.StringResource(R.string.database_was_cleared)
                    )
                }
            ),
            Components.Row(
                title = stringResource(id = R.string.clear_not_in_library_books),
                onClick = {
                    vm.viewModelScope.launchIO {
                        vm.deleteUseCase.deleteNotInLibraryBooks()
                        vm.showSnackBar(
                            UiText.StringResource(R.string.success)
                        )
                    }

                }
            ),
            Components.Row(
                title = stringResource(id = R.string.clear_all_chapters),
                onClick = {
                    vm.deleteAllChapters()
                    vm.showSnackBar(
                        UiText.StringResource(R.string.chapters_was_cleared)
                    )
                }
            ),
            Components.Row(
                title = stringResource(id = R.string.clear_all_cache),
                subtitle = getCacheSize(context = context),
                onClick = {
                    context.cacheDir.deleteRecursively()
                    vm.showSnackBar(UiText.DynamicString("Clear was cleared."))
                }
            ),
            Components.Row(
                title = stringResource(id = R.string.clear_all_cover_cache),
                onClick = {
                    vm.coverCache.clearMemoryCache()
                    vm.showSnackBar(UiText.DynamicString("Clear was cleared."))
                }
            ),
            Components.Header(stringResource(R.string.reset_setting)),
            Components.Row(
                title = stringResource(id = R.string.reset_reader_screen_settings),
                onClick = {
                    vm.deleteDefaultSettings()
                }
            ),
        )

        SetupSettingComponents(scaffoldPadding = controller.scaffoldPadding, items = items)

//        Box(modifier = Modifier.padding(controller.scaffoldPadding)) {
//            AdvanceSettings(
//                vm = vm,
//                onBackStack = {
//                    controller.navController.popBackStack()
//                },
//                snackBarHostState = controller.snackBarHostState
//            )
//        }

    }
}
