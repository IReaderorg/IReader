package ireader.presentation.core.ui

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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.component.components.setupUiComponent
import ireader.presentation.ui.settings.reader.ReaderSettingScreenViewModel
import ireader.presentation.R
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold


class ReaderSettingSpec : VoyagerScreen() {



    @OptIn(
         ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(

    ) {
        val context = LocalContext.current
        val vm: ReaderSettingScreenViewModel = getIViewModel()
        val navigator = LocalNavigator.currentOrThrow

        val items = remember {
            listOf<Components>(
                Components.Header(
                    context.getString(R.string.font)
                ),
                Components.Row(
                    title = context.getString(R.string.font),
                    onClick = {
                        navigator.push(FontScreenSpec())
                    },
                ),
                Components.Slider(
                    preferenceAsInt = vm.fontSize,
                    title = context.getString(R.string.font_size),
                    trailing = vm.fontSize.value.toInt().toString(),
                    valueRange = 8.0F..32.0F,
                ),
                Components.Header(
                    context.getString(R.string.paragraph)
                ),
                Components.Slider(
                    preferenceAsInt = vm.paragraphsIndent,
                    title = context.getString(R.string.paragraph_indent),
                    trailing = vm.paragraphsIndent.value.toInt().toString(),
                    valueRange = 0.0F..32.0F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.distanceBetweenParagraphs,
                    title = context.getString(R.string.paragraph_distance),
                    trailing = vm.distanceBetweenParagraphs.value.toInt().toString(),
                    valueRange = 0.0F..8.0F,
                ),
                Components.Header(
                    context.getString(R.string.line)
                ),
                Components.Slider(
                    preferenceAsInt = vm.lineHeight,
                    title = context.getString(R.string.line_height),
                    trailing = vm.lineHeight.value.toInt().toString(),
                    valueRange = 22.0F..48.0F,
                ),
                Components.Header(
                    context.getString(R.string.autoscroll)
                ),
                Components.Slider(
                    preferenceAsLong = vm.autoScrollInterval,
                    title = context.getString(R.string.interval),
                    trailing = (vm.autoScrollInterval.value / 1000).toInt().toString(),
                    valueRange = 500.0F..10000.0F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.autoScrollOffset,
                    title = context.getString(R.string.offset),
                    trailing = (vm.autoScrollOffset.value / 1000).toInt().toString(),
                    valueRange = 500.0F..10000F,
                ),
                Components.Header(
                    context.getString(R.string.scrollIndicator)
                ),
                Components.Slider(
                    preferenceAsInt = vm.scrollIndicatorPadding,
                    title = context.getString(R.string.padding),
                    trailing = vm.scrollIndicatorPadding.value.toString(),
                    valueRange = 0F..32F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.scrollIndicatorWith,
                    title = context.getString(R.string.width),
                    trailing = vm.scrollIndicatorWith.value.toString(),
                    valueRange = 0F..32F,
                ),
            )
        }

        IScaffold(topBar = { scrollBehavior ->
            TitleToolbar(
                title = stringResource(R.string.reader),
                scrollBehavior = scrollBehavior,
                popBackStack = {
                    popBackStack(navigator)
                }
            )
        }) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                setupUiComponent(items)
            }
        }

    }
}
