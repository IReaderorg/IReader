package ireader.presentation.ui.characterart

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

actual class ImagePicker(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onImageReady: (bytes: ByteArray, fileName: String, path: String) -> Unit
) {
    private var selectedUri: Uri? = null
    private var selectedBytes: ByteArray? = null
    private var selectedFileName: String? = null
    
    // Launcher reference - set by rememberImagePicker
    internal var launcher: ActivityResultLauncher<String>? = null
    
    /**
     * Launch the image picker to select an image
     */
    actual fun launchPicker() {
        launcher?.launch("image/*")
    }
    
    /**
     * Called when user selects an image from the picker
     */
    internal fun onUriSelected(uri: Uri?) {
        if (uri == null) return
        
        selectedUri = uri
        
        // Process the image in background
        scope.launch {
            try {
                withContext(ioDispatcher) {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { 
                        it.readBytes() 
                    } ?: throw Exception("Failed to read image")
                    
                    // Validate it's an image
                    val options = BitmapFactory.Options().apply { 
                        inJustDecodeBounds = true 
                    }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    
                    if (options.outWidth <= 0 || options.outHeight <= 0) {
                        throw Exception("Invalid image file")
                    }
                    
                    // Get filename
                    val fileName = getFileName(uri) ?: "image_${System.currentTimeMillis()}.jpg"
                    
                    selectedBytes = bytes
                    selectedFileName = fileName
                    
                    withContext(Dispatchers.Main) {
                        onImageReady(bytes, fileName, uri.toString())
                    }
                }
            } catch (e: Exception) {
                // Error handling - could add error callback
            }
        }
    }
    
    actual suspend fun pickImage(
        onImagePicked: (bytes: ByteArray, fileName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uri = selectedUri
        if (uri == null) {
            launchPicker()
            return
        }
        
        val bytes = selectedBytes
        val fileName = selectedFileName
        
        if (bytes != null && fileName != null) {
            onImagePicked(bytes, fileName)
        } else {
            onError("No image data available")
        }
    }
    
    actual fun getSelectedImagePath(): String? {
        return selectedUri?.toString()
    }
    
    actual fun clearSelection() {
        selectedUri = null
        selectedBytes = null
        selectedFileName = null
    }
    
    private fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            if (nameIndex >= 0) cursor.getString(nameIndex) else null
        }
    }
}

@Composable
actual fun rememberImagePicker(): ImagePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    
    val picker = remember(context, scope) { 
        ImagePicker(context, scope) { bytes, _, path ->
            selectedImageBytes = bytes
            selectedImagePath = path
        }
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        picker.onUriSelected(uri)
    }
    
    // Set launcher reference on picker
    LaunchedEffect(launcher) {
        picker.launcher = launcher
    }
    
    return picker
}
