package org.ireader.infinity.initiators

import android.app.Application
import org.ireader.core_api.log.Log
import org.ireader.domain.notification.Notifications
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsInitializer @Inject constructor(
    context: Application,
) {
    init {
        try {
            Notifications.createChannels(context)
        } catch (e: Exception) {
            Log.error { "Failed to modify notification channels" }
        }
    }

}
