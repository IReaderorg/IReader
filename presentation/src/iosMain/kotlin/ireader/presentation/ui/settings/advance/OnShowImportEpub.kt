package ireader.presentation.ui.settings.advance

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
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeZIP
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private class EpubDocumentPickerDelegate(
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

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun OnShowImportEpub(show: Boolean, onFileSelected: suspend (List<Uri>) -> Unit) {
    val scope = rememberCoroutineScope()
    var delegate by remember { mutableStateOf<EpubDocumentPickerDelegate?>(null) }
    
    LaunchedEffect(show) {
        if (show) {
            withContext(Dispatchers.Main) {
                val rootViewController = getRootViewController()
                if (rootViewController == null) {
                    scope.launchIO { onFileSelected(emptyList()) }
                    return@withContext
                }
                
                // EPUB and ZIP content types
                val epubType = UTType.typeWithFilenameExtension("epub") ?: UTTypeData
                val contentTypes = listOf(epubType, UTTypeZIP)
                
                val picker = UIDocumentPickerViewController(
                    forOpeningContentTypes = contentTypes,
                    asCopy = true
                )
                
                delegate = EpubDocumentPickerDelegate { path ->
                    scope.launchIO {
                        if (path != null) {
                            onFileSelected(listOf(Uri.parse(path)))
                        } else {
                            onFileSelected(emptyList())
                        }
                    }
                }
                
                picker.delegate = delegate
                rootViewController.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
