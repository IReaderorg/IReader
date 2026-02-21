package ireader.data.sync

import app.cash.sqldelight.db.SqlDriver
import ireader.data.createTestDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TDD tests for sync-related database tables.
 * 
 * Tests the following tables:
 * - sync_metadata: Device sync metadata
 * - trusted_devices: Paired/trusted devices
 * - sync_log: Sync operation history
 * 
 * Following TDD RED-GREEN-REFACTOR cycle:
 * 1. Write test first (RED - should fail)
 * 2. Implement minimal schema (GREEN - make it pass)
 * 3. Refactor for quality (REFACTOR - keep tests green)
 */
class SyncDatabaseTest {
    
    private lateinit var driver: SqlDriver
    
    @BeforeTest
    fun setup() {
        driver = createTestDriver()
        createSyncTables(driver)
    }
    
    @AfterTest
    fun tearDown() {
        driver.close()
    }
    
    // ==================== sync_metadata Table Tests ====================
    
    @Test
    fun `sync_metadata table should exist`() {
        // Arrange & Act
        var tableExists = false
        driver.executeQuery(
            null,
            "SELECT name FROM sqlite_master WHERE type='table' AND name='sync_metadata'",
            { cursor ->
                val result = cursor.next()
                tableExists = result.value
                result
            },
            0
        )
        
        // Assert
        assertTrue(tableExists, "sync_metadata table should exist")
    }
    
    @Test
    fun `sync_metadata should insert and retrieve device metadata`() {
        // Arrange
        val deviceId = "device-123"
        val deviceName = "My Phone"
        val deviceType = "ANDROID"
        val lastSyncTime = 1234567890L
        val createdAt = 1234567800L
        val updatedAt = 1234567890L
        
        // Act
        driver.execute(
            null,
            """
            INSERT INTO sync_metadata(device_id, device_name, device_type, last_sync_time, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            6
        ) {
            bindString(0, deviceId)
            bindString(1, deviceName)
            bindString(2, deviceType)
            bindLong(3, lastSyncTime)
            bindLong(4, createdAt)
            bindLong(5, updatedAt)
        }
        
        // Assert
        var retrievedDeviceId: String? = null
        var retrievedDeviceName: String? = null
        var retrievedDeviceType: String? = null
        var retrievedLastSyncTime: Long? = null
        
        driver.executeQuery(
            null,
            "SELECT device_id, device_name, device_type, last_sync_time FROM sync_metadata WHERE device_id = ?",
            { cursor ->
                val result = cursor.next()
                if (result.value) {
                    retrievedDeviceId = cursor.getString(0)
                    retrievedDeviceName = cursor.getString(1)
                    retrievedDeviceType = cursor.getString(2)
                    retrievedLastSyncTime = cursor.getLong(3)
                }
                result
            },
            1
        ) {
            bindString(0, deviceId)
        }
        
        assertEquals(deviceId, retrievedDeviceId)
        assertEquals(deviceName, retrievedDeviceName)
        assertEquals(deviceType, retrievedDeviceType)
        assertEquals(lastSyncTime, retrievedLastSyncTime)
    }
    
    @Test
    fun `sync_metadata should update last_sync_time`() {
        // Arrange
        val deviceId = "device-456"
        val initialSyncTime = 1000L
        val updatedSyncTime = 2000L
        
        driver.execute(
            null,
            """
            INSERT INTO sync_metadata(device_id, device_name, device_type, last_sync_time, created_at, updated_at)
            VALUES (?, 'Test Device', 'DESKTOP', ?, 1000, 1000)
            """.trimIndent(),
            2
        ) {
            bindString(0, deviceId)
            bindLong(1, initialSyncTime)
        }
        
        // Act
        driver.execute(
            null,
            "UPDATE sync_metadata SET last_sync_time = ?, updated_at = ? WHERE device_id = ?",
            3
        ) {
            bindLong(0, updatedSyncTime)
            bindLong(1, updatedSyncTime)
            bindString(2, deviceId)
        }
        
        // Assert
        var retrievedSyncTime: Long? = null
        driver.executeQuery(
            null,
            "SELECT last_sync_time FROM sync_metadata WHERE device_id = ?",
            { cursor ->
                val result = cursor.next()
                if (result.value) {
                    retrievedSyncTime = cursor.getLong(0)
                }
                result
            },
            1
        ) {
            bindString(0, deviceId)
        }
        
        assertEquals(updatedSyncTime, retrievedSyncTime)
    }
    
    @Test
    fun `sync_metadata should have unique device_id constraint`() {
        // Arrange
        val deviceId = "device-unique"
        
        driver.execute(
            null,
            """
            INSERT INTO sync_metadata(device_id, device_name, device_type, last_sync_time, created_at, updated_at)
            VALUES (?, 'Device 1', 'ANDROID', 1000, 1000, 1000)
            """.trimIndent(),
            1
        ) {
            bindString(0, deviceId)
        }
        
        // Act & Assert
        var exceptionThrown = false
        try {
            driver.execute(
                null,
                """
                INSERT INTO sync_metadata(device_id, device_name, device_type, last_sync_time, created_at, updated_at)
                VALUES (?, 'Device 2', 'IOS', 2000, 2000, 2000)
                """.trimIndent(),
                1
            ) {
                bindString(0, deviceId)
            }
        } catch (e: Exception) {
            exceptionThrown = true
        }
        
        assertTrue(exceptionThrown, "Should throw exception for duplicate device_id")
    }
    
    // ==================== trusted_devices Table Tests ====================
    
    @Test
    fun `trusted_devices table should exist`() {
        // Arrange & Act
        var tableExists = false
        driver.executeQuery(
            null,
            "SELECT name FROM sqlite_master WHERE type='table' AND name='trusted_devices'",
            { cursor ->
                val result = cursor.next()
                tableExists = result.value
                result
            },
            0
        )
        
        // Assert
        assertTrue(tableExists, "trusted_devices table should exist")
    }
    
    @Test
    fun `trusted_devices should insert and retrieve trusted device`() {
        // Arrange
        val deviceId = "trusted-device-123"
        val deviceName = "Friend's Tablet"
        val pairedAt = 1234567890L
        val expiresAt = 1234567890L + (30L * 24 * 60 * 60 * 1000) // 30 days later
        val isActive = true
        
        // Act
        driver.execute(
            null,
            """
            INSERT INTO trusted_devices(device_id, device_name, paired_at, expires_at, is_active)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            5
        ) {
            bindString(0, deviceId)
            bindString(1, deviceName)
            bindLong(2, pairedAt)
            bindLong(3, expiresAt)
            bindBoolean(4, isActive)
        }
        
        // Assert
        var retrievedDeviceId: String? = null
        var retrievedDeviceName: String? = null
        var retrievedIsActive: Boolean? = null
        
        driver.executeQuery(
            null,
            "SELECT device_id, device_name, is_active FROM trusted_devices WHERE device_id = ?",
            { cursor ->
                val result = cursor.next()
                if (result.value) {
                    retrievedDeviceId = cursor.getString(0)
                    retrievedDeviceName = cursor.getString(1)
                    retrievedIsActive = cursor.getBoolean(2)
                }
                result
            },
            1
        ) {
            bindString(0, deviceId)
        }
        
        assertEquals(deviceId, retrievedDeviceId)
        assertEquals(deviceName, retrievedDeviceName)
        assertEquals(isActive, retrievedIsActive)
    }
    
    @Test
    fun `trusted_devices should update is_active status`() {
        // Arrange
        val deviceId = "trusted-device-456"
        
        driver.execute(
            null,
            """
            INSERT INTO trusted_devices(device_id, device_name, paired_at, expires_at, is_active)
            VALUES (?, 'Test Device', 1000, 2000, ?)
            """.trimIndent(),
            2
        ) {
            bindString(0, deviceId)
            bindBoolean(1, true)
        }
        
        // Act
        driver.execute(
            null,
            "UPDATE trusted_devices SET is_active = ? WHERE device_id = ?",
            2
        ) {
            bindBoolean(0, false)
            bindString(1, deviceId)
        }
        
        // Assert
        var retrievedIsActive: Boolean? = null
        driver.executeQuery(
            null,
            "SELECT is_active FROM trusted_devices WHERE device_id = ?",
            { cursor ->
                val result = cursor.next()
                if (result.value) {
                    retrievedIsActive = cursor.getBoolean(0)
                }
                result
            },
            1
        ) {
            bindString(0, deviceId)
        }
        
        assertEquals(false, retrievedIsActive)
    }
    
    @Test
    fun `trusted_devices should delete expired devices`() {
        // Arrange
        val deviceId = "expired-device"
        val currentTime = System.currentTimeMillis()
        val expiredTime = currentTime - 1000 // Already expired
        
        driver.execute(
            null,
            """
            INSERT INTO trusted_devices(device_id, device_name, paired_at, expires_at, is_active)
            VALUES (?, 'Expired Device', ?, ?, ?)
            """.trimIndent(),
            4
        ) {
            bindString(0, deviceId)
            bindLong(1, currentTime - 10000)
            bindLong(2, expiredTime)
            bindBoolean(3, true)
        }
        
        // Act
        driver.execute(
            null,
            "DELETE FROM trusted_devices WHERE expires_at < ?",
            1
        ) {
            bindLong(0, currentTime)
        }
        
        // Assert
        var deviceExists = false
        driver.executeQuery(
            null,
            "SELECT COUNT(*) FROM trusted_devices WHERE device_id = ?",
            { cursor ->
                val result = cursor.next()
                if (result.value) {
                    deviceExists = (cursor.getLong(0) ?: 0) > 0
                }
                result
            },
            1
        ) {
            bindString(0, deviceId)
        }
        
        assertEquals(false, deviceExists, "Expired device should be deleted")
    }
    
    // ==================== sync_log Table Tests ====================
    
    @Test
    fun `sync_log table should exist`() {
        // Arrange & Act
        var tableExists = false
        driver.executeQuery(
            null,
            "SELECT name FROM sqlite_master WHERE type='table' AND name='sync_log'",
            { cursor ->
                val result = cursor.next()
                tableExists = result.value
                result
            },
            0
        )
        
        // Assert
        assertTrue(tableExists, "sync_log table should exist")
    }
    
    @Test
    fun `sync_log should insert and retrieve sync operation`() {
        // Arrange
        val syncId = "sync-operation-123"
        val deviceId = "device-789"
        val status = "COMPLETED"
        val itemsSynced = 42
        val duration = 5000L
        val errorMessage: String? = null
        val timestamp = 1234567890L
        
        // Act
        driver.execute(
            null,
            """
            INSERT INTO sync_log(sync_id, device_id, status, items_synced, duration, error_message, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            7
        ) {
            bindString(0, syncId)
            bindString(1, deviceId)
            bindString(2, status)
            bindLong(3, itemsSynced.toLong())
            bindLong(4, duration)
            bindString(5, errorMessage)
            bindLong(6, timestamp)
        }
        
        // Assert
        var retrievedSyncId: String? = null
        var retrievedStatus: String? = null
        var retrievedItemsSynced: Long? = null
        var retrievedDuration: Long? = null
        
        driver.executeQuery(
            null,
            "SELECT sync_id, status, items_synced, duration FROM sync_log WHERE sync_id = ?",
            { cursor ->
                val result = cursor.next()
                if (result.value) {
                    retrievedSyncId = cursor.getString(0)
                    retrievedStatus = cursor.getString(1)
                    retrievedItemsSynced = cursor.getLong(2)
                    retrievedDuration = cursor.getLong(3)
                }
                result
            },
            1
        ) {
            bindString(0, syncId)
        }
        
        assertEquals(syncId, retrievedSyncId)
        assertEquals(status, retrievedStatus)
        assertEquals(itemsSynced.toLong(), retrievedItemsSynced)
        assertEquals(duration, retrievedDuration)
    }
    
    @Test
    fun `sync_log should store error messages for failed syncs`() {
        // Arrange
        val syncId = "sync-failed-123"
        val errorMessage = "Connection timeout"
        
        // Act
        driver.execute(
            null,
            """
            INSERT INTO sync_log(sync_id, device_id, status, items_synced, duration, error_message, timestamp)
            VALUES (?, 'device-1', 'FAILED', 0, 1000, ?, 1234567890)
            """.trimIndent(),
            2
        ) {
            bindString(0, syncId)
            bindString(1, errorMessage)
        }
        
        // Assert
        var retrievedErrorMessage: String? = null
        driver.executeQuery(
            null,
            "SELECT error_message FROM sync_log WHERE sync_id = ?",
            { cursor ->
                val result = cursor.next()
                if (result.value) {
                    retrievedErrorMessage = cursor.getString(0)
                }
                result
            },
            1
        ) {
            bindString(0, syncId)
        }
        
        assertEquals(errorMessage, retrievedErrorMessage)
    }
    
    @Test
    fun `sync_log should retrieve logs by device_id`() {
        // Arrange
        val deviceId = "device-multi-sync"
        
        driver.execute(
            null,
            """
            INSERT INTO sync_log(sync_id, device_id, status, items_synced, duration, error_message, timestamp)
            VALUES ('sync-1', ?, 'COMPLETED', 10, 1000, NULL, 1000)
            """.trimIndent(),
            1
        ) {
            bindString(0, deviceId)
        }
        
        driver.execute(
            null,
            """
            INSERT INTO sync_log(sync_id, device_id, status, items_synced, duration, error_message, timestamp)
            VALUES ('sync-2', ?, 'COMPLETED', 20, 2000, NULL, 2000)
            """.trimIndent(),
            1
        ) {
            bindString(0, deviceId)
        }
        
        // Act & Assert
        var logCount = 0L
        driver.executeQuery(
            null,
            "SELECT COUNT(*) FROM sync_log WHERE device_id = ?",
            { cursor ->
                val result = cursor.next()
                if (result.value) {
                    logCount = cursor.getLong(0) ?: 0
                }
                result
            },
            1
        ) {
            bindString(0, deviceId)
        }
        
        assertEquals(2, logCount, "Should have 2 sync logs for the device")
    }
    
    @Test
    fun `sync_log should order by timestamp descending`() {
        // Arrange
        val deviceId = "device-ordered"
        
        driver.execute(
            null,
            """
            INSERT INTO sync_log(sync_id, device_id, status, items_synced, duration, error_message, timestamp)
            VALUES ('sync-old', ?, 'COMPLETED', 10, 1000, NULL, 1000)
            """.trimIndent(),
            1
        ) {
            bindString(0, deviceId)
        }
        
        driver.execute(
            null,
            """
            INSERT INTO sync_log(sync_id, device_id, status, items_synced, duration, error_message, timestamp)
            VALUES ('sync-new', ?, 'COMPLETED', 20, 2000, NULL, 3000)
            """.trimIndent(),
            1
        ) {
            bindString(0, deviceId)
        }
        
        // Act & Assert
        var firstSyncId: String? = null
        driver.executeQuery(
            null,
            "SELECT sync_id FROM sync_log WHERE device_id = ? ORDER BY timestamp DESC LIMIT 1",
            { cursor ->
                val result = cursor.next()
                if (result.value) {
                    firstSyncId = cursor.getString(0)
                }
                result
            },
            1
        ) {
            bindString(0, deviceId)
        }
        
        assertEquals("sync-new", firstSyncId, "Most recent sync should be first")
    }
    
    // ==================== Index Tests ====================
    
    @Test
    fun `sync_metadata should have index on device_id`() {
        // Arrange & Act
        var indexExists = false
        driver.executeQuery(
            null,
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_sync_metadata_device_id'",
            { cursor ->
                val result = cursor.next()
                indexExists = result.value
                result
            },
            0
        )
        
        // Assert
        assertTrue(indexExists, "Index on sync_metadata.device_id should exist")
    }
    
    @Test
    fun `trusted_devices should have index on device_id`() {
        // Arrange & Act
        var indexExists = false
        driver.executeQuery(
            null,
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_trusted_devices_device_id'",
            { cursor ->
                val result = cursor.next()
                indexExists = result.value
                result
            },
            0
        )
        
        // Assert
        assertTrue(indexExists, "Index on trusted_devices.device_id should exist")
    }
    
    @Test
    fun `trusted_devices should have index on expires_at`() {
        // Arrange & Act
        var indexExists = false
        driver.executeQuery(
            null,
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_trusted_devices_expires_at'",
            { cursor ->
                val result = cursor.next()
                indexExists = result.value
                result
            },
            0
        )
        
        // Assert
        assertTrue(indexExists, "Index on trusted_devices.expires_at should exist")
    }
    
    @Test
    fun `sync_log should have index on device_id`() {
        // Arrange & Act
        var indexExists = false
        driver.executeQuery(
            null,
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_sync_log_device_id'",
            { cursor ->
                val result = cursor.next()
                indexExists = result.value
                result
            },
            0
        )
        
        // Assert
        assertTrue(indexExists, "Index on sync_log.device_id should exist")
    }
    
    @Test
    fun `sync_log should have index on timestamp`() {
        // Arrange & Act
        var indexExists = false
        driver.executeQuery(
            null,
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_sync_log_timestamp'",
            { cursor ->
                val result = cursor.next()
                indexExists = result.value
                result
            },
            0
        )
        
        // Assert
        assertTrue(indexExists, "Index on sync_log.timestamp should exist")
    }
    
    // ==================== Helper Functions ====================
    
    private fun createSyncTables(driver: SqlDriver) {
        // Create sync_metadata table
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS sync_metadata(
                device_id TEXT NOT NULL PRIMARY KEY,
                device_name TEXT NOT NULL,
                device_type TEXT NOT NULL,
                last_sync_time INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            );
        """.trimIndent(), 0)
        
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_metadata_device_id ON sync_metadata(device_id);", 0)
        
        // Create trusted_devices table
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS trusted_devices(
                device_id TEXT NOT NULL PRIMARY KEY,
                device_name TEXT NOT NULL,
                paired_at INTEGER NOT NULL,
                expires_at INTEGER NOT NULL,
                is_active INTEGER NOT NULL DEFAULT 1
            );
        """.trimIndent(), 0)
        
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_trusted_devices_device_id ON trusted_devices(device_id);", 0)
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_trusted_devices_expires_at ON trusted_devices(expires_at);", 0)
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_trusted_devices_is_active ON trusted_devices(is_active) WHERE is_active = 1;", 0)
        
        // Create sync_log table
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS sync_log(
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                sync_id TEXT NOT NULL,
                device_id TEXT NOT NULL,
                status TEXT NOT NULL,
                items_synced INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                error_message TEXT,
                timestamp INTEGER NOT NULL
            );
        """.trimIndent(), 0)
        
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_log_device_id ON sync_log(device_id);", 0)
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_log_timestamp ON sync_log(timestamp DESC);", 0)
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_log_sync_id ON sync_log(sync_id);", 0)
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_log_status ON sync_log(status);", 0)
    }
}
