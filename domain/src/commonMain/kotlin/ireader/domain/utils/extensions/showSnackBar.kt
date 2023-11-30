package ireader.domain.utils.extensions

import ireader.i18n.UiEvent
import ireader.i18n.UiText
import kotlinx.coroutines.flow.MutableSharedFlow

suspend fun MutableSharedFlow<UiEvent>.showSnackBar(message: UiText?) {
    this.emit(
        UiEvent.ShowSnackbar(
            uiText = message ?: UiText.MStringResource() { xml ->
                xml.errorUnknown
            }
        )
    )
}
