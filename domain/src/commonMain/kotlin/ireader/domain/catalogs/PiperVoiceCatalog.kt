package ireader.domain.catalogs

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import ireader.core.log.Log
import ireader.domain.models.tts.VoiceGender
import ireader.domain.models.tts.VoiceModel
import ireader.domain.models.tts.VoiceQuality
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Dynamic catalog of Piper TTS voices fetched from official source.
 * Voices are loaded from https://rhasspy.github.io/piper-samples/voices.json
 */
object PiperVoiceCatalog {
    
    private const val VOICES_JSON_URL = "https://rhasspy.github.io/piper-samples/voices.json"
    private const val BASE_DOWNLOAD_URL = "https://huggingface.co/rhasspy/piper-voices/resolve/main/"
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    private var cachedVoices: List<VoiceModel>? = null
    private val mutex = Mutex()
    private var lastFetchTime: Long = 0
    private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    
    /**
     * Get all available voices. Returns cached voices or fallback if not yet loaded.
     */
    fun getAllVoices(): List<VoiceModel> = cachedVoices ?: fallbackVoices
    
    /**
     * Fetch voices from remote source. Call this on app startup or when refreshing.
     */
    suspend fun fetchVoices(httpClient: HttpClient): Result<List<VoiceModel>> {
        return mutex.withLock {
            // Return cached if still valid
            val now = currentTimeToLong()
            if (cachedVoices != null && (now - lastFetchTime) < CACHE_DURATION_MS) {
                return@withLock Result.success(cachedVoices!!)
            }
            
            try {
                Log.info { "[PiperVoiceCatalog] Fetching voices from $VOICES_JSON_URL" }
                val response = httpClient.get(VOICES_JSON_URL)
                val jsonText = response.bodyAsText()
                
                val voices = parseVoicesJson(jsonText)
                cachedVoices = voices
                lastFetchTime = now
                
                Log.info { "[PiperVoiceCatalog] Loaded ${voices.size} voices" }
                Result.success(voices)
            } catch (e: Exception) {
                Log.error { "[PiperVoiceCatalog] Failed to fetch voices: ${e.message}" }
                // Return fallback on error
                if (cachedVoices == null) {
                    cachedVoices = fallbackVoices
                }
                Result.failure(e)
            }
        }
    }
    
    /**
     * Force refresh voices from remote source.
     */
    suspend fun refreshVoices(httpClient: HttpClient): Result<List<VoiceModel>> {
        lastFetchTime = 0 // Reset cache
        return fetchVoices(httpClient)
    }
    
    private fun parseVoicesJson(jsonText: String): List<VoiceModel> {
        val voices = mutableListOf<VoiceModel>()
        
        try {
            val rootObject = json.parseToJsonElement(jsonText).jsonObject
            
            for ((key, value) in rootObject) {
                try {
                    val voiceObj = value.jsonObject
                    val voice = parseVoiceEntry(key, voiceObj)
                    if (voice != null) {
                        voices.add(voice)
                    }
                } catch (e: Exception) {
                    Log.warn { "[PiperVoiceCatalog] Failed to parse voice $key: ${e.message}" }
                }
            }
        } catch (e: Exception) {
            Log.error { "[PiperVoiceCatalog] Failed to parse voices JSON" }
        }
        
        return voices.sortedBy { it.language + it.name }
    }
    
    private fun parseVoiceEntry(key: String, obj: JsonObject): VoiceModel? {
        val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: return null
        val qualityStr = obj["quality"]?.jsonPrimitive?.contentOrNull ?: "medium"
        val numSpeakers = obj["num_speakers"]?.jsonPrimitive?.intOrNull ?: 1
        
        val langObj = obj["language"]?.jsonObject ?: return null
        val langCode = langObj["code"]?.jsonPrimitive?.contentOrNull ?: return null
        val langFamily = langObj["family"]?.jsonPrimitive?.contentOrNull ?: langCode.substringBefore("_")
        val langRegion = langObj["region"]?.jsonPrimitive?.contentOrNull ?: ""
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
        
        // Infer gender from name (heuristic)
        val gender = inferGender(name)
        
        val locale = langCode.replace("_", "-")
        val displayName = "$langEnglish ($country) - ${name.replaceFirstChar { it.uppercase() }} - ${qualityStr.replaceFirstChar { it.uppercase() }}"
        
        val downloadUrl = BASE_DOWNLOAD_URL + onnxPath
        val configUrl = "$downloadUrl.json"
        
        val tags = mutableListOf(langEnglish.lowercase(), country.lowercase())
        if (numSpeakers > 1) tags.add("multi-speaker")
        
        return VoiceModel(
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
            tags = tags
        )
    }
    
    private fun inferGender(name: String): VoiceGender {
        val femaleName = listOf(
            "amy", "alba", "jenny", "cori", "kathleen", "kristin", "lessac", "ljspeech",
            "eva", "kerstin", "ramona", "siwis", "rapunzelina", "carla", "daniela",
            "anna", "berta", "paola", "irina", "lada", "lisa", "nathalie", "gosia",
            "maya", "meera", "priyamvada", "padmavathi", "lili", "salka", "ugla",
            "natia", "raya", "marylux", "ona", "huayan"
        )
        val maleName = listOf(
            "alan", "ryan", "joe", "john", "danny", "bryce", "norman", "sam", "kusal",
            "thorsten", "karlsson", "pavoque", "gilles", "tom", "riccardo", "faber",
            "dmitri", "ruslan", "denis", "kareem", "amir", "harri", "imre", "mihai",
            "artur", "darkman", "fettah", "fahrettin", "aivars", "arjun", "venkatesh",
            "rohan", "pratham", "bui", "steinn", "pim", "ronnie", "cadu", "jeff"
        )
        
        val lowerName = name.lowercase()
        return when {
            femaleName.any { lowerName.contains(it) } -> VoiceGender.FEMALE
            maleName.any { lowerName.contains(it) } -> VoiceGender.MALE
            lowerName.contains("female") -> VoiceGender.FEMALE
            lowerName.contains("male") -> VoiceGender.MALE
            else -> VoiceGender.NEUTRAL
        }
    }
    
    fun getVoiceById(id: String): VoiceModel? = getAllVoices().find { it.id == id }
    
    fun getVoicesByLanguage(language: String): List<VoiceModel> {
        return getAllVoices().filter { it.language == language }
    }
    
    fun getSupportedLanguages(): List<String> {
        return getAllVoices().map { it.language }.distinct().sorted()
    }

    
    // Fallback voices for offline use or when fetch fails
    private val fallbackVoices = listOf(
        VoiceModel(
            id = "en_US-lessac-medium",
            name = "English (US) - Lessac - Medium",
            language = "en",
            locale = "en-US",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63201294L,
            downloadUrl = "${BASE_DOWNLOAD_URL}en/en_US/lessac/medium/en_US-lessac-medium.onnx",
            configUrl = "${BASE_DOWNLOAD_URL}en/en_US/lessac/medium/en_US-lessac-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Professional US English voice",
            tags = listOf("english", "us", "male")
        ),
        VoiceModel(
            id = "en_US-amy-medium",
            name = "English (US) - Amy - Medium",
            language = "en",
            locale = "en-US",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63201294L,
            downloadUrl = "${BASE_DOWNLOAD_URL}en/en_US/amy/medium/en_US-amy-medium.onnx",
            configUrl = "${BASE_DOWNLOAD_URL}en/en_US/amy/medium/en_US-amy-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural US English female voice",
            tags = listOf("english", "us", "female")
        ),
        VoiceModel(
            id = "en_GB-alan-medium",
            name = "English (GB) - Alan - Medium",
            language = "en",
            locale = "en-GB",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63201294L,
            downloadUrl = "${BASE_DOWNLOAD_URL}en/en_GB/alan/medium/en_GB-alan-medium.onnx",
            configUrl = "${BASE_DOWNLOAD_URL}en/en_GB/alan/medium/en_GB-alan-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "British English male voice",
            tags = listOf("english", "british", "male")
        ),
        VoiceModel(
            id = "de_DE-thorsten-medium",
            name = "German - Thorsten - Medium",
            language = "de",
            locale = "de-DE",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63201294L,
            downloadUrl = "${BASE_DOWNLOAD_URL}de/de_DE/thorsten/medium/de_DE-thorsten-medium.onnx",
            configUrl = "${BASE_DOWNLOAD_URL}de/de_DE/thorsten/medium/de_DE-thorsten-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "German male voice",
            tags = listOf("german", "germany", "male")
        ),
        VoiceModel(
            id = "fr_FR-siwis-medium",
            name = "French - Siwis - Medium",
            language = "fr",
            locale = "fr-FR",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63201294L,
            downloadUrl = "${BASE_DOWNLOAD_URL}fr/fr_FR/siwis/medium/fr_FR-siwis-medium.onnx",
            configUrl = "${BASE_DOWNLOAD_URL}fr/fr_FR/siwis/medium/fr_FR-siwis-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "French female voice",
            tags = listOf("french", "france", "female")
        ),
        VoiceModel(
            id = "es_ES-davefx-medium",
            name = "Spanish (Spain) - Davefx - Medium",
            language = "es",
            locale = "es-ES",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63201294L,
            downloadUrl = "${BASE_DOWNLOAD_URL}es/es_ES/davefx/medium/es_ES-davefx-medium.onnx",
            configUrl = "${BASE_DOWNLOAD_URL}es/es_ES/davefx/medium/es_ES-davefx-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Spanish male voice",
            tags = listOf("spanish", "spain", "male")
        ),
        VoiceModel(
            id = "it_IT-paola-medium",
            name = "Italian - Paola - Medium",
            language = "it",
            locale = "it-IT",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63511038L,
            downloadUrl = "${BASE_DOWNLOAD_URL}it/it_IT/paola/medium/it_IT-paola-medium.onnx",
            configUrl = "${BASE_DOWNLOAD_URL}it/it_IT/paola/medium/it_IT-paola-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Italian female voice",
            tags = listOf("italian", "italy", "female")
        ),
        VoiceModel(
            id = "pt_BR-faber-medium",
            name = "Portuguese (Brazil) - Faber - Medium",
            language = "pt",
            locale = "pt-BR",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63201294L,
            downloadUrl = "${BASE_DOWNLOAD_URL}pt/pt_BR/faber/medium/pt_BR-faber-medium.onnx",
            configUrl = "${BASE_DOWNLOAD_URL}pt/pt_BR/faber/medium/pt_BR-faber-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Brazilian Portuguese male voice",
            tags = listOf("portuguese", "brazil", "male")
        ),
        VoiceModel(
            id = "ru_RU-dmitri-medium",
            name = "Russian - Dmitri - Medium",
            language = "ru",
            locale = "ru-RU",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63201294L,
            downloadUrl = "${BASE_DOWNLOAD_URL}ru/ru_RU/dmitri/medium/ru_RU-dmitri-medium.onnx",
            configUrl = "${BASE_DOWNLOAD_URL}ru/ru_RU/dmitri/medium/ru_RU-dmitri-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Russian male voice",
            tags = listOf("russian", "russia", "male")
        ),
        VoiceModel(
            id = "zh_CN-huayan-medium",
            name = "Chinese (Mandarin) - Huayan - Medium",
            language = "zh",
            locale = "zh-CN",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63201294L,
            downloadUrl = "${BASE_DOWNLOAD_URL}zh/zh_CN/huayan/medium/zh_CN-huayan-medium.onnx",
            configUrl = "${BASE_DOWNLOAD_URL}zh/zh_CN/huayan/medium/zh_CN-huayan-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Mandarin Chinese female voice",
            tags = listOf("chinese", "mandarin", "female")
        )
    )
}
