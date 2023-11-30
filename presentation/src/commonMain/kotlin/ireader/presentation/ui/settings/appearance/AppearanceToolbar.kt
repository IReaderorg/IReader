package ireader.presentation.ui.settings.appearance

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import ireader.i18n.localize

import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceToolbar(
    vm: AppearanceViewModel,
    onPopBackStack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {

    when (vm.themeEditMode) {
        false -> MainAppearanceToolbar(
            vm = vm,
            onPopBackStack = onPopBackStack,
            scrollBehavior = scrollBehavior
        )

        true -> EditToolbar(
            vm,
            scrollBehavior = scrollBehavior
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppearanceToolbar(
    vm: AppearanceViewModel,
    onPopBackStack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            BigSizeTextComposable(text = localize { xml ->
                xml.appearance
            })
        },
        navigationIcon = {
            TopAppBarBackButton() {
                onPopBackStack()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditToolbar(
    vm: AppearanceViewModel,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {},
        navigationIcon = {
            AppIconButton(
                imageVector = Icons.Default.Close,
                onClick = {
                    vm.themeEditMode = false
                }
            )
        },
    )
}
