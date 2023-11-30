package ireader.presentation.ui.settings.general

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.currentOrThrow

import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@Composable
actual fun mangeNotificationRow(): Components.Row {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    return Components.Row(
        title = localizeHelper.localize { xml ->
            xml.manageNotification
        },
        onClick = {

        },
        visible = false
    )
}