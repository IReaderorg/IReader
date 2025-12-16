package ireader.presentation.ui.update

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.domain.models.update_service_models.Release
import ireader.domain.models.update_service_models.Version
import ireader.domain.preferences.prefs.AppPreferences
import ireader.i18n.BuildKonfig
import ireader.i18n.github_api_url
import ireader.i18n.repo_url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Android implementation of AppUpdateChecker
 */
class AndroidAppUpdateChecker(
    private val context: Application,
    private val httpClients: HttpClients,
    private val appPreferences: AppPreferences,
) : AppUpdateChecker {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    private var downloadJob: Job? = null
    
    companion object {
        private const val TAG = "AndroidAppUpdateChecker"
    }
    
    override suspend fun checkForUpdate(): Result<Release?> = withContext(Dispatchers.IO) {
        try {
            val url = github_api_url + repo_url
            Log.info { "$TAG: Checking for updates at $url" }
            
            val response = httpClients.default.get(url) {
                headers {
                    append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                    append(HttpHeaders.UserAgent, "IReader-App")
                }
            }
            
            if (response.status.value != 200) {
                val errorBody = response.bodyAsText()
                Log.error { "$TAG: Failed to fetch release. Status: ${response.status}, Body: $errorBody" }
                return@withContext Result.failure(Exception("Failed to fetch release: ${response.status}"))
            }
            
            val responseText = response.bodyAsText()
            val release = json.decodeFromString<Release>(responseText)
            
            Log.info { "$TAG: Latest release: ${release.tag_name}, Current: ${BuildKonfig.VERSION_NAME}" }
            
            // Check if this is a newer version
            val tagName = release.tag_name
            if (tagName != null && Version.isNewVersion(tagName, BuildKonfig.VERSION_NAME)) {
                Log.info { "$TAG: Update available: $tagName" }
                Result.success(release)
            } else {
                Log.info { "$TAG: No update available" }
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
        withContext(Dispatchers.IO) {
            try {
                Log.info { "$TAG: Starting download from $url" }
                
                // Create download directory
                val downloadDir = File(context.getExternalFilesDir(null), "updates")
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs()
                }
                
                val outputFile = File(downloadDir, fileName)
                
                // Delete existing file if present
                if (outputFile.exists()) {
                    outputFile.delete()
                }
                
                val response = httpClients.default.prepareGet(url) {
                    headers {
                        append(HttpHeaders.UserAgent, "IReader-App")
                    }
                }.execute { httpResponse ->
                    val contentLength = httpResponse.contentLength() ?: 0L
                    Log.info { "$TAG: Content length: $contentLength bytes" }
                    
                    val channel = httpResponse.bodyAsChannel()
                    var downloadedBytes = 0L
                    
                    outputFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        
                        while (!channel.isClosedForRead && isActive) {
                            val bytesRead = channel.readAvailable(buffer)
                            if (bytesRead <= 0) break
                            
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            
                            if (contentLength > 0) {
                                val progress = downloadedBytes.toFloat() / contentLength.toFloat()
                                withContext(Dispatchers.Main) {
                                    onProgress(progress.coerceIn(0f, 1f))
                                }
                            }
                        }
                    }
                    
                    Log.info { "$TAG: Download complete: ${outputFile.absolutePath}" }
                    httpResponse
                }
                
                withContext(Dispatchers.Main) {
                    onComplete(outputFile.absolutePath)
                }
                
            } catch (e: Exception) {
                Log.error("$TAG: Download failed", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Download failed")
                }
            }
        }
    }
    
    override fun installApk(filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.error { "$TAG: APK file not found: $filePath" }
                return
            }
            
            Log.info { "$TAG: Installing APK: $filePath" }
            
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
            
        } catch (e: Exception) {
            Log.error("$TAG: Failed to install APK", e)
            throw e
        }
    }
    
    override fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        Log.info { "$TAG: Download cancelled" }
    }
}
