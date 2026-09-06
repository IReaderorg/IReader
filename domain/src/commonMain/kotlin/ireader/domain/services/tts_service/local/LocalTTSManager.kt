package ireader.domain.services.tts_service.local

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.core.log.Log
import ireader.domain.services.tts_service.GradioAudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Manager for Local TTS Server configuration, health checking, voice discovery, and engine creation.
 */
class LocalTTSManager(
    private val httpClient: HttpClient,
    private val audioPlayerFactory: () -> GradioAudioPlayer,
    private val saveConfig: (String) -> Unit,
    private val loadConfig: () -> String?
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val _config = MutableStateFlow(loadInitialConfig())
    val config: StateFlow<LocalTTSConfig> = _config.asStateFlow()

    companion object {
        private const val TAG = "LocalTTSManager"
        val DEFAULT_VOICES = listOf(
            LocalTTSVoice(id = "default", name = "Persian (Standard - Thomcles)", language = "fa", gender = "female"),
            LocalTTSVoice(id = "persian_female", name = "Persian Female Voice", language = "fa", gender = "female"),
            LocalTTSVoice(id = "persian_male", name = "Persian Male Voice", language = "fa", gender = "male")
        )
    }

    private fun loadInitialConfig(): LocalTTSConfig {
        return try {
            val savedJson = loadConfig()
            if (!savedJson.isNullOrBlank()) {
                json.decodeFromString<LocalTTSConfig>(savedJson)
            } else {
                LocalTTSConfig()
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to parse saved LocalTTSConfig: ${e.message}" }
            LocalTTSConfig()
        }
    }

    /**
     * Updates and persists the Local TTS configuration.
     */
    fun updateConfig(newConfig: LocalTTSConfig) {
        _config.value = newConfig
        try {
            val encoded = json.encodeToString(LocalTTSConfig.serializer(), newConfig)
            saveConfig(encoded)
            Log.info { "$TAG: Successfully persisted updated LocalTTSConfig" }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to persist LocalTTSConfig: ${e.message}" }
        }
    }

    /**
     * Tests connectivity and checks model status on the target TTS server.
     */
    suspend fun testConnection(url: String? = null, apiKey: String? = null): Result<LocalTTSHealthResponse> {
        val targetUrl = sanitizeUrl(url ?: _config.value.serverUrl)
        val key = apiKey ?: _config.value.apiKey

        return try {
            val endpoint = "$targetUrl/health"
            val response = httpClient.get(endpoint) {
                accept(ContentType.Application.Json)
                key?.takeIf { it.isNotBlank() }?.let {
                    header(HttpHeaders.Authorization, "Bearer $it")
                }
                timeout {
                    requestTimeoutMillis = 10000L
                }
            }

            if (response.status.isSuccess()) {
                val bodyText = response.bodyAsText()
                val health = json.decodeFromString<LocalTTSHealthResponse>(bodyText)
                Result.success(health)
            } else {
                Result.failure(Exception("Server returned HTTP ${response.status.value}: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Health check failed for $targetUrl: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Queries available voices from the server, falling back to default Persian voices.
     */
    suspend fun fetchVoices(url: String? = null, apiKey: String? = null): List<LocalTTSVoice> {
        val targetUrl = sanitizeUrl(url ?: _config.value.serverUrl)
        val key = apiKey ?: _config.value.apiKey

        return try {
            val endpoint = "$targetUrl/api/voices"
            val response = httpClient.get(endpoint) {
                accept(ContentType.Application.Json)
                key?.takeIf { it.isNotBlank() }?.let {
                    header(HttpHeaders.Authorization, "Bearer $it")
                }
                timeout {
                    requestTimeoutMillis = 10000L
                }
            }

            if (response.status.isSuccess()) {
                val bodyText = response.bodyAsText()
                val voices = json.decodeFromString<List<LocalTTSVoice>>(bodyText)
                if (voices.isNotEmpty()) voices else DEFAULT_VOICES
            } else {
                DEFAULT_VOICES
            }
        } catch (e: Exception) {
            Log.warn { "$TAG: Failed to fetch voices from $targetUrl, using defaults: ${e.message}" }
            DEFAULT_VOICES
        }
    }

    /**
     * Creates an instance of [LocalTTSEngine] with the current or supplied configuration.
     */
    fun createEngine(config: LocalTTSConfig = this.config.value): LocalTTSEngine {
        return LocalTTSEngine(
            config = config,
            httpClient = httpClient,
            audioPlayer = audioPlayerFactory()
        )
    }

    private fun sanitizeUrl(rawUrl: String): String {
        var url = rawUrl.trim().trimEnd('/')
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        return url
    }
}
