package ireader.presentation.ui.sync

import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ireader.domain.usecases.sync.CancelSyncUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

/**
 * TDD Test for SyncCancelActionReceiver.
 * 
 * Tests the BroadcastReceiver that handles the cancel action from the sync notification.
 * Following TDD methodology - these tests are written BEFORE implementation.
 */
class SyncCancelActionReceiverTest : KoinTest {
    
    private lateinit var receiver: SyncCancelActionReceiver
    private lateinit var mockContext: Context
    private lateinit var mockCancelSyncUseCase: CancelSyncUseCase
    
    @Before
    fun setup() {
        // Stop any existing Koin instance
        stopKoin()
        
        // Create mocks
        mockContext = mockk(relaxed = true)
        mockCancelSyncUseCase = mockk(relaxed = true)
        
        // Setup Koin with mock dependencies
        startKoin {
            modules(
                module {
                    single { mockCancelSyncUseCase }
                }
            )
        }
        
        // Create receiver instance
        receiver = SyncCancelActionReceiver()
    }
    
    @Test
    fun `onReceive with ACTION_CANCEL_SYNC should trigger cancelSync`() = runTest {
        // Arrange
        val intent = Intent(SyncCancelActionReceiver.ACTION_CANCEL_SYNC)
        every { mockCancelSyncUseCase.invoke() } returns Result.success(Unit)
        
        // Act
        receiver.onReceive(mockContext, intent)
        
        // Assert
        verify(timeout = 1000) { mockCancelSyncUseCase.invoke() }
    }
    
    @Test
    fun `onReceive with unknown action should not trigger cancelSync`() = runTest {
        // Arrange
        val intent = Intent("unknown.action")
        
        // Act
        receiver.onReceive(mockContext, intent)
        
        // Assert
        verify(exactly = 0) { mockCancelSyncUseCase.invoke() }
    }
    
    @Test
    fun `onReceive with null action should not crash`() = runTest {
        // Arrange
        val intent = Intent()
        
        // Act & Assert - should not throw exception
        receiver.onReceive(mockContext, intent)
    }
}
