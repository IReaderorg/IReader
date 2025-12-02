package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readBytes

/**
 * Piper TTS synthesizer using subprocess approach.
 * Calls piper.exe as an external process instead of using JNI.
 * 
 * This is simpler and more reliable than JNI integration.
 */
class PiperSubprocessSynthesizer {
    
    private var piperExecutable: File? = null
    private var currentModelPath: String? = null
    private var currentConfigPath: String? = null
    private var speechRate: Float = 1.0f
    
    /**
     * Initialize with voice model paths
     */
    suspend fun initialize(modelPath: String, configPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Find piper.exe
            piperExecutable = findPiperExecutable()
            if (piperExecutable == null || !piperExecutable!!.exists()) {
                throw IllegalStateException("piper.exe not found")
            }
            
            // Verify model files exist
            val modelFile = File(modelPath)
            val configFile = File(configPath)
            
            if (!modelFile.exists()) {
                throw IllegalArgumentException("Model file not found: $modelPath")
            }
            
            if (!configFile.exists()) {
                throw IllegalArgumentException("Config file not found: $configPath")
            }
            
            currentModelPath = modelPath
            currentConfigPath = configPath
            
            true
        } catch (_: Exception) {
            false
        }
    }
    
    /**
     * Synthesize text to audio using piper.exe subprocess
     */
    suspend fun synthesize(text: String): ByteArray = withContext(Dispatchers.IO) {
        if (piperExecutable == null || currentModelPath == null) {
            throw IllegalStateException("Piper not initialized")
        }
        
        if (text.isBlank()) {
            return@withContext ByteArray(0)
        }
        
        // Create temporary files
        val tempDir = Files.createTempDirectory("piper_tts")
        val inputFile = tempDir.resolve("input.txt").toFile()
        val outputFile = tempDir.resolve("output.wav")
        
        try {
            // Write input text
            inputFile.writeText(text)
            
            // Build command
            val command = buildList {
                add(piperExecutable!!.absolutePath)
                add("--model")
                add(currentModelPath!!)
                add("--config")
                add(currentConfigPath!!)
                add("--output_file")
                add(outputFile.toString())
                
                // Add espeak-ng data directory
                val espeakDataDir = piperExecutable!!.parentFile.resolve("espeak-ng-data")
                if (espeakDataDir.exists()) {
                    add("--espeak_data")
                    add(espeakDataDir.absolutePath)
                }
                
                // Add speech rate if not default
                if (speechRate != 1.0f) {
                    add("--length_scale")
                    add((1.0f / speechRate).toString())
                }
            }
            
            // Execute piper with working directory set to where the DLLs are
            val workingDir = piperExecutable!!.parentFile
            val process = ProcessBuilder(command)
                .directory(workingDir)  // Set working directory to where piper.exe and DLLs are
                .redirectInput(inputFile)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            
            // Wait for completion (with timeout)
            val completed = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS)
            
            if (!completed) {
                process.destroyForcibly()
                throw RuntimeException("Piper process timed out")
            }
            
            val exitCode = process.exitValue()
            if (exitCode != 0) {
                val error = process.errorStream.bufferedReader().readText()
                throw RuntimeException("Piper failed with exit code $exitCode: $error")
            }
            
            // Read output WAV file
            if (!outputFile.exists()) {
                throw RuntimeException("Piper did not create output file")
            }
            
            val audioData = outputFile.readBytes()
            
            // Convert WAV to raw PCM (skip 44-byte WAV header)
            if (audioData.size > 44) {
                audioData.copyOfRange(44, audioData.size)
            } else {
                ByteArray(0)
            }
            
        } finally {
            // Cleanup temp files
            inputFile.delete()
            if (outputFile.exists()) {
                outputFile.toFile().delete()
            }
            tempDir.toFile().deleteRecursively()
        }
    }
    
    /**
     * Set speech rate (0.5 = slower, 2.0 = faster)
     */
    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
    }
    
    /**
     * Get sample rate (Piper outputs 22050 Hz)
     */
    fun getSampleRate(): Int = 22050
    
    /**
     * Shutdown (cleanup resources)
     */
    fun shutdown() {
        piperExecutable = null
        currentModelPath = null
        currentConfigPath = null
    }
    
    /**
     * Find piper.exe in resources
     */
    private fun findPiperExecutable(): File? {
        // Try to extract from resources to a persistent directory with all DLLs
        val resourcePath = when {
            System.getProperty("os.name").contains("Windows", ignoreCase = true) -> 
                "/native/windows-x64/piper.exe"
            System.getProperty("os.name").contains("Mac", ignoreCase = true) -> 
                "/native/macos-x64/piper"
            else -> 
                "/native/linux-x64/piper"
        }
        
        val stream = javaClass.getResourceAsStream(resourcePath)
        if (stream != null) {
            // Extract to a persistent directory (not temp) so DLLs can be found
            val piperDir = File(System.getProperty("user.home"), ".ireader/piper")
            piperDir.mkdirs()
            
            val piperFile = File(piperDir, if (resourcePath.endsWith(".exe")) "piper.exe" else "piper")
            
            // Only extract if doesn't exist or is different
            if (!piperFile.exists() || piperFile.length() == 0L) {
                stream.use { input ->
                    piperFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            
            // Make executable on Unix
            if (!System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
                piperFile.setExecutable(true)
            }
            
            // Also extract DLLs to the same directory
            extractDependencies(piperDir)
            
            return piperFile
        }
        
        return null
    }
    
    /**
     * Extract required DLLs to the piper directory
     */
    private fun extractDependencies(piperDir: File) {
        val dependencies = when {
            System.getProperty("os.name").contains("Windows", ignoreCase = true) -> listOf(
                "onnxruntime.dll",
                "onnxruntime_providers_shared.dll",
                "espeak-ng.dll",
                "piper_phonemize.dll"
            )
            else -> emptyList()
        }
        
        dependencies.forEach { dll ->
            val resourcePath = "/native/windows-x64/$dll"
            val stream = javaClass.getResourceAsStream(resourcePath)
            if (stream != null) {
                val dllFile = File(piperDir, dll)
                if (!dllFile.exists() || dllFile.length() == 0L) {
                    stream.use { input ->
                        dllFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
        
        // Also extract espeak-ng-data directory
        extractEspeakData(piperDir)
    }
    
    /**
     * Extract espeak-ng-data directory
     */
    private fun extractEspeakData(piperDir: File) {
        // For now, just create a marker that espeak-ng-data should be extracted
        // This is complex as it's a directory structure
        // We'll handle this if needed
    }
}
