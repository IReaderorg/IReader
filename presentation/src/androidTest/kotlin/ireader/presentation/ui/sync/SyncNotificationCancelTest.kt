package ireader.presentation.ui.sync

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Android instrumentation test for sync notification cancel action.
 * 
 * Tests that the sync notification includes a cancel action button and
 * that clicking it triggers the cancel callback.
 * 
 * Following TDD methodology - these tests verify the implementation.
 */
@RunWith(AndroidJUnit4::class)
class SyncNotificationCancelTest {
    
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    @Test
    fun syncNotificationShouldHaveCancelAction() {
        // Arrange
        val deviceName = "Test Device"
        
        // Act
        SyncForegroundService.startSync(context, deviceName)
        
        // Give the service time to create the notification
        Thread.sleep(1000)
        
        // Assert
        val activeNotifications = notificationManager.activeNotifications
        val syncNotification = activeNotifications.find { 
            it.id == SyncForegroundService.NOTIFICATION_ID 
        }
        
        assertNotNull(syncNotification, "Sync notification should be active")
        
        val notification = syncNotification.notification
        assertNotNull(notification.actions, "Notification should have actions")
        assertTrue(notification.actions.isNotEmpty(), "Notification should have at least one action")
        
        // Find the cancel action
        val cancelAction = notification.actions.find { 
            it.title.toString().contains("Cancel", ignoreCase = true) 
        }
        assertNotNull(cancelAction, "Notification should have a Cancel action")
        assertEquals("Cancel", cancelAction.title.toString())
    }
    
    @Test
    fun cancelActionShouldTriggerCallback() {
        // Arrange
        var callbackInvoked = false
        SyncForegroundService.onCancelCallback = {
            callbackInvoked = true
        }
        
        // Act
        val intent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_CANCEL_SYNC
        }
        context.startService(intent)
        
        // Give the service time to process
        Thread.sleep(500)
        
        // Assert
        assertTrue(callbackInvoked, "Cancel callback should be invoked")
    }
}
