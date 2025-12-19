package ireader.domain.services.tts_service.piper

import ireader.core.log.Log
import ireader.domain.plugins.PluginClassLoader
import ireader.domain.plugins.getNativePlatformIdImpl
import ireader.domain.plugins.getPluginPackagePathImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

/**
 * Piper TTS synthesizer that uses the standalone Piper executable from the plugin.
 * 
 * This implementation runs piper as a subprocess, which is more reliable than JNI
 * and doesn't require Visual C++ Redistributable or other system dependencies.
 */
class PiperJNISynthesizer {
    
    companion object {
        private const val PIPER_PLUGIN_ID = "io.github.ireaderorg.plugins.piper-tts"
        
        // Cached state
        private var piperReady = false
        private var initAttempted = false
        private var piperExecutable: File? = null
        private var piperDir: File? = null
        
        /**
         * Check if Piper is ready to use.
         */
        fun isPiperReady(): Boolean = piperReady
        
        /**
         * Try to initialize Piper from the plugin.
         */
        @Synchronized
        fun tryInitializePiper(): Boolean {
            if (piperReady) return true
            if (initAttempted) return false
            
            Log.info { "PiperJNISynthesizer: Attempting to initialize Piper from plugin..." }
            
            try {
                // Check if plugin is registered
                val packagePath = getPluginPackagePathImpl(PIPER_PLUGIN_ID)
                if (packagePath == null) {
                    Log.info { "PiperJNISynthesizer: Plugin package path not registered" }
                    return false
                }
                
                val packageFile = File(packagePath)
                if (!packageFile.exists()) {
                    Log.info { "PiperJNISynthesizer: Plugin package not found: $packagePath" }
                    return false
                }
                
                // Extract Piper executable and dependencies
                val extractedDir = extractPiperFiles(packageFile)
                if (extractedDir == null) {
                    Log.error { "PiperJNISynthesizer: Failed to extract Piper files" }
                    initAttempted = true
                    return false
                }
                
                piperDir = extractedDir
                
                // Find the piper executable
                val platformId = getNativePlatformIdImpl()
                val exeName = if (platformId.startsWith("windows")) "piper.exe" else "piper"
                val executable = File(extractedDir, exeName)
                
                if (!executable.exists()) {
                    Log.error { "PiperJNISynthesizer: Piper executable not found: ${executable.absolutePath}" }
                    initAttempted = true
                    return false
                }
                
                // Make executable on Unix
                if (!platformId.startsWith("windows")) {
                    executable.setExecutable(true)
                }
                
                piperExecutable = executable
                piperReady = true
                initAttempted = true
                Log.info { "PiperJNISynthesizer: Piper initialized successfully at ${executable.absolutePath}" }
                return true
                
            } catch (e: Exception) {
                Log.error { "PiperJNISynthesizer: Failed to initialize: ${e.message}" }
                e.printStackTrace()
                initAttempted = true
                return false
            }
        }
        
        /**
         * Extract Piper files from the plugin package.
         */
        private fun extractPiperFiles(packageFile: File): File? {
            val platformId = getNativePlatformIdImpl()
            val userHome = System.getProperty("user.home")
            val extractDir = File(userHome, ".ireader/plugins/$PIPER_PLUGIN_ID/piper/$platformId")
            
            if (!extractDir.exists()) {
                extractDir.mkdirs()
            }
            
            Log.info { "PiperJNISynthesizer: Extracting Piper files for $platformId to ${extractDir.absolutePath}" }
            
            try {
                ZipFile(packageFile).use { zip ->
                    val prefix = "native/$platformId/"
                    zip.entries().asSequence()
                        .filter { it.name.startsWith(prefix) && !it.isDirectory }
                        .forEach { entry ->
                            // Preserve directory structure within the platform folder
                            val relativePath = entry.name.removePrefix(prefix)
                            val outputFile = File(extractDir, relativePath)
                            
                            // Create parent directories if needed
                            outputFile.parentFile?.mkdirs()
                            
                            // Only extract if not exists or different size
                            if (!outputFile.exists() || outputFile.length() != entry.size) {
                                zip.getInputStream(entry).use { input ->
                                    outputFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                Log.info { "PiperJNISynthesizer: Extracted: $relativePath" }
                            }
                        }
                }
                
                return extractDir
                
            } catch (e: Exception) {
                Log.error { "PiperJNISynthesizer: Failed to extract Piper files: ${e.message}" }
                return null
            }
        }
        
        /**
         * Reset the initialization state.
         */
        fun reset() {
            piperReady = false
            initAttempted = false
            piperExecutable = null
            piperDir = null
        }
    }

    
    // Instance state
    private var modelPath: String? = null
    private var configPath: String? = null
    private var sampleRate: Int = 22050
    private var speechRate: Float = 1.0f
    private var initialized: Boolean = false
    
    /**
     * Initialize Piper with a voice model.
     */
    suspend fun initialize(modelPath: String, configPath: String): Boolean {
        // First, try to initialize the Piper executable from plugin
        if (!piperReady && !tryInitializePiper()) {
            Log.warn { 
                "Piper TTS is not available. Please install the 'Piper TTS' plugin " +
                "from the Feature Store to use neural text-to-speech."
            }
            return false
        }
        
        if (!piperReady || piperExecutable == null) {
            Log.warn { "PiperJNISynthesizer: Piper not ready" }
            return false
        }
        
        try {
            Log.info { "PiperJNISynthesizer: Initializing with model: $modelPath" }
            
            // Verify model files exist
            val modelFile = File(modelPath)
            val configFile = File(configPath)
            
            if (!modelFile.exists()) {
                Log.error { "PiperJNISynthesizer: Model file not found: $modelPath" }
                return false
            }
            
            if (!configFile.exists()) {
                Log.error { "PiperJNISynthesizer: Config file not found: $configPath" }
                return false
            }
            
            this.modelPath = modelPath
            this.configPath = configPath
            this.initialized = true
            
            Log.info { "PiperJNISynthesizer: Initialized successfully with model: ${modelFile.name}" }
            return true
            
        } catch (e: Exception) {
            Log.error { "PiperJNISynthesizer: Failed to initialize: ${e.message}" }
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Synthesize text to audio using piper subprocess.
     */
    suspend fun synthesize(text: String): ByteArray = withContext(Dispatchers.IO) {
        if (!initialized || !piperReady || piperExecutable == null) {
            Log.warn { "PiperJNISynthesizer: Not initialized, cannot synthesize" }
            return@withContext ByteArray(0)
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
                add(modelPath!!)
                add("--config")
                add(configPath!!)
                add("--output_file")
                add(outputFile.toString())
                
                // Add espeak-ng data directory if it exists
                val espeakDataDir = piperDir?.resolve("espeak-ng-data")
                if (espeakDataDir?.exists() == true) {
                    add("--espeak_data")
                    add(espeakDataDir.absolutePath)
                }
                
                // Add speech rate if not default
                if (speechRate != 1.0f) {
                    add("--length_scale")
                    add((1.0f / speechRate).toString())
                }
            }
            
            Log.info { "PiperJNISynthesizer: Running command: ${command.joinToString(" ")}" }
            
            // Execute piper with working directory set to where the executable is
            val workingDir = piperExecutable!!.parentFile
            val process = ProcessBuilder(command)
                .directory(workingDir)
                .redirectInput(inputFile)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            
            // Wait for completion (with timeout)
            val completed = process.waitFor(60, TimeUnit.SECONDS)
            
            if (!completed) {
                process.destroyForcibly()
                Log.error { "PiperJNISynthesizer: Process timed out" }
                return@withContext ByteArray(0)
            }
            
            val exitCode = process.exitValue()
            if (exitCode != 0) {
                val error = process.errorStream.bufferedReader().readText()
                Log.error { "PiperJNISynthesizer: Process failed with exit code $exitCode: $error" }
                return@withContext ByteArray(0)
            }
            
            // Read output WAV file
            if (!outputFile.toFile().exists()) {
                Log.error { "PiperJNISynthesizer: Output file not created" }
                return@withContext ByteArray(0)
            }
            
            val audioData = outputFile.toFile().readBytes()
            
            // Convert WAV to raw PCM (skip 44-byte WAV header)
            if (audioData.size > 44) {
                audioData.copyOfRange(44, audioData.size)
            } else {
                ByteArray(0)
            }
            
        } catch (e: Exception) {
            Log.error { "PiperJNISynthesizer: Synthesis failed: ${e.message}" }
            ByteArray(0)
        } finally {
            // Cleanup temp files
            try {
                inputFile.delete()
                outputFile.toFile().delete()
                tempDir.toFile().deleteRecursively()
            } catch (_: Exception) {}
        }
    }
    
    /**
     * Set speech rate (0.5 = slower, 2.0 = faster)
     */
    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
    }
    
    /**
     * Get sample rate of the loaded model
     */
    fun getSampleRate(): Int = sampleRate
    
    /**
     * Check if initialized.
     */
    fun isInitialized(): Boolean = initialized && piperReady
    
    /**
     * Shutdown and release resources.
     */
    fun shutdown() {
        initialized = false
        modelPath = null
        configPath = null
    }
}
