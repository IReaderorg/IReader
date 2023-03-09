package ireader.presentation.ui.settings.general

import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.Components
import android.provider.Settings
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@Composable
actual fun mangeNotificationRow(): Components.Row {
    val context = LocalContext.current
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    return Components.Row(
            title = localizeHelper.localize(MR.strings.manage_notification),
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                }
            },
            visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    )
}