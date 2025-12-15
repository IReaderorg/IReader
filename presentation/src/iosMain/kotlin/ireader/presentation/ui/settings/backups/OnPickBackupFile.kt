package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import ireader.domain.models.common.Uri
import ireader.domain.utils.extensions.launchIO
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeGZIP
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private class BackupPickerDelegate(
    private val onResult: (String?) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {
    
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        onResult(url?.path)
    }
    
    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun getRootViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull { window ->
            (window as? UIWindow)?.isKeyWindow() == true
        } as? UIWindow
    
    var rootVC = keyWindow?.rootViewController
    while (rootVC?.presentedViewController != null) {
        rootVC = rootVC.presentedViewController
    }
    return rootVC
}

/**
 * iOS implementation of backup file picker for restore
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun OnPickBackupFile(
    show: Boolean,
    onFileSelected: (Uri?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var delegate by remember { mutableStateOf<BackupPickerDelegate?>(null) }
    
    LaunchedEffect(show) {
        if (show) {
            withContext(Dispatchers.Main) {
                val rootViewController = getRootViewController()
                if (rootViewController == null) {
                    scope.launchIO { onFileSelected(null) }
                    return@withContext
                }
                
                // GZIP and JSON content types
                val gzipType = UTType.typeWithFilenameExtension("gz") ?: UTTypeGZIP
                val jsonType = UTTypeJSON
                val contentTypes = listOf(gzipType, jsonType)
                
                val picker = UIDocumentPickerViewController(
                    forOpeningContentTypes = contentTypes,
                    asCopy = true
                )
                
                delegate = BackupPickerDelegate { path ->
                    scope.launchIO {
                        if (path != null) {
                            onFileSelected(Uri.parse(path))
                        } else {
                            onFileSelected(null)
                        }
                    }
                }
                
                picker.delegate = delegate
                rootViewController.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

/**
 * iOS implementation of backup file saver
 * Note: iOS uses UIDocumentPickerViewController for export as well
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun OnSaveBackupFile(
    show: Boolean,
    defaultFileName: String,
    onLocationSelected: (Uri?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var delegate by remember { mutableStateOf<BackupPickerDelegate?>(null) }
    
    LaunchedEffect(show) {
        if (show) {
            withContext(Dispatchers.Main) {
                val rootViewController = getRootViewController()
                if (rootViewController == null) {
                    scope.launchIO { onLocationSelected(null) }
                    return@withContext
                }
                
                // For saving, we need to use a different approach on iOS
                // UIDocumentPickerViewController with forExportingURLs requires an existing file
                // For now, we'll use the Documents directory and let the user share/export
                // This is a simplified implementation - full implementation would need
                // to create a temp file first and then use forExportingURLs
                
                val gzipType = UTType.typeWithFilenameExtension("gz") ?: UTTypeGZIP
                val contentTypes = listOf(gzipType)
                
                val picker = UIDocumentPickerViewController(
                    forOpeningContentTypes = contentTypes,
                    asCopy = false
                )
                
                delegate = BackupPickerDelegate { path ->
                    scope.launchIO {
                        if (path != null) {
                            onLocationSelected(Uri.parse(path))
                        } else {
                            onLocationSelected(null)
                        }
                    }
                }
                
                picker.delegate = delegate
                rootViewController.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
