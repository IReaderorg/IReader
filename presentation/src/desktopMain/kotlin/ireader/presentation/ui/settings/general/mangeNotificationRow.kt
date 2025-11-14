package ireader.presentation.ui.settings.general

import androidx.compose.runtime.Composable
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@Composable
actual fun mangeNotificationRow(): Components.Row {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    return Components.Row(
        title = localizeHelper.localize(Res.string.manage_notification),
        onClick = {

        },
        visible = false
    )
}