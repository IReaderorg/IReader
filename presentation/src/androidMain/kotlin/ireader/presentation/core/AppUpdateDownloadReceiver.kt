package ireader.presentation.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import ireader.presentation.ui.update.AppUpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Android-specific broadcast receiver for handling app update download events
 */
class AppUpdateDownloadReceiver(
    private val updateState: MutableStateFlow<AppUpdateState>
) : BroadcastReceiver() {
    
    companion object {
        fun createIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction("ireader.UPDATE_DOWNLOAD_CONNECTING")
                addAction("ireader.UPDATE_DOWNLOAD_PROGRESS")
                addAction("ireader.UPDATE_DOWNLOAD_COMPLETE")
                addAction("ireader.UPDATE_DOWNLOAD_ERROR")
                addAction("ireader.UPDATE_DOWNLOAD_CANCELLED")
            }
        }
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "ireader.UPDATE_DOWNLOAD_CONNECTING" -> {
                updateState.update { current ->
                    current.copy(
                        isConnecting = true,
                        isDownloading = true,
                        isDownloaded = false,
                        downloadedFilePath = null,
                        error = null,
                        downloadProgress = 0f
                    )
                }
            }
            
            "ireader.UPDATE_DOWNLOAD_PROGRESS" -> {
                val progress = intent.getFloatExtra("progress", 0f)
                updateState.update { current ->
                    current.copy(
                        isConnecting = false,
                        isDownloading = true,
                        isDownloaded = false,
                        error = null,
                        downloadProgress = if (progress >= 0f) progress else current.downloadProgress
                    )
                }
            }
            
            "ireader.UPDATE_DOWNLOAD_COMPLETE" -> {
                val filePath = intent.getStringExtra("file_path")
                updateState.update { current ->
                    current.copy(
                        isConnecting = false,
                        isDownloading = false,
                        isDownloaded = true,
                        downloadedFilePath = filePath,
                        downloadProgress = 1f,
                        error = null
                    )
                }
            }
            
            "ireader.UPDATE_DOWNLOAD_ERROR" -> {
                val error = intent.getStringExtra("error")
                updateState.update { current ->
                    current.copy(
                        isConnecting = false,
                        isDownloading = false,
                        isDownloaded = false,
                        downloadedFilePath = null,
                        error = error,
                        downloadProgress = 0f
                    )
                }
            }
            
            "ireader.UPDATE_DOWNLOAD_CANCELLED" -> {
                updateState.update { current ->
                    current.copy(
                        isConnecting = false,
                        isDownloading = false,
                        isDownloaded = false,
                        downloadedFilePath = null,
                        downloadProgress = 0f,
                        error = null
                    )
                }
            }
        }
    }
}