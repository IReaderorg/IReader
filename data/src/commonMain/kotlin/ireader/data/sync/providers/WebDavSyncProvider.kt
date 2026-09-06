package ireader.data.sync.providers

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import ireader.core.log.Log
import ireader.domain.models.sync.SyncProviderType
import ireader.domain.models.sync.UnifiedSyncManifest
import ireader.domain.preferences.prefs.SyncPreferences
import ireader.domain.services.sync.SyncProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Custom Cloud (WebDAV / Nextcloud / TrueNAS) implementation of SyncProvider.
 * Allows users to host their sync manifest on their own private servers without relying on Supabase.
 */
class WebDavSyncProvider(
    private val syncPreferences: SyncPreferences,
    private val httpClient: HttpClient = HttpClient()
) : SyncProvider {

    companion object {
        private const val TAG = "WebDavSyncProvider"
        private const val MANIFEST_FILE_NAME = "sync_manifest.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    override val type: SyncProviderType = SyncProviderType.CUSTOM_CLOUD
    override val name: String = "Custom Cloud (Nextcloud / TrueNAS / WebDAV)"

    override suspend fun isAuthenticated(): Boolean {
        return syncPreferences.isCustomCloudConfigured()
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun getAuthHeader(): String? {
        val user = syncPreferences.customCloudUsername().get().trim()
        val pass = syncPreferences.customCloudPassword().get().trim()
        if (user.isBlank() && pass.isBlank()) return null
        val token = "$user:$pass"
        return "Basic " + Base64.Default.encode(token.encodeToByteArray())
    }

    private fun getManifestUrl(): String {
        val rawUrl = syncPreferences.customCloudUrl().get().trim().trimEnd('/')
        val subpath = syncPreferences.customCloudPath().get().trim().trim('/')
        return if (subpath.isNotBlank()) {
            "$rawUrl/$subpath/$MANIFEST_FILE_NAME"
        } else {
            "$rawUrl/$MANIFEST_FILE_NAME"
        }
    }

    override suspend fun fetchRemoteManifest(): Result<UnifiedSyncManifest?> {
        return try {
            if (!isAuthenticated()) {
                return Result.failure(IllegalStateException("Custom Cloud is not configured with URL and credentials"))
            }

            val targetUrl = getManifestUrl()
            val authHeader = getAuthHeader()

            val response = httpClient.get(targetUrl) {
                if (authHeader != null) {
                    header("Authorization", authHeader)
                }
                header("Accept", "application/json")
            }

            when {
                response.status == HttpStatusCode.NotFound -> {
                    Log.info { "$TAG: No remote manifest found at $targetUrl (fresh cloud setup)" }
                    Result.success(null)
                }
                response.status.isSuccess() -> {
                    val body = response.bodyAsText()
                    if (body.isBlank()) {
                        Result.success(null)
                    } else {
                        val manifest = json.decodeFromString<UnifiedSyncManifest>(body)
                        Result.success(manifest)
                    }
                }
                else -> {
                    val errorMsg = "HTTP ${response.status.value}: ${response.status.description}"
                    Log.warn { "$TAG: Failed to fetch manifest: $errorMsg" }
                    Result.failure(IllegalStateException("Failed to fetch remote manifest ($errorMsg)"))
                }
            }
        } catch (e: Exception) {
            Log.warn { "$TAG: Exception while fetching remote manifest: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun uploadManifest(manifest: UnifiedSyncManifest): Result<Unit> {
        return try {
            if (!isAuthenticated()) {
                return Result.failure(IllegalStateException("Custom Cloud is not configured with URL and credentials"))
            }

            val targetUrl = getManifestUrl()
            val authHeader = getAuthHeader()
            val payload = json.encodeToString(manifest)

            val response = httpClient.put(targetUrl) {
                if (authHeader != null) {
                    header("Authorization", authHeader)
                }
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            if (response.status.isSuccess() || response.status == HttpStatusCode.Created || response.status == HttpStatusCode.NoContent) {
                Log.info { "$TAG: Successfully uploaded manifest to $targetUrl (${payload.length} chars)" }
                Result.success(Unit)
            } else {
                val errorMsg = "HTTP ${response.status.value}: ${response.status.description}"
                Log.error { "$TAG: Upload manifest failed: $errorMsg" }
                Result.failure(IllegalStateException("Failed to upload manifest to Custom Cloud ($errorMsg)"))
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Exception while uploading manifest: ${e.message}" }
            Result.failure(e)
        }
    }
}
