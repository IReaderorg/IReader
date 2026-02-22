package ireader.presentation.ui.sync

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TDD Test for sync notification cancel action.
 * 
 * Tests that the sync notification includes a cancel action button.
 * Following TDD methodology - these tests are written BEFORE implementation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class SyncNotificationActionTest {
    
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    @Test
    fun `sync notification should have cancel action`() {
        // Arrange
        val deviceName = "Test Device"
        
        // Act
        SyncForegroundService.startSync(context, deviceName)
        
        // Give the service time to create the notification
        Thread.sleep(500)
        
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
    }
    
    @Test
    fun `cancel action should have correct title`() {
        // Arrange
        val deviceName = "Test Device"
        
        // Act
        SyncForegroundService.startSync(context, deviceName)
        Thread.sleep(500)
        
        // Assert
        val activeNotifications = notificationManager.activeNotifications
        val syncNotification = activeNotifications.find { 
            it.id == SyncForegroundService.NOTIFICATION_ID 
        }
        
        assertNotNull(syncNotification)
        val cancelAction = syncNotification.notification.actions.find { 
            it.title.toString().contains("Cancel", ignoreCase = true) 
        }
        
        assertNotNull(cancelAction)
        assertEquals("Cancel", cancelAction.title.toString())
    }
    
    @Test
    fun `cancel action should have pending intent`() {
        // Arrange
        val deviceName = "Test Device"
        
        // Act
        SyncForegroundService.startSync(context, deviceName)
        Thread.sleep(500)
        
        // Assert
        val activeNotifications = notificationManager.activeNotifications
        val syncNotification = activeNotifications.find { 
            it.id == SyncForegroundService.NOTIFICATION_ID 
        }
        
        assertNotNull(syncNotification)
        val cancelAction = syncNotification.notification.actions.find { 
            it.title.toString().contains("Cancel", ignoreCase = true) 
        }
        
        assertNotNull(cancelAction)
        assertNotNull(cancelAction.actionIntent, "Cancel action should have a PendingIntent")
    }
}
