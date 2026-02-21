package ireader.domain.usecases.sync

import ireader.domain.models.sync.*
import ireader.domain.repositories.Connection
import ireader.domain.repositories.FakeSyncRepository
import ireader.domain.repositories.SyncResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncWithDeviceUseCaseTest {

    @Test
    fun `invoke should successfully sync when no conflicts exist`() = runTest {
        // Arrange
        val repository = FakeSyncRepository()
        val detectConflicts = DetectConflictsUseCase()
        val resolveConflicts = ResolveConflictsUseCase()
        val useCase = SyncWithDeviceUseCase(repository, detectConflicts, resolveConflicts)

        // Act
        val result = useCase("test-device-123", ConflictResolutionStrategy.LATEST_TIMESTAMP)

        // Assert
        assertTrue(result.isSuccess)
        val syncResult = result.getOrThrow()
        assertEquals("test-device-123", syncResult.deviceId)
    }

    @Test
    fun `invoke should fail when device not found`() = runTest {
        // Arrange
        val repository = FakeSyncRepository(shouldFail = true)
        val detectConflicts = DetectConflictsUseCase()
        val resolveConflicts = ResolveConflictsUseCase()
        val useCase = SyncWithDeviceUseCase(repository, detectConflicts, resolveConflicts)

        // Act
        val result = useCase("non-existent-device", ConflictResolutionStrategy.LATEST_TIMESTAMP)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke should fail when connection fails`() = runTest {
        // Arrange
        val repository = FakeSyncRepository(shouldFailConnection = true)
        val detectConflicts = DetectConflictsUseCase()
        val resolveConflicts = ResolveConflictsUseCase()
        val useCase = SyncWithDeviceUseCase(repository, detectConflicts, resolveConflicts)

        // Act
        val result = useCase("test-device-123", ConflictResolutionStrategy.LATEST_TIMESTAMP)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke should resolve conflicts using specified strategy`() = runTest {
        // Arrange
        val repository = FakeSyncRepository(hasConflicts = true)
        val detectConflicts = DetectConflictsUseCase()
        val resolveConflicts = ResolveConflictsUseCase()
        val useCase = SyncWithDeviceUseCase(repository, detectConflicts, resolveConflicts)

        // Act
        val result = useCase("test-device-123", ConflictResolutionStrategy.LOCAL_WINS)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should fail when manual resolution required`() = runTest {
        // Arrange
        val repository = FakeSyncRepository(hasConflicts = true)
        val detectConflicts = DetectConflictsUseCase()
        val resolveConflicts = ResolveConflictsUseCase()
        val useCase = SyncWithDeviceUseCase(repository, detectConflicts, resolveConflicts)

        // Act
        val result = useCase("test-device-123", ConflictResolutionStrategy.MANUAL)

        // Assert
        assertTrue(result.isFailure)
    }
}
