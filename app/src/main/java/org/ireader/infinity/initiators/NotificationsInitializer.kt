package org.ireader.infinity.initiators

import android.app.Application
import org.ireader.domain.feature_services.notification.Notifications
import timber.log.Timber

class NotificationsInitializer(
    context: Application,
) {

    init {
        try {
            Notifications.createChannels(context)
        } catch (e: Exception) {
            Timber.e("Failed to modify notification channels")
        }
    }

}
