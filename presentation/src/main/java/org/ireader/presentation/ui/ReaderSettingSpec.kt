package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.ireader.components.Controller
import org.ireader.components.components.Components
import org.ireader.components.components.TitleToolbar
import org.ireader.components.components.setupUiComponent
import org.ireader.settings.setting.reader.ReaderSettingScreenViewModel
import org.ireader.ui_settings.R

object ReaderSettingSpec : ScreenSpec {

    override val navHostRoute: String = "reader_settings_screen_route"
    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.reader),
            navController = controller.navController,
            scrollBehavior = controller.scrollBehavior
        )
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val context = LocalContext.current
        val vm: ReaderSettingScreenViewModel = hiltViewModel()
        val items = remember {
            listOf<Components>(
                Components.Header(
                    context.getString(R.string.font)
                ),
                Components.Row(
                    title = context.getString(R.string.font),
                    onClick = {
                        controller.navController.navigate(FontScreenSpec.navHostRoute)
                    },
                ),
                Components.Slider(
                    preferenceAsInt = vm.fontSize,
                    title = context.getString(org.ireader.ui_reader.R.string.font_size),
                    trailing = vm.fontSize.value.toInt().toString(),
                    valueRange = 8.0F..32.0F,
                ),
                Components.Header(
                    context.getString(org.ireader.ui_reader.R.string.paragraph)
                ),
                Components.Slider(
                    preferenceAsInt = vm.paragraphsIndent,
                    title = context.getString(org.ireader.ui_reader.R.string.paragraph_indent),
                    trailing = vm.paragraphsIndent.value.toInt().toString(),
                    valueRange = 0.0F..32.0F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.distanceBetweenParagraphs,
                    title = context.getString(org.ireader.ui_reader.R.string.paragraph_distance),
                    trailing = vm.distanceBetweenParagraphs.value.toInt().toString(),
                    valueRange = 0.0F..8.0F,
                ),
                Components.Header(
                    context.getString(org.ireader.ui_reader.R.string.line)
                ),
                Components.Slider(
                    preferenceAsInt = vm.lineHeight,
                    title = context.getString(org.ireader.ui_reader.R.string.line_height),
                    trailing = vm.lineHeight.value.toInt().toString(),
                    valueRange = 22.0F..48.0F,
                ),
                Components.Header(
                    context.getString(org.ireader.ui_reader.R.string.autoscroll)
                ),
                Components.Slider(
                    preferenceAsLong = vm.autoScrollInterval,
                    title = context.getString(org.ireader.ui_reader.R.string.interval),
                    trailing = (vm.autoScrollInterval.value / 1000).toInt().toString(),
                    valueRange = 500.0F..10000.0F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.autoScrollOffset,
                    title = context.getString(org.ireader.ui_reader.R.string.offset),
                    trailing = (vm.autoScrollOffset.value / 1000).toInt().toString(),
                    valueRange = 500.0F..10000F,
                ),
                Components.Header(
                    context.getString(org.ireader.ui_reader.R.string.scrollIndicator)
                ),
                Components.Slider(
                    preferenceAsInt = vm.scrollIndicatorPadding,
                    title = context.getString(org.ireader.ui_reader.R.string.padding),
                    trailing = vm.scrollIndicatorPadding.value.toString(),
                    valueRange = 0F..32F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.scrollIndicatorWith,
                    title = context.getString(org.ireader.ui_reader.R.string.width),
                    trailing = vm.scrollIndicatorWith.value.toString(),
                    valueRange = 0F..32F,
                ),
            )
        }

        LazyColumn(
            modifier = Modifier
                .padding(controller.scaffoldPadding)
                .fillMaxSize()
        ) {
            setupUiComponent(items)
        }
    }
}
