package ireader.presentation.ui.settings.notifications

import ireader.core.log.Log
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.concurrent.Volatile

/**
 * iOS-specific implementation of NotificationHelper.
 * Uses UNUserNotificationCenter for notification management.
 */
@OptIn(ExperimentalForeignApi::class)
class IosNotificationHelper : NotificationHelper {
    
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    
    @Volatile
    private var cachedPermissionStatus: Boolean? = null
    
    override fun hasNotificationPermission(): Boolean {
        // Return cached value if available, otherwise assume true
        // The actual check is async, so we update the cache when possible
        checkPermissionAsync()
        return cachedPermissionStatus ?: true
    }
    
    private fun checkPermissionAsync() {
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            if (settings != null) {
                cachedPermissionStatus = settings.authorizationStatus == UNAuthorizationStatusAuthorized ||
                        settings.authorizationStatus == UNAuthorizationStatusProvisional
            }
        }
    }
    
    override fun requestNotificationPermission() {
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        
        notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
            if (error != null) {
                Log.error("Failed to request notification permission: ${error.localizedDescription}")
            } else {
                cachedPermissionStatus = granted
                Log.info { "Notification permission ${if (granted) "granted" else "denied"}" }
            }
        }
    }
    
    override fun openNotificationSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null && UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
            UIApplication.sharedApplication.openURL(
                settingsUrl,
                options = emptyMap<Any?, Any?>(),
                completionHandler = null
            )
        } else {
            Log.warn { "Cannot open iOS settings URL" }
        }
    }
}
