package ireader.presentation.ui.sync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TDD Tests for BatteryMonitor - Written FIRST before implementation.
 * 
 * Tests battery monitoring functionality including:
 * - Battery level detection
 * - Battery saver mode detection
 * - Charging state detection
 * - Battery state flow observation
 * 
 * Following TDD methodology:
 * 1. Write test (RED) - Test should FAIL
 * 2. Implement minimal code (GREEN) - Make test PASS
 * 3. Refactor (REFACTOR) - Improve while keeping tests green
 */
class BatteryMonitorTest {

    private lateinit var context: Context
    private lateinit var powerManager: PowerManager
    private lateinit var batteryManager: BatteryManager
    private lateinit var batteryMonitor: BatteryMonitor

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        powerManager = mockk(relaxed = true)
        batteryManager = mockk(relaxed = true)
        
        every { context.getSystemService(Context.POWER_SERVICE) } returns powerManager
        every { context.getSystemService(Context.BATTERY_SERVICE) } returns batteryManager
        
        batteryMonitor = BatteryMonitor(context)
    }

    @Test
    fun `getBatteryLevel should return value between 0 and 100`() {
        // Arrange
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 75
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100

        // Act
        val batteryLevel = batteryMonitor.getBatteryLevel()

        // Assert
        assertEquals(75, batteryLevel)
        assertTrue(batteryLevel in 0..100, "Battery level should be between 0 and 100")
    }

    @Test
    fun `getBatteryLevel should return 0 when battery info unavailable`() {
        // Arrange
        every { context.registerReceiver(null, any<IntentFilter>()) } returns null

        // Act
        val batteryLevel = batteryMonitor.getBatteryLevel()

        // Assert
        assertEquals(0, batteryLevel)
    }

    @Test
    fun `isBatterySaverEnabled should return true when power save mode active`() {
        // Arrange
        every { powerManager.isPowerSaveMode } returns true

        // Act
        val isBatterySaverEnabled = batteryMonitor.isBatterySaverEnabled()

        // Assert
        assertTrue(isBatterySaverEnabled)
    }

    @Test
    fun `isBatterySaverEnabled should return false when power save mode inactive`() {
        // Arrange
        every { powerManager.isPowerSaveMode } returns false

        // Act
        val isBatterySaverEnabled = batteryMonitor.isBatterySaverEnabled()

        // Assert
        assertFalse(isBatterySaverEnabled)
    }

    @Test
    fun `isCharging should return true when device is charging`() {
        // Arrange
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_CHARGING

        // Act
        val isCharging = batteryMonitor.isCharging()

        // Assert
        assertTrue(isCharging)
    }

    @Test
    fun `isCharging should return true when device is full`() {
        // Arrange
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_FULL

        // Act
        val isCharging = batteryMonitor.isCharging()

        // Assert
        assertTrue(isCharging)
    }

    @Test
    fun `isCharging should return false when device is not charging`() {
        // Arrange
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_DISCHARGING

        // Act
        val isCharging = batteryMonitor.isCharging()

        // Assert
        assertFalse(isCharging)
    }

    @Test
    fun `isLowBattery should return true when battery below 20 percent`() {
        // Arrange
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 15
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100

        // Act
        val isLowBattery = batteryMonitor.isLowBattery()

        // Assert
        assertTrue(isLowBattery)
    }

    @Test
    fun `isLowBattery should return false when battery above 20 percent`() {
        // Arrange
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 50
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100

        // Act
        val isLowBattery = batteryMonitor.isLowBattery()

        // Assert
        assertFalse(isLowBattery)
    }

    @Test
    fun `shouldPauseSync should return true when battery saver enabled`() {
        // Arrange
        every { powerManager.isPowerSaveMode } returns true
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 50
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_DISCHARGING

        // Act
        val shouldPause = batteryMonitor.shouldPauseSync()

        // Assert
        assertTrue(shouldPause)
    }

    @Test
    fun `shouldPauseSync should return true when battery low and not charging`() {
        // Arrange
        every { powerManager.isPowerSaveMode } returns false
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 15
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_DISCHARGING

        // Act
        val shouldPause = batteryMonitor.shouldPauseSync()

        // Assert
        assertTrue(shouldPause)
    }

    @Test
    fun `shouldPauseSync should return false when charging`() {
        // Arrange
        every { powerManager.isPowerSaveMode } returns false
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 15
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_CHARGING

        // Act
        val shouldPause = batteryMonitor.shouldPauseSync()

        // Assert
        assertFalse(shouldPause)
    }

    @Test
    fun `shouldPauseSync should return false when battery sufficient and not in saver mode`() {
        // Arrange
        every { powerManager.isPowerSaveMode } returns false
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 50
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_DISCHARGING

        // Act
        val shouldPause = batteryMonitor.shouldPauseSync()

        // Assert
        assertFalse(shouldPause)
    }

    @Test
    fun `getBatteryTemperature should return temperature in celsius`() {
        // Arrange
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) } returns 350 // 35.0°C

        // Act
        val temperature = batteryMonitor.getBatteryTemperature()

        // Assert
        assertEquals(35.0f, temperature)
    }

    @Test
    fun `getBatteryTemperature should return 0 when temperature unavailable`() {
        // Arrange
        every { context.registerReceiver(null, any<IntentFilter>()) } returns null

        // Act
        val temperature = batteryMonitor.getBatteryTemperature()

        // Assert
        assertEquals(0f, temperature)
    }

    @Test
    fun `isOverheating should return true when temperature exceeds 45 celsius`() {
        // Arrange
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) } returns 480 // 48.0°C

        // Act
        val isOverheating = batteryMonitor.isOverheating()

        // Assert
        assertTrue(isOverheating)
    }

    @Test
    fun `isOverheating should return false when temperature below 45 celsius`() {
        // Arrange
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) } returns 350 // 35.0°C

        // Act
        val isOverheating = batteryMonitor.isOverheating()

        // Assert
        assertFalse(isOverheating)
    }

    @Test
    fun `observeBatteryState should emit battery state`() = runTest {
        // Arrange
        every { powerManager.isPowerSaveMode } returns false
        val batteryIntent = mockk<Intent>(relaxed = true)
        every { context.registerReceiver(null, any<IntentFilter>()) } returns batteryIntent
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 75
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_DISCHARGING
        every { batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) } returns 350

        // Act
        val batteryState = batteryMonitor.observeBatteryState().first()

        // Assert
        assertNotNull(batteryState)
        assertEquals(75, batteryState.level)
        assertFalse(batteryState.isCharging)
        assertFalse(batteryState.isBatterySaverEnabled)
        assertEquals(35.0f, batteryState.temperature)
    }
}
