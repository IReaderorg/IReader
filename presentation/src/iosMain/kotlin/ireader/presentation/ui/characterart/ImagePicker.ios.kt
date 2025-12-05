package ireader.presentation.ui.characterart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
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

@OptIn(ExperimentalForeignApi::class)
actual class ImagePicker {
    private var selectedPath: String? = null
    private var selectedBytes: ByteArray? = null
    private var pendingCallback: ((ByteArray, String) -> Unit)? = null
    private var pendingErrorCallback: ((String) -> Unit)? = null
    private var delegate: ImagePickerDelegateImpl? = null
    
    actual fun launchPicker() {
        MainScope().launch {
            pickImage(
                onImagePicked = { bytes, name ->
                    pendingCallback?.invoke(bytes, name)
                },
                onError = { error ->
                    pendingErrorCallback?.invoke(error)
                }
            )
        }
    }
    
    actual suspend fun pickImage(
        onImagePicked: (bytes: ByteArray, fileName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            val status = PHPhotoLibrary.authorizationStatus()
            
            when (status) {
                PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> {
                    showImagePicker(onImagePicked, onError)
                }
                PHAuthorizationStatusNotDetermined -> {
                    PHPhotoLibrary.requestAuthorization { newStatus ->
                        if (newStatus == PHAuthorizationStatusAuthorized || 
                            newStatus == PHAuthorizationStatusLimited) {
                            MainScope().launch {
                                showImagePicker(onImagePicked, onError)
                            }
                        } else {
                            onError("Photo library access denied")
                        }
                    }
                }
                else -> {
                    onError("Photo library access denied. Please enable in Settings.")
                }
            }
        }
    }
    
    private fun showImagePicker(
        onImagePicked: (ByteArray, String) -> Unit,
        onError: (String) -> Unit
    ) {
        pendingCallback = onImagePicked
        pendingErrorCallback = onError
        
        val rootViewController = getRootViewController()
        if (rootViewController == null) {
            onError("Unable to present image picker")
            return
        }
        
        if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)) {
            onError("Photo library not available")
            return
        }
        
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        picker.allowsEditing = false
        
        delegate = ImagePickerDelegateImpl(
            onImageSelected = { image, url ->
                handleImageSelected(image, url)
            },
            onCancelled = {
                pendingErrorCallback?.invoke("Image selection cancelled")
                clearCallbacks()
            }
        )
        picker.delegate = delegate
        
        rootViewController.presentViewController(picker, animated = true, completion = null)
    }
    
    private fun handleImageSelected(image: UIImage, url: NSURL?) {
        val imageData = UIImageJPEGRepresentation(image, 0.9)
        
        if (imageData == null) {
            pendingErrorCallback?.invoke("Failed to process image")
            clearCallbacks()
            return
        }
        
        val bytes = imageData.toByteArray()
        val fileName = url?.lastPathComponent ?: "image_${currentTimeMillis()}.jpg"
        
        selectedPath = url?.path
        selectedBytes = bytes
        
        pendingCallback?.invoke(bytes, fileName)
        clearCallbacks()
    }
    
    private fun clearCallbacks() {
        pendingCallback = null
        pendingErrorCallback = null
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
    
    actual fun getSelectedImagePath(): String? = selectedPath
    
    actual fun clearSelection() {
        selectedPath = null
        selectedBytes = null
    }
    
    suspend fun pickFromCamera(
        onImagePicked: (ByteArray, String) -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            val rootViewController = getRootViewController()
            if (rootViewController == null) {
                onError("Unable to present camera")
                return@withContext
            }
            
            if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
                onError("Camera not available")
                return@withContext
            }
            
            pendingCallback = onImagePicked
            pendingErrorCallback = onError
            
            val picker = UIImagePickerController()
            picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            picker.allowsEditing = false
            
            delegate = ImagePickerDelegateImpl(
                onImageSelected = { image, url ->
                    handleImageSelected(image, url)
                },
                onCancelled = {
                    pendingErrorCallback?.invoke("Camera cancelled")
                    clearCallbacks()
                }
            )
            picker.delegate = delegate
            
            rootViewController.presentViewController(picker, animated = true, completion = null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ImagePickerDelegateImpl(
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

@Composable
actual fun rememberImagePicker(): ImagePicker {
    return remember { ImagePicker() }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    if (length == 0) return ByteArray(0)
    
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return bytes
}

private fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
