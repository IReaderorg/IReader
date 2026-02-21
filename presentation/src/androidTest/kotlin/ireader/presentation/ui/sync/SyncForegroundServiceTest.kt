package ireader.presentation.ui.sync

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Tests for SyncForegroundService notification functionality.
 * 
 * Following TDD methodology - these tests are written BEFORE enhancing the notification implementation.
 * 
 * Tests verify:
 * - Notification displays progress bar with actual percentage
 * - Notification shows current item being synced
 * - Notification shows total items (e.g., "Syncing 5 of 20 books")
 * - Notification updates smoothly without flickering
 * - Notification uses appropriate priority and category
 */
@RunWith(AndroidJUnit4::class)
class SyncForegroundServiceTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @After
    fun tearDown() {
        // Clean up any notifications
        notificationManager.cancel(SyncForegroundService.NOTIFICATION_ID)
    }

    @Test
    fun testNotificationShowsProgressBar() {
        // Arrange
        val deviceName = "Test Device"
        val progress = 50
        val currentItem = "Book1.epub"
        val currentIndex = 5
        val totalItems = 10

        // Act - Start service with progress
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val updateIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_UPDATE_PROGRESS
            putExtra(SyncForegroundService.EXTRA_PROGRESS, progress)
            putExtra(SyncForegroundService.EXTRA_CURRENT_ITEM, currentItem)
            putExtra(SyncForegroundService.EXTRA_CURRENT_INDEX, currentIndex)
            putExtra(SyncForegroundService.EXTRA_TOTAL_ITEMS, totalItems)
        }
        context.startService(updateIntent)

        // Wait for notification to be posted
        Thread.sleep(500)

        // Assert - Verify notification exists
        val notifications = notificationManager.activeNotifications
        assertTrue("Notification should be active", notifications.isNotEmpty())
        
        val notification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        assertNotNull("Sync notification should exist", notification)
        
        // Verify notification has progress
        val notificationObj = notification?.notification
        assertNotNull("Notification object should exist", notificationObj)
        
        // Check that progress is set (not indeterminate)
        val extras = notificationObj?.extras
        val progressMax = extras?.getInt("android.progressMax", -1) ?: -1
        val progressCurrent = extras?.getInt("android.progress", -1) ?: -1
        val progressIndeterminate = extras?.getBoolean("android.progressIndeterminate", true) ?: true
        
        assertEquals("Progress max should be 100", 100, progressMax)
        assertEquals("Progress current should match", progress, progressCurrent)
        assertFalse("Progress should not be indeterminate", progressIndeterminate)
    }

    @Test
    fun testNotificationShowsCurrentItem() {
        // Arrange
        val deviceName = "Test Device"
        val currentItem = "MyBook.epub"
        val currentIndex = 3
        val totalItems = 15

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val updateIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_UPDATE_PROGRESS
            putExtra(SyncForegroundService.EXTRA_PROGRESS, 20)
            putExtra(SyncForegroundService.EXTRA_CURRENT_ITEM, currentItem)
            putExtra(SyncForegroundService.EXTRA_CURRENT_INDEX, currentIndex)
            putExtra(SyncForegroundService.EXTRA_TOTAL_ITEMS, totalItems)
        }
        context.startService(updateIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        assertNotNull("Notification should exist", notification)
        
        val contentText = notification?.notification?.extras?.getString("android.text")
        assertNotNull("Content text should exist", contentText)
        assertTrue("Content text should contain current item", contentText?.contains(currentItem) == true)
    }

    @Test
    fun testNotificationShowsTotalItems() {
        // Arrange
        val deviceName = "Test Device"
        val currentIndex = 5
        val totalItems = 20

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val updateIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_UPDATE_PROGRESS
            putExtra(SyncForegroundService.EXTRA_PROGRESS, 25)
            putExtra(SyncForegroundService.EXTRA_CURRENT_ITEM, "Book.epub")
            putExtra(SyncForegroundService.EXTRA_CURRENT_INDEX, currentIndex)
            putExtra(SyncForegroundService.EXTRA_TOTAL_ITEMS, totalItems)
        }
        context.startService(updateIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        assertNotNull("Notification should exist", notification)
        
        val contentText = notification?.notification?.extras?.getString("android.text")
        assertNotNull("Content text should exist", contentText)
        
        // Should show format like "Syncing 5 of 20 books"
        assertTrue(
            "Content text should show item count",
            contentText?.contains("$currentIndex") == true && contentText.contains("$totalItems")
        )
    }

    @Test
    fun testNotificationUsesLowPriority() {
        // Arrange & Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, "Test Device")
        }
        serviceRule.startService(startIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        assertNotNull("Notification should exist", notification)
        
        // Priority should be low (not intrusive)
        val priority = notification?.notification?.priority
        assertTrue(
            "Notification should use low priority",
            priority == android.app.Notification.PRIORITY_LOW || priority == android.app.Notification.PRIORITY_DEFAULT
        )
    }

    @Test
    fun testNotificationUsesServiceCategory() {
        // Arrange & Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, "Test Device")
        }
        serviceRule.startService(startIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        assertNotNull("Notification should exist", notification)
        
        val category = notification?.notification?.category
        assertEquals(
            "Notification should use SERVICE category",
            android.app.Notification.CATEGORY_SERVICE,
            category
        )
    }

    @Test
    fun testNotificationIsOngoing() {
        // Arrange & Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, "Test Device")
        }
        serviceRule.startService(startIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        assertNotNull("Notification should exist", notification)
        
        val flags = notification?.notification?.flags ?: 0
        assertTrue(
            "Notification should be ongoing",
            (flags and android.app.Notification.FLAG_ONGOING_EVENT) != 0
        )
    }

    @Test
    fun testNotificationUpdatesWithoutFlickering() {
        // Arrange
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, "Test Device")
        }
        serviceRule.startService(startIntent)

        Thread.sleep(300)

        // Act - Update progress multiple times rapidly
        for (i in 1..5) {
            val updateIntent = Intent(context, SyncForegroundService::class.java).apply {
                action = SyncForegroundService.ACTION_UPDATE_PROGRESS
                putExtra(SyncForegroundService.EXTRA_PROGRESS, i * 20)
                putExtra(SyncForegroundService.EXTRA_CURRENT_ITEM, "Book$i.epub")
                putExtra(SyncForegroundService.EXTRA_CURRENT_INDEX, i)
                putExtra(SyncForegroundService.EXTRA_TOTAL_ITEMS, 10)
            }
            context.startService(updateIntent)
            Thread.sleep(100) // Small delay between updates
        }

        Thread.sleep(300)

        // Assert - Notification should still exist and show latest progress
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        assertNotNull("Notification should still exist after multiple updates", notification)
        
        val progressCurrent = notification?.notification?.extras?.getInt("android.progress", -1) ?: -1
        assertEquals("Progress should show latest value", 100, progressCurrent)
    }

    @Test
    fun testNotificationShowsPercentage() {
        // Arrange
        val progress = 75

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, "Test Device")
        }
        serviceRule.startService(startIntent)

        val updateIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_UPDATE_PROGRESS
            putExtra(SyncForegroundService.EXTRA_PROGRESS, progress)
            putExtra(SyncForegroundService.EXTRA_CURRENT_ITEM, "Book.epub")
            putExtra(SyncForegroundService.EXTRA_CURRENT_INDEX, 15)
            putExtra(SyncForegroundService.EXTRA_TOTAL_ITEMS, 20)
        }
        context.startService(updateIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        assertNotNull("Notification should exist", notification)
        
        val contentText = notification?.notification?.extras?.getString("android.text")
        assertNotNull("Content text should exist", contentText)
        assertTrue(
            "Content text should show percentage",
            contentText?.contains("$progress%") == true
        )
    }

    @Test
    fun testCompletionNotificationShowsDeviceName() {
        // Arrange
        val deviceName = "My Android Phone"
        val syncedItems = 15
        val durationMs = 45000L // 45 seconds

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val completeIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_COMPLETED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_SYNCED_ITEMS, syncedItems)
            putExtra(SyncForegroundService.EXTRA_DURATION, durationMs)
        }
        context.startService(completeIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.COMPLETION_NOTIFICATION_ID }
        assertNotNull("Completion notification should exist", notification)
        
        val contentText = notification?.notification?.extras?.getString("android.text")
        assertNotNull("Content text should exist", contentText)
        assertTrue(
            "Content text should contain device name",
            contentText?.contains(deviceName) == true
        )
    }

    @Test
    fun testCompletionNotificationShowsSyncedItems() {
        // Arrange
        val deviceName = "Test Device"
        val syncedItems = 25
        val durationMs = 60000L

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val completeIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_COMPLETED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_SYNCED_ITEMS, syncedItems)
            putExtra(SyncForegroundService.EXTRA_DURATION, durationMs)
        }
        context.startService(completeIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.COMPLETION_NOTIFICATION_ID }
        assertNotNull("Completion notification should exist", notification)
        
        val contentText = notification?.notification?.extras?.getString("android.text")
        assertNotNull("Content text should exist", contentText)
        assertTrue(
            "Content text should show synced items count",
            contentText?.contains("$syncedItems") == true
        )
    }

    @Test
    fun testCompletionNotificationShowsDuration() {
        // Arrange
        val deviceName = "Test Device"
        val syncedItems = 10
        val durationMs = 125000L // 2 minutes 5 seconds

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val completeIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_COMPLETED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_SYNCED_ITEMS, syncedItems)
            putExtra(SyncForegroundService.EXTRA_DURATION, durationMs)
        }
        context.startService(completeIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.COMPLETION_NOTIFICATION_ID }
        assertNotNull("Completion notification should exist", notification)
        
        val contentText = notification?.notification?.extras?.getString("android.text")
        assertNotNull("Content text should exist", contentText)
        // Should show duration in human-readable format (e.g., "2m 5s" or "2 minutes")
        assertTrue(
            "Content text should show duration",
            contentText?.contains("2") == true && (contentText.contains("min") || contentText.contains("m"))
        )
    }

    @Test
    fun testCompletionNotificationIsDismissible() {
        // Arrange
        val deviceName = "Test Device"
        val syncedItems = 5
        val durationMs = 30000L

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val completeIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_COMPLETED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_SYNCED_ITEMS, syncedItems)
            putExtra(SyncForegroundService.EXTRA_DURATION, durationMs)
        }
        context.startService(completeIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.COMPLETION_NOTIFICATION_ID }
        assertNotNull("Completion notification should exist", notification)
        
        val flags = notification?.notification?.flags ?: 0
        // Should NOT be ongoing (should be dismissible)
        assertFalse(
            "Completion notification should not be ongoing",
            (flags and android.app.Notification.FLAG_ONGOING_EVENT) != 0
        )
        
        // Should be auto-cancel (dismiss when tapped)
        assertTrue(
            "Completion notification should auto-cancel",
            (flags and android.app.Notification.FLAG_AUTO_CANCEL) != 0
        )
    }

    @Test
    fun testCompletionNotificationUsesSuccessIcon() {
        // Arrange
        val deviceName = "Test Device"
        val syncedItems = 8
        val durationMs = 20000L

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val completeIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_COMPLETED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_SYNCED_ITEMS, syncedItems)
            putExtra(SyncForegroundService.EXTRA_DURATION, durationMs)
        }
        context.startService(completeIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.COMPLETION_NOTIFICATION_ID }
        assertNotNull("Completion notification should exist", notification)
        
        val smallIcon = notification?.notification?.smallIcon
        assertNotNull("Notification should have an icon", smallIcon)
        // Icon should be a checkmark/success icon (stat_notify_sync_noanim or stat_sys_upload_done)
        // We can't easily verify the exact icon, but we can verify it's set
    }

    @Test
    fun testCompletionNotificationHasTapAction() {
        // Arrange
        val deviceName = "Test Device"
        val syncedItems = 12
        val durationMs = 40000L

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val completeIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_COMPLETED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_SYNCED_ITEMS, syncedItems)
            putExtra(SyncForegroundService.EXTRA_DURATION, durationMs)
        }
        context.startService(completeIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.COMPLETION_NOTIFICATION_ID }
        assertNotNull("Completion notification should exist", notification)
        
        val contentIntent = notification?.notification?.contentIntent
        assertNotNull("Completion notification should have a tap action", contentIntent)
    }

    @Test
    fun testCompletionNotificationUsesDefaultPriority() {
        // Arrange
        val deviceName = "Test Device"
        val syncedItems = 7
        val durationMs = 35000L

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val completeIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_COMPLETED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_SYNCED_ITEMS, syncedItems)
            putExtra(SyncForegroundService.EXTRA_DURATION, durationMs)
        }
        context.startService(completeIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.COMPLETION_NOTIFICATION_ID }
        assertNotNull("Completion notification should exist", notification)
        
        val priority = notification?.notification?.priority
        assertEquals(
            "Completion notification should use default priority",
            android.app.Notification.PRIORITY_DEFAULT,
            priority
        )
    }

    @Test
    fun testCompletionNotificationRemovesProgressNotification() {
        // Arrange
        val deviceName = "Test Device"
        val syncedItems = 20
        val durationMs = 90000L

        // Act - Start sync with progress notification
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        Thread.sleep(300)

        // Verify progress notification exists
        var notifications = notificationManager.activeNotifications
        var progressNotification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        assertNotNull("Progress notification should exist", progressNotification)

        // Complete sync
        val completeIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_COMPLETED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_SYNCED_ITEMS, syncedItems)
            putExtra(SyncForegroundService.EXTRA_DURATION, durationMs)
        }
        context.startService(completeIntent)

        Thread.sleep(500)

        // Assert - Progress notification should be removed, completion notification should exist
        notifications = notificationManager.activeNotifications
        progressNotification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        val completionNotification = notifications.find { it.id == SyncForegroundService.COMPLETION_NOTIFICATION_ID }
        
        assertNull("Progress notification should be removed", progressNotification)
        assertNotNull("Completion notification should exist", completionNotification)
    }

    // ========== ERROR NOTIFICATION TESTS ==========

    @Test
    fun testErrorNotificationShowsDeviceName() {
        // Arrange
        val deviceName = "My Desktop"
        val errorMessage = "Network connection lost"

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val errorIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_FAILED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_ERROR_MESSAGE, errorMessage)
        }
        context.startService(errorIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.ERROR_NOTIFICATION_ID }
        assertNotNull("Error notification should exist", notification)
        
        val contentTitle = notification?.notification?.extras?.getString("android.title")
        assertNotNull("Content title should exist", contentTitle)
        assertTrue(
            "Content title should contain device name",
            contentTitle?.contains(deviceName) == true
        )
    }

    @Test
    fun testErrorNotificationShowsErrorMessage() {
        // Arrange
        val deviceName = "Test Device"
        val errorMessage = "Connection timeout after 30 seconds"

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val errorIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_FAILED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_ERROR_MESSAGE, errorMessage)
        }
        context.startService(errorIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.ERROR_NOTIFICATION_ID }
        assertNotNull("Error notification should exist", notification)
        
        val contentText = notification?.notification?.extras?.getString("android.text")
        assertNotNull("Content text should exist", contentText)
        assertTrue(
            "Content text should contain error message",
            contentText?.contains(errorMessage) == true
        )
    }

    @Test
    fun testErrorNotificationShowsSuggestion() {
        // Arrange
        val deviceName = "Test Device"
        val errorMessage = "WiFi connection lost"
        val suggestion = "Check your WiFi connection and try again"

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val errorIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_FAILED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_ERROR_MESSAGE, errorMessage)
            putExtra(SyncForegroundService.EXTRA_ERROR_SUGGESTION, suggestion)
        }
        context.startService(errorIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.ERROR_NOTIFICATION_ID }
        assertNotNull("Error notification should exist", notification)
        
        val contentText = notification?.notification?.extras?.getString("android.text")
        assertNotNull("Content text should exist", contentText)
        assertTrue(
            "Content text should contain suggestion",
            contentText?.contains(suggestion) == true
        )
    }

    @Test
    fun testErrorNotificationUsesErrorIcon() {
        // Arrange
        val deviceName = "Test Device"
        val errorMessage = "Sync failed"

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val errorIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_FAILED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_ERROR_MESSAGE, errorMessage)
        }
        context.startService(errorIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.ERROR_NOTIFICATION_ID }
        assertNotNull("Error notification should exist", notification)
        
        val smallIcon = notification?.notification?.smallIcon
        assertNotNull("Notification should have an icon", smallIcon)
        // Icon should be an error/warning icon
    }

    @Test
    fun testErrorNotificationIsDismissible() {
        // Arrange
        val deviceName = "Test Device"
        val errorMessage = "Sync failed"

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val errorIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_FAILED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_ERROR_MESSAGE, errorMessage)
        }
        context.startService(errorIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.ERROR_NOTIFICATION_ID }
        assertNotNull("Error notification should exist", notification)
        
        val flags = notification?.notification?.flags ?: 0
        // Should NOT be ongoing (should be dismissible)
        assertFalse(
            "Error notification should not be ongoing",
            (flags and android.app.Notification.FLAG_ONGOING_EVENT) != 0
        )
        
        // Should be auto-cancel (dismiss when tapped)
        assertTrue(
            "Error notification should auto-cancel",
            (flags and android.app.Notification.FLAG_AUTO_CANCEL) != 0
        )
    }

    @Test
    fun testErrorNotificationHasTapAction() {
        // Arrange
        val deviceName = "Test Device"
        val errorMessage = "Sync failed"

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val errorIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_FAILED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_ERROR_MESSAGE, errorMessage)
        }
        context.startService(errorIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.ERROR_NOTIFICATION_ID }
        assertNotNull("Error notification should exist", notification)
        
        val contentIntent = notification?.notification?.contentIntent
        assertNotNull("Error notification should have a tap action", contentIntent)
    }

    @Test
    fun testErrorNotificationUsesHighPriority() {
        // Arrange
        val deviceName = "Test Device"
        val errorMessage = "Sync failed"

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        val errorIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_FAILED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_ERROR_MESSAGE, errorMessage)
        }
        context.startService(errorIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.ERROR_NOTIFICATION_ID }
        assertNotNull("Error notification should exist", notification)
        
        val priority = notification?.notification?.priority
        assertTrue(
            "Error notification should use high or default priority (higher than low)",
            priority == android.app.Notification.PRIORITY_HIGH || 
            priority == android.app.Notification.PRIORITY_DEFAULT
        )
    }

    @Test
    fun testErrorNotificationRemovesProgressNotification() {
        // Arrange
        val deviceName = "Test Device"
        val errorMessage = "Sync failed"

        // Act - Start sync with progress notification
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
        }
        serviceRule.startService(startIntent)

        Thread.sleep(300)

        // Verify progress notification exists
        var notifications = notificationManager.activeNotifications
        var progressNotification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        assertNotNull("Progress notification should exist", progressNotification)

        // Fail sync
        val errorIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_FAILED
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(SyncForegroundService.EXTRA_ERROR_MESSAGE, errorMessage)
        }
        context.startService(errorIntent)

        Thread.sleep(500)

        // Assert - Progress notification should be removed, error notification should exist
        notifications = notificationManager.activeNotifications
        progressNotification = notifications.find { it.id == SyncForegroundService.NOTIFICATION_ID }
        val errorNotification = notifications.find { it.id == SyncForegroundService.ERROR_NOTIFICATION_ID }
        
        assertNull("Progress notification should be removed", progressNotification)
        assertNotNull("Error notification should exist", errorNotification)
    }

    @Test
    fun testErrorNotificationHandlesNullDeviceName() {
        // Arrange - Device name can be null if error occurs before device selection
        val errorMessage = "No devices found on network"

        // Act
        val startIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_START_SYNC
            putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, "Initial Device")
        }
        serviceRule.startService(startIntent)

        val errorIntent = Intent(context, SyncForegroundService::class.java).apply {
            action = SyncForegroundService.ACTION_SYNC_FAILED
            // No device name provided
            putExtra(SyncForegroundService.EXTRA_ERROR_MESSAGE, errorMessage)
        }
        context.startService(errorIntent)

        Thread.sleep(500)

        // Assert
        val notifications = notificationManager.activeNotifications
        val notification = notifications.find { it.id == SyncForegroundService.ERROR_NOTIFICATION_ID }
        assertNotNull("Error notification should exist even without device name", notification)
        
        val contentTitle = notification?.notification?.extras?.getString("android.title")
        assertNotNull("Content title should exist", contentTitle)
        // Should show generic title when device name is not available
        assertTrue(
            "Content title should show generic error message",
            contentTitle?.contains("Sync Failed") == true
        )
    }
}
