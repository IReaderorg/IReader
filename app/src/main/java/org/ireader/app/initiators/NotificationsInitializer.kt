package org.ireader.app.initiators


import android.app.Application
import ireader.core.log.Log
import ireader.domain.notification.Notifications
import ireader.i18n.LocalizeHelper


class NotificationsInitializer(
    context: Application,
    localizeHelper: LocalizeHelper
) {
    init {
        try {
            Notifications.createChannels(context,localizeHelper)
        } catch (e: Throwable) {
            Log.error { "Failed to modify notification channels" }
        }
    }
}
