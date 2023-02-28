package ireader.domain.utils.extensions

import kotlinx.coroutines.flow.MutableSharedFlow
import ireader.i18n.UiEvent
import ireader.i18n.UiText
import ireader.domain.R
import ireader.i18n.resources.MR

suspend fun MutableSharedFlow<UiEvent>.showSnackBar(message: UiText?) {
    this.emit(
        UiEvent.ShowSnackbar(
            uiText = message ?: UiText.MStringResource(MR.strings.error_unknown)
        )
    )
}
