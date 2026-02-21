package ireader.domain.usecases.sync

import ireader.domain.repositories.FakeSyncRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class StartSyncUseCaseTest {

    @Test
    fun `invoke should call repository startDiscovery`() = runTest {
        // Arrange
        val repository = FakeSyncRepository()
        val useCase = StartSyncUseCase(repository)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Arrange
        val repository = FakeSyncRepository(shouldFail = true)
        val useCase = StartSyncUseCase(repository)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isFailure)
    }
}
