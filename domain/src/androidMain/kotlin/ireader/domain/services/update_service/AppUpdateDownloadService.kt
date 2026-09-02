package ireader.domain.services.update_service
import ireader.domain.utils.DrawableResources

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.*
import java.io.File

/**
 * Foreground service for downloading APK updates with progress notifications
 */
class AppUpdateDownloadService : Service() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private var downloadJob: Job? = null
    
    // Pre-initialize HttpClients to avoid Koin lookup delay during download
    private val httpClients: HttpClients by lazy {
        try {
            org.koin.core.context.GlobalContext.get().get<HttpClients>()
        } catch (e: Exception) {
            Log.error("$TAG: Failed to get HttpClients from Koin during initialization", e)
            throw e
        }
    }
    
    companion object {
        private const val TAG = "AppUpdateDownloadService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "app_update_download"
        private const val CHANNEL_NAME = "App Update Downloads"
        
        const val ACTION_START_DOWNLOAD = "START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "CANCEL_DOWNLOAD"
        const val EXTRA_DOWNLOAD_URL = "download_url"
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_VERSION = "version"
        const val EXTRA_TOTAL_SIZE = "total_size"
        
        fun startDownload(
            context: Context,
            downloadUrl: String,
            fileName: String,
            version: String,
            totalSize: Long = -1L
        ) {
            val intent = Intent(context, AppUpdateDownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_VERSION, version)
                putExtra(EXTRA_TOTAL_SIZE, totalSize)
            }
            context.startForegroundService(intent)
        }
        
        fun cancelDownload(context: Context) {
            val intent = Intent(context, AppUpdateDownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
            }
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Pre-initialize HttpClients to reduce startup delay
        try {
            httpClients
        } catch (e: Exception) {
            // Continue anyway
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val downloadUrl = intent.getStringExtra(EXTRA_DOWNLOAD_URL)
                val fileName = intent.getStringExtra(EXTRA_FILE_NAME)
                val version = intent.getStringExtra(EXTRA_VERSION)
                val totalSize = intent.getLongExtra(EXTRA_TOTAL_SIZE, -1L)
                
                if (downloadUrl != null && fileName != null && version != null) {
                    startDownload(downloadUrl, fileName, version, totalSize)
                } else {
                    stopSelf()
                }
            }
            ACTION_CANCEL_DOWNLOAD -> {
                cancelDownload()
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        downloadJob?.cancel()
        serviceScope.cancel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for app update downloads"
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startDownload(downloadUrl: String, fileName: String, version: String, totalSizeExtra: Long = -1L) {
        // Cancel any existing download
        downloadJob?.cancel()
        
        // Start foreground service with initial notification
        val initialNotification = createProgressNotification(
            version = version,
            progress = 0,
            isIndeterminate = true,
            status = "Starting download..."
        )
        
        try {
            startForeground(NOTIFICATION_ID, initialNotification)
        } catch (e: Exception) {
            // Continue anyway
        }
        
        downloadJob = serviceScope.launch {
            val downloadDir = File(getExternalFilesDir(null), "updates")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val outputFile = File(downloadDir, fileName)

            try {
                // Delete existing file if present before download starts
                if (outputFile.exists()) {
                    outputFile.delete()
                }

                // Broadcast connecting state
                val connectingIntent = Intent("ireader.UPDATE_DOWNLOAD_CONNECTING").apply {
                    setPackage(packageName)
                    putExtra("version", version)
                }
                sendBroadcast(connectingIntent)
                
                httpClients.default.prepareGet(downloadUrl) {
                    headers {
                        append(HttpHeaders.UserAgent, "IReader-App")
                        append(HttpHeaders.AcceptEncoding, "identity")
                    }
                }.execute { httpResponse ->
                    if (httpResponse.status.value !in 200..299) {
                        throw Exception("HTTP error: ${httpResponse.status}")
                    }
                    
                    val rawContentLength = httpResponse.contentLength() ?: 0L
                    val effectiveTotal = when {
                        rawContentLength > 0L -> rawContentLength
                        totalSizeExtra > 0L -> totalSizeExtra
                        else -> -1L
                    }

                    val channel = httpResponse.bodyAsChannel()
                    var downloadedBytes = 0L
                    var lastProgressUpdate = 0L
                    var lastProgressTime = System.currentTimeMillis()
                    
                    outputFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        
                        while (!channel.isClosedForRead && isActive) {
                            val bytesRead = try {
                                channel.readAvailable(buffer)
                            } catch (e: Exception) {
                                throw Exception("Network error during download: ${e.message}")
                            }
                            
                            if (bytesRead <= 0) break
                            
                            try {
                                output.write(buffer, 0, bytesRead)
                                downloadedBytes += bytesRead
                            } catch (e: Exception) {
                                throw Exception("File write error: ${e.message}")
                            }
                            
                            val currentTime = System.currentTimeMillis()
                            val progressBytes = downloadedBytes - lastProgressUpdate
                            val timeDiff = currentTime - lastProgressTime
                            
                            if (progressBytes >= 32 * 1024 || timeDiff >= 100) {
                                lastProgressUpdate = downloadedBytes
                                lastProgressTime = currentTime
                                
                                if (effectiveTotal > 0L) {
                                    val progress = (downloadedBytes.toFloat() / effectiveTotal.toFloat()).coerceIn(0f, 1f)
                                    val progressPercent = (progress * 100).toInt()
                                    val progressMB = downloadedBytes / (1024 * 1024)
                                    val totalMB = effectiveTotal / (1024 * 1024)
                                    
                                    // Update notification
                                    try {
                                        val notification = createProgressNotification(
                                            version = version,
                                            progress = progressPercent,
                                            isIndeterminate = false,
                                            status = "Downloading... ${progressPercent}% (${progressMB}MB / ${totalMB}MB)"
                                        )
                                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                        notificationManager.notify(NOTIFICATION_ID, notification)
                                    } catch (_: Exception) {}
                                    
                                    // Broadcast progress
                                    try {
                                        val progressIntent = Intent("ireader.UPDATE_DOWNLOAD_PROGRESS").apply {
                                            setPackage(packageName)
                                            putExtra("progress", progress)
                                            putExtra("downloaded_bytes", downloadedBytes)
                                            putExtra("total_bytes", effectiveTotal)
                                            putExtra("version", version)
                                        }
                                        sendBroadcast(progressIntent)
                                    } catch (_: Exception) {}
                                } else {
                                    val progressMB = downloadedBytes / (1024 * 1024)
                                    try {
                                        val notification = createProgressNotification(
                                            version = version,
                                            progress = 0,
                                            isIndeterminate = true,
                                            status = "Downloading... ${progressMB}MB"
                                        )
                                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                        notificationManager.notify(NOTIFICATION_ID, notification)
                                    } catch (_: Exception) {}
                                    
                                    try {
                                        val progressIntent = Intent("ireader.UPDATE_DOWNLOAD_PROGRESS").apply {
                                            setPackage(packageName)
                                            putExtra("progress", -1f)
                                            putExtra("downloaded_bytes", downloadedBytes)
                                            putExtra("version", version)
                                        }
                                        sendBroadcast(progressIntent)
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                        output.flush()
                    }
                    
                    // Strict file verification
                    if (!outputFile.exists() || outputFile.length() != downloadedBytes) {
                        outputFile.delete()
                        throw Exception("File verification failed: size mismatch")
                    }
                    
                    // Verify that download wasn't prematurely closed before expected size was received
                    if (effectiveTotal > 0L && downloadedBytes < effectiveTotal) {
                        outputFile.delete()
                        throw Exception("Download incomplete: received $downloadedBytes bytes of expected $effectiveTotal bytes")
                    }
                    
                    // Verify minimum valid APK size (at least 1MB)
                    if (outputFile.length() < 1024 * 1024) {
                        outputFile.delete()
                        throw Exception("Downloaded file is too small to be a valid APK (${outputFile.length() / 1024} KB)")
                    }
                    
                    // Verify valid APK package archive structure
                    val archiveInfo = packageManager.getPackageArchiveInfo(outputFile.absolutePath, 0)
                    if (archiveInfo == null) {
                        outputFile.delete()
                        throw Exception("Corrupt or invalid APK package archive")
                    }
                    
                    // Send final progress
                    try {
                        val finalProgressIntent = Intent("ireader.UPDATE_DOWNLOAD_PROGRESS").apply {
                            setPackage(packageName)
                            putExtra("progress", 1.0f)
                            putExtra("downloaded_bytes", downloadedBytes)
                            putExtra("total_bytes", downloadedBytes)
                            putExtra("version", version)
                        }
                        sendBroadcast(finalProgressIntent)
                    } catch (_: Exception) {}
                    
                    httpResponse
                }
                
                // Download completed successfully
                try {
                    val completionNotification = createCompletionNotification(
                        version = version,
                        filePath = outputFile.absolutePath
                    )
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, completionNotification)
                } catch (_: Exception) {}
                
                // Broadcast completion
                try {
                    val completionIntent = Intent("ireader.UPDATE_DOWNLOAD_COMPLETE").apply {
                        setPackage(packageName)
                        putExtra("file_path", outputFile.absolutePath)
                        putExtra("version", version)
                    }
                    sendBroadcast(completionIntent)
                } catch (_: Exception) {}
                
                delay(3000)
                stopSelf()
                
            } catch (e: kotlinx.coroutines.CancellationException) {
                if (outputFile.exists()) {
                    outputFile.delete()
                }
                
                try {
                    val cancelNotification = createCancelNotification()
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, cancelNotification)
                } catch (_: Exception) {}
                
                try {
                    val cancelIntent = Intent("ireader.UPDATE_DOWNLOAD_CANCELLED").apply {
                        setPackage(packageName)
                    }
                    sendBroadcast(cancelIntent)
                } catch (_: Exception) {}
                
                stopSelf()
            } catch (e: Exception) {
                if (outputFile.exists()) {
                    outputFile.delete()
                }
                
                val errorMessage = when {
                    e.message?.contains("incomplete", ignoreCase = true) == true -> e.message ?: "Download was incomplete"
                    e.message?.contains("Corrupt", ignoreCase = true) == true -> "Corrupt package: download incomplete or invalid"
                    e.message?.contains("Network", ignoreCase = true) == true -> "Network connection lost"
                    e.message?.contains("timeout", ignoreCase = true) == true -> "Download timed out"
                    e.message?.contains("HTTP", ignoreCase = true) == true -> "Server error: ${e.message}"
                    e.message?.contains("File", ignoreCase = true) == true -> "Storage error: ${e.message}"
                    else -> e.message ?: "Download failed"
                }
                
                try {
                    val errorNotification = createErrorNotification(
                        version = version,
                        error = errorMessage
                    )
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, errorNotification)
                } catch (_: Exception) {}
                
                try {
                    val errorIntent = Intent("ireader.UPDATE_DOWNLOAD_ERROR").apply {
                        setPackage(packageName)
                        putExtra("error", errorMessage)
                        putExtra("version", version)
                    }
                    sendBroadcast(errorIntent)
                } catch (_: Exception) {}
                
                delay(5000)
                stopSelf()
            }
        }
    }
    
    private fun cancelDownload() {
        downloadJob?.cancel()
        
        try {
            val downloadDir = File(getExternalFilesDir(null), "updates")
            downloadDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".apk")) {
                    file.delete()
                }
            }
        } catch (_: Exception) {}
        
        val cancelNotification = createCancelNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, cancelNotification)
        
        val cancelIntent = Intent("ireader.UPDATE_DOWNLOAD_CANCELLED").apply {
            setPackage(packageName)
        }
        sendBroadcast(cancelIntent)
        
        stopSelf()
    }
    
    private fun createProgressNotification(
        version: String,
        progress: Int,
        isIndeterminate: Boolean,
        status: String
    ): Notification {
        val cancelIntent = Intent(this, AppUpdateDownloadService::class.java).apply {
            action = ACTION_CANCEL_DOWNLOAD
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(DrawableResources.ic_infinity)
            .setContentTitle("Downloading IReader $version")
            .setContentText(status)
            .setProgress(100, progress, isIndeterminate)
            .setOngoing(true)
            .addAction(
                DrawableResources.baseline_close_24,
                "Cancel",
                cancelPendingIntent
            )
            .build()
    }
    
    private fun createCompletionNotification(version: String, filePath: String): Notification {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )
        
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        val installPendingIntent = PendingIntent.getActivity(
            this,
            0,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(DrawableResources.ic_infinity)
            .setContentTitle("IReader $version downloaded")
            .setContentText("Tap to install the update")
            .setAutoCancel(true)
            .setContentIntent(installPendingIntent)
            .addAction(
                DrawableResources.ic_input_add, // Using system install icon
                "Install",
                installPendingIntent
            )
            .build()
    }
    
    private fun createErrorNotification(version: String, error: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(DrawableResources.ic_infinity)
            .setContentTitle("Download failed")
            .setContentText("Failed to download IReader $version: $error")
            .setAutoCancel(true)
            .build()
    }
    
    private fun createCancelNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(DrawableResources.ic_infinity)
            .setContentTitle("Download cancelled")
            .setContentText("Update download was cancelled")
            .setAutoCancel(true)
            .build()
    }
}
