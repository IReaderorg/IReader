package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for PiperInitializer.
 * Note: These tests will fail if native libraries are not present,
 * which is expected during development.
 */
class PiperInitializerTest {
    
    @Test
    fun `getStatusInfo returns platform information`() {
        val statusInfo = PiperInitializer.getStatusInfo()
        
        assertNotNull(statusInfo)
        assertTrue(statusInfo.contains("Piper TTS Status"))
        assertTrue(statusInfo.contains("Platform Information"))
    }
    
    @Test
    fun `isAvailable returns false when libraries not loaded`() = runTest {
        // Reset state for testing
        PiperInitializer.resetForTesting()
        
        // Before initialization, should not be available
        assertFalse(PiperInitializer.isAvailable())
    }
    
    @Test
    fun `initialize handles missing libraries gracefully`() = runTest {
        // Reset state for testing
        PiperInitializer.resetForTesting()
        
        // Attempt to initialize (will fail if libraries not present)
        val result = PiperInitializer.initialize()
        
        // Should return a result (success or failure)
        assertNotNull(result)
        
        // If it failed, should have an error
        if (result.isFailure) {
            assertNotNull(PiperInitializer.getInitializationError())
        }
    }
}
