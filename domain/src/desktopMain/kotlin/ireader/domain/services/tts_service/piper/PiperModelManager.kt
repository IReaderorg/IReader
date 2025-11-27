package ireader.domain.services.tts_service.piper

import ireader.core.log.Log
import ireader.core.storage.AppDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest

/**
 * Manages Piper voice models including downloading, storage, and retrieval
 * 
 * @property appDataDir The application data directory for storing models
 */
class PiperModelManager(
    private val appDataDir: File = AppDir
) : ModelManager {
    
    private val modelsDir = File(appDataDir, "piper_models").apply { mkdirs() }
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * Get list of all available voice models from embedded JSON resource
     */
    override suspend fun getAvailableModels(): List<VoiceModel> {
        Log.info { "getAvailableModels() called" }
        return try {
            val models = withContext(Dispatchers.IO) {
                loadModelsFromResource()
            }
            Log.info { "getAvailableModels returning ${models.size} models" }
            models
        } catch (e: Exception) {
            Log.error { "Failed to load available models: ${e.message}" }
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Download a voice model with progress tracking
     */
    override suspend fun downloadModel(model: VoiceModel): Flow<DownloadProgress> = 
        kotlinx.coroutines.flow.channelFlow {
            // Check available disk space before downloading
            val availableSpace = modelsDir.usableSpace
            val requiredSpace = model.sizeBytes + (10 * 1024 * 1024) // Add 10MB buffer
            
            if (availableSpace < requiredSpace) {
                throw InsufficientStorageException(
                    required = requiredSpace,
                    available = availableSpace,
                    message = "Insufficient storage space. Required: ${requiredSpace / (1024 * 1024)}MB, Available: ${availableSpace / (1024 * 1024)}MB"
                )
            }
            
            val modelDir = File(modelsDir, model.id).apply { mkdirs() }
            val modelFile = File(modelDir, "model.onnx")
            val configFile = File(modelDir, "config.json")
            
            try {
                // Download model file
                send(DownloadProgress(0, model.sizeBytes, "Downloading model..."))
                downloadFile(model.modelUrl, modelFile, model.sizeBytes) { progress ->
                    trySend(DownloadProgress(progress, model.sizeBytes, "Downloading model..."))
                }
                
                // Download config file (usually small, so no progress tracking)
                send(DownloadProgress(model.sizeBytes, model.sizeBytes, "Downloading config..."))
                downloadFile(model.configUrl, configFile, 0) { }
                
                // Verify integrity
                send(DownloadProgress(model.sizeBytes, model.sizeBytes, "Verifying..."))
                verifyModelIntegrity(modelFile, configFile, model.modelChecksum, model.configChecksum)
                
                send(DownloadProgress(model.sizeBytes, model.sizeBytes, "Complete"))
            } catch (e: Exception) {
                // Clean up on failure
                modelDir.deleteRecursively()
                throw e
            }
        }.flowOn(Dispatchers.IO)
    
    /**
     * Get list of models that have been downloaded locally
     */
    override suspend fun getDownloadedModels(): List<VoiceModel> = withContext(Dispatchers.IO) {
        try {
            val allModels = getAvailableModels()
            allModels.filter { model ->
                val paths = getModelPaths(model.id)
                paths != null && File(paths.modelPath).exists() && File(paths.configPath).exists()
            }.map { it.copy(isDownloaded = true) }
        } catch (e: Exception) {
            Log.error { "Failed to get downloaded models: ${e.message}" }
            emptyList()
        }
    }
    
    /**
     * Delete a downloaded model from local storage
     */
    override suspend fun deleteModel(modelId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val modelDir = File(modelsDir, modelId)
            if (modelDir.exists()) {
                modelDir.deleteRecursively()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error { "Failed to delete model $modelId: ${e.message}" }
            Result.failure(e)
        }
    }
    
    /**
     * Get file paths for a downloaded model
     */
    override fun getModelPaths(modelId: String): ModelPaths? {
        val modelDir = File(modelsDir, modelId)
        val modelFile = File(modelDir, "model.onnx")
        val configFile = File(modelDir, "config.json")
        
        return if (modelFile.exists() && configFile.exists()) {
            ModelPaths(modelFile.absolutePath, configFile.absolutePath)
        } else {
            null
        }
    }
    
    /**
     * Load voice models from embedded JSON resource
     * Falls back to PiperVoiceCatalog if resource file is not found
     */
    private fun loadModelsFromResource(): List<VoiceModel> {
        Log.info { "loadModelsFromResource() called - START" }
        
        // Skip resource file and go directly to PiperVoiceCatalog
        // The resource file approach was causing issues
        Log.info { "Loading voices directly from PiperVoiceCatalog..." }
        
        try {
            Log.info { "Calling PiperVoiceCatalog.getAllVoices()..." }
            val catalogModels = ireader.domain.catalogs.PiperVoiceCatalog.getAllVoices()
            Log.info { "PiperVoiceCatalog.getAllVoices() returned ${catalogModels.size} voices" }
            
            if (catalogModels.isEmpty()) {
                Log.error { "PiperVoiceCatalog returned empty list! This should not happen." }
                return emptyList()
            }
            
            // Log first few voices for debugging
            catalogModels.take(3).forEach { voice ->
                Log.info { "  Voice: ${voice.id} - ${voice.name} (${voice.locale})" }
            }
            
            val result = catalogModels.map { catalogVoice ->
                VoiceModel(
                    id = catalogVoice.id,
                    name = catalogVoice.name,
                    language = catalogVoice.locale,
                    quality = when (catalogVoice.quality) {
                        ireader.domain.models.tts.VoiceQuality.LOW -> VoiceModel.Quality.LOW
                        ireader.domain.models.tts.VoiceQuality.MEDIUM -> VoiceModel.Quality.MEDIUM
                        ireader.domain.models.tts.VoiceQuality.HIGH -> VoiceModel.Quality.HIGH
                        ireader.domain.models.tts.VoiceQuality.PREMIUM -> VoiceModel.Quality.HIGH
                    },
                    gender = when (catalogVoice.gender) {
                        ireader.domain.models.tts.VoiceGender.MALE -> VoiceModel.Gender.MALE
                        ireader.domain.models.tts.VoiceGender.FEMALE -> VoiceModel.Gender.FEMALE
                        ireader.domain.models.tts.VoiceGender.NEUTRAL -> VoiceModel.Gender.NEUTRAL
                    },
                    sizeBytes = catalogVoice.modelSize,
                    modelUrl = catalogVoice.downloadUrl,
                    configUrl = catalogVoice.configUrl,
                    modelChecksum = catalogVoice.checksum.takeIf { it.isNotEmpty() && !it.contains("placeholder") },
                    configChecksum = null,
                    isDownloaded = false
                )
            }
            Log.info { "Successfully mapped ${result.size} voice models from catalog" }
            Log.info { "loadModelsFromResource() - END (success)" }
            return result
        } catch (e: Exception) {
            Log.error { "Failed to load from PiperVoiceCatalog: ${e.message}" }
            Log.error { "Exception type: ${e::class.simpleName}" }
            e.printStackTrace()
            Log.info { "loadModelsFromResource() - END (error)" }
            return emptyList()
        }
    }
    
    /**
     * Download a file from URL with progress tracking
     */
    private suspend fun downloadFile(
        url: String,
        destination: File,
        totalSize: Long,
        onProgress: suspend (Long) -> Unit
    ) = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection()
        connection.connect()
        
        connection.getInputStream().use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    onProgress(totalBytesRead)
                }
            }
        }
    }
    
    /**
     * Verify that downloaded model files are valid
     */
    private fun verifyModelIntegrity(
        modelFile: File, 
        configFile: File,
        modelChecksum: String? = null,
        configChecksum: String? = null
    ) {
        require(modelFile.exists() && modelFile.length() > 0) { 
            "Model file is missing or empty" 
        }
        require(configFile.exists() && configFile.length() > 0) { 
            "Config file is missing or empty" 
        }
        
        // Verify checksums if provided
        if (modelChecksum != null) {
            val actualChecksum = calculateChecksum(modelFile)
            require(actualChecksum.equals(modelChecksum, ignoreCase = true)) {
                "Model file checksum mismatch. Expected: $modelChecksum, Got: $actualChecksum"
            }
        }
        
        if (configChecksum != null) {
            val actualChecksum = calculateChecksum(configFile)
            require(actualChecksum.equals(configChecksum, ignoreCase = true)) {
                "Config file checksum mismatch. Expected: $configChecksum, Got: $actualChecksum"
            }
        }
        
        // Verify config is valid JSON
        try {
            configFile.readText()
        } catch (e: Exception) {
            throw IllegalStateException("Config file is not valid JSON", e)
        }
    }
    
    /**
     * Calculate MD5 checksum of a file
     */
    private fun calculateChecksum(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}

/**
 * Data class for deserializing the piper_models.json file
 */
@Serializable
private data class VoiceModelsData(
    val models: List<VoiceModel>
)

/**
 * Exception thrown when there is insufficient storage space for downloading a model
 */
class InsufficientStorageException(
    val required: Long,
    val available: Long,
    message: String
) : Exception(message)
