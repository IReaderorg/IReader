package org.ireader.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ireader.appearance.AppearanceSettingScreen
import org.ireader.appearance.AppearanceViewModel
import org.ireader.common_resources.UiText
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.core_ui.preferences.PreferenceValues
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.theme.prefs.CustomThemes
import org.ireader.core_ui.theme.prefs.toCustomTheme
import org.ireader.core_ui.theme.themes
import org.ireader.core_ui.ui.SnackBarListener
import org.ireader.ui_appearance.R

object AppearanceScreenSpec : ScreenSpec {

    override val navHostRoute: String = "appearance_setting_route"

    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        controller: ScreenSpec.Controller
    ) {
        val vm: AppearanceViewModel = hiltViewModel(controller.navBackStackEntry)
        val theme = derivedStateOf { themes.getOrNull(vm.colorTheme.value) }
        val currentPallet by derivedStateOf { if (vm.themeMode.value == PreferenceValues.ThemeMode.Dark) theme.value?.darkColor else theme.value?.lightColor }
        val customizedColor = vm.getCustomizedColors()
        val appbarColors = AppColors.current
        val isSavable =
            derivedStateOf { currentPallet?.primary != customizedColor.value.primary || currentPallet?.secondary != customizedColor.value.secondary || appbarColors.bars != customizedColor.value.bars }

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
                if (isSavable.value) {
                    AppIconButton(
                        imageVector = Icons.Default.Save,
                        onClick = {
                            vm.getThemes(themes.lastIndex)?.let {
                                vm.customThemes.set(CustomThemes(listOf(it.toCustomTheme())))
                                vm.showSnackBar(UiText.StringResource(R.string.theme_was_saved))
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