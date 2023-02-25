package org.ireader.app.initiators

import android.app.Application
import ireader.core.log.Log
import ireader.domain.notification.Notifications


import javax.inject.Singleton


class NotificationsInitializer(
    context: Application,
) {
    init {
        try {
            Notifications.createChannels(context)
        } catch (e: Throwable) {
            Log.error { "Failed to modify notification channels" }
        }
    }
}
