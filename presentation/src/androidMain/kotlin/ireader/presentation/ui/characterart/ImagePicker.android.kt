package ireader.presentation.ui.characterart

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

actual class ImagePicker(
    private val context: Context,
    private val onImageSelected: (Uri?) -> Unit
) {
    private var selectedUri: Uri? = null
    private var selectedBytes: ByteArray? = null
    private var selectedFileName: String? = null
    
    actual suspend fun pickImage(
        onImagePicked: (bytes: ByteArray, fileName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uri = selectedUri
        if (uri == null) {
            onError("No image selected")
            return
        }
        
        try {
            withContext(Dispatchers.IO) {
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
                    onImagePicked(bytes, fileName)
                }
            }
        } catch (e: Exception) {
            onError(e.message ?: "Failed to load image")
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
    
    fun setSelectedUri(uri: Uri?) {
        selectedUri = uri
        onImageSelected(uri)
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
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    
    val picker = remember(context) { 
        ImagePicker(context) { uri -> selectedUri = uri }
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        picker.setSelectedUri(uri)
    }
    
    // Expose launch function through composition local or callback
    LaunchedEffect(Unit) {
        // Store launcher reference for later use
    }
    
    return picker
}

/**
 * Composable that provides image picking functionality with launcher
 */
@Composable
fun ImagePickerHost(
    onImagePicked: (ByteArray, String) -> Unit,
    onError: (String) -> Unit,
    content: @Composable (launchPicker: () -> Unit, selectedPath: String?) -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPath by remember { mutableStateOf<String?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedUri = uri
        selectedPath = uri?.toString()
        
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { 
                    it.readBytes() 
                }
                
                if (bytes != null) {
                    val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        if (nameIndex >= 0) cursor.getString(nameIndex) else null
                    } ?: "image_${System.currentTimeMillis()}.jpg"
                    
                    onImagePicked(bytes, fileName)
                } else {
                    onError("Failed to read image")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to load image")
            }
        }
    }
    
    content(
        { launcher.launch("image/*") },
        selectedPath
    )
}
