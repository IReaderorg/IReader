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
                    localizeHelper.localize() { xml ->
                        xml.font
                    }
                ),
                Components.Row(
                    title = localizeHelper.localize() { xml ->
                        xml.font
                    },
                    onClick = {
                        navigator.push(FontScreenSpec())
                    },
                ),
                Components.Slider(
                    preferenceAsInt = vm.fontSize,
                    title = localizeHelper.localize() { xml ->
                        xml.fontSize
                    },
                    trailing = vm.fontSize.value.toInt().toString(),
                    valueRange = 8.0F..32.0F,
                ),
                Components.Header(
                    localizeHelper.localize() { xml ->
                        xml.paragraph
                    }
                ),
                Components.Slider(
                    preferenceAsInt = vm.paragraphsIndent,
                    title = localizeHelper.localize { xml -> xml.paragraphIndent },
                    trailing = vm.paragraphsIndent.value.toInt().toString(),
                    valueRange = 0.0F..32.0F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.distanceBetweenParagraphs,
                    title = localizeHelper.localize { xml -> xml.paragraphDistance },
                    trailing = vm.distanceBetweenParagraphs.value.toInt().toString(),
                    valueRange = 0.0F..8.0F,
                ),
                Components.Header(
                    localizeHelper.localize { xml -> xml.line }
                ),
                Components.Slider(
                    preferenceAsInt = vm.lineHeight,
                    title = localizeHelper.localize { xml -> xml.lineHeight },
                    trailing = vm.lineHeight.value.toInt().toString(),
                    valueRange = 22.0F..48.0F,
                ),
                Components.Header(
                    localizeHelper.localize { xml -> xml.autoscroll }
                ),
                Components.Slider(
                    preferenceAsLong = vm.autoScrollInterval,
                    title = localizeHelper.localize { xml -> xml.interval },
                    trailing = (vm.autoScrollInterval.value / 1000).toInt().toString(),
                    valueRange = 500.0F..10000.0F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.autoScrollOffset,
                    title = localizeHelper.localize { xml -> xml.offset },
                    trailing = (vm.autoScrollOffset.value / 1000).toInt().toString(),
                    valueRange = 500.0F..10000F,
                ),
                Components.Header(
                    localizeHelper.localize { xml -> xml.scrollIndicator }
                ),
                Components.Slider(
                    preferenceAsInt = vm.scrollIndicatorPadding,
                    title = localizeHelper.localize { xml -> xml.padding },
                    trailing = vm.scrollIndicatorPadding.value.toString(),
                    valueRange = 0F..32F,
                ),
                Components.Slider(
                    preferenceAsInt = vm.scrollIndicatorWith,
                    title = localizeHelper.localize { xml -> xml.width },
                    trailing = vm.scrollIndicatorWith.value.toString(),
                    valueRange = 0F..32F,
                ),
            )
        }

        IScaffold(topBar = { scrollBehavior ->
            TitleToolbar(
                title = localize { xml -> xml.reader },
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
