package org.ireader.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ireader.appearance.AppearanceSettingScreen
import org.ireader.appearance.AppearanceViewModel
import org.ireader.common_extensions.launchIO
import org.ireader.common_resources.UiText
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.core_ui.ui.SnackBarListener
import org.ireader.domain.use_cases.theme.toCustomTheme
import org.ireader.ui_appearance.R

object AppearanceScreenSpec : ScreenSpec {

    override val navHostRoute: String = "appearance_setting_route"

    @ExperimentalMaterial3Api
    @Composable
    override fun TopBar(
        controller: ScreenSpec.Controller
    ) {
        val vm: AppearanceViewModel = hiltViewModel(controller.navBackStackEntry)

        val scope = rememberCoroutineScope()
        val isNotSavable = vm.getIsNotSavable()

        Toolbar(
            title = {
                BigSizeTextComposable(text = stringResource(R.string.appearance))
            },
            navigationIcon = {
                TopAppBarBackButton() {
                    popBackStack(controller.navController)
                }
            },
            actions = {
                AnimatedVisibility(
                    visible = !isNotSavable,
                ) {
                    AppIconButton(
                        imageVector = Icons.Default.Save,
                        onClick = {
                            val theme =  vm.getThemes(vm.colorTheme.value)
                                if (theme != null) {
                                    scope.launchIO {
                                        val themeId = vm.themeRepository.insert(theme.toCustomTheme(false))
                                        vm.colorTheme.value = themeId
                                        vm.showSnackBar(UiText.StringResource(R.string.theme_was_saved))
                                    }
                                } else {
                                    vm.showSnackBar(UiText.StringResource(R.string.theme_was_not_valid))
                                }
                        }
                    )
                }

            }
        )
    }

    @Composable
    override fun Content(
        controller: ScreenSpec.Controller
    ) {
        val viewModel: AppearanceViewModel = hiltViewModel(controller.navBackStackEntry)
        SnackBarListener(viewModel, controller.snackBarHostState)
        AppearanceSettingScreen(
            modifier = Modifier.padding(controller.scaffoldPadding),
            saveDarkModePreference = { theme ->
                viewModel.saveNightModePreferences(theme)
            },
            onPopBackStack = {
                controller.navController.popBackStack()
            },
            vm = viewModel,
            scaffoldPaddingValues = controller.scaffoldPadding

        )
    }
}

fun popBackStack(navController: NavController) {
    navController.popBackStack()
}