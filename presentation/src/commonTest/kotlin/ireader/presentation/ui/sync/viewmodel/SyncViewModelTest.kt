package ireader.presentation.ui.sync.viewmodel

import ireader.domain.models.sync.*
import ireader.domain.usecases.sync.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Test suite for SyncViewModel following TDD methodology.
 * Tests written FIRST before implementation (RED phase).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncViewModelTest {

    private lateinit var viewModel: SyncViewModel
    private lateinit var fakeStartSyncUseCase: FakeStartSyncUseCase
    private lateinit var fakeStopSyncUseCase: FakeStopSyncUseCase
    private lateinit var fakeSyncWithDeviceUseCase: FakeSyncWithDeviceUseCase
    private lateinit var fakeGetDiscoveredDevicesUseCase: FakeGetDiscoveredDevicesUseCase
    private lateinit var fakeGetSyncStatusUseCase: FakeGetSyncStatusUseCase
    private lateinit var fakeCancelSyncUseCase: FakeCancelSyncUseCase
    private lateinit var fakeResolveConflictsUseCase: FakeResolveConflictsUseCase
    
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        fakeStartSyncUseCase = FakeStartSyncUseCase()
        fakeStopSyncUseCase = FakeStopSyncUseCase()
        fakeSyncWithDeviceUseCase = FakeSyncWithDeviceUseCase()
        fakeGetDiscoveredDevicesUseCase = FakeGetDiscoveredDevicesUseCase()
        fakeGetSyncStatusUseCase = FakeGetSyncStatusUseCase()
        fakeCancelSyncUseCase = FakeCancelSyncUseCase()
        fakeResolveConflictsUseCase = FakeResolveConflictsUseCase()
        
        viewModel = SyncViewModel(
            startSyncUseCase = fakeStartSyncUseCase,
            stopSyncUseCase = fakeStopSyncUseCase,
            syncWithDeviceUseCase = fakeSyncWithDeviceUseCase,
            getDiscoveredDevicesUseCase = fakeGetDiscoveredDevicesUseCase,
            getSyncStatusUseCase = fakeGetSyncStatusUseCase,
            cancelSyncUseCase = fakeCancelSyncUseCase,
            resolveConflictsUseCase = fakeResolveConflictsUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        viewModel.onDispose()
    }

    // Test 1: Initial state should be correct
    @Test
    fun `initial state should have empty devices and idle status`() {
        // Assert
        val state = viewModel.state.value
        assertEquals(emptyList(), state.discoveredDevices)
        assertEquals(SyncStatus.Idle, state.syncStatus)
        assertNull(state.selectedDevice)
        assertFalse(state.isDiscovering)
        assertNull(state.error)
        assertFalse(state.showPairingDialog)
        assertFalse(state.showConflictDialog)
        assertEquals(emptyList(), state.conflicts)
    }

    // Test 2: Start discovery should update isDiscovering state
    @Test
    fun `startDiscovery should update isDiscovering to true`() = runTest {
        // Arrange
        fakeStartSyncUseCase.result = Result.success(Unit)
        
        // Act
        viewModel.startDiscovery()
        advanceUntilIdle()
        
        // Assert
        assertTrue(viewModel.state.value.isDiscovering)
        assertTrue(fakeStartSyncUseCase.invoked)
    }

    // Test 3: Stop discovery should update isDiscovering state
    @Test
    fun `stopDiscovery should update isDiscovering to false`() = runTest {
        // Arrange
        fakeStopSyncUseCase.result = Result.success(Unit)
        viewModel.startDiscovery()
        advanceUntilIdle()
        
        // Act
        viewModel.stopDiscovery()
        advanceUntilIdle()
        
        // Assert
        assertFalse(viewModel.state.value.isDiscovering)
        assertTrue(fakeStopSyncUseCase.invoked)
    }

    // Test 4: Discovered devices flow should update state
    @Test
    fun `discovered devices flow should update state with devices`() = runTest {
        // Arrange
        val testDevice = createTestDevice("Device1")
        fakeGetDiscoveredDevicesUseCase.devicesFlow.value = listOf(testDevice)
        
        // Act
        advanceUntilIdle()
        
        // Assert
        assertEquals(1, viewModel.state.value.discoveredDevices.size)
        assertEquals("Device1", viewModel.state.value.discoveredDevices.first().deviceInfo.deviceName)
    }

    // Test 5: Select device should update selectedDevice state
    @Test
    fun `selectDevice should update selectedDevice in state`() = runTest {
        // Arrange
        val testDevice = createTestDevice("Device1")
        
        // Act
        viewModel.selectDevice(testDevice)
        advanceUntilIdle()
        
        // Assert
        assertEquals(testDevice, viewModel.state.value.selectedDevice)
    }

    // Test 6: Sync with device should trigger use case
    @Test
    fun `syncWithDevice should invoke syncWithDeviceUseCase`() = runTest {
        // Arrange
        val deviceId = "device-123"
        fakeSyncWithDeviceUseCase.result = Result.success(Unit)
        
        // Act
        viewModel.syncWithDevice(deviceId)
        advanceUntilIdle()
        
        // Assert
        assertTrue(fakeSyncWithDeviceUseCase.invoked)
        assertEquals(deviceId, fakeSyncWithDeviceUseCase.lastDeviceId)
    }

    // Test 7: Sync status flow should update state
    @Test
    fun `sync status flow should update syncStatus in state`() = runTest {
        // Arrange
        val syncingStatus = SyncStatus.Syncing("Device1", 0.5f, "Book1")
        fakeGetSyncStatusUseCase.statusFlow.value = syncingStatus
        
        // Act
        advanceUntilIdle()
        
        // Assert
        assertEquals(syncingStatus, viewModel.state.value.syncStatus)
    }

    // Test 8: Cancel sync should trigger use case
    @Test
    fun `cancelSync should invoke cancelSyncUseCase`() = runTest {
        // Arrange
        fakeCancelSyncUseCase.result = Result.success(Unit)
        
        // Act
        viewModel.cancelSync()
        advanceUntilIdle()
        
        // Assert
        assertTrue(fakeCancelSyncUseCase.invoked)
    }

    // Test 9: Error handling should update error state
    @Test
    fun `startDiscovery failure should update error state`() = runTest {
        // Arrange
        val errorMessage = "Network error"
        fakeStartSyncUseCase.result = Result.failure(Exception(errorMessage))
        
        // Act
        viewModel.startDiscovery()
        advanceUntilIdle()
        
        // Assert
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.error!!.contains(errorMessage))
    }

    // Test 10: Dismiss error should clear error state
    @Test
    fun `dismissError should clear error from state`() = runTest {
        // Arrange
        fakeStartSyncUseCase.result = Result.failure(Exception("Error"))
        viewModel.startDiscovery()
        advanceUntilIdle()
        
        // Act
        viewModel.dismissError()
        advanceUntilIdle()
        
        // Assert
        assertNull(viewModel.state.value.error)
    }

    // Test 11: Pairing dialog state management
    @Test
    fun `showPairingDialog should update showPairingDialog state`() = runTest {
        // Act
        viewModel.showPairingDialog()
        advanceUntilIdle()
        
        // Assert
        assertTrue(viewModel.state.value.showPairingDialog)
    }

    // Test 12: Dismiss pairing dialog
    @Test
    fun `dismissPairingDialog should clear showPairingDialog state`() = runTest {
        // Arrange
        viewModel.showPairingDialog()
        advanceUntilIdle()
        
        // Act
        viewModel.dismissPairingDialog()
        advanceUntilIdle()
        
        // Assert
        assertFalse(viewModel.state.value.showPairingDialog)
    }

    // Test 13: Conflict resolution should trigger use case
    @Test
    fun `resolveConflict should invoke resolveConflictsUseCase`() = runTest {
        // Arrange
        val conflict = createTestConflict()
        val strategy = ConflictResolutionStrategy.LOCAL_WINS
        fakeResolveConflictsUseCase.result = Result.success(emptyList())
        
        // Act
        viewModel.resolveConflict(conflict, strategy)
        advanceUntilIdle()
        
        // Assert
        assertTrue(fakeResolveConflictsUseCase.invoked)
        assertEquals(listOf(conflict), fakeResolveConflictsUseCase.lastConflicts)
        assertEquals(strategy, fakeResolveConflictsUseCase.lastStrategy)
    }

    // Test 14: Multiple devices discovered should update list
    @Test
    fun `multiple discovered devices should all appear in state`() = runTest {
        // Arrange
        val devices = listOf(
            createTestDevice("Device1"),
            createTestDevice("Device2"),
            createTestDevice("Device3")
        )
        fakeGetDiscoveredDevicesUseCase.devicesFlow.value = devices
        
        // Act
        advanceUntilIdle()
        
        // Assert
        assertEquals(3, viewModel.state.value.discoveredDevices.size)
    }

    // Test 15: Sync completion should update status
    @Test
    fun `sync completion status should update state correctly`() = runTest {
        // Arrange
        val completedStatus = SyncStatus.Completed("Device1", 10, 5000L)
        fakeGetSyncStatusUseCase.statusFlow.value = completedStatus
        
        // Act
        advanceUntilIdle()
        
        // Assert
        assertEquals(completedStatus, viewModel.state.value.syncStatus)
    }

    // Test 16: Sync failure should update status with error
    @Test
    fun `sync failure status should update state with error`() = runTest {
        // Arrange
        val failedStatus = SyncStatus.Failed("Device1", SyncError.NetworkError("Connection lost"))
        fakeGetSyncStatusUseCase.statusFlow.value = failedStatus
        
        // Act
        advanceUntilIdle()
        
        // Assert
        assertEquals(failedStatus, viewModel.state.value.syncStatus)
    }

    // Test 17: Discovery failure should update error
    @Test
    fun `stopDiscovery failure should update error state`() = runTest {
        // Arrange
        val errorMessage = "Failed to stop discovery"
        fakeStopSyncUseCase.result = Result.failure(Exception(errorMessage))
        
        // Act
        viewModel.stopDiscovery()
        advanceUntilIdle()
        
        // Assert
        assertNotNull(viewModel.state.value.error)
    }

    // Test 18: Conflicts should show conflict dialog
    @Test
    fun `conflicts detected should show conflict dialog`() = runTest {
        // Arrange
        val conflicts = listOf(createTestConflict())
        
        // Act
        viewModel.showConflictDialog(conflicts)
        advanceUntilIdle()
        
        // Assert
        assertTrue(viewModel.state.value.showConflictDialog)
        assertEquals(conflicts, viewModel.state.value.conflicts)
    }

    // Test 19: Dismiss conflict dialog
    @Test
    fun `dismissConflictDialog should clear conflict dialog state`() = runTest {
        // Arrange
        viewModel.showConflictDialog(listOf(createTestConflict()))
        advanceUntilIdle()
        
        // Act
        viewModel.dismissConflictDialog()
        advanceUntilIdle()
        
        // Assert
        assertFalse(viewModel.state.value.showConflictDialog)
        assertEquals(emptyList(), viewModel.state.value.conflicts)
    }

    // Test 20: Sync with device failure should update error
    @Test
    fun `syncWithDevice failure should update error state`() = runTest {
        // Arrange
        val errorMessage = "Device not reachable"
        fakeSyncWithDeviceUseCase.result = Result.failure(Exception(errorMessage))
        
        // Act
        viewModel.syncWithDevice("device-123")
        advanceUntilIdle()
        
        // Assert
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.error!!.contains(errorMessage))
    }

    // Helper functions
    private fun createTestDevice(name: String): DiscoveredDevice {
        return DiscoveredDevice(
            deviceInfo = DeviceInfo(
                deviceId = "id-$name",
                deviceName = name,
                deviceType = DeviceType.ANDROID,
                ipAddress = "192.168.1.100",
                port = 8080
            ),
            isReachable = true,
            discoveredAt = System.currentTimeMillis()
        )
    }

    private fun createTestConflict(): DataConflict {
        return DataConflict(
            conflictType = ConflictType.READING_PROGRESS,
            localData = "Chapter 5",
            remoteData = "Chapter 6",
            conflictField = "currentChapter"
        )
    }
}

// Fake implementations for testing
class FakeStartSyncUseCase : StartSyncUseCase(FakeSyncRepository()) {
    var result: Result<Unit> = Result.success(Unit)
    var invoked = false

    override suspend fun invoke(): Result<Unit> {
        invoked = true
        return result
    }
}

class FakeStopSyncUseCase : StopSyncUseCase(FakeSyncRepository()) {
    var result: Result<Unit> = Result.success(Unit)
    var invoked = false

    override suspend fun invoke(): Result<Unit> {
        invoked = true
        return result
    }
}

class FakeSyncWithDeviceUseCase : SyncWithDeviceUseCase(FakeSyncRepository()) {
    var result: Result<Unit> = Result.success(Unit)
    var invoked = false
    var lastDeviceId: String? = null

    override suspend fun invoke(deviceId: String): Result<Unit> {
        invoked = true
        lastDeviceId = deviceId
        return result
    }
}

class FakeGetDiscoveredDevicesUseCase : GetDiscoveredDevicesUseCase(FakeSyncRepository()) {
    val devicesFlow = MutableStateFlow<List<DiscoveredDevice>>(emptyList())

    override fun invoke() = devicesFlow
}

class FakeGetSyncStatusUseCase : GetSyncStatusUseCase(FakeSyncRepository()) {
    val statusFlow = MutableStateFlow<SyncStatus>(SyncStatus.Idle)

    override fun invoke() = statusFlow
}

class FakeCancelSyncUseCase : CancelSyncUseCase(FakeSyncRepository()) {
    var result: Result<Unit> = Result.success(Unit)
    var invoked = false

    override suspend fun invoke(): Result<Unit> {
        invoked = true
        return result
    }
}

class FakeResolveConflictsUseCase : ResolveConflictsUseCase() {
    var result: Result<List<Any>> = Result.success(emptyList())
    var invoked = false
    var lastConflicts: List<DataConflict>? = null
    var lastStrategy: ConflictResolutionStrategy? = null

    override fun invoke(
        conflicts: List<DataConflict>,
        strategy: ConflictResolutionStrategy
    ): Result<List<Any>> {
        invoked = true
        lastConflicts = conflicts
        lastStrategy = strategy
        return result
    }
}

// Minimal fake repository for use case constructors
class FakeSyncRepository : ireader.domain.repositories.SyncRepository {
    override suspend fun startDiscovery() = Result.success(Unit)
    override suspend fun stopDiscovery() = Result.success(Unit)
    override fun observeDiscoveredDevices() = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    override fun observeSyncStatus() = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override suspend fun syncWithDevice(deviceId: String) = Result.success(Unit)
    override suspend fun cancelSync() = Result.success(Unit)
}
