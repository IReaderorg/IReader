package ireader.domain.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android implementation of VoiceStorage
 * Handles voice model file storage on Android devices
 */
class AndroidVoiceStorage(
    private val baseDirectory: File
) : VoiceStorage {
    
    private val voicesDir = File(baseDirectory, "voices").apply { mkdirs() }
    
    override suspend fun saveVoiceModel(voiceId: String, data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(voicesDir, "$voiceId.voice")
            file.writeBytes(data)
        }
    }
    
    override suspend fun loadVoiceModel(voiceId: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(voicesDir, "$voiceId.voice")
            if (!file.exists()) {
                throw IllegalArgumentException("Voice model not found: $voiceId")
            }
            file.readBytes()
        }
    }
    
    override suspend fun deleteVoiceModel(voiceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(voicesDir, "$voiceId.voice")
            if (file.exists()) {
                file.delete()
            }
        }
    }
    
    override suspend fun getInstalledVoices(): List<String> = withContext(Dispatchers.IO) {
        voicesDir.listFiles()
            ?.filter { it.extension == "voice" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }
    
    override suspend fun getVoiceModelPath(voiceId: String): String? = withContext(Dispatchers.IO) {
        val file = File(voicesDir, "$voiceId.voice")
        if (file.exists()) file.absolutePath else null
    }
    
    override suspend fun getVoiceModelSize(voiceId: String): Long = withContext(Dispatchers.IO) {
        val file = File(voicesDir, "$voiceId.voice")
        if (file.exists()) file.length() else 0L
    }
}
