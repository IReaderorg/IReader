package ireader.common.extensions

import kotlinx.coroutines.flow.MutableSharedFlow
import ireader.common.resources.UiEvent
import ireader.common.resources.UiText


suspend fun MutableSharedFlow<UiEvent>.showSnackBar(message: UiText?) {
    this.emit(
        UiEvent.ShowSnackbar(
            uiText = message ?: UiText.StringResource(R.string.error_unknown)
        )
    )
}
