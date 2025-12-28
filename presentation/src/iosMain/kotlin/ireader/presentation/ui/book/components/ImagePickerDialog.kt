package ireader.presentation.ui.book.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import ireader.core.log.Log
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.posix.memcpy

/**
 * iOS implementation of image picker dialog.
 * Uses UIImagePickerController to select images from the photo library.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ImagePickerDialog(
    show: Boolean,
    onImageSelected: (uri: String) -> Unit,
    onDismiss: () -> Unit
) {
    val pickerState = remember { IosImagePickerState() }
    
    DisposableEffect(show) {
        if (show) {
            MainScope().launch {
                pickerState.showPicker(
                    onImagePicked = { uri ->
                        onImageSelected(uri)
                    },
                    onCancelled = {
                        onDismiss()
                    }
                )
            }
        }
        onDispose {
            pickerState.cleanup()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosImagePickerState {
    private var delegate: ImagePickerDialogDelegate? = null
    
    suspend fun showPicker(
        onImagePicked: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        withContext(Dispatchers.Main) {
            val status = PHPhotoLibrary.authorizationStatus()
            
            when (status) {
                PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> {
                    presentPicker(onImagePicked, onCancelled)
                }
                PHAuthorizationStatusNotDetermined -> {
                    PHPhotoLibrary.requestAuthorization { newStatus ->
                        if (newStatus == PHAuthorizationStatusAuthorized || 
                            newStatus == PHAuthorizationStatusLimited) {
                            MainScope().launch {
                                presentPicker(onImagePicked, onCancelled)
                            }
                        } else {
                            Log.warn { "Photo library access denied" }
                            onCancelled()
                        }
                    }
                }
                else -> {
                    Log.warn { "Photo library access denied. Please enable in Settings." }
                    onCancelled()
                }
            }
        }
    }
    
    private fun presentPicker(
        onImagePicked: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        val rootViewController = getRootViewController()
        if (rootViewController == null) {
            Log.error { "Unable to present image picker - no root view controller" }
            onCancelled()
            return
        }
        
        if (!UIImagePickerController.isSourceTypeAvailable(
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            )) {
            Log.error { "Photo library not available" }
            onCancelled()
            return
        }
        
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        picker.allowsEditing = false
        
        delegate = ImagePickerDialogDelegate(
            onImageSelected = { image, url ->
                handleImageSelected(image, url, onImagePicked, onCancelled)
            },
            onCancelled = onCancelled
        )
        picker.delegate = delegate
        
        rootViewController.presentViewController(picker, animated = true, completion = null)
    }
    
    private fun handleImageSelected(
        image: UIImage,
        url: NSURL?,
        onImagePicked: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        // If we have a URL, use it directly
        if (url != null) {
            val path = url.path
            if (path != null) {
                onImagePicked("file://$path")
                return
            }
        }
        
        // Otherwise, save the image to a temp file and return that path
        val imageData = UIImageJPEGRepresentation(image, 0.9)
        if (imageData == null) {
            Log.error { "Failed to convert image to JPEG" }
            onCancelled()
            return
        }
        
        val tempDir = NSTemporaryDirectory()
        val fileName = "cover_${currentTimeMillis()}.jpg"
        val tempPath = "$tempDir$fileName"
        
        val success = imageData.writeToFile(tempPath, atomically = true)
        if (success) {
            onImagePicked("file://$tempPath")
        } else {
            Log.error { "Failed to save image to temp file" }
            onCancelled()
        }
    }
    
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
    
    fun cleanup() {
        delegate = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ImagePickerDialogDelegate(
    private val onImageSelected: (UIImage, NSURL?) -> Unit,
    private val onCancelled: () -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val url = didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as? NSURL
        
        if (image != null) {
            onImageSelected(image, url)
        } else {
            onCancelled()
        }
    }
    
    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onCancelled()
    }
}

private fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
