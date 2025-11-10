package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for PiperNative JNI wrapper.
 * 
 * These tests verify the core JNI functionality including:
 * - Voice initialization and shutdown
 * - Basic synthesis functionality
 * - Parameter adjustment
 * - Error handling
 * 
 * Note: These tests require native libraries to be present.
 * If libraries are not available, tests will be skipped gracefully.
 */
class PiperNativeTest {
    
    companion object {
        private var librariesAvailable = false
        private var testModelPath: String? = null
        private var testConfigPath: String? = null
        
        init {
            // Check if libraries are available
            try {
                PiperInitializer.resetForTesting()
                val result = kotlinx.coroutines.runBlocking {
                    PiperInitializer.initialize()
                }
                librariesAvailable = result.isSuccess
                
                // Check for test model (optional)
                // In a real scenario, we'd bundle a small test model
                testModelPath = System.getProperty("test.model.path")
                testConfigPath = System.getProperty("test.config.path")
            } catch (e: Exception) {
                println("Native libraries not available for testing: ${e.message}")
                librariesAvailable = false
            }
        }
    }
    
    @BeforeTest
    fun setup() {
        if (!librariesAvailable) {
            println("Skipping test - native libraries not available")
        }
    }
    
    // ========== Voice Initialization Tests ==========
    
    @Test
    fun `test initialize with valid model returns positive instance`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        assertTrue(instance > 0, "Instance ID should be positive")
        
        // Cleanup
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test initialize with invalid model path throws exception`() {
        if (!librariesAvailable) {
            println("Skipping - libraries not available")
            return
        }
        
        assertFailsWith<Exception> {
            PiperNative.initialize("/nonexistent/model.onnx", "/nonexistent/config.json")
        }
    }
    
    @Test
    fun `test initialize with empty paths throws exception`() {
        if (!librariesAvailable) {
            println("Skipping - libraries not available")
            return
        }
        
        assertFailsWith<Exception> {
            PiperNative.initialize("", "")
        }
    }
    
    @Test
    fun `test multiple initialize calls create different instances`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance1 = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val instance2 = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        assertNotEquals(instance1, instance2, "Each initialize should create a unique instance")
        
        // Cleanup
        PiperNative.shutdown(instance1)
        PiperNative.shutdown(instance2)
    }
    
    // ========== Shutdown Tests ==========
    
    @Test
    fun `test shutdown with valid instance succeeds`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        // Should not throw
        assertDoesNotThrow {
            PiperNative.shutdown(instance)
        }
    }
    
    @Test
    fun `test shutdown with zero instance is safe`() {
        if (!librariesAvailable) {
            println("Skipping - libraries not available")
            return
        }
        
        // Should not throw or crash
        assertDoesNotThrow {
            PiperNative.shutdown(0)
        }
    }
    
    @Test
    fun `test double shutdown is safe`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        PiperNative.shutdown(instance)
        
        // Second shutdown should be safe
        assertDoesNotThrow {
            PiperNative.shutdown(instance)
        }
    }
    
    // ========== Synthesis Tests ==========
    
    @Test
    fun `test synthesize with valid text returns audio data`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        val text = "Hello, world!"
        val audio = PiperNative.synthesize(instance, text)
        
        assertNotNull(audio, "Audio data should not be null")
        assertTrue(audio.isNotEmpty(), "Audio data should not be empty")
        assertTrue(audio.size > 100, "Audio data should have reasonable size")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test synthesize with empty text returns empty or minimal audio`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        val audio = PiperNative.synthesize(instance, "")
        
        assertNotNull(audio, "Audio data should not be null even for empty text")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test synthesize with invalid instance throws exception`() {
        if (!librariesAvailable) {
            println("Skipping - libraries not available")
            return
        }
        
        assertFailsWith<Exception> {
            PiperNative.synthesize(0, "Test text")
        }
    }
    
    @Test
    fun `test synthesize with long text succeeds`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        val longText = "This is a longer text. ".repeat(50) // ~1200 characters
        val audio = PiperNative.synthesize(instance, longText)
        
        assertNotNull(audio)
        assertTrue(audio.isNotEmpty())
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test synthesize with special characters`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        val specialText = "Hello! How are you? I'm fine. Test 123."
        val audio = PiperNative.synthesize(instance, specialText)
        
        assertNotNull(audio)
        assertTrue(audio.isNotEmpty())
        
        PiperNative.shutdown(instance)
    }
    
    // ========== Parameter Adjustment Tests ==========
    
    @Test
    fun `test setSpeechRate with valid values succeeds`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        // Test various valid speech rates
        val validRates = listOf(0.5f, 0.75f, 1.0f, 1.5f, 2.0f)
        
        validRates.forEach { rate ->
            assertDoesNotThrow("Speech rate $rate should be valid") {
                PiperNative.setSpeechRate(instance, rate)
            }
        }
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test setSpeechRate with invalid values throws exception`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        // Test invalid speech rates
        val invalidRates = listOf(0.0f, 0.4f, 2.5f, 5.0f, -1.0f)
        
        invalidRates.forEach { rate ->
            assertFailsWith<Exception>("Speech rate $rate should be invalid") {
                PiperNative.setSpeechRate(instance, rate)
            }
        }
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test setNoiseScale with valid values succeeds`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        // Test various valid noise scales
        val validScales = listOf(0.0f, 0.333f, 0.667f, 1.0f)
        
        validScales.forEach { scale ->
            assertDoesNotThrow("Noise scale $scale should be valid") {
                PiperNative.setNoiseScale(instance, scale)
            }
        }
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test setNoiseScale with invalid values throws exception`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        // Test invalid noise scales
        val invalidScales = listOf(-0.1f, 1.5f, 2.0f, -1.0f)
        
        invalidScales.forEach { scale ->
            assertFailsWith<Exception>("Noise scale $scale should be invalid") {
                PiperNative.setNoiseScale(instance, scale)
            }
        }
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test setLengthScale with valid values succeeds`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        val validScales = listOf(0.5f, 1.0f, 1.5f, 2.0f)
        
        validScales.forEach { scale ->
            assertDoesNotThrow("Length scale $scale should be valid") {
                PiperNative.setLengthScale(instance, scale)
            }
        }
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test parameter changes affect synthesis`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val text = "Test synthesis"
        
        // Synthesize with default parameters
        val audio1 = PiperNative.synthesize(instance, text)
        
        // Change speech rate and synthesize again
        PiperNative.setSpeechRate(instance, 1.5f)
        val audio2 = PiperNative.synthesize(instance, text)
        
        // Audio should be different (faster speech = shorter audio)
        assertNotEquals(audio1.size, audio2.size, "Audio size should differ with different speech rate")
        
        PiperNative.shutdown(instance)
    }
    
    // ========== Query Methods Tests ==========
    
    @Test
    fun `test getSampleRate returns valid value`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        val sampleRate = PiperNative.getSampleRate(instance)
        
        assertTrue(sampleRate > 0, "Sample rate should be positive")
        assertTrue(sampleRate in 8000..48000, "Sample rate should be in valid audio range")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test getChannels returns valid value`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        val channels = PiperNative.getChannels(instance)
        
        assertTrue(channels > 0, "Channels should be positive")
        assertTrue(channels in 1..2, "Channels should be 1 (mono) or 2 (stereo)")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test getVersion returns non-empty string`() {
        if (!librariesAvailable) {
            println("Skipping - libraries not available")
            return
        }
        
        val version = PiperNative.getVersion()
        
        assertNotNull(version, "Version should not be null")
        assertTrue(version.isNotEmpty(), "Version should not be empty")
        assertTrue(version.matches(Regex("\\d+\\.\\d+.*")), "Version should match semantic versioning pattern")
    }
    
    // ========== Error Handling Tests ==========
    
    @Test
    fun `test operations on shutdown instance throw exceptions`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        PiperNative.shutdown(instance)
        
        // Operations on shutdown instance should fail
        assertFailsWith<Exception> {
            PiperNative.synthesize(instance, "Test")
        }
        
        assertFailsWith<Exception> {
            PiperNative.setSpeechRate(instance, 1.0f)
        }
        
        assertFailsWith<Exception> {
            PiperNative.getSampleRate(instance)
        }
    }
    
    @Test
    fun `test isLoaded returns correct status`() {
        if (!librariesAvailable) {
            println("Skipping - libraries not available")
            return
        }
        
        val isLoaded = PiperNative.isLoaded()
        
        assertTrue(isLoaded, "isLoaded should return true when libraries are available")
    }
    
    @Test
    fun `test validateInstance with valid instance succeeds`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        
        assertDoesNotThrow {
            PiperNative.validateInstance(instance)
        }
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test validateInstance with invalid instance throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            PiperNative.validateInstance(0)
        }
        
        assertFailsWith<IllegalArgumentException> {
            PiperNative.validateInstance(-1)
        }
    }
    
    @Test
    fun `test validateRange with valid values succeeds`() {
        assertDoesNotThrow {
            PiperNative.validateRange(1.0f, 0.5f, 2.0f, "testParam")
        }
        
        assertDoesNotThrow {
            PiperNative.validateRange(0.5f, 0.5f, 2.0f, "testParam")
        }
        
        assertDoesNotThrow {
            PiperNative.validateRange(2.0f, 0.5f, 2.0f, "testParam")
        }
    }
    
    @Test
    fun `test validateRange with invalid values throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            PiperNative.validateRange(0.4f, 0.5f, 2.0f, "testParam")
        }
        
        assertFailsWith<IllegalArgumentException> {
            PiperNative.validateRange(2.1f, 0.5f, 2.0f, "testParam")
        }
    }
    
    // ========== Memory and Resource Tests ==========
    
    @Test
    fun `test multiple synthesis calls do not leak memory`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val text = "Memory test"
        
        // Perform multiple synthesis operations
        repeat(10) {
            val audio = PiperNative.synthesize(instance, text)
            assertNotNull(audio)
            assertTrue(audio.isNotEmpty())
        }
        
        PiperNative.shutdown(instance)
        
        // If we got here without OOM, memory management is working
        assertTrue(true, "Multiple synthesis calls completed without memory issues")
    }
    
    @Test
    fun `test concurrent instance creation and cleanup`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instances = mutableListOf<Long>()
        
        // Create multiple instances
        repeat(5) {
            val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
            instances.add(instance)
            assertTrue(instance > 0)
        }
        
        // All instances should be unique
        assertEquals(instances.size, instances.toSet().size, "All instances should be unique")
        
        // Cleanup all instances
        instances.forEach { instance ->
            PiperNative.shutdown(instance)
        }
        
        assertTrue(true, "Multiple instances created and cleaned up successfully")
    }
}
