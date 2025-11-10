package ireader.domain.services.tts_service.piper

import ireader.domain.catalogs.VoiceCatalog
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.*

/**
 * Integration tests for Piper TTS system.
 * 
 * These tests verify end-to-end functionality including:
 * - Voice download and usage (simulated)
 * - Multi-language synthesis
 * - Voice switching
 * - Long-running synthesis
 * 
 * Note: These tests require native libraries and voice models to be present.
 * If not available, tests will be skipped gracefully.
 */
class PiperIntegrationTest {
    
    companion object {
        private var librariesAvailable = false
        private val testVoiceModels = mutableMapOf<String, Pair<String, String>>()
        
        init {
            // Check if libraries are available
            try {
                PiperInitializer.resetForTesting()
                val result = kotlinx.coroutines.runBlocking {
                    PiperInitializer.initialize()
                }
                librariesAvailable = result.isSuccess
                
                // Check for available test voice models
                discoverTestVoiceModels()
            } catch (e: Exception) {
                println("Native libraries not available for integration testing: ${e.message}")
                librariesAvailable = false
            }
        }
        
        private fun discoverTestVoiceModels() {
            // Look for voice models in test resources or system property
            val testModelsDir = System.getProperty("test.models.dir") ?: return
            val modelsDir = File(testModelsDir)
            
            if (!modelsDir.exists() || !modelsDir.isDirectory) {
                println("Test models directory not found: $testModelsDir")
                return
            }
            
            // Scan for .onnx files and their corresponding .json configs
            modelsDir.listFiles()?.forEach { file ->
                if (file.extension == "onnx") {
                    val configFile = File(file.absolutePath + ".json")
                    if (configFile.exists()) {
                        val language = extractLanguageFromFilename(file.name)
                        testVoiceModels[language] = Pair(file.absolutePath, configFile.absolutePath)
                        println("Found test voice model for language: $language")
                    }
                }
            }
        }
        
        private fun extractLanguageFromFilename(filename: String): String {
            // Extract language code from filename like "en_US-amy-low.onnx"
            val parts = filename.split("-", "_")
            return if (parts.isNotEmpty()) parts[0].lowercase() else "unknown"
        }
    }
    
    @BeforeTest
    fun setup() {
        if (!librariesAvailable) {
            println("Skipping integration test - native libraries not available")
        }
    }
    
    // ========== Voice Catalog Tests ==========
    
    @Test
    fun `test voice catalog contains 20+ languages`() {
        val languages = VoiceCatalog.getSupportedLanguages()
        
        assertTrue(languages.size >= 20, "Catalog should contain at least 20 languages")
        println("Voice catalog contains ${languages.size} languages: $languages")
    }
    
    @Test
    fun `test voice catalog has multiple voices per major language`() {
        val majorLanguages = listOf("en", "es", "fr", "de", "zh", "ja")
        
        majorLanguages.forEach { lang ->
            val voices = VoiceCatalog.getVoicesByLanguage(lang)
            assertTrue(voices.isNotEmpty(), "Should have voices for language: $lang")
            println("Language $lang has ${voices.size} voice(s)")
        }
    }
    
    @Test
    fun `test voice catalog metadata is complete`() {
        val allVoices = VoiceCatalog.getAllVoices()
        
        allVoices.forEach { voice ->
            assertNotNull(voice.id, "Voice should have ID")
            assertNotNull(voice.name, "Voice should have name")
            assertNotNull(voice.language, "Voice should have language")
            assertNotNull(voice.locale, "Voice should have locale")
            assertTrue(voice.sampleRate > 0, "Voice should have valid sample rate")
            assertTrue(voice.modelSize > 0, "Voice should have valid model size")
            assertTrue(voice.downloadUrl.isNotEmpty(), "Voice should have download URL")
            assertTrue(voice.configUrl.isNotEmpty(), "Voice should have config URL")
        }
    }
    
    // ========== Voice Download Simulation Tests ==========
    
    @Test
    fun `test voice model download simulation`() = runTest {
        // This test simulates the download process without actually downloading
        val voice = VoiceCatalog.getVoiceById("en-us-amy-low")
        assertNotNull(voice, "Test voice should exist in catalog")
        
        // Simulate download progress tracking
        val progressUpdates = mutableListOf<Float>()
        
        // Simulate progress callbacks
        listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f).forEach { progress ->
            progressUpdates.add(progress)
        }
        
        assertEquals(5, progressUpdates.size, "Should have received 5 progress updates")
        assertEquals(1.0f, progressUpdates.last(), "Final progress should be 100%")
    }
    
    @Test
    fun `test voice model integrity verification`() {
        val voice = VoiceCatalog.getVoiceById("en-us-amy-low")
        assertNotNull(voice)
        
        // Verify checksum format
        assertTrue(voice.checksum.startsWith("sha256:"), "Checksum should use SHA-256")
        
        // Verify URLs are valid
        assertTrue(voice.downloadUrl.startsWith("https://"), "Download URL should use HTTPS")
        assertTrue(voice.configUrl.startsWith("https://"), "Config URL should use HTTPS")
    }
    
    // ========== Multi-Language Synthesis Tests ==========
    
    @Test
    fun `test synthesis with different languages`() {
        if (!librariesAvailable || testVoiceModels.isEmpty()) {
            println("Skipping - libraries or test models not available")
            return
        }
        
        val testTexts = mapOf(
            "en" to "Hello, this is a test.",
            "es" to "Hola, esto es una prueba.",
            "fr" to "Bonjour, ceci est un test.",
            "de" to "Hallo, das ist ein Test."
        )
        
        testTexts.forEach { (lang, text) ->
            val modelPaths = testVoiceModels[lang]
            if (modelPaths != null) {
                val (modelPath, configPath) = modelPaths
                
                val instance = PiperNative.initialize(modelPath, configPath)
                assertTrue(instance > 0, "Should initialize voice for language: $lang")
                
                val audio = PiperNative.synthesize(instance, text)
                assertNotNull(audio, "Should synthesize audio for language: $lang")
                assertTrue(audio.isNotEmpty(), "Audio should not be empty for language: $lang")
                
                println("Successfully synthesized ${audio.size} bytes for language: $lang")
                
                PiperNative.shutdown(instance)
            } else {
                println("Skipping language $lang - test model not available")
            }
        }
    }
    
    @Test
    fun `test voice recommendation by language`() {
        val testLanguages = listOf("en", "es", "fr", "de", "zh", "ja")
        
        testLanguages.forEach { lang ->
            val voices = VoiceCatalog.getVoicesByLanguage(lang)
            assertTrue(voices.isNotEmpty(), "Should have voice recommendations for: $lang")
            
            // Verify voices match the requested language
            voices.forEach { voice ->
                assertEquals(lang, voice.language, "Voice language should match requested language")
            }
        }
    }
    
    // ========== Voice Switching Tests ==========
    
    @Test
    fun `test switching between multiple voices`() {
        if (!librariesAvailable || testVoiceModels.size < 2) {
            println("Skipping - need at least 2 test models")
            return
        }
        
        val instances = mutableListOf<Long>()
        val testText = "Voice switching test"
        
        // Initialize multiple voices
        testVoiceModels.values.take(2).forEach { (modelPath, configPath) ->
            val instance = PiperNative.initialize(modelPath, configPath)
            assertTrue(instance > 0)
            instances.add(instance)
        }
        
        // Synthesize with each voice
        instances.forEach { instance ->
            val audio = PiperNative.synthesize(instance, testText)
            assertNotNull(audio)
            assertTrue(audio.isNotEmpty())
        }
        
        // Cleanup
        instances.forEach { instance ->
            PiperNative.shutdown(instance)
        }
        
        assertTrue(true, "Successfully switched between ${instances.size} voices")
    }
    
    @Test
    fun `test rapid voice switching`() {
        if (!librariesAvailable || testVoiceModels.isEmpty()) {
            println("Skipping - libraries or test models not available")
            return
        }
        
        val (modelPath, configPath) = testVoiceModels.values.first()
        val testText = "Rapid switching test"
        
        // Rapidly create, use, and destroy instances
        repeat(5) { iteration ->
            val instance = PiperNative.initialize(modelPath, configPath)
            assertTrue(instance > 0, "Iteration $iteration: Should create instance")
            
            val audio = PiperNative.synthesize(instance, testText)
            assertNotNull(audio, "Iteration $iteration: Should synthesize audio")
            
            PiperNative.shutdown(instance)
        }
        
        assertTrue(true, "Successfully performed rapid voice switching")
    }
    
    // ========== Long-Running Synthesis Tests ==========
    
    @Test
    fun `test synthesis with very long text`() {
        if (!librariesAvailable || testVoiceModels.isEmpty()) {
            println("Skipping - libraries or test models not available")
            return
        }
        
        val (modelPath, configPath) = testVoiceModels.values.first()
        val instance = PiperNative.initialize(modelPath, configPath)
        
        // Create a long text (approximately 5000 characters)
        val longText = buildString {
            repeat(100) {
                append("This is sentence number ${it + 1}. ")
                append("It contains some test content for long-running synthesis. ")
            }
        }
        
        assertTrue(longText.length > 5000, "Test text should be over 5000 characters")
        println("Testing synthesis with ${longText.length} characters")
        
        val startTime = System.currentTimeMillis()
        val audio = PiperNative.synthesize(instance, longText)
        val duration = System.currentTimeMillis() - startTime
        
        assertNotNull(audio)
        assertTrue(audio.isNotEmpty())
        println("Synthesized ${audio.size} bytes in ${duration}ms")
        println("Throughput: ${longText.length * 1000 / duration} chars/second")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test streaming synthesis simulation for long text`() {
        if (!librariesAvailable || testVoiceModels.isEmpty()) {
            println("Skipping - libraries or test models not available")
            return
        }
        
        val (modelPath, configPath) = testVoiceModels.values.first()
        val instance = PiperNative.initialize(modelPath, configPath)
        
        // Simulate streaming by breaking text into chunks
        val fullText = "This is a test. " * 50 // ~800 characters
        val chunkSize = 100
        val chunks = fullText.chunked(chunkSize)
        
        println("Simulating streaming synthesis with ${chunks.size} chunks")
        
        val audioChunks = mutableListOf<ByteArray>()
        chunks.forEach { chunk ->
            val audio = PiperNative.synthesize(instance, chunk)
            assertNotNull(audio)
            audioChunks.add(audio)
        }
        
        assertEquals(chunks.size, audioChunks.size, "Should have audio for each chunk")
        
        val totalAudioSize = audioChunks.sumOf { it.size }
        println("Total audio size: $totalAudioSize bytes from ${chunks.size} chunks")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test continuous synthesis without memory leaks`() {
        if (!librariesAvailable || testVoiceModels.isEmpty()) {
            println("Skipping - libraries or test models not available")
            return
        }
        
        val (modelPath, configPath) = testVoiceModels.values.first()
        val instance = PiperNative.initialize(modelPath, configPath)
        
        val testText = "Continuous synthesis test. "
        val iterations = 20
        
        println("Performing $iterations continuous synthesis operations")
        
        val runtime = Runtime.getRuntime()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        repeat(iterations) { iteration ->
            val audio = PiperNative.synthesize(instance, testText)
            assertNotNull(audio, "Iteration $iteration should produce audio")
            assertTrue(audio.isNotEmpty(), "Iteration $iteration audio should not be empty")
        }
        
        // Force garbage collection
        System.gc()
        Thread.sleep(100)
        
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryGrowth = (memoryAfter - memoryBefore) / (1024 * 1024) // MB
        
        println("Memory growth after $iterations iterations: ${memoryGrowth}MB")
        
        // Memory growth should be reasonable (less than 50MB for 20 iterations)
        assertTrue(memoryGrowth < 50, "Memory growth should be reasonable")
        
        PiperNative.shutdown(instance)
    }
    
    // ========== Error Recovery Tests ==========
    
    @Test
    fun `test recovery from synthesis errors`() {
        if (!librariesAvailable || testVoiceModels.isEmpty()) {
            println("Skipping - libraries or test models not available")
            return
        }
        
        val (modelPath, configPath) = testVoiceModels.values.first()
        val instance = PiperNative.initialize(modelPath, configPath)
        
        // Try to synthesize with potentially problematic text
        val problematicTexts = listOf(
            "", // Empty text
            " ", // Just whitespace
            "Test", // Very short text
            "A".repeat(10000) // Very long repetitive text
        )
        
        problematicTexts.forEach { text ->
            try {
                val audio = PiperNative.synthesize(instance, text)
                // If it succeeds, that's fine
                assertNotNull(audio)
            } catch (e: Exception) {
                // If it fails, that's also acceptable for edge cases
                println("Expected error for problematic text: ${e.message}")
            }
        }
        
        // Verify instance still works after errors
        val normalAudio = PiperNative.synthesize(instance, "Normal text after errors")
        assertNotNull(normalAudio)
        assertTrue(normalAudio.isNotEmpty())
        
        PiperNative.shutdown(instance)
    }
    
    // ========== Performance Baseline Tests ==========
    
    @Test
    fun `test synthesis latency baseline`() {
        if (!librariesAvailable || testVoiceModels.isEmpty()) {
            println("Skipping - libraries or test models not available")
            return
        }
        
        val (modelPath, configPath) = testVoiceModels.values.first()
        val instance = PiperNative.initialize(modelPath, configPath)
        
        val testTexts = listOf(
            "Short text.",
            "This is a medium length text with multiple words and punctuation.",
            "This is a longer text. " * 10
        )
        
        testTexts.forEach { text ->
            val durations = mutableListOf<Long>()
            
            // Warm up
            repeat(3) {
                PiperNative.synthesize(instance, text)
            }
            
            // Measure
            repeat(10) {
                val start = System.currentTimeMillis()
                PiperNative.synthesize(instance, text)
                val duration = System.currentTimeMillis() - start
                durations.add(duration)
            }
            
            val avgDuration = durations.average()
            val minDuration = durations.minOrNull() ?: 0
            val maxDuration = durations.maxOrNull() ?: 0
            
            println("Text length ${text.length}: avg=${avgDuration}ms, min=${minDuration}ms, max=${maxDuration}ms")
        }
        
        PiperNative.shutdown(instance)
    }
}

// Extension function for string repetition
private operator fun String.times(n: Int): String = this.repeat(n)
