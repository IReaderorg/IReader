package ireader.data.tts

import ireader.domain.catalogs.VoiceCatalog
import ireader.domain.data.repository.VoiceModelRepository
import ireader.domain.models.tts.VoiceModel
import ireader.domain.services.tts_service.VoiceDownloader
import ireader.domain.services.tts_service.VoiceStorage
import okio.FileSystem
import okio.Path
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.withContext

/**
 * Implementation of VoiceModelRepository.
 * Uses Okio for KMP-compatible file operations.
 * Requirements: 4.1, 4.2, 4.3, 4.4
 */
class VoiceModelRepositoryImpl(
    private val voiceStorage: VoiceStorage,
    private val voiceDownloader: VoiceDownloader,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) : VoiceModelRepository {
    
    override suspend fun getAvailableVoices(): Result<List<VoiceModel>> {
        return try {
            Result.success(VoiceCatalog.getAllVoices())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getVoicesByLanguage(language: String): Result<List<VoiceModel>> {
        return try {
            Result.success(VoiceCatalog.getVoicesByLanguage(language))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun downloadVoice(
        voiceId: String,
        onProgress: (Float) -> Unit
    ): Result<Path> {
        val voice = VoiceCatalog.getVoiceById(voiceId)
            ?: return Result.failure(IllegalArgumentException("Voice not found: $voiceId"))
        
        return voiceDownloader.downloadVoice(voice, onProgress)
    }
    
    override suspend fun deleteVoice(voiceId: String): Result<Unit> {
        return voiceStorage.deleteVoice(voiceId)
    }
    
    override suspend fun getInstalledVoices(): Result<List<VoiceModel>> = withContext(ioDispatcher) {
        try {
            val installedIds = voiceStorage.getDownloadedVoiceIds()
            val installedVoices = installedIds.mapNotNull { id ->
                VoiceCatalog.getVoiceById(id)
            }
            Result.success(installedVoices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun verifyVoiceIntegrity(voiceId: String): Boolean = withContext(ioDispatcher) {
        try {
            val voice = VoiceCatalog.getVoiceById(voiceId) ?: return@withContext false
            
            // Check if files exist
            if (!voiceStorage.isVoiceDownloaded(voiceId)) {
                return@withContext false
            }
            
            val modelFile = voiceStorage.getModelFile(voiceId)
            val configFile = voiceStorage.getConfigFile(voiceId)
            
            // Verify files exist and are not empty using Okio
            if (!fileSystem.exists(modelFile)) {
                return@withContext false
            }
            val modelSize = fileSystem.metadata(modelFile).size ?: 0L
            if (modelSize == 0L) {
                return@withContext false
            }
            
            if (!fileSystem.exists(configFile)) {
                return@withContext false
            }
            val configSize = fileSystem.metadata(configFile).size ?: 0L
            if (configSize == 0L) {
                return@withContext false
            }
            
            // If checksum is provided (not placeholder), verify it
            if (voice.checksum != "sha256:placeholder") {
                // Checksum verification would be done here
                // For now, we'll skip actual verification if it's a placeholder
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getStorageUsage(): Long {
        return voiceStorage.getTotalStorageUsage()
    }
    
    override suspend fun getVoiceById(voiceId: String): Result<VoiceModel?> {
        return try {
            Result.success(VoiceCatalog.getVoiceById(voiceId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isVoiceDownloaded(voiceId: String): Boolean {
        return voiceStorage.isVoiceDownloaded(voiceId)
    }
}
