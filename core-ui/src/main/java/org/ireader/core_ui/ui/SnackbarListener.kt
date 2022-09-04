package org.ireader.core_ui.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collectLatest
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.asString
import org.ireader.core_ui.viewmodel.BaseViewModel

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
