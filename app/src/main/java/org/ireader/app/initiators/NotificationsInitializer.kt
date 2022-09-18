package org.ireader.app.initiators

import android.app.Application
import ireader.core.log.Log
import ireader.domain.notification.Notifications
import org.koin.core.annotation.Factory

import javax.inject.Singleton

@Factory
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
