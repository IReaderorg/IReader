package ireader.data.repository

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.PiperVoiceRepository
import ireader.domain.models.tts.PiperVoice
import ireader.domain.models.tts.VoiceGender
import ireader.domain.models.tts.VoiceQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of PiperVoiceRepository using SQLDelight database.
 */
class PiperVoiceRepositoryImpl(
    private val handler: DatabaseHandler
) : PiperVoiceRepository {
    
    override fun subscribeAll(): Flow<List<PiperVoice>> {
        return handler.subscribeToList {
            piperVoiceQueries.selectAll(piperVoiceMapper)
        }
    }
    
    override suspend fun getAll(): List<PiperVoice> {
        return handler.awaitList {
            piperVoiceQueries.selectAll(piperVoiceMapper)
        }
    }
    
    override suspend fun getById(id: String): PiperVoice? {
        return handler.awaitOneOrNull {
            piperVoiceQueries.selectById(id, piperVoiceMapper)
        }
    }
    
    override suspend fun getByLanguage(language: String): List<PiperVoice> {
        return handler.awaitList {
            piperVoiceQueries.selectByLanguage(language, piperVoiceMapper)
        }
    }
    
    override suspend fun getDownloaded(): List<PiperVoice> {
        return handler.awaitList {
            piperVoiceQueries.selectDownloaded(piperVoiceMapper)
        }
    }
    
    override fun subscribeDownloaded(): Flow<List<PiperVoice>> {
        return handler.subscribeToList {
            piperVoiceQueries.selectDownloaded(piperVoiceMapper)
        }
    }
    
    override suspend fun getLanguages(): List<String> {
        return handler.awaitList {
            piperVoiceQueries.selectLanguages()
        }
    }
    
    override suspend fun upsert(voice: PiperVoice) {
        handler.await {
            piperVoiceQueries.upsert(
                id = voice.id,
                name = voice.name,
                language = voice.language,
                locale = voice.locale,
                gender = voice.gender.name,
                quality = voice.quality.name,
                sampleRate = voice.sampleRate.toLong(),
                modelSize = voice.modelSize,
                downloadUrl = voice.downloadUrl,
                configUrl = voice.configUrl,
                checksum = voice.checksum,
                license = voice.license,
                description = voice.description,
                tags = voice.tags.joinToString(","),
                isDownloaded = if (voice.isDownloaded) 1L else 0L,
                lastUpdated = voice.lastUpdated
            )
        }
    }
    
    override suspend fun upsertAll(voices: List<PiperVoice>) {
        handler.await(inTransaction = true) {
            voices.forEach { voice ->
                piperVoiceQueries.upsert(
                    id = voice.id,
                    name = voice.name,
                    language = voice.language,
                    locale = voice.locale,
                    gender = voice.gender.name,
                    quality = voice.quality.name,
                    sampleRate = voice.sampleRate.toLong(),
                    modelSize = voice.modelSize,
                    downloadUrl = voice.downloadUrl,
                    configUrl = voice.configUrl,
                    checksum = voice.checksum,
                    license = voice.license,
                    description = voice.description,
                    tags = voice.tags.joinToString(","),
                    isDownloaded = if (voice.isDownloaded) 1L else 0L,
                    lastUpdated = voice.lastUpdated
                )
            }
        }
    }
    
    override suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean) {
        handler.await {
            piperVoiceQueries.updateDownloadStatus(
                isDownloaded = if (isDownloaded) 1L else 0L,
                id = id
            )
        }
    }
    
    override suspend fun deleteAll() {
        handler.await {
            piperVoiceQueries.deleteAll()
        }
    }
    
    override suspend fun count(): Long {
        return handler.awaitOne {
            piperVoiceQueries.countAll()
        }
    }
    
    override suspend fun countDownloaded(): Long {
        return handler.awaitOne {
            piperVoiceQueries.countDownloaded()
        }
    }
    
    override suspend fun search(query: String): List<PiperVoice> {
        return handler.awaitList {
            piperVoiceQueries.search(query, query, query, piperVoiceMapper)
        }
    }
}

private val piperVoiceMapper: (
    id: String,
    name: String,
    language: String,
    locale: String,
    gender: String,
    quality: String,
    sampleRate: Long,
    modelSize: Long,
    downloadUrl: String,
    configUrl: String,
    checksum: String,
    license: String,
    description: String,
    tags: String,
    isDownloaded: Long,
    lastUpdated: Long
) -> PiperVoice = { id, name, language, locale, gender, quality, sampleRate, modelSize,
                    downloadUrl, configUrl, checksum, license, description, tags, isDownloaded, lastUpdated ->
    PiperVoice(
        id = id,
        name = name,
        language = language,
        locale = locale,
        gender = try { VoiceGender.valueOf(gender) } catch (e: Exception) { VoiceGender.NEUTRAL },
        quality = try { VoiceQuality.valueOf(quality) } catch (e: Exception) { VoiceQuality.MEDIUM },
        sampleRate = sampleRate.toInt(),
        modelSize = modelSize,
        downloadUrl = downloadUrl,
        configUrl = configUrl,
        checksum = checksum,
        license = license,
        description = description,
        tags = if (tags.isBlank()) emptyList() else tags.split(","),
        isDownloaded = isDownloaded == 1L,
        lastUpdated = lastUpdated
    )
}
