package ireader.presentation.ui.characterart

import androidx.compose.runtime.*
import platform.UIKit.*
import platform.Foundation.*
import platform.darwin.NSObject
import platform.Photos.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import kotlin.coroutines.resume

/**
 * iOS implementation of ImagePicker
 * 
 * Uses UIImagePickerController for image selection
 */
@OptIn(ExperimentalForeignApi::class)
actual class ImagePicker {
    private var selectedPath: String? = null
    private var selectedBytes: ByteArray? = null
    private var pendingCallback: ((ByteArray, String) -> Unit)? = null
    private var pendingErrorCallback: ((String) -> Unit)? = null
    
    /**
     * Pick an image from the photo library
     */
    actual suspend fun pickImage(
        onImagePicked: (bytes: ByteArray, fileName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            // Check photo library permission
            val status = PHPhotoLibrary.authorizationStatus()
            
            when (status) {
                PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> {
                    showImagePicker(onImagePicked, onError)
                }
                PHAuthorizationStatusNotDetermined -> {
                    // Request permission
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
    
    /**
     * Show the image picker UI
     */
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
        
        // Check if image picker is available
        if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceTypePhotoLibrary)) {
            onError("Photo library not available")
            return
        }
        
        val picker = UIImagePickerController().apply {
            sourceType = UIImagePickerControllerSourceTypePhotoLibrary
            allowsEditing = false
            
            // Set delegate
            delegate = ImagePickerDelegate(
                onImageSelected = { image, url ->
                    handleImageSelected(image, url)
                },
                onCancelled = {
                    pendingErrorCallback?.invoke("Image selection cancelled")
                    clearCallbacks()
                }
            )
        }
        
        rootViewController.presentViewController(picker, animated = true, completion = null)
    }
    
    /**
     * Handle selected image
     */
    private fun handleImageSelected(image: UIImage, url: NSURL?) {
        // Convert UIImage to bytes
        val imageData = UIImageJPEGRepresentation(image, 0.9)
        
        if (imageData == null) {
            pendingErrorCallback?.invoke("Failed to process image")
            clearCallbacks()
            return
        }
        
        val bytes = imageData.toByteArray()
        val fileName = url?.lastPathComponent ?: "image_${System.currentTimeMillis()}.jpg"
        
        selectedPath = url?.path
        selectedBytes = bytes
        
        pendingCallback?.invoke(bytes, fileName)
        clearCallbacks()
    }
    
    private fun clearCallbacks() {
        pendingCallback = null
        pendingErrorCallback = null
    }
    
    /**
     * Get the root view controller
     */
    private fun getRootViewController(): UIViewController? {
        val keyWindow = UIApplication.sharedApplication.keyWindow
            ?: UIApplication.sharedApplication.windows.firstOrNull { 
                (it as? UIWindow)?.isKeyWindow == true 
            } as? UIWindow
        
        var rootVC = keyWindow?.rootViewController
        
        while (rootVC?.presentedViewController != null) {
            rootVC = rootVC.presentedViewController
        }
        
        return rootVC
    }
    
    actual fun getSelectedImagePath(): String? {
        return selectedPath
    }
    
    actual fun clearSelection() {
        selectedPath = null
        selectedBytes = null
    }
    
    /**
     * Pick image from camera
     */
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
            
            if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceTypeCamera)) {
                onError("Camera not available")
                return@withContext
            }
            
            pendingCallback = onImagePicked
            pendingErrorCallback = onError
            
            val picker = UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceTypeCamera
                allowsEditing = false
                
                delegate = ImagePickerDelegate(
                    onImageSelected = { image, url ->
                        handleImageSelected(image, url)
                    },
                    onCancelled = {
                        pendingErrorCallback?.invoke("Camera cancelled")
                        clearCallbacks()
                    }
                )
            }
            
            rootViewController.presentViewController(picker, animated = true, completion = null)
        }
    }
}

/**
 * UIImagePickerController delegate
 */
@OptIn(ExperimentalForeignApi::class)
private class ImagePickerDelegate(
    private val onImageSelected: (UIImage, NSURL?) -> Unit,
    private val onCancelled: () -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        
        // Get the selected image
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

/**
 * Composable that provides image picking functionality for iOS
 */
@Composable
fun ImagePickerHost(
    onImagePicked: (ByteArray, String) -> Unit,
    onError: (String) -> Unit,
    content: @Composable (launchPicker: () -> Unit, selectedPath: String?) -> Unit
) {
    val picker = remember { ImagePicker() }
    var selectedPath by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    content(
        launchPicker = {
            scope.launch {
                picker.pickImage(
                    onImagePicked = { bytes, fileName ->
                        selectedPath = picker.getSelectedImagePath()
                        onImagePicked(bytes, fileName)
                    },
                    onError = onError
                )
            }
        },
        selectedPath = selectedPath
    )
}

/**
 * Extension to convert NSData to ByteArray
 */
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    if (length == 0) return ByteArray(0)
    
    return ByteArray(length).apply {
        usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}

private object System {
    fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
}
