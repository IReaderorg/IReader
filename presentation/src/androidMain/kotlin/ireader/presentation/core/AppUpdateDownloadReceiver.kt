package ireader.presentation.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import ireader.presentation.ui.update.AppUpdateState
import kotlinx.coroutines.flow.MutableStateFlow

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
                updateState.tryEmit(updateState.value.copy(
                    isConnecting = true,
                    isDownloading = true,
                    downloadProgress = 0f
                ))
            }
            
            "ireader.UPDATE_DOWNLOAD_PROGRESS" -> {
                val progress = intent.getFloatExtra("progress", 0f)
                updateState.tryEmit(updateState.value.copy(
                    isConnecting = false,
                    downloadProgress = progress
                ))
            }
            
            "ireader.UPDATE_DOWNLOAD_COMPLETE" -> {
                val filePath = intent.getStringExtra("file_path")
                updateState.value = updateState.value.copy(
                    isConnecting = false,
                    isDownloading = false,
                    isDownloaded = true,
                    downloadedFilePath = filePath,
                    downloadProgress = 1f
                )
            }
            
            "ireader.UPDATE_DOWNLOAD_ERROR" -> {
                val error = intent.getStringExtra("error")
                updateState.value = updateState.value.copy(
                    isConnecting = false,
                    isDownloading = false,
                    error = error,
                    downloadProgress = 0f
                )
            }
            
            "ireader.UPDATE_DOWNLOAD_CANCELLED" -> {
                updateState.value = updateState.value.copy(
                    isConnecting = false,
                    isDownloading = false,
                    downloadProgress = 0f
                )
            }
        }
    }
}