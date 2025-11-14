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
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

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
                    localizeHelper.localize(Res.string.font)
                ),
                Components.Row(
                    title = localizeHelper.localize(Res.string.font),
                    onClick = {
                        navigator.push(FontScreenSpec())
                    },
                ),
                Components.Slider(
                    preferenceAsInt = vm.fontSize,
                    title = localizeHelper.localize(Res.string.font_size),
                    trailing = vm.fontSize.value.toInt().toString(),
                    valueRange = 8.0F..32.0F,
                ),
                Components.Header(
                    localizeHelper.localize(Res.string.paragraph)
                ),
                Components.Slider(
                    preferenceAsInt = vm.paragraphsIndent,
                    title = localizeHelper.localize(Res.string.paragraph_indent),
                    trailing = vm.paragraphsIndent.value.toInt().toString(),
                    valueRange = 0.0F..32.0F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.distanceBetweenParagraphs,
                    title = localizeHelper.localize(Res.string.paragraph_distance),
                    trailing = vm.distanceBetweenParagraphs.value.toInt().toString(),
                    valueRange = 0.0F..8.0F,
                ),
                Components.Header(
                    localizeHelper.localize(Res.string.line)
                ),
                Components.Slider(
                    preferenceAsInt = vm.lineHeight,
                    title = localizeHelper.localize(Res.string.line_height),
                    trailing = vm.lineHeight.value.toInt().toString(),
                    valueRange = 22.0F..48.0F,
                ),
                Components.Header(
                    localizeHelper.localize(Res.string.autoscroll)
                ),
                Components.Slider(
                    preferenceAsLong = vm.autoScrollInterval,
                    title = localizeHelper.localize(Res.string.interval),
                    trailing = (vm.autoScrollInterval.value / 1000).toInt().toString(),
                    valueRange = 500.0F..10000.0F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.autoScrollOffset,
                    title = localizeHelper.localize(Res.string.offset),
                    trailing = (vm.autoScrollOffset.value / 1000).toInt().toString(),
                    valueRange = 500.0F..10000F,
                ),
                Components.Header(
                    localizeHelper.localize(Res.string.scrollIndicator)
                ),
                Components.Slider(
                    preferenceAsInt = vm.scrollIndicatorPadding,
                    title = localizeHelper.localize(Res.string.padding),
                    trailing = vm.scrollIndicatorPadding.value.toString(),
                    valueRange = 0F..32F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.scrollIndicatorWith,
                    title = localizeHelper.localize(Res.string.width),
                    trailing = vm.scrollIndicatorWith.value.toString(),
                    valueRange = 0F..32F,
                ),
                Components.Header(
                    localizeHelper.localize(Res.string.preload_settings)
                ),
                Components.Switch(
                    preference = vm.autoPreloadNextChapter,
                    title = localizeHelper.localize(Res.string.auto_preload_next_chapter),
                    subtitle = localizeHelper.localize(Res.string.auto_preload_next_chapter_summary)
                ),
                Components.Switch(
                    preference = vm.preloadOnlyOnWifi,
                    title = localizeHelper.localize(Res.string.preload_only_on_wifi),
                    subtitle = localizeHelper.localize(Res.string.preload_only_on_wifi_summary),
                ),
                Components.Header(
                    localizeHelper.localize(Res.string.reading_speed)
                ),
                Components.Slider(
                    preferenceAsInt = vm.readingSpeedWPM,
                    title = localizeHelper.localize(Res.string.reading_speed),
                    trailing = "${vm.readingSpeedWPM.value} ${localizeHelper.localize(Res.string.reading_speed_wpm)}",
                    valueRange = 150.0F..400.0F,
                ),
            )
        }

        IScaffold(topBar = { scrollBehavior ->
            TitleToolbar(
                title = localize(Res.string.reader),
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
