package org.ireader.common_extensions

import kotlinx.coroutines.flow.MutableSharedFlow
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText


suspend fun MutableSharedFlow<UiEvent>.showSnackBar(message: UiText?) {
    this.emit(
        UiEvent.ShowSnackbar(
            uiText = message ?: UiText.StringResource(R.string.error_unknown)
        )
    )
}
