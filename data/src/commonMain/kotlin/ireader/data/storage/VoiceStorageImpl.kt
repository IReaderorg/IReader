package ireader.data.storage

import ireader.core.log.Log
import ireader.domain.storage.VoiceStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.buffer
import okio.use

/**
 * Implementation of VoiceStorage for managing voice model files.
 * Uses Okio for KMP-compatible file operations.
 * Requirements: 3.1, 3.2
 */
class VoiceStorageImpl(
    private val baseDirectory: Path,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) : VoiceStorage {
    
    private val voicesDir = baseDirectory / "tts_voices"
    
    init {
        if (!fileSystem.exists(voicesDir)) {
            fileSystem.createDirectories(voicesDir)
        }
    }
    
    override suspend fun saveVoiceModel(voiceId: String, data: ByteArray): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val voiceFile = voicesDir / "$voiceId.voice"
                fileSystem.sink(voiceFile).buffer().use { sink ->
                    sink.write(data)
                }
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
                val voiceFile = voicesDir / "$voiceId.voice"
                if (!fileSystem.exists(voiceFile)) {
                    return@withContext Result.failure(
                        IOException("Voice model not found: $voiceId")
                    )
                }
                val data = fileSystem.source(voiceFile).buffer().use { source ->
                    source.readByteArray()
                }
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
                val voiceFile = voicesDir / "$voiceId.voice"
                if (fileSystem.exists(voiceFile)) {
                    fileSystem.delete(voiceFile)
                    Log.info { "Voice model deleted: $voiceId" }
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
                if (!fileSystem.exists(voicesDir)) return@withContext emptyList()
                fileSystem.list(voicesDir)
                    .filter { it.name.endsWith(".voice") }
                    .map { it.name.removeSuffix(".voice") }
            } catch (e: Exception) {
                Log.error("Failed to list installed voices", e)
                emptyList()
            }
        }
    }
    
    override suspend fun getVoiceModelPath(voiceId: String): String? {
        return withContext(Dispatchers.IO) {
            val voiceFile = voicesDir / "$voiceId.voice"
            if (fileSystem.exists(voiceFile)) voiceFile.toString() else null
        }
    }
    
    override suspend fun getVoiceModelSize(voiceId: String): Long {
        return withContext(Dispatchers.IO) {
            val voiceFile = voicesDir / "$voiceId.voice"
            if (fileSystem.exists(voiceFile)) {
                fileSystem.metadata(voiceFile).size ?: 0L
            } else {
                0L
            }
        }
    }
}
