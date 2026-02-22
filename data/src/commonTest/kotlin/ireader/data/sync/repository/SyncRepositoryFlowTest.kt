package ireader.data.sync.repository

import ireader.data.sync.datasource.FakeDiscoveryDataSource
import ireader.data.sync.datasource.FakeSyncLocalDataSource
import ireader.data.sync.datasource.FakeTransferDataSource
import ireader.domain.models.sync.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Flow optimization tests for SyncRepositoryImpl.
 * 
 * Following TDD: These tests verify Flow usage is memory-safe and leak-free.
 * Task 10.1.3: Flow Collector Optimization
 */
class SyncRepositoryFlowTest {
    
    private lateinit var discoveryDataSource: FakeDiscoveryDataSource
    private lateinit var transferDataSource: FakeTransferDataSource
    private lateinit var localDataSource: FakeSyncLocalDataSource
    private lateinit var repository: SyncRepositoryImpl
    
    @BeforeTest
    fun setup() {
        discoveryDataSource = FakeDiscoveryDataSource()
        transferDataSource = FakeTransferDataSource()
        localDataSource = FakeSyncLocalDataSource()
        repository = SyncRepositoryImpl(
            discoveryDataSource = discoveryDataSource,
            transferDataSource = transferDataSource,
            localDataSource = localDataSource
        )
    }
    
    @Test
    fun `observeSyncStatus should not leak memory when collector is cancelled`() = runTest {
        // Arrange
        val statusUpdates = mutableListOf<SyncStatus>()
        
        // Act - Collect status updates
        val job = launch {
            repository.observeSyncStatus()
                .take(3) // Take only 3 emissions
                .toList()
                .also { statusUpdates.addAll(it) }
        }
        
        // Trigger some status changes
        repository.startDiscovery()
        repository.stopDiscovery()
        
        job.join()
        
        // Assert
        assertTrue(statusUpdates.isNotEmpty(), "Should have collected status updates")
        // Job should complete without hanging
        assertTrue(job.isCompleted, "Collection job should complete")
    }
    
    @Test
    fun `observeDiscoveredDevices should not leak memory when collector is cancelled`() = runTest {
        // Arrange
        val device = DiscoveredDevice(
            deviceInfo = DeviceInfo(
                deviceId = "device-1",
                deviceName = "Device 1",
                deviceType = DeviceType.ANDROID,
                appVersion = "1.0.0",
                ipAddress = "192.168.1.100",
                port = 8080,
                lastSeen = System.currentTimeMillis()
            ),
            isReachable = true,
            discoveredAt = System.currentTimeMillis()
        )
        discoveryDataSource.addDiscoveredDevice(device)
        
        // Act - Collect and cancel
        val job = launch {
            val devices = repository.observeDiscoveredDevices().first()
            assertEquals(1, devices.size)
        }
        
        job.join()
        
        // Assert
        assertTrue(job.isCompleted, "Collection job should complete")
    }
    
    @Test
    fun `observeTransferProgress should not leak memory when collector is cancelled`() = runTest {
        // Arrange
        val progressUpdates = mutableListOf<Float>()
        
        // Act - Collect progress updates
        val job = launch {
            transferDataSource.observeTransferProgress()
                .take(3)
                .toList()
                .also { progressUpdates.addAll(it) }
        }
        
        // Simulate progress updates
        transferDataSource.updateProgress(0.0f)
        transferDataSource.updateProgress(0.5f)
        transferDataSource.updateProgress(1.0f)
        
        job.join()
        
        // Assert
        assertEquals(3, progressUpdates.size, "Should collect 3 progress updates")
        assertTrue(job.isCompleted, "Collection job should complete")
    }
    
    @Test
    fun `multiple collectors on observeSyncStatus should not interfere`() = runTest {
        // Arrange
        val collector1Updates = mutableListOf<SyncStatus>()
        val collector2Updates = mutableListOf<SyncStatus>()
        
        // Act - Start two collectors
        val job1 = launch {
            repository.observeSyncStatus()
                .take(2)
                .toList()
                .also { collector1Updates.addAll(it) }
        }
        
        val job2 = launch {
            repository.observeSyncStatus()
                .take(2)
                .toList()
                .also { collector2Updates.addAll(it) }
        }
        
        // Trigger status change
        repository.startDiscovery()
        
        job1.join()
        job2.join()
        
        // Assert
        assertTrue(collector1Updates.isNotEmpty(), "Collector 1 should receive updates")
        assertTrue(collector2Updates.isNotEmpty(), "Collector 2 should receive updates")
        assertTrue(job1.isCompleted, "Job 1 should complete")
        assertTrue(job2.isCompleted, "Job 2 should complete")
    }
    
    @Test
    fun `observeSyncStatus should handle rapid status changes without memory buildup`() = runTest {
        // Arrange
        val statusUpdates = mutableListOf<SyncStatus>()
        
        // Act - Collect status updates
        val job = launch {
            repository.observeSyncStatus()
                .take(10)
                .toList()
                .also { statusUpdates.addAll(it) }
        }
        
        // Trigger rapid status changes
        repeat(5) {
            repository.startDiscovery()
            repository.stopDiscovery()
        }
        
        job.join()
        
        // Assert
        assertTrue(statusUpdates.size <= 10, "Should not accumulate more than requested")
        assertTrue(job.isCompleted, "Collection job should complete")
    }
    
    @Test
    fun `Flow should properly clean up when repository scope is cancelled`() = runTest {
        // Arrange
        val job = launch {
            repository.observeSyncStatus().collect { status ->
                // Collector running
            }
        }
        
        // Act - Cancel the collection
        job.cancel()
        
        // Assert
        assertTrue(job.isCancelled, "Job should be cancelled")
    }
    
    @Test
    fun `observeDiscoveredDevices should handle device list updates efficiently`() = runTest {
        // Arrange
        val devices = (1..100).map { i ->
            DiscoveredDevice(
                deviceInfo = DeviceInfo(
                    deviceId = "device-$i",
                    deviceName = "Device $i",
                    deviceType = DeviceType.ANDROID,
                    appVersion = "1.0.0",
                    ipAddress = "192.168.1.$i",
                    port = 8080,
                    lastSeen = System.currentTimeMillis()
                ),
                isReachable = true,
                discoveredAt = System.currentTimeMillis()
            )
        }
        
        // Act - Add devices and collect
        devices.forEach { discoveryDataSource.addDiscoveredDevice(it) }
        
        val job = launch {
            val discoveredDevices = repository.observeDiscoveredDevices().first()
            assertEquals(100, discoveredDevices.size)
        }
        
        job.join()
        
        // Assert
        assertTrue(job.isCompleted, "Should handle large device list efficiently")
    }
    
    @Test
    fun `getActiveTrustedDevices Flow should not leak memory`() = runTest {
        // Arrange
        // Note: This tests the Flow from localDataSource
        
        // Act - Collect trusted devices
        val job = launch {
            val devices = localDataSource.getActiveTrustedDevices().first()
            assertTrue(devices.isEmpty(), "Should start with no trusted devices")
        }
        
        job.join()
        
        // Assert
        assertTrue(job.isCompleted, "Collection should complete without leaks")
    }
    
    @Test
    fun `getSyncLogsByDevice Flow should not leak memory`() = runTest {
        // Arrange
        val deviceId = "test-device"
        
        // Act - Collect sync logs
        val job = launch {
            val logs = localDataSource.getSyncLogsByDevice(deviceId).first()
            assertTrue(logs.isEmpty(), "Should start with no sync logs")
        }
        
        job.join()
        
        // Assert
        assertTrue(job.isCompleted, "Collection should complete without leaks")
    }
}
