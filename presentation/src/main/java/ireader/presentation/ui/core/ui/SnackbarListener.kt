package ireader.presentation.ui.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collectLatest
import ireader.i18n.UiEvent
import ireader.i18n.asString
import ireader.presentation.ui.core.viewmodel.BaseViewModel

@Composable
fun SnackBarListener(vm: BaseViewModel, host: SnackbarHostState) {
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    host.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
                else -> {}
            }
        }
    }
}
