package ireader.data.storage

import ireader.core.log.Log
import ireader.domain.storage.VoiceStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

/**
 * Implementation of VoiceStorage for managing voice model files
 * Requirements: 3.1, 3.2
 */
class VoiceStorageImpl(
    private val baseDirectory: File
) : VoiceStorage {
    
    private val voicesDir = File(baseDirectory, "tts_voices").apply { mkdirs() }
    
    override suspend fun saveVoiceModel(voiceId: String, data: ByteArray): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val voiceFile = File(voicesDir, "$voiceId.voice")
                voiceFile.writeBytes(data)
                Log.info { "Voice model saved: $voiceId (${data.size} bytes)" }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.error("Failed to save voice model: $voiceId", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun loadVoiceModel(voiceId: String): Result<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                val voiceFile = File(voicesDir, "$voiceId.voice")
                if (!voiceFile.exists()) {
                    return@withContext Result.failure(
                        FileNotFoundException("Voice model not found: $voiceId")
                    )
                }
                val data = voiceFile.readBytes()
                Log.info { "Voice model loaded: $voiceId (${data.size} bytes)" }
                Result.success(data)
            } catch (e: Exception) {
                Log.error("Failed to load voice model: $voiceId", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun deleteVoiceModel(voiceId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val voiceFile = File(voicesDir, "$voiceId.voice")
                if (voiceFile.exists()) {
                    val deleted = voiceFile.delete()
                    if (deleted) {
                        Log.info { "Voice model deleted: $voiceId" }
                    } else {
                        Log.warn { "Failed to delete voice model file: $voiceId" }
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.error("Failed to delete voice model: $voiceId", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getInstalledVoices(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                voicesDir.listFiles()
                    ?.filter { it.extension == "voice" }
                    ?.map { it.nameWithoutExtension }
                    ?: emptyList()
            } catch (e: Exception) {
                Log.error("Failed to list installed voices", e)
                emptyList()
            }
        }
    }
    
    override suspend fun getVoiceModelPath(voiceId: String): String? {
        return withContext(Dispatchers.IO) {
            val voiceFile = File(voicesDir, "$voiceId.voice")
            if (voiceFile.exists()) voiceFile.absolutePath else null
        }
    }
    
    override suspend fun getVoiceModelSize(voiceId: String): Long {
        return withContext(Dispatchers.IO) {
            val voiceFile = File(voicesDir, "$voiceId.voice")
            if (voiceFile.exists()) voiceFile.length() else 0L
        }
    }
}
