package org.ireader.infinity.initiators

import android.app.Application
import org.ireader.domain.feature_services.notification.Notifications
import timber.log.Timber
import javax.inject.Inject

class NotificationsInitializer @Inject constructor(
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
