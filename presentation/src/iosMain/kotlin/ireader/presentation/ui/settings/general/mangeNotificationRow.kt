package ireader.presentation.ui.settings.general

import androidx.compose.runtime.Composable
import ireader.i18n.resources.Res
import ireader.i18n.resources.manage_notification
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun mangeNotificationRow(): Components.Row {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    return Components.Row(
        title = localizeHelper.localize(Res.string.manage_notification),
        onClick = {
            val url = NSURL.URLWithString("App-Prefs:root=NOTIFICATIONS_ID")
            if (url != null) {
                UIApplication.sharedApplication.openURL(url)
            }
        },
        visible = true
    )
}
