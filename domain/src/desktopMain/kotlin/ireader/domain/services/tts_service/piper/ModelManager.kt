package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing Piper voice models
 */
interface ModelManager {
    /**
     * Get list of available voice models
     */
    suspend fun getAvailableModels(): List<VoiceModel>
    
    /**
     * Download a voice model with progress tracking
     */
    suspend fun downloadModel(model: VoiceModel): Flow<DownloadProgress>
    
    /**
     * Get list of downloaded models
     */
    suspend fun getDownloadedModels(): List<VoiceModel>
    
    /**
     * Delete a downloaded model
     */
    suspend fun deleteModel(modelId: String): Result<Unit>
    
    /**
     * Get paths for a downloaded model
     */
    fun getModelPaths(modelId: String): ModelPaths?
}

/**
 * Paths to model files
 */
data class ModelPaths(
    val modelPath: String,
    val configPath: String
)
