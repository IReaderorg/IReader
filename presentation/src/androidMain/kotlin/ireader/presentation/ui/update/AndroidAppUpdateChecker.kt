package ireader.presentation.ui.update

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.domain.models.update_service_models.Release
import ireader.domain.models.update_service_models.Version
import ireader.domain.utils.extensions.ioDispatcher
import ireader.domain.preferences.prefs.AppPreferences
import ireader.i18n.BuildKonfig
import ireader.i18n.github_api_url
import ireader.i18n.repo_url
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Android implementation of AppUpdateChecker
 */
class AndroidAppUpdateChecker(
    private val context: Application,
    private val httpClients: HttpClients,
    @Suppress("UNUSED_PARAMETER") appPreferences: AppPreferences,
) : AppUpdateChecker {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    companion object {
        private const val TAG = "AndroidAppUpdateChecker"
    }
    
    override suspend fun checkForUpdate(): Result<Release?> = withContext(ioDispatcher) {
        try {
            // Determine if current version is prerelease or stable
            val currentVersion = BuildKonfig.VERSION_NAME
            val isCurrentPrerelease = isPrerelease(currentVersion)
            
            // Get appropriate release based on current version type
            val release = if (isCurrentPrerelease) {
                getLatestPrerelease()
            } else {
                getLatestStableRelease()
            }
            
            release?.let { rel ->
                Result.success(rel)
            } ?: Result.success(null)
        } catch (e: Exception) {
            Log.error("$TAG: Error checking for updates", e)
            Result.failure(e)
        }
    }
    
    private suspend fun getLatestStableRelease(): Release? {
        val url = "$github_api_url$repo_url"
        
        val response = httpClients.default.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                append(HttpHeaders.UserAgent, "IReader-App")
            }
        }
        
        if (response.status.value != 200) {
            return null
        }
        
        return json.decodeFromString<Release>(response.bodyAsText())
    }
    
    private suspend fun getLatestPrerelease(): Release? {
        val url = "$github_api_url/repos/ireaderorg/IReader/releases"
        
        val response = httpClients.default.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                append(HttpHeaders.UserAgent, "IReader-App")
            }
        }
        
        if (response.status.value != 200) {
            return null
        }
        
        val releases = json.decodeFromString<List<Release>>(response.bodyAsText())
        
        // Find the latest prerelease
        return releases.firstOrNull { release ->
            release.prerelease == true || release.draft == true || isPrerelease(release.tag_name ?: "")
        }
    }
    
    private fun isPrerelease(version: String): Boolean {
        val lowerVersion = version.lowercase()
        return lowerVersion.contains("alpha") || 
               lowerVersion.contains("beta") || 
               lowerVersion.contains("rc") || 
               lowerVersion.contains("preview") || 
               lowerVersion.contains("dev") ||
               lowerVersion.contains("-pre") ||
               lowerVersion.contains("snapshot")
    }
    
    override suspend fun downloadApk(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            // Extract version from filename or URL for display
            val version = fileName.substringAfter("IReader-v").substringBefore("-release.apk")
                .takeIf { it.isNotEmpty() } ?: fileName.substringBefore(".apk")
            
            // Pre-resolve GitHub redirect to get direct CDN URL
            // This reduces connection time by ~2-3 seconds
            val directUrl = resolveGitHubRedirect(url) ?: url
            
            // Start the download service with resolved URL
            ireader.domain.services.update_service.AppUpdateDownloadService.startDownload(
                context = context,
                downloadUrl = directUrl,
                fileName = fileName,
                version = version
            )
            
            onProgress(0f)
        } catch (e: Exception) {
            onError(e.message ?: "Failed to start download")
        }
    }
    
    /**
     * Pre-resolve GitHub release download URL to get direct CDN URL.
     * GitHub redirects release downloads through multiple servers.
     * By resolving this upfront, we skip the redirect chain during actual download.
     */
    private suspend fun resolveGitHubRedirect(url: String): String? {
        return try {
            // Make a HEAD request to follow redirects without downloading
            val response = httpClients.default.head(url) {
                headers {
                    append(HttpHeaders.UserAgent, "IReader-App")
                }
            }
            
            // Get the final URL after redirects
            val finalUrl = response.call.request.url.toString()
            if (finalUrl != url && finalUrl.contains("githubusercontent.com")) {
                finalUrl
            } else {
                null
            }
        } catch (e: Exception) {
            // If redirect resolution fails, use original URL
            null
        }
    }
    
    override fun installApk(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return
        
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        context.startActivity(intent)
    }
    
    override fun cancelDownload() {
        ireader.domain.services.update_service.AppUpdateDownloadService.cancelDownload(context)
    }
}
