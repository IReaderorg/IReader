package ireader.presentation.core.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.localize
import ireader.i18n.resources.MR

import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.component.components.setupUiComponent
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.reader.ReaderSettingScreenViewModel

class ReaderSettingSpec : VoyagerScreen() {



    @OptIn(
         ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(

    ) {
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        val vm: ReaderSettingScreenViewModel = getIViewModel()
        val navigator = LocalNavigator.currentOrThrow

        val items = remember {
            listOf<Components>(
                Components.Header(
                    localizeHelper.localize(MR.strings.font)
                ),
                Components.Row(
                    title = localizeHelper.localize(MR.strings.font),
                    onClick = {
                        navigator.push(FontScreenSpec())
                    },
                ),
                Components.Slider(
                    preferenceAsInt = vm.fontSize,
                    title = localizeHelper.localize(MR.strings.font_size),
                    trailing = vm.fontSize.value.toInt().toString(),
                    valueRange = 8.0F..32.0F,
                ),
                Components.Header(
                    localizeHelper.localize(MR.strings.paragraph)
                ),
                Components.Slider(
                    preferenceAsInt = vm.paragraphsIndent,
                    title = localizeHelper.localize(MR.strings.paragraph_indent),
                    trailing = vm.paragraphsIndent.value.toInt().toString(),
                    valueRange = 0.0F..32.0F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.distanceBetweenParagraphs,
                    title = localizeHelper.localize(MR.strings.paragraph_distance),
                    trailing = vm.distanceBetweenParagraphs.value.toInt().toString(),
                    valueRange = 0.0F..8.0F,
                ),
                Components.Header(
                    localizeHelper.localize(MR.strings.line)
                ),
                Components.Slider(
                    preferenceAsInt = vm.lineHeight,
                    title = localizeHelper.localize(MR.strings.line_height),
                    trailing = vm.lineHeight.value.toInt().toString(),
                    valueRange = 22.0F..48.0F,
                ),
                Components.Header(
                    localizeHelper.localize(MR.strings.autoscroll)
                ),
                Components.Slider(
                    preferenceAsLong = vm.autoScrollInterval,
                    title = localizeHelper.localize(MR.strings.interval),
                    trailing = (vm.autoScrollInterval.value / 1000).toInt().toString(),
                    valueRange = 500.0F..10000.0F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.autoScrollOffset,
                    title = localizeHelper.localize(MR.strings.offset),
                    trailing = (vm.autoScrollOffset.value / 1000).toInt().toString(),
                    valueRange = 500.0F..10000F,
                ),
                Components.Header(
                    localizeHelper.localize(MR.strings.scrollIndicator)
                ),
                Components.Slider(
                    preferenceAsInt = vm.scrollIndicatorPadding,
                    title = localizeHelper.localize(MR.strings.padding),
                    trailing = vm.scrollIndicatorPadding.value.toString(),
                    valueRange = 0F..32F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.scrollIndicatorWith,
                    title = localizeHelper.localize(MR.strings.width),
                    trailing = vm.scrollIndicatorWith.value.toString(),
                    valueRange = 0F..32F,
                ),
                Components.Header(
                    localizeHelper.localize(MR.strings.preload_settings)
                ),
                Components.Switch(
                    preference = vm.autoPreloadNextChapter,
                    title = localizeHelper.localize(MR.strings.auto_preload_next_chapter),
                    subtitle = localizeHelper.localize(MR.strings.auto_preload_next_chapter_summary)
                ),
                Components.Switch(
                    preference = vm.preloadOnlyOnWifi,
                    title = localizeHelper.localize(MR.strings.preload_only_on_wifi),
                    subtitle = localizeHelper.localize(MR.strings.preload_only_on_wifi_summary),
                ),
            )
        }

        IScaffold(topBar = { scrollBehavior ->
            TitleToolbar(
                title = localize(MR.strings.reader),
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
