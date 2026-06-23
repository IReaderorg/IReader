package ireader.presentation.core

import android.content.Context
import android.os.Build
import ireader.presentation.ui.update.AppUpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of DownloadEventHandler
 */
actual class DownloadEventHandler actual constructor(
    updateState: MutableStateFlow<AppUpdateState>
) : KoinComponent {
    
    private val context: Context by inject()
    private val receiver = AppUpdateDownloadReceiver(updateState)
    
    init {
        // The download service sends package-explicit broadcasts (Intent.setPackage(packageName)),
        // so this receiver is registered NOT_EXPORTED — the correct, secure pattern for
        // app-internal broadcasts on Android 13+. Implicit (action-only) broadcasts were being
        // dropped on Android 14+ and several OEM ROMs, which left the UI stuck at 0% until the
        // download completed (issue #228).
        val filter = AppUpdateDownloadReceiver.createIntentFilter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }
    
    actual fun cleanup() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
}

/**
 * Create Android-specific download event handler
 */
actual fun createDownloadEventHandler(updateState: MutableStateFlow<AppUpdateState>): DownloadEventHandler {
    return DownloadEventHandler(updateState)
}
