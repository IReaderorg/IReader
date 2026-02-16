package ireader.domain.models.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.datetime.Clock

/**
 * Unit tests for SourceHealth and SourceStatus
 */
class SourceHealthTest {

    // ==================== SourceHealth Creation Tests ====================

    @Test
    fun `SourceHealth with Online status`() {
        val health = SourceHealth(
            sourceId = 1L,
            status = SourceStatus.Online,
            responseTime = 150L
        )
        
        assertEquals(1L, health.sourceId)
        assertTrue(health.status is SourceStatus.Online)
        assertEquals(150L, health.responseTime)
    }

    @Test
    fun `SourceHealth with Offline status`() {
        val health = SourceHealth(
            sourceId = 2L,
            status = SourceStatus.Offline
        )
        
        assertEquals(2L, health.sourceId)
        assertTrue(health.status is SourceStatus.Offline)
        assertNull(health.responseTime)
    }

    @Test
    fun `SourceHealth with LoginRequired status`() {
        val health = SourceHealth(
            sourceId = 3L,
            status = SourceStatus.LoginRequired
        )
        
        assertTrue(health.status is SourceStatus.LoginRequired)
    }

    @Test
    fun `SourceHealth with Error status`() {
        val health = SourceHealth(
            sourceId = 4L,
            status = SourceStatus.Error("Connection timeout")
        )
        
        assertTrue(health.status is SourceStatus.Error)
        assertEquals("Connection timeout", (health.status as SourceStatus.Error).errorMessage)
    }

    @Test
    fun `SourceHealth lastChecked has default value`() {
        // Use a fixed timestamp for testing
        val health = SourceHealth(
            sourceId = 1L,
            status = SourceStatus.Online
        )
        
        // Just verify lastChecked is a reasonable timestamp (after year 2020)
        assertTrue(health.lastChecked > 1577836800000L)
    }

    @Test
    fun `SourceHealth with custom lastChecked`() {
        val customTime = 1234567890L
        val health = SourceHealth(
            sourceId = 1L,
            status = SourceStatus.Online,
            lastChecked = customTime
        )
        
        assertEquals(customTime, health.lastChecked)
    }

    // ==================== SourceStatus Tests ====================

    @Test
    fun `SourceStatus Online is singleton`() {
        val status1 = SourceStatus.Online
        val status2 = SourceStatus.Online
        
        assertSame(status1, status2)
    }

    @Test
    fun `SourceStatus Offline is singleton`() {
        val status1 = SourceStatus.Offline
        val status2 = SourceStatus.Offline
        
        assertSame(status1, status2)
    }

    @Test
    fun `SourceStatus LoginRequired is singleton`() {
        val status1 = SourceStatus.LoginRequired
        val status2 = SourceStatus.LoginRequired
        
        assertSame(status1, status2)
    }

    @Test
    fun `SourceStatus Error with same message are equal`() {
        val error1 = SourceStatus.Error("Same error")
        val error2 = SourceStatus.Error("Same error")
        
        assertEquals(error1, error2)
    }

    @Test
    fun `SourceStatus Error with different messages are not equal`() {
        val error1 = SourceStatus.Error("Error 1")
        val error2 = SourceStatus.Error("Error 2")
        
        assertNotEquals(error1, error2)
    }

    // ==================== When Expression Tests ====================

    @Test
    fun `when expression handles all SourceStatus types`() {
        fun getStatusMessage(status: SourceStatus): String {
            return when (status) {
                is SourceStatus.Online -> "Source is online"
                is SourceStatus.Offline -> "Source is offline"
                is SourceStatus.LoginRequired -> "Login required"
                is SourceStatus.Error -> "Error: ${status.errorMessage}"
                is SourceStatus.Working -> "Source is working"
                is SourceStatus.Outdated -> "Source is outdated"
                is SourceStatus.LoadFailed -> "Load failed: ${status.error}"
                is SourceStatus.RequiresPlugin -> "Requires plugin: ${status.pluginName}"
                is SourceStatus.Incompatible -> "Source is incompatible"
                is SourceStatus.Deprecated -> "Source is deprecated"
                is SourceStatus.Unknown -> "Source status unknown"
            }
        }
        
        assertEquals("Source is online", getStatusMessage(SourceStatus.Online))
        assertEquals("Source is offline", getStatusMessage(SourceStatus.Offline))
        assertEquals("Login required", getStatusMessage(SourceStatus.LoginRequired))
        assertEquals("Error: Test error", getStatusMessage(SourceStatus.Error("Test error")))
    }

    // ==================== SourceHealth Equality Tests ====================

    @Test
    fun `SourceHealth equality with same values`() {
        val health1 = SourceHealth(
            sourceId = 1L,
            status = SourceStatus.Online,
            lastChecked = 1000L,
            responseTime = 100L
        )
        val health2 = SourceHealth(
            sourceId = 1L,
            status = SourceStatus.Online,
            lastChecked = 1000L,
            responseTime = 100L
        )
        
        assertEquals(health1, health2)
    }

    @Test
    fun `SourceHealth inequality with different sourceId`() {
        val health1 = SourceHealth(sourceId = 1L, status = SourceStatus.Online)
        val health2 = SourceHealth(sourceId = 2L, status = SourceStatus.Online)
        
        assertNotEquals(health1, health2)
    }

    @Test
    fun `SourceHealth inequality with different status`() {
        val health1 = SourceHealth(sourceId = 1L, status = SourceStatus.Online)
        val health2 = SourceHealth(sourceId = 1L, status = SourceStatus.Offline)
        
        assertNotEquals(health1, health2)
    }

    // ==================== Copy Tests ====================

    @Test
    fun `SourceHealth copy creates new instance`() {
        val original = SourceHealth(
            sourceId = 1L,
            status = SourceStatus.Online,
            responseTime = 100L
        )
        
        val copy = original.copy(status = SourceStatus.Offline)
        
        assertTrue(original.status is SourceStatus.Online)
        assertTrue(copy.status is SourceStatus.Offline)
        assertEquals(original.sourceId, copy.sourceId)
        assertEquals(original.responseTime, copy.responseTime)
    }

    @Test
    fun `SourceHealth copy with updated responseTime`() {
        val original = SourceHealth(
            sourceId = 1L,
            status = SourceStatus.Online,
            responseTime = 100L
        )
        
        val copy = original.copy(responseTime = 200L)
        
        assertEquals(100L, original.responseTime)
        assertEquals(200L, copy.responseTime)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `SourceHealth with zero responseTime`() {
        val health = SourceHealth(
            sourceId = 1L,
            status = SourceStatus.Online,
            responseTime = 0L
        )
        
        assertEquals(0L, health.responseTime)
    }

    @Test
    fun `SourceHealth with very large responseTime`() {
        val health = SourceHealth(
            sourceId = 1L,
            status = SourceStatus.Online,
            responseTime = Long.MAX_VALUE
        )
        
        assertEquals(Long.MAX_VALUE, health.responseTime)
    }

    @Test
    fun `SourceStatus Error with empty message`() {
        val error = SourceStatus.Error("")
        
        assertEquals("", error.errorMessage)
    }

    @Test
    fun `SourceStatus Error with very long message`() {
        val longMessage = "A".repeat(1000)
        val error = SourceStatus.Error(longMessage)
        
        assertEquals(1000, error.errorMessage.length)
    }

    @Test
    fun `SourceStatus Error with special characters`() {
        val specialMessage = "Error: 日本語 🔥 <script>alert('xss')</script>"
        val error = SourceStatus.Error(specialMessage)
        
        assertEquals(specialMessage, error.errorMessage)
    }

    // ==================== Practical Usage Tests ====================

    @Test
    fun `simulate health check result processing`() {
        val healthResults = listOf(
            SourceHealth(1L, SourceStatus.Online, responseTime = 50L),
            SourceHealth(2L, SourceStatus.Online, responseTime = 150L),
            SourceHealth(3L, SourceStatus.Offline),
            SourceHealth(4L, SourceStatus.Error("Timeout"))
        )
        
        val onlineSources = healthResults.filter { it.status is SourceStatus.Online }
        val offlineSources = healthResults.filter { it.status is SourceStatus.Offline }
        val errorSources = healthResults.filter { it.status is SourceStatus.Error }
        
        assertEquals(2, onlineSources.size)
        assertEquals(1, offlineSources.size)
        assertEquals(1, errorSources.size)
    }

    @Test
    fun `calculate average response time for online sources`() {
        val healthResults = listOf(
            SourceHealth(1L, SourceStatus.Online, responseTime = 100L),
            SourceHealth(2L, SourceStatus.Online, responseTime = 200L),
            SourceHealth(3L, SourceStatus.Offline),
            SourceHealth(4L, SourceStatus.Online, responseTime = 300L)
        )
        
        val avgResponseTime = healthResults
            .filter { it.status is SourceStatus.Online && it.responseTime != null }
            .mapNotNull { it.responseTime }
            .average()
        
        assertEquals(200.0, avgResponseTime, 0.001)
    }
}
