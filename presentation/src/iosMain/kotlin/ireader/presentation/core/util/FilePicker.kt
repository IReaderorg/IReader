package ireader.presentation.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ireader.domain.models.common.Uri
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
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.UniformTypeIdentifiers.UTTypeGZIP
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.UniformTypeIdentifiers.UTTypePlainText
import platform.UniformTypeIdentifiers.UTTypeZIP
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private class DocumentPickerDelegate(
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

private fun getUTTypeForExtension(ext: String): UTType {
    return when (ext.lowercase()) {
        "epub" -> UTType.typeWithFilenameExtension("epub") ?: UTTypeData
        "zip" -> UTTypeZIP
        "gz", "gzip" -> UTTypeGZIP
        "json" -> UTTypeJSON
        "txt" -> UTTypePlainText
        "pdf" -> UTTypePDF
        else -> UTTypeData
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun FilePicker(
    show: Boolean,
    initialDirectory: String?,
    fileExtensions: List<String>,
    onFileSelected: (Uri?) -> Unit
) {
    var delegate by remember { mutableStateOf<DocumentPickerDelegate?>(null) }
    
    LaunchedEffect(show) {
        if (show) {
            withContext(Dispatchers.Main) {
                val rootViewController = getRootViewController()
                if (rootViewController == null) {
                    onFileSelected(null)
                    return@withContext
                }
                
                val contentTypes = if (fileExtensions.isNotEmpty()) {
                    fileExtensions.mapNotNull { ext -> getUTTypeForExtension(ext) }
                } else {
                    listOf(UTTypeData)
                }
                
                val picker = UIDocumentPickerViewController(
                    forOpeningContentTypes = contentTypes,
                    asCopy = true
                )
                
                delegate = DocumentPickerDelegate { path ->
                    if (path != null) {
                        onFileSelected(Uri.parse(path))
                    } else {
                        onFileSelected(null)
                    }
                }
                
                picker.delegate = delegate
                rootViewController.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun DirectoryPicker(
    show: Boolean,
    initialDirectory: String?,
    onFileSelected: (String?) -> Unit
) {
    var delegate by remember { mutableStateOf<DocumentPickerDelegate?>(null) }
    
    LaunchedEffect(show) {
        if (show) {
            withContext(Dispatchers.Main) {
                val rootViewController = getRootViewController()
                if (rootViewController == null) {
                    onFileSelected(null)
                    return@withContext
                }
                
                val picker = UIDocumentPickerViewController(
                    forOpeningContentTypes = listOf(UTTypeFolder),
                    asCopy = false
                )
                
                delegate = DocumentPickerDelegate { path ->
                    onFileSelected(path)
                }
                
                picker.delegate = delegate
                rootViewController.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
