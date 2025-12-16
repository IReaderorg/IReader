package ireader.presentation.ui.update

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.domain.models.update_service_models.Release
import ireader.domain.models.update_service_models.Version
import ireader.i18n.BuildKonfig
import ireader.i18n.github_api_url
import ireader.i18n.repo_url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.net.URI

/**
 * Desktop implementation of AppUpdateChecker
 * Opens browser for download instead of in-app download
 */
class DesktopAppUpdateChecker(
    private val httpClients: HttpClients,
) : AppUpdateChecker {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    companion object {
        private const val TAG = "DesktopAppUpdateChecker"
    }
    
    override suspend fun checkForUpdate(): Result<Release?> = withContext(Dispatchers.IO) {
        try {
            val url = github_api_url + repo_url
            Log.info { "$TAG: Checking for updates at $url" }
            
            val response = httpClients.default.get(url) {
                headers {
                    append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                    append(HttpHeaders.UserAgent, "IReader-Desktop")
                }
            }
            
            if (response.status.value != 200) {
                return@withContext Result.failure(Exception("Failed to fetch release: ${response.status}"))
            }
            
            val responseText = response.bodyAsText()
            val release = json.decodeFromString<Release>(responseText)
            
            val tagName = release.tag_name
            if (tagName != null && Version.isNewVersion(tagName, BuildKonfig.VERSION_NAME)) {
                Result.success(release)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.error("$TAG: Error checking for updates", e)
            Result.failure(e)
        }
    }
    
    override suspend fun downloadApk(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        // On desktop, open browser to download page
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
                onComplete(url)
            } else {
                onError("Desktop browsing not supported")
            }
        } catch (e: Exception) {
            onError(e.message ?: "Failed to open browser")
        }
    }
    
    override fun installApk(filePath: String) {
        // Not applicable for desktop
        Log.info { "$TAG: Install not supported on desktop" }
    }
    
    override fun cancelDownload() {
        // No-op for desktop
    }
}
