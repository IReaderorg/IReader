package org.ireader.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.ireader.Controller
import org.ireader.components.components.SearchToolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.getDefaultFont
import org.ireader.settings.setting.font_screens.FontScreen
import org.ireader.settings.setting.font_screens.FontScreenViewModel
import org.ireader.ui_settings.R

@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterialApi::class)
object FontScreenSpec : ScreenSpec {
    override val navHostRoute: String = "font_screen_spec"

    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: FontScreenViewModel = hiltViewModel(   controller.navBackStackEntry)
        SearchToolbar(
            title = stringResource(R.string.font),
            actions = {
                      AppIconButton(
                          imageVector = Icons.Default.Preview,
                          tint = if (vm.previewMode.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                          onClick = {
                              vm.previewMode.value = !vm.previewMode.value

                          }
                      )
            },
            onPopBackStack = {
                controller.navController.popBackStack()
            },
            onValueChange = {
                vm.searchQuery = it
            },
            onSearch = {
                vm.searchQuery = it
            },
            scrollBehavior = controller.scrollBehavior
        )
    }

    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: FontScreenViewModel = hiltViewModel(   controller.navBackStackEntry)

        Box(modifier = Modifier.padding(controller.scaffoldPadding)) {
            FontScreen(
                vm,
                onFont = { font ->
                    vm.readerPreferences.font()
                        .set(FontType(font, getDefaultFont().fontFamily))
                }
            )
        }

    }
}


