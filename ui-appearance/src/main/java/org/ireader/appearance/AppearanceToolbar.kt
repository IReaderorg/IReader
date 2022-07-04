package org.ireader.appearance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import org.ireader.common_extensions.launchIO
import org.ireader.common_resources.UiText
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.domain.use_cases.theme.toCustomTheme
import org.ireader.ui_appearance.R

@Composable
fun AppearanceToolbar(vm: AppearanceViewModel,onPopBackStack: () -> Unit) {

        when(vm.themeEditMode) {
            false-> MainAppearanceToolbar(
                vm = vm,
                onPopBackStack = onPopBackStack
            )
            true -> EditToolbar(
                vm
            )
        }

}

@Composable
private fun MainAppearanceToolbar(
    vm: AppearanceViewModel,
    onPopBackStack:() -> Unit
) {
    val scope = rememberCoroutineScope()
    val isNotSavable = vm.getIsNotSavable()
    Toolbar(
        title = {
            BigSizeTextComposable(text = stringResource(R.string.appearance))
        },
        navigationIcon = {
            TopAppBarBackButton() {
                onPopBackStack()
            }
        },
        actions = {
            AnimatedVisibility(
                visible = !isNotSavable,
            ) {
                AppIconButton(
                    imageVector = Icons.Default.Save,
                    onClick = {
                        val theme =  vm.getThemes(vm.colorTheme.value)
                        if (theme != null) {
                            scope.launchIO {
                                val themeId = vm.themeRepository.insert(theme.toCustomTheme())
                                vm.colorTheme.value = themeId
                                vm.showSnackBar(UiText.StringResource(R.string.theme_was_saved))
                            }
                        } else {
                            vm.showSnackBar(UiText.StringResource(R.string.theme_was_not_valid))
                        }
                    }
                )
            }

        }
    )
}

@Composable
private fun EditToolbar(
    vm: AppearanceViewModel
) {
    Toolbar(
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