package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.domain.preferences.models.FontType
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.font
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.SearchToolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.settings.font_screens.FontScreen
import ireader.presentation.ui.settings.font_screens.FontScreenViewModel

@ExperimentalMaterial3Api
class FontScreenSpec {

    @Composable
    fun Content() {
        val vm: FontScreenViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        IScaffold(
            topBar = { scrollBehavior ->
                SearchToolbar(
                    title = localize(Res.string.font),
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
                       navController.safePopBackStack()
                    },
                    onValueChange = {
                        vm.searchQuery = it
                    },
                    onSearch = {
                        vm.searchQuery = it
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                FontScreen(
                    vm,
                    onFont = { fontName ->
                        // Set the font as a Custom font (Google Font)
                        vm.androidUiPreferences.font()
                            ?.set(FontType(fontName, ireader.domain.models.common.FontFamilyModel.Custom(fontName)))
                    }
                )
            }
        }

    }
}
