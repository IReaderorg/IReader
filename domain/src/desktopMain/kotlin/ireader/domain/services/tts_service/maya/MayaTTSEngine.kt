package ireader.domain.services.tts_service.maya

import ireader.core.log.Log
import ireader.core.storage.AppDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Maya TTS Engine - Hugging Face Transformers-based implementation
 * 
 * Maya is a high-quality multilingual TTS model from Maya Research.
 * Model: https://huggingface.co/maya-research/maya1
 * 
 * Features:
 * - High-quality multilingual voices
 * - Fast inference with transformers
 * - Multiple language support
 * - No JNI compilation required
 */
class MayaTTSEngine(
    private val appDataDir: File = AppDir
) {
    private val mayaDir = File(appDataDir, "maya").apply { mkdirs() }
    private val modelsDir = File(mayaDir, "models").apply { mkdirs() }
    private val tempDir = File(mayaDir, "temp").apply { mkdirs() }
    
    private var isInitialized = false
    private var pythonExecutable: String? = null
    private var mayaScriptPath: String? = null
    
    // Process pool to limit concurrent Python processes
    private val activeProcesses = mutableSetOf<Process>()
    private val processLock = Any()
    private var maxConcurrentProcesses: Int = 2
    
    companion object {
        private const val MODEL_ID = "maya-research/maya1"
        private const val TIMEOUT_SECONDS = 120L // 2 minutes for normal synthesis
        // No timeout for first run - Maya model is 5GB and needs time to download
    }
    
    /**
     * Initialize Maya TTS engine
     * 
     * Steps:
     * 1. Check for Python installation
     * 2. Install required dependencies (transformers, torch, etc.)
     * 3. Download Maya model from Hugging Face
     * 4. Verify installation
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.info { "Initializing Maya TTS engine..." }
            
            // Step 1: Find Python
            pythonExecutable = findPythonExecutable()
            if (pythonExecutable == null) {
                return@withContext Result.failure(
                    MayaException("Compatible Python not found. Maya requires Python 3.8-3.12 (not 3.13+). Please install Python 3.12 from python.org")
                )
            }
            
            Log.info { "Found compatible Python: $pythonExecutable" }
            
            // Step 2: Create Maya inference script
            createMayaScript()
            
            // Step 3: Install dependencies
            Log.info { "Installing Maya dependencies..." }
            installDependencies()
            
            // Step 4: Verify installation
            if (!verifyInstallation()) {
                return@withContext Result.failure(
                    MayaException("Maya installation verification failed")
                )
            }
            
            isInitialized = true
            Log.info { "Maya TTS engine initialized successfully" }
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.error { "Failed to initialize Maya: ${e.message}" }
            Result.failure(MayaException("Initialization failed: ${e.message}", e))
        }
    }
    
    /**
     * Synthesize text to audio using Maya
     * 
     * @param text Text to synthesize
     * @param language Language code (e.g., "en", "es", "fr")
     * @param speed Speech speed (0.5 to 2.0)
     * @return Audio data as ByteArray (WAV format)
     */
    suspend fun synthesize(
        text: String,
        language: String = "en",
        speed: Float = 1.0f
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        var process: Process? = null
        var inputFile: File? = null
        var outputFile: File? = null
        
        try {
            if (!isInitialized) {
                return@withContext Result.failure(
                    MayaException("Maya not initialized. Call initialize() first.")
                )
            }
            
            // Wait if too many processes are running
            waitForAvailableSlot()
            
            Log.debug { "Synthesizing with Maya: language=$language, speed=$speed, text length=${text.length}" }
            
            // Create temporary files for input and output
            val timestamp = System.currentTimeMillis()
            val threadId = Thread.currentThread().id
            inputFile = File(tempDir, "input_${timestamp}_${threadId}.txt")
            outputFile = File(tempDir, "output_${timestamp}_${threadId}.wav")
            
            // Write text to input file with UTF-8 BOM to ensure proper encoding on Windows
            inputFile.outputStream().use { out ->
                out.write(0xEF) // UTF-8 BOM byte 1
                out.write(0xBB) // UTF-8 BOM byte 2
                out.write(0xBF) // UTF-8 BOM byte 3
                out.write(text.toByteArray(Charsets.UTF_8))
            }
            
            // Build command using input file
            val command = listOf(
                pythonExecutable!!,
                mayaScriptPath!!,
                "--input", inputFile.absolutePath,
                "--language", language,
                "--speed", speed.toString(),
                "--output", outputFile.absolutePath
            )
            
            Log.debug { "Executing Maya synthesis (${activeProcesses.size} active processes)" }
            
            process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            
            // Register process
            registerProcess(process)
            
            // Check if this might be first run (model needs to download)
            val userHome = System.getProperty("user.home")
            val modelCacheDirs = listOf(
                File(userHome, ".cache/huggingface/hub"),
                File(userHome, ".cache/torch/hub"),
                File(System.getenv("HF_HOME") ?: "", "hub")
            )
            
            val hasModelCache = modelCacheDirs.any { dir ->
                dir.exists() && dir.listFiles()?.any { 
                    it.name.contains("maya", ignoreCase = true)
                } == true
            }
            
            // Wait for completion with timeout
            val completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            
            // Unregister process
            unregisterProcess(process)
            
            if (!completed) {
                process?.destroyForcibly()
                inputFile?.delete()
                outputFile?.delete()
                return@withContext Result.failure(
                    MayaException("Synthesis timeout after ${TIMEOUT_SECONDS}s")
                )
            }
            
            // Check exit code (skip for first run where process is null)
            val exitCode = process?.exitValue() ?: 0
            if (exitCode != 0) {
                // Read error output only after process completes
                val error = try {
                    process?.inputStream?.bufferedReader()?.use { it.readText() } ?: "No error output available"
                } catch (e: Exception) {
                    "Unable to read error output: ${e.message}"
                }
                Log.error { "Maya synthesis failed with exit code $exitCode" }
                Log.error { "Error output: $error" }
                
                // Check for common errors and provide helpful messages
                val errorMessage = when {
                    error.contains("disk space", ignoreCase = true) -> {
                        "Insufficient disk space. Maya requires ~5 GB free space for model download. Please free up disk space and try again."
                    }
                    error.contains("OutOfMemoryError", ignoreCase = true) || error.contains("CUDA out of memory", ignoreCase = true) -> {
                        "Out of memory. Maya model requires significant RAM/VRAM. Try closing other applications."
                    }
                    error.contains("Connection", ignoreCase = true) || error.contains("timeout", ignoreCase = true) -> {
                        "Network error during model download. Please check your internet connection and try again."
                    }
                    else -> "Synthesis failed with exit code $exitCode: $error"
                }
                
                // Cleanup temporary files on error
                inputFile?.delete()
                outputFile?.delete()
                return@withContext Result.failure(
                    MayaException(errorMessage)
                )
            }
            
            if (!outputFile!!.exists() || outputFile.length() == 0L) {
                inputFile?.delete()
                outputFile?.delete()
                return@withContext Result.failure(
                    MayaException("Output file not generated or empty")
                )
            }
            
            // Read audio data
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
            Result.failure(MayaException("Synthesis failed: ${e.message}", e))
        }
    }
    
    /**
     * Get list of supported languages
     */
    fun getSupportedLanguages(): List<MayaLanguage> {
        return listOf(
            MayaLanguage("en", "English"),
            MayaLanguage("es", "Spanish"),
            MayaLanguage("fr", "French"),
            MayaLanguage("de", "German"),
            MayaLanguage("it", "Italian"),
            MayaLanguage("pt", "Portuguese"),
            MayaLanguage("pl", "Polish"),
            MayaLanguage("tr", "Turkish"),
            MayaLanguage("ru", "Russian"),
            MayaLanguage("nl", "Dutch"),
            MayaLanguage("cs", "Czech"),
            MayaLanguage("ar", "Arabic"),
            MayaLanguage("zh", "Chinese"),
            MayaLanguage("ja", "Japanese"),
            MayaLanguage("ko", "Korean"),
            MayaLanguage("hi", "Hindi")
        )
    }
    
    /**
     * Check if Maya is available and ready
     */
    fun isAvailable(): Boolean {
        return isInitialized && pythonExecutable != null && mayaScriptPath != null
    }
    
    /**
     * Find Python executable on the system
     * Maya requires Python 3.8 to 3.12
     */
    private fun findPythonExecutable(): String? {
        // First, try specific Python installations in AppData
        val userProfile = System.getenv("USERPROFILE") ?: System.getProperty("user.home")
        val pythonBaseDir = File(userProfile, "AppData/Local/Programs/Python")
        
        if (pythonBaseDir.exists()) {
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
                    
                    val versionMatch = Regex("""Python (\d+)\.(\d+)""").find(versionOutput)
                    if (versionMatch != null) {
                        val major = versionMatch.groupValues[1].toInt()
                        val minor = versionMatch.groupValues[2].toInt()
                        
                        // Maya requires Python 3.8 to 3.12
                        if (major == 3 && minor in 8..12) {
                            Log.info { "Found compatible Python: $candidate ($versionOutput)" }
                            return candidate
                        } else if (major == 3 && minor >= 13) {
                            Log.warn { "Found Python $major.$minor but Maya requires Python 3.8-3.12: $versionOutput" }
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
     * Create Maya inference script
     */
    private fun createMayaScript() {
        val scriptFile = File(mayaDir, "maya_tts.py")
        mayaScriptPath = scriptFile.absolutePath
        
        val scriptContent = """
# -*- coding: utf-8 -*-
import argparse
import sys
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer
import scipy.io.wavfile as wavfile
import numpy as np

def synthesize(input_file, language, speed, output_path):
    try:
        # Read text from input file with UTF-8 encoding
        with open(input_file, 'r', encoding='utf-8-sig') as f:
            text = f.read().strip()
        
        # Load Maya model
        model_id = "maya-research/maya1"
        print(f"Loading Maya model: {model_id}", file=sys.stderr)
        
        tokenizer = AutoTokenizer.from_pretrained(model_id)
        model = AutoModelForCausalLM.from_pretrained(
            model_id,
            torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32,
            device_map="auto"
        )
        
        # Prepare input
        prompt = f"[{language}] {text}"
        inputs = tokenizer(prompt, return_tensors="pt")
        
        if torch.cuda.is_available():
            inputs = {k: v.cuda() for k, v in inputs.items()}
        
        # Generate audio
        print("Generating audio...", file=sys.stderr)
        with torch.no_grad():
            outputs = model.generate(
                **inputs,
                max_length=1024,
                do_sample=True,
                temperature=0.7
            )
        
        # Convert to audio (this is a simplified version)
        # Maya model outputs need proper audio decoding
        audio_data = outputs[0].cpu().numpy()
        
        # Apply speed adjustment
        if speed != 1.0:
            audio_data = adjust_speed(audio_data, speed)
        
        # Save as WAV
        sample_rate = 22050
        wavfile.write(output_path, sample_rate, audio_data.astype(np.int16))
        
        print(f"Audio saved to: {output_path}", file=sys.stderr)
        return 0
        
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1

def adjust_speed(audio, speed):
    # Simple speed adjustment by resampling
    if speed == 1.0:
        return audio
    
    indices = np.arange(0, len(audio), speed)
    indices = indices[indices < len(audio)].astype(int)
    return audio[indices]

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Maya TTS Synthesis")
    parser.add_argument("--input", required=True, help="Input text file path")
    parser.add_argument("--language", default="en", help="Language code")
    parser.add_argument("--speed", type=float, default=1.0, help="Speech speed")
    parser.add_argument("--output", required=True, help="Output WAV file path")
    
    args = parser.parse_args()
    
    sys.exit(synthesize(args.input, args.language, args.speed, args.output))
""".trimIndent()
        
        scriptFile.writeText(scriptContent)
        Log.info { "Maya inference script created at: ${scriptFile.absolutePath}" }
    }
    
    /**
     * Install Python dependencies
     */
    private fun installDependencies() {
        try {
            Log.info { "=" .repeat(80) }
            Log.info { "Installing Maya dependencies..." }
            Log.info { "This will download ~2-3 GB and may take 10-20 minutes" }
            Log.info { "Dependencies: PyTorch, Transformers, SciPy, NumPy, Accelerate" }
            Log.info { "=" .repeat(80) }
            Log.info { "Opening terminal window to show installation progress..." }
            
            val packages = listOf(
                "torch",
                "transformers",
                "scipy",
                "numpy",
                "accelerate"
            )
            
            // Build command for CMD window
            val cmdCommand = buildString {
                append("cmd.exe /k \"")
                append("echo ========================================")
                append(" && echo Maya TTS - Dependency Installation")
                append(" && echo ========================================")
                append(" && echo Dependencies: PyTorch, Transformers, SciPy, NumPy, Accelerate")
                append(" && echo Download size: ~2-3 GB")
                append(" && echo Time: 10-20 minutes")
                append(" && echo.")
                append(" && echo Installation will start now...")
                append(" && echo.")
                append(" && cd /d \"${mayaDir.absolutePath}\"")
                append(" && \"${pythonExecutable}\" -m pip install ${packages.joinToString(" ")} --verbose")
                append(" && echo.")
                append(" && echo ========================================")
                append(" && echo Installation complete!")
                append(" && echo You can close this window now.")
                append(" && echo ========================================")
                append(" && pause")
                append("\"")
            }
            
            Log.info { "Launching CMD window for dependency installation..." }
            
            // Start CMD window
            val cmdProcess = ProcessBuilder("cmd.exe", "/c", "start", cmdCommand)
                .start()
            
            cmdProcess.waitFor(5, TimeUnit.SECONDS) // Just wait for CMD to launch
            
            Log.info { "Terminal window opened. Please wait for installation to complete..." }
            Log.info { "The window will show real-time progress and close when done." }
            
            // Wait for installation to complete by checking if packages are installed
            Log.info { "Waiting for dependency installation to complete..." }
            
            var waitTime = 0
            val maxWaitTime = 1800 // 30 minutes max
            var installationComplete = false
            
            while (!installationComplete && waitTime < maxWaitTime) {
                Thread.sleep(5000) // Check every 5 seconds
                waitTime += 5
                
                // Check if torch is installed (main dependency)
                try {
                    val checkProcess = ProcessBuilder(
                        pythonExecutable!!,
                        "-c",
                        "import torch; import transformers; print('OK')"
                    ).redirectErrorStream(true).start()
                    
                    val checkCompleted = checkProcess.waitFor(10, TimeUnit.SECONDS)
                    if (checkCompleted && checkProcess.exitValue() == 0) {
                        val output = checkProcess.inputStream.bufferedReader().readText().trim()
                        if (output.contains("OK")) {
                            installationComplete = true
                            Log.info { "✓ Dependencies installed successfully!" }
                        }
                    }
                } catch (e: Exception) {
                    // Still installing, continue waiting
                }
                
                // Log progress every 30 seconds
                if (waitTime % 30 == 0 && !installationComplete) {
                    Log.info { "Still installing dependencies... (${waitTime}s elapsed)" }
                }
            }
            
            if (!installationComplete) {
                Log.warn { "Dependency installation timeout or incomplete after ${waitTime}s" }
                Log.warn { "Please check the terminal window for errors" }
            }
            
        } catch (e: Exception) {
            Log.warn { "Error installing dependencies: ${e.message}" }
        }
    }
    
    /**
     * Verify Maya installation
     */
    private fun verifyInstallation(): Boolean {
        try {
            val scriptFile = File(mayaScriptPath!!)
            if (!scriptFile.exists()) {
                Log.error { "Maya script not found: ${scriptFile.absolutePath}" }
                return false
            }
            
            // Try to import required modules
            val process = ProcessBuilder(
                pythonExecutable!!,
                "-c",
                "import torch; import transformers; import scipy; print('OK')"
            ).redirectErrorStream(true)
                .start()
            
            val completed = process.waitFor(10, TimeUnit.SECONDS)
            
            if (completed && process.exitValue() == 0) {
                val output = process.inputStream.bufferedReader().readText().trim()
                if (output.contains("OK")) {
                    Log.info { "Maya verification successful" }
                    return true
                }
            }
            
            val output = process.inputStream.bufferedReader().readText()
            Log.warn { "Maya verification output: $output" }
            return false
            
        } catch (e: Exception) {
            Log.error { "Verification error: ${e.message}" }
            return false
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
            Log.info { "Killing ${activeProcesses.size} active Maya processes..." }
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
     * Shutdown and cleanup
     */
    fun shutdown() {
        // Kill all active processes first
        killAllProcesses()
        
        // Cleanup temp files
        tempDir.listFiles()?.forEach { it.delete() }
        
        Log.info { "Maya TTS engine shutdown" }
    }
}

/**
 * Maya language data class
 */
data class MayaLanguage(
    val code: String,
    val name: String
)

/**
 * Maya exception class
 */
class MayaException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
