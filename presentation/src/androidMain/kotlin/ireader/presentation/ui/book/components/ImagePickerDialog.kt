package ireader.presentation.ui.book.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import ireader.core.log.Log

/**
 * Android implementation of image picker using ActivityResultContracts.GetContent.
 * 
 * Launches the system image picker when [show] is true.
 * Returns the content:// URI of the selected image.
 */
@Composable
actual fun ImagePickerDialog(
    show: Boolean,
    onImageSelected: (uri: String) -> Unit,
    onDismiss: () -> Unit
) {
    // Use rememberUpdatedState to safely reference lambdas in effects
    val currentOnDismiss = rememberUpdatedState(onDismiss)
    val currentOnImageSelected = rememberUpdatedState(onImageSelected)
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Log.info { "Image selected: $uri" }
            currentOnImageSelected.value(uri.toString())
        } else {
            Log.info { "Image picker dismissed without selection" }
            currentOnDismiss.value()
        }
    }
    
    LaunchedEffect(show) {
        if (show) {
            try {
                launcher.launch("image/*")
            } catch (e: Exception) {
                Log.error("Failed to launch image picker", e)
                currentOnDismiss.value()
            }
        }
    }
}
