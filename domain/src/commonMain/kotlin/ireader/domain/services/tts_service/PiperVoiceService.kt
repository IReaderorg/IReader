package ireader.domain.services.tts_service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import ireader.core.log.Log
import ireader.domain.data.repository.PiperVoiceRepository
import ireader.domain.models.tts.PiperVoice
import ireader.domain.models.tts.VoiceGender
import ireader.domain.models.tts.VoiceQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Unified service for managing Piper TTS voices.
 * 
 * Features:
 * - Fetches voice catalog from https://rhasspy.github.io/piper-samples/voices.json
 * - Stores voices in local database for offline access
 * - Provides reactive updates via Flow
 * - Supports manual refresh
 * - Desktop-only: handles voice model downloads
 */
class PiperVoiceService(
    private val repository: PiperVoiceRepository,
    private val httpClient: HttpClient
) {
    companion object {
        private const val VOICES_JSON_URL = "https://rhasspy.github.io/piper-samples/voices.json"
        private const val BASE_DOWNLOAD_URL = "https://huggingface.co/rhasspy/piper-voices/resolve/main/"
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    private val mutex = Mutex()
    private var lastFetchTime: Long = 0
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()
    
    /**
     * Subscribe to all voices (reactive)
     */
    fun subscribeAll(): Flow<List<PiperVoice>> = repository.subscribeAll()
    
    /**
     * Subscribe to downloaded voices only (reactive)
     */
    fun subscribeDownloaded(): Flow<List<PiperVoice>> = repository.subscribeDownloaded()
    
    /**
     * Get all voices (one-shot)
     */
    suspend fun getAll(): List<PiperVoice> = repository.getAll()
    
    /**
     * Get downloaded voices (one-shot)
     */
    suspend fun getDownloaded(): List<PiperVoice> = repository.getDownloaded()
    
    /**
     * Get voice by ID
     */
    suspend fun getById(id: String): PiperVoice? = repository.getById(id)
    
    /**
     * Get voices by language
     */
    suspend fun getByLanguage(language: String): List<PiperVoice> = repository.getByLanguage(language)
    
    /**
     * Get all available languages
     */
    suspend fun getLanguages(): List<String> = repository.getLanguages()
    
    /**
     * Search voices
     */
    suspend fun search(query: String): List<PiperVoice> = repository.search(query)
    
    /**
     * Initialize service - fetch voices if needed
     */
    suspend fun initialize(): Result<Unit> {
        return mutex.withLock {
            try {
                val count = repository.count()
                val now = currentTimeToLong()
                
                // Fetch if empty or cache expired
                if (count == 0L || (now - lastFetchTime) > CACHE_DURATION_MS) {
                    fetchAndStoreVoices()
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.error { "[PiperVoiceService] Initialize failed: ${e.message}" }
                Result.failure(e)
            }
        }
    }
    
    /**
     * Force refresh voices from remote source
     */
    suspend fun refresh(): Result<Int> {
        return mutex.withLock {
            _isRefreshing.value = true
            _refreshError.value = null
            
            try {
                val count = fetchAndStoreVoices()
                lastFetchTime = currentTimeToLong()
                Result.success(count)
            } catch (e: Exception) {
                Log.error { "[PiperVoiceService] Refresh failed: ${e.message}" }
                _refreshError.value = e.message ?: "Unknown error"
                Result.failure(e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Mark a voice as downloaded
     */
    suspend fun markAsDownloaded(voiceId: String) {
        repository.updateDownloadStatus(voiceId, true)
    }
    
    /**
     * Mark a voice as not downloaded
     */
    suspend fun markAsNotDownloaded(voiceId: String) {
        repository.updateDownloadStatus(voiceId, false)
    }
    
    /**
     * Fetch voices from remote and store in database
     */
    private suspend fun fetchAndStoreVoices(): Int {
        Log.info { "[PiperVoiceService] Fetching voices from $VOICES_JSON_URL" }
        
        val response = httpClient.get(VOICES_JSON_URL)
        val jsonText = response.bodyAsText()
        
        val voices = parseVoicesJson(jsonText)
        Log.info { "[PiperVoiceService] Parsed ${voices.size} voices" }
        
        if (voices.isNotEmpty()) {
            // Preserve download status of existing voices
            val existingVoices = repository.getAll().associateBy { it.id }
            val voicesWithStatus = voices.map { voice ->
                val existing = existingVoices[voice.id]
                if (existing != null) {
                    voice.copy(isDownloaded = existing.isDownloaded)
                } else {
                    voice
                }
            }
            
            repository.upsertAll(voicesWithStatus)
        }
        
        return voices.size
    }
    
    /**
     * Parse the Piper voices JSON format
     */
    private fun parseVoicesJson(jsonText: String): List<PiperVoice> {
        val voices = mutableListOf<PiperVoice>()
        val now = currentTimeToLong()
        
        try {
            val rootObject = json.parseToJsonElement(jsonText).jsonObject
            
            for ((key, value) in rootObject) {
                try {
                    val voiceObj = value.jsonObject
                    val voice = parseVoiceEntry(key, voiceObj, now)
                    if (voice != null) {
                        voices.add(voice)
                    }
                } catch (e: Exception) {
                    Log.warn { "[PiperVoiceService] Failed to parse voice $key: ${e.message}" }
                }
            }
        } catch (e: Exception) {
            Log.error { "[PiperVoiceService] Failed to parse voices JSON: ${e.message}" }
        }
        
        return voices.sortedBy { it.language + it.name }
    }
    
    private fun parseVoiceEntry(key: String, obj: kotlinx.serialization.json.JsonObject, timestamp: Long): PiperVoice? {
        val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: return null
        val qualityStr = obj["quality"]?.jsonPrimitive?.contentOrNull ?: "medium"
        val numSpeakers = obj["num_speakers"]?.jsonPrimitive?.intOrNull ?: 1
        
        val langObj = obj["language"]?.jsonObject ?: return null
        val langCode = langObj["code"]?.jsonPrimitive?.contentOrNull ?: return null
        val langFamily = langObj["family"]?.jsonPrimitive?.contentOrNull ?: langCode.substringBefore("_")
        val langEnglish = langObj["name_english"]?.jsonPrimitive?.contentOrNull ?: langFamily
        val country = langObj["country_english"]?.jsonPrimitive?.contentOrNull ?: ""
        
        // Find ONNX file info
        val filesObj = obj["files"]?.jsonObject ?: return null
        var onnxPath: String? = null
        var onnxSize: Long = 0
        
        for ((filePath, fileInfo) in filesObj) {
            if (filePath.endsWith(".onnx") && !filePath.endsWith(".onnx.json")) {
                onnxPath = filePath
                onnxSize = fileInfo.jsonObject["size_bytes"]?.jsonPrimitive?.longOrNull ?: 0
                break
            }
        }
        
        if (onnxPath == null) return null
        
        val quality = when (qualityStr) {
            "x_low" -> VoiceQuality.LOW
            "low" -> VoiceQuality.LOW
            "medium" -> VoiceQuality.MEDIUM
            "high" -> VoiceQuality.HIGH
            else -> VoiceQuality.MEDIUM
        }
        
        val gender = inferGender(name)
        val locale = langCode.replace("_", "-")
        val displayName = "$langEnglish ($country) - ${name.replaceFirstChar { it.uppercase() }} - ${qualityStr.replaceFirstChar { it.uppercase() }}"
        
        val downloadUrl = BASE_DOWNLOAD_URL + onnxPath
        val configUrl = "$downloadUrl.json"
        
        val tags = mutableListOf(langEnglish.lowercase(), country.lowercase())
        if (numSpeakers > 1) tags.add("multi-speaker")
        
        return PiperVoice(
            id = key,
            name = displayName,
            language = langFamily,
            locale = locale,
            gender = gender,
            quality = quality,
            sampleRate = 22050,
            modelSize = onnxSize,
            downloadUrl = downloadUrl,
            configUrl = configUrl,
            checksum = "",
            license = "MIT",
            description = "$langEnglish voice from $country" + if (numSpeakers > 1) " ($numSpeakers speakers)" else "",
            tags = tags,
            isDownloaded = false,
            lastUpdated = timestamp
        )
    }
    
    private fun inferGender(name: String): VoiceGender {
        val femaleNames = listOf(
            "amy", "alba", "jenny", "cori", "kathleen", "kristin", "lessac", "ljspeech",
            "eva", "kerstin", "ramona", "siwis", "rapunzelina", "carla", "daniela",
            "anna", "berta", "paola", "irina", "lada", "lisa", "nathalie", "gosia",
            "maya", "meera", "priyamvada", "padmavathi", "lili", "salka", "ugla",
            "natia", "raya", "marylux", "ona", "huayan"
        )
        val maleNames = listOf(
            "alan", "ryan", "joe", "john", "danny", "bryce", "norman", "sam", "kusal",
            "thorsten", "karlsson", "pavoque", "gilles", "tom", "riccardo", "faber",
            "dmitri", "ruslan", "denis", "kareem", "amir", "harri", "imre", "mihai",
            "artur", "darkman", "fettah", "fahrettin", "aivars", "arjun", "venkatesh",
            "rohan", "pratham", "bui", "steinn", "pim", "ronnie", "cadu", "jeff"
        )
        
        val lowerName = name.lowercase()
        return when {
            femaleNames.any { lowerName.contains(it) } -> VoiceGender.FEMALE
            maleNames.any { lowerName.contains(it) } -> VoiceGender.MALE
            lowerName.contains("female") -> VoiceGender.FEMALE
            lowerName.contains("male") -> VoiceGender.MALE
            else -> VoiceGender.NEUTRAL
        }
    }
}
