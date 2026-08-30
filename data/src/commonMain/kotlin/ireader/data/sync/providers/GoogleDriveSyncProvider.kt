package ireader.data.sync.providers

import ireader.core.log.Log
import ireader.data.backup.GoogleDriveAuthenticator
import ireader.data.backup.GoogleDriveClient
import ireader.domain.models.sync.SyncProviderType
import ireader.domain.models.sync.UnifiedSyncManifest
import ireader.domain.services.sync.SyncProvider
import kotlinx.serialization.json.Json

/**
 * Google Drive implementation of SyncProvider.
 * Synchronizes lightweight delta manifests (sync_manifest.json) via Google Drive API.
 */
class GoogleDriveSyncProvider(
    private val authenticator: GoogleDriveAuthenticator
) : SyncProvider {

    companion object {
        private const val TAG = "GoogleDriveSyncProvider"
        private const val SYNC_MANIFEST_FILE_NAME = "sync_manifest.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private var driveClient: GoogleDriveClient? = null

    private fun getClient(): GoogleDriveClient {
        return driveClient ?: GoogleDriveClient(
            getAccessToken = { authenticator.getAccessToken() }
        ).also { driveClient = it }
    }

    override val type: SyncProviderType = SyncProviderType.GOOGLE_DRIVE
    override val name: String = "Google Drive"

    override suspend fun isAuthenticated(): Boolean {
        return authenticator.isAuthenticated()
    }

    override suspend fun fetchRemoteManifest(): Result<UnifiedSyncManifest?> {
        return try {
            if (!isAuthenticated()) {
                return Result.failure(IllegalStateException("Google Drive not authenticated"))
            }

            val client = getClient()
            val filesResult = client.listFiles(SYNC_MANIFEST_FILE_NAME)
            val manifestFile = filesResult.getOrNull()?.firstOrNull()

            if (manifestFile != null) {
                val downloadResult = client.downloadFile(manifestFile.id)
                val bytes = downloadResult.getOrNull()
                if (bytes != null) {
                    val manifest = json.decodeFromString<UnifiedSyncManifest>(bytes.decodeToString())
                    Result.success(manifest)
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.warn { "$TAG: Failed to fetch remote manifest: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun uploadManifest(manifest: UnifiedSyncManifest): Result<Unit> {
        return try {
            if (!isAuthenticated()) {
                return Result.failure(IllegalStateException("Google Drive not authenticated"))
            }

            val client = getClient()
            val jsonString = json.encodeToString(UnifiedSyncManifest.serializer(), manifest)
            val jsonBytes = jsonString.encodeToByteArray()

            // Check if file already exists to overwrite or create new
            val existingFiles = client.listFiles(SYNC_MANIFEST_FILE_NAME).getOrNull()
            val existingFile = existingFiles?.firstOrNull()

            if (existingFile != null) {
                // Delete old file and upload updated version
                client.deleteFile(existingFile.id)
            }

            client.uploadFile(
                fileName = SYNC_MANIFEST_FILE_NAME,
                mimeType = "application/json",
                content = jsonBytes
            ).map { Unit }

        } catch (e: Exception) {
            Log.error { "$TAG: Failed to upload manifest: ${e.message}" }
            Result.failure(e)
        }
    }
}
