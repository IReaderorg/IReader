package ireader.presentation.ui.settings.advance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import ireader.domain.models.common.Uri
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.darwin.NSObject

/**
 * iOS PDF document picker delegate
 */
@OptIn(ExperimentalForeignApi::class)
private class PdfDocumentPickerDelegate(
    private val onFilesSelected: (List<Uri>) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {
    
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val uris = didPickDocumentsAtURLs.mapNotNull { url ->
            (url as? NSURL)?.path?.let { Uri.parse(it) }
        }
        onFilesSelected(uris)
    }
    
    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onFilesSelected(emptyList())
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun OnShowImportPdf(show: Boolean, onFileSelected: suspend (List<Uri>) -> Unit) {
    val scope = rememberCoroutineScope()
    var delegate by remember { mutableStateOf<PdfDocumentPickerDelegate?>(null) }
    
    LaunchedEffect(show) {
        if (show) {
            val newDelegate = PdfDocumentPickerDelegate { uris ->
                scope.launch {
                    onFileSelected(uris)
                }
            }
            delegate = newDelegate
            
            val documentTypes = listOf(UTTypePDF)
            val picker = UIDocumentPickerViewController(
                forOpeningContentTypes = documentTypes,
                asCopy = true
            )
            picker.delegate = newDelegate
            picker.allowsMultipleSelection = true
            
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                picker,
                animated = true,
                completion = null
            )
        }
    }
}
