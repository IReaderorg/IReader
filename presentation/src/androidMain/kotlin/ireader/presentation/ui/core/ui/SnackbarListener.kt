package ireader.presentation.ui.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.UiEvent
import ireader.i18n.asString
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SnackBarListener(vm: ireader.presentation.ui.core.viewmodel.BaseViewModel, host: SnackbarHostState) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    host.showSnackbar(
                        event.uiText.asString(localizeHelper)
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun SnackBarListener(vm: ireader.presentation.ui.core.viewmodel.BaseViewModel) : SnackbarHostState{
    val host = remember { SnackbarHostState() }
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    host.showSnackbar(
                        event.uiText.asString(localizeHelper)
                    )
                }
                else -> {}
            }
        }
    }
    return host
}
