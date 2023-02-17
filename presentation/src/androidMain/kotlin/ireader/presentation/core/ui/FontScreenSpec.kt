package ireader.presentation.core.ui

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

import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.SearchToolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.models.getDefaultFont
import ireader.presentation.ui.settings.font_screens.FontScreen
import ireader.presentation.ui.settings.font_screens.FontScreenViewModel
import ireader.presentation.R
import ireader.presentation.ui.component.IScaffold
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterialApi::class)
object FontScreenSpec : ScreenSpec {
    override val navHostRoute: String = "font_screen_spec"

    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: FontScreenViewModel = getViewModel(viewModelStoreOwner = controller.navBackStackEntry)

        IScaffold(
            topBar = { scrollBehavior ->
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
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                FontScreen(
                    vm,
                    onFont = { font ->
                        vm.androidUiPreferences.font()
                            .set(FontType(font, getDefaultFont().fontFamily))
                    }
                )
            }
        }

    }
}
