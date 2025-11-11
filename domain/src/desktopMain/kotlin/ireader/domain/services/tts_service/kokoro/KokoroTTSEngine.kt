package ireader.domain.services.tts_service.kokoro

import ireader.core.log.Log
import ireader.core.storage.AppDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Kokoro TTS Engine - Subprocess-based implementation
 * 
 * Kokoro is a high-quality neural TTS engine that runs as a separate process.
 * This implementation manages the Kokoro subprocess and handles audio generation.
 * 
 * GitHub: https://github.com/hexgrad/kokoro
 * 
 * Features:
 * - High-quality neural voices
 * - Multiple language support
 * - Fast inference
 * - No JNI compilation required
 */
class KokoroTTSEngine(
    private val appDataDir: File = AppDir,
    private var maxConcurrentProcesses: Int = 2
) {
    private val kokoroDir = File(appDataDir, "kokoro").apply { mkdirs() }
    private val modelsDir = File(kokoroDir, "models").apply { mkdirs() }
    private val tempDir = File(kokoroDir, "temp").apply { mkdirs() }
    
    private var isInitialized = false
    private var pythonExecutable: String? = null
    private var kokoroScriptPath: String? = null
    
    // Process pool to limit concurrent Python processes
    private val activeProcesses = mutableSetOf<Process>()
    private val processLock = Any()
    
    companion object {
        private const val KOKORO_REPO = "https://github.com/hexgrad/kokoro.git"
        private const val TIMEOUT_SECONDS = 10000L // 2 minutes for normal synthesis
        private const val FIRST_RUN_TIMEOUT_SECONDS = 60000L // 10 minutes for model download on first run
    }
    
    /**
     * Initialize Kokoro TTS engine
     * 
     * Steps:
     * 1. Check for Python installation
     * 2. Clone/update Kokoro repository
     * 3. Install dependencies
     * 4. Verify installation
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.info { "Initializing Kokoro TTS engine..." }
            
            // Step 1: Find Python
            pythonExecutable = findPythonExecutable()
            if (pythonExecutable == null) {
                return@withContext Result.failure(
                    KokoroException("Compatible Python not found. Kokoro requires Python 3.8-3.12 (not 3.13+). Please install Python 3.12 from python.org")
                )
            }
            
            Log.info { "Found compatible Python: $pythonExecutable" }
            
            // Step 2: Setup Kokoro
            val kokoroPath = File(kokoroDir, "kokoro-tts")
            
            // Check if repository exists and is valid
            if (kokoroPath.exists()) {
                Log.info { "Kokoro repository already exists at: ${kokoroPath.absolutePath}" }
                
                // List contents for debugging
                val contents = kokoroPath.listFiles()?.map { it.name } ?: emptyList()
                Log.info { "Repository contents: $contents" }
                
                // Check if it's a valid git repository
                val gitDir = File(kokoroPath, ".git")
                if (!gitDir.exists() || contents.isEmpty()) {
                    Log.warn { "Repository appears incomplete or corrupted, deleting and re-cloning..." }
                    kokoroPath.deleteRecursively()
                    cloneKokoroRepo(kokoroPath)
                }
            } else {
                Log.info { "Cloning Kokoro repository..." }
                cloneKokoroRepo(kokoroPath)
            }
            
            // Kokoro is a Python package, use -m kokoro to run it
            val kokoroModuleDir = File(kokoroPath, "kokoro")
            val mainFile = File(kokoroModuleDir, "__main__.py")
            
            if (!kokoroModuleDir.exists() || !mainFile.exists()) {
                Log.error { "Kokoro module not found in repository" }
                Log.error { "Expected: ${kokoroModuleDir.absolutePath}" }
                return@withContext Result.failure(
                    KokoroException("Kokoro module not found in repository")
                )
            }
            
            // Store the repository path (we'll use python -m kokoro)
            kokoroScriptPath = kokoroPath.absolutePath
            Log.info { "Found Kokoro module at: ${kokoroModuleDir.absolutePath}" }
            
            // Step 3: Install dependencies
            Log.info { "Installing Kokoro dependencies..." }
            installDependencies(kokoroPath)
            
            // Step 4: Verify installation
            if (!verifyInstallation()) {
                return@withContext Result.failure(
                    KokoroException("Kokoro installation verification failed")
                )
            }
            
            isInitialized = true
            Log.info { "Kokoro TTS engine initialized successfully" }
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.error { "Failed to initialize Kokoro: ${e.message}" }
            Result.failure(KokoroException("Initialization failed: ${e.message}", e))
        }
    }
    
    /**
     * Synthesize text to audio using Kokoro
     * 
     * @param text Text to synthesize
     * @param voice Voice model to use (e.g., "af_bella", "af_sarah")
     * @param speed Speech speed (0.5 to 2.0)
     * @return Audio data as ByteArray (WAV format)
     */
    suspend fun synthesize(
        text: String,
        voice: String = "af_bella",
        speed: Float = 1.0f
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized) {
                return@withContext Result.failure(
                    KokoroException("Kokoro not initialized. Call initialize() first.")
                )
            }
            
            // For long text, split into chunks and process in parallel
            val maxCharsPerChunk = 800 // Kokoro works best with shorter chunks
            
            if (text.length > maxCharsPerChunk && maxConcurrentProcesses > 1) {
                Log.info { "Text length ${text.length} exceeds chunk size, splitting into parallel chunks..." }
                return@withContext synthesizeParallel(text, voice, speed, maxCharsPerChunk)
            }
            
            // For short text, use single process
            return@withContext synthesizeSingle(text, voice, speed)
            
        } catch (e: Exception) {
            Log.error { "Synthesis error: ${e.message}" }
            Result.failure(KokoroException("Synthesis failed: ${e.message}", e))
        }
    }
    
    /**
     * Synthesize text in parallel chunks for faster processing
     */
    private suspend fun synthesizeParallel(
        text: String,
        voice: String,
        speed: Float,
        maxCharsPerChunk: Int
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            // Split text into sentences to avoid cutting mid-sentence
            val sentences = text.split(Regex("(?<=[.!?])\\s+"))
            
            // Group sentences into chunks
            val chunks = mutableListOf<String>()
            var currentChunk = StringBuilder()
            
            sentences.forEach { sentence ->
                if (currentChunk.length + sentence.length > maxCharsPerChunk && currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString())
                    currentChunk = StringBuilder()
                }
                if (currentChunk.isNotEmpty()) {
                    currentChunk.append(" ")
                }
                currentChunk.append(sentence)
            }
            if (currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.toString())
            }
            
            Log.info { "Split text into ${chunks.size} chunks for parallel synthesis (max ${maxConcurrentProcesses} concurrent)" }
            
            // Process chunks in parallel batches
            val audioChunks = mutableListOf<ByteArray>()
            
            chunks.chunked(maxConcurrentProcesses).forEach { batch ->
                Log.debug { "Processing batch of ${batch.size} chunks in parallel..." }
                
                val results = coroutineScope {
                    batch.map { chunkText ->
                        this.async {
                            synthesizeSingle(chunkText, voice, speed)
                        }
                    }.map { it.await() }
                }
                
                // Check for failures
                results.forEach { result ->
                    result.onSuccess { audioData ->
                        audioChunks.add(audioData)
                    }.onFailure { error ->
                        return@withContext Result.failure(error)
                    }
                }
            }
            
            Log.info { "Parallel synthesis completed, merging ${audioChunks.size} audio chunks..." }
            
            // Merge audio chunks (simple concatenation for now)
            val mergedAudio = audioChunks.reduce { acc, bytes -> acc + bytes }
            
            Result.success(mergedAudio)
            
        } catch (e: Exception) {
            Log.error { "Parallel synthesis error: ${e.message}" }
            Result.failure(KokoroException("Parallel synthesis failed: ${e.message}", e))
        }
    }
    
    /**
     * Synthesize a single text chunk
     */
    private suspend fun synthesizeSingle(
        text: String,
        voice: String,
        speed: Float
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        var process: Process? = null
        var inputFile: File? = null
        var outputFile: File? = null
        
        try {
            if (!isInitialized) {
                return@withContext Result.failure(
                    KokoroException("Kokoro not initialized. Call initialize() first.")
                )
            }
            
            // Wait if too many processes are running
            waitForAvailableSlot()
            
            Log.debug { "Synthesizing with Kokoro: voice=$voice, speed=$speed, text length=${text.length}" }
            
            // Create temporary files for input and output
            val timestamp = System.currentTimeMillis()
            val threadId = Thread.currentThread().id
            inputFile = File(tempDir, "input_${timestamp}_${threadId}.txt")
            outputFile = File(tempDir, "output_${timestamp}_${threadId}.wav")
            
            // Write text to input file with UTF-8 BOM to ensure proper encoding on Windows
            // The BOM (0xEF, 0xBB, 0xBF) tells Python to read the file as UTF-8
            inputFile.outputStream().use { out ->
                out.write(0xEF) // UTF-8 BOM byte 1
                out.write(0xBB) // UTF-8 BOM byte 2
                out.write(0xBF) // UTF-8 BOM byte 3
                out.write(text.toByteArray(Charsets.UTF_8))
            }
            
            // Build command - use python -m kokoro with input file
            // Note: Kokoro uses short flags: -i for input file, -m for voice, -s for speed, -o for output
            val command = buildList {
                add(pythonExecutable!!)
                add("-m")
                add("kokoro")
                add("-m")
                add(voice)
                add("-i")
                add(inputFile.absolutePath)
                add("-s")
                add(speed.toString())
                add("-o")
                add(outputFile.absolutePath)
            }
            
            Log.debug { "Executing Kokoro synthesis (${activeProcesses.size} active processes)" }
            
            // Execute Kokoro from the repository directory
            process = ProcessBuilder(command)
                .directory(File(kokoroScriptPath!!))
                .redirectErrorStream(true)
                .start()
            
            // Register process
            registerProcess(process)
            
            // Check if this might be first run (model needs to download)
            // Kokoro models are cached in HuggingFace cache directory
            val userHome = System.getProperty("user.home")
            val modelCacheDirs = listOf(
                File(userHome, ".cache/huggingface/hub"),
                File(userHome, ".cache/torch/hub"),
                File(System.getenv("HF_HOME") ?: "", "hub")
            )
            
            val hasModelCache = modelCacheDirs.any { dir ->
                dir.exists() && dir.listFiles()?.any { 
                    it.name.contains("kokoro", ignoreCase = true) || 
                    it.name.contains("hexgrad", ignoreCase = true)
                } == true
            }
            
            val timeout = if (!hasModelCache) {
                Log.info { "First run detected - using extended timeout for model download (${FIRST_RUN_TIMEOUT_SECONDS}s)" }
                Log.info { "Kokoro will download ~327 MB model on first use. This may take 5-10 minutes..." }
                FIRST_RUN_TIMEOUT_SECONDS
            } else {
                TIMEOUT_SECONDS
            }
            
            // Wait for completion with timeout
            val completed = process.waitFor(timeout, TimeUnit.SECONDS)
            
            // Unregister process
            unregisterProcess(process)
            
            if (!completed) {
                process.destroyForcibly()
                return@withContext Result.failure(
                    KokoroException("Synthesis timeout after ${timeout}s. If this is first run, the model download may have failed.")
                )
            }
            
            val exitCode = process.exitValue()
            if (exitCode != 0) {
                // Read error output only after process completes
                val error = try {
                    process.inputStream.bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    "Unable to read error output: ${e.message}"
                }
                Log.error { "Kokoro synthesis failed with exit code $exitCode" }
                Log.error { "Error output: $error" }
                return@withContext Result.failure(
                    KokoroException("Synthesis failed with exit code $exitCode: $error")
                )
            }
            
            // Read generated audio
            if (!outputFile.exists() || outputFile.length() == 0L) {
                return@withContext Result.failure(
                    KokoroException("Output file not generated or empty")
                )
            }
            
            val audioData = outputFile.readBytes()
            
            // Cleanup temporary files
            inputFile?.delete()
            outputFile?.delete()
            
            Log.debug { "Synthesis completed: ${audioData.size} bytes" }
            Result.success(audioData)
            
        } catch (e: Exception) {
            Log.error { "Synthesis error: ${e.message}" }
            // Make sure to unregister process on error
            process?.let { unregisterProcess(it) }
            // Cleanup temporary files on error
            inputFile?.delete()
            outputFile?.delete()
            Result.failure(KokoroException("Synthesis failed: ${e.message}", e))
        }
    }
    
    /**
     * Wait for an available process slot
     */
    private suspend fun waitForAvailableSlot() {
        while (true) {
            synchronized(processLock) {
                if (activeProcesses.size < maxConcurrentProcesses) {
                    return
                }
            }
            // Wait a bit before checking again
            kotlinx.coroutines.delay(100)
        }
    }
    
    /**
     * Register a process as active
     */
    private fun registerProcess(process: Process) {
        synchronized(processLock) {
            activeProcesses.add(process)
            Log.debug { "Registered process (${activeProcesses.size}/$maxConcurrentProcesses active)" }
        }
    }
    
    /**
     * Unregister a process
     */
    private fun unregisterProcess(process: Process) {
        synchronized(processLock) {
            activeProcesses.remove(process)
            Log.debug { "Unregistered process (${activeProcesses.size}/$maxConcurrentProcesses active)" }
        }
    }
    
    /**
     * Kill all active processes
     */
    private fun killAllProcesses() {
        synchronized(processLock) {
            Log.info { "Killing ${activeProcesses.size} active Kokoro processes..." }
            activeProcesses.forEach { process ->
                try {
                    if (process.isAlive) {
                        process.destroyForcibly()
                    }
                } catch (e: Exception) {
                    Log.error { "Failed to kill process: ${e.message}" }
                }
            }
            activeProcesses.clear()
        }
    }
    
    /**
     * Get list of available Kokoro voices
     */
    fun getAvailableVoices(): List<KokoroVoice> {
        return listOf(
            KokoroVoice("af_bella", "Bella", "American", "Female", "Warm and friendly"),
            KokoroVoice("af_sarah", "Sarah", "American", "Female", "Professional and clear"),
            KokoroVoice("am_adam", "Adam", "American", "Male", "Deep and authoritative"),
            KokoroVoice("am_michael", "Michael", "American", "Male", "Casual and natural"),
            KokoroVoice("bf_emma", "Emma", "British", "Female", "Elegant and refined"),
            KokoroVoice("bf_isabella", "Isabella", "British", "Female", "Sophisticated"),
            KokoroVoice("bm_george", "George", "British", "Male", "Distinguished"),
            KokoroVoice("bm_lewis", "Lewis", "British", "Male", "Articulate and clear"),
        )
    }
    
    /**
     * Check if Kokoro is available and ready
     */
    fun isAvailable(): Boolean {
        return isInitialized && pythonExecutable != null && kokoroScriptPath != null
    }
    
    /**
     * Get number of active processes
     */
    fun getActiveProcessCount(): Int {
        synchronized(processLock) {
            return activeProcesses.size
        }
    }
    
    /**
     * Update maximum concurrent processes
     * @param max New maximum (1-8)
     */
    fun setMaxConcurrentProcesses(max: Int) {
        maxConcurrentProcesses = max.coerceIn(1, 8)
        Log.info { "Max concurrent processes updated to: $maxConcurrentProcesses" }
    }
    
    /**
     * Get current max concurrent processes setting
     */
    fun getMaxConcurrentProcesses(): Int {
        return maxConcurrentProcesses
    }
    
    /**
     * Find Python executable on the system
     * Kokoro requires Python 3.8 to 3.12 (not 3.13+)
     */
    private fun findPythonExecutable(): String? {
        // First, try specific Python installations in AppData
        val userProfile = System.getenv("USERPROFILE") ?: System.getProperty("user.home")
        val pythonBaseDir = File(userProfile, "AppData/Local/Programs/Python")
        
        if (pythonBaseDir.exists()) {
            // Try Python 3.12, 3.11, 3.10, 3.9, 3.8 in order
            val preferredVersions = listOf("Python312", "Python311", "Python310", "Python39", "Python38")
            
            for (versionDir in preferredVersions) {
                val pythonExe = File(pythonBaseDir, "$versionDir/python.exe")
                if (pythonExe.exists()) {
                    try {
                        val process = ProcessBuilder(pythonExe.absolutePath, "--version")
                            .redirectErrorStream(true)
                            .start()
                        
                        val completed = process.waitFor(5, TimeUnit.SECONDS)
                        if (completed && process.exitValue() == 0) {
                            val versionOutput = process.inputStream.bufferedReader().readText().trim()
                            Log.info { "Found compatible Python: ${pythonExe.absolutePath} ($versionOutput)" }
                            return pythonExe.absolutePath
                        }
                    } catch (e: Exception) {
                        // Try next version
                    }
                }
            }
        }
        
        // Fallback to system PATH
        val candidates = listOf("python3", "python", "py")
        
        for (candidate in candidates) {
            try {
                val process = ProcessBuilder(candidate, "--version")
                    .redirectErrorStream(true)
                    .start()
                
                val completed = process.waitFor(5, TimeUnit.SECONDS)
                if (completed && process.exitValue() == 0) {
                    val versionOutput = process.inputStream.bufferedReader().readText().trim()
                    
                    // Extract version number (e.g., "Python 3.12.0" -> "3.12")
                    val versionMatch = Regex("""Python (\d+)\.(\d+)""").find(versionOutput)
                    if (versionMatch != null) {
                        val major = versionMatch.groupValues[1].toInt()
                        val minor = versionMatch.groupValues[2].toInt()
                        
                        // Kokoro requires Python 3.8 to 3.12
                        if (major == 3 && minor in 8..12) {
                            Log.info { "Found compatible Python: $candidate ($versionOutput)" }
                            return candidate
                        } else if (major == 3 && minor >= 13) {
                            Log.warn { "Found Python $major.$minor but Kokoro requires Python 3.8-3.12: $versionOutput" }
                        }
                    }
                }
            } catch (e: Exception) {
                // Try next candidate
            }
        }
        
        return null
    }
    
    /**
     * Clone Kokoro repository
     */
    private fun cloneKokoroRepo(targetPath: File) {
        try {
            Log.info { "Cloning Kokoro repository to: ${targetPath.absolutePath}" }
            
            val process = ProcessBuilder(
                "git", "clone", "--depth", "1", KOKORO_REPO, targetPath.absolutePath
            ).redirectErrorStream(true).start()
            
            val completed = process.waitFor(120, TimeUnit.SECONDS)
            
            if (!completed) {
                process.destroyForcibly()
                throw KokoroException("Git clone timeout after 120 seconds")
            }
            
            if (process.exitValue() != 0) {
                val output = process.inputStream.bufferedReader().readText()
                Log.error { "Git clone failed: $output" }
                throw KokoroException("Failed to clone Kokoro repository: $output")
            }
            
            Log.info { "Kokoro repository cloned successfully" }
            
        } catch (e: Exception) {
            if (e is KokoroException) throw e
            throw KokoroException("Git clone error: ${e.message}", e)
        }
    }
    
    /**
     * Install Python dependencies
     */
    private fun installDependencies(kokoroPath: File) {
        try {
            // Kokoro uses pyproject.toml for dependency management
            val pyprojectFile = File(kokoroPath, "pyproject.toml")
            
            if (!pyprojectFile.exists()) {
                Log.warn { "pyproject.toml not found, skipping dependency installation" }
                return
            }
            
            Log.info { "Installing Kokoro dependencies (this may take several minutes and download ~2-3 GB)..." }
            Log.info { "Dependencies: PyTorch, Transformers, NumPy, Hugging Face Hub, Loguru, Misaki" }
            
            // Install the package in editable mode with pip
            val process = ProcessBuilder(
                pythonExecutable!!,
                "-m", "pip", "install", "-e", kokoroPath.absolutePath, "--verbose"
            ).directory(kokoroPath)
                .redirectErrorStream(true)
                .start()
            
            // Stream output in real-time
            val reader = process.inputStream.bufferedReader()
            var line: String?
            var lastLogTime = System.currentTimeMillis()
            
            while (reader.readLine().also { line = it } != null) {
                val currentTime = System.currentTimeMillis()
                
                // Log important lines or every 2 seconds to avoid spam
                if (line!!.contains("Collecting") || 
                    line!!.contains("Downloading") || 
                    line!!.contains("Installing") ||
                    line!!.contains("Successfully") ||
                    line!!.contains("ERROR") ||
                    line!!.contains("WARNING") ||
                    (currentTime - lastLogTime > 2000)) {
                    
                    Log.info { "pip: $line" }
                    lastLogTime = currentTime
                }
            }
            
            val completed = process.waitFor(300, TimeUnit.SECONDS) // Increased to 5 minutes
            
            if (!completed) {
                process.destroyForcibly()
                Log.warn { "Dependency installation timeout after 300 seconds" }
            } else if (process.exitValue() != 0) {
                Log.error { "Dependency installation failed with exit code: ${process.exitValue()}" }
            } else {
                Log.info { "✓ Dependencies installed successfully!" }
            }
        } catch (e: Exception) {
            Log.warn { "Error installing dependencies: ${e.message}" }
        }
    }
    
    /**
     * Verify Kokoro installation
     */
    private fun verifyInstallation(): Boolean {
        try {
            val repoDir = File(kokoroScriptPath!!)
            if (!repoDir.exists()) {
                Log.error { "Kokoro repository not found: ${repoDir.absolutePath}" }
                return false
            }
            
            // Try to run with --help using python -m kokoro
            val process = ProcessBuilder(
                pythonExecutable!!,
                "-m",
                "kokoro",
                "--help"
            ).directory(repoDir)
                .redirectErrorStream(true)
                .start()
            
            // Read output first (before checking exit code)
            val output = process.inputStream.bufferedReader().readText()
            
            val completed = process.waitFor(10, TimeUnit.SECONDS)
            
            if (completed && process.exitValue() == 0) {
                // Check if output contains expected help text
                if (output.contains("--voice") && output.contains("--output-file")) {
                    Log.info { "✓ Kokoro verification successful - CLI is working" }
                    return true
                } else {
                    Log.warn { "Kokoro help output unexpected: $output" }
                    return false
                }
            } else {
                Log.error { "Kokoro verification failed with exit code ${process.exitValue()}: $output" }
                return false
            }
            
        } catch (e: Exception) {
            Log.error { "Verification failed: ${e.message}" }
            return false
        }
    }
    
    /**
     * Find kokoro.py script recursively
     */
    private fun findKokoroScript(directory: File, maxDepth: Int = 3): File? {
        fun searchRecursive(dir: File, depth: Int): File? {
            if (depth > maxDepth) return null
            
            // Check current directory
            val scriptFile = File(dir, "kokoro.py")
            if (scriptFile.exists()) return scriptFile
            
            // Search subdirectories
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    val found = searchRecursive(file, depth + 1)
                    if (found != null) return found
                }
            }
            
            return null
        }
        
        return searchRecursive(directory, 0)
    }
    
    /**
     * List directory structure for debugging
     */
    private fun listDirectoryStructure(directory: File, depth: Int, maxDepth: Int) {
        if (depth > maxDepth) return
        
        val indent = "  ".repeat(depth)
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                Log.info { "$indent[DIR] ${file.name}" }
                listDirectoryStructure(file, depth + 1, maxDepth)
            } else {
                Log.info { "$indent${file.name}" }
            }
        }
    }
    
    /**
     * Shutdown and cleanup
     */
    fun shutdown() {
        // Kill all active processes first
        killAllProcesses()
        
        // Cleanup temp files
        tempDir.listFiles()?.forEach { it.delete() }
        
        Log.info { "Kokoro TTS engine shutdown" }
    }
}

/**
 * Kokoro voice model information
 */
data class KokoroVoice(
    val id: String,
    val name: String,
    val accent: String,
    val gender: String,
    val description: String
)

/**
 * Kokoro-specific exception
 */
class KokoroException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
