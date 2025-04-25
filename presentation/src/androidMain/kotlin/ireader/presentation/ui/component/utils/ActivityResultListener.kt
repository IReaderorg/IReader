package ireader.presentation.ui.component.utils

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.common.Uri
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope

/**
 * A utility to listen for activity results in Compose.
 */
@Composable
actual fun ActivityResultListener(
        onSuccess: suspend (Uri) -> Unit,
        onError: (Throwable) -> Unit,
) : ActivityResultLauncher {
    val globalScope = LocalGlobalCoroutineScope.currentOrThrow
    val result = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
        if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
            val uri = resultIntent.data!!.data!!
            globalScope.launchIO {
                try {
                    onSuccess(Uri(uri!!))
                } catch (e: Throwable) {
                    onError(e)
                }
            }
        }
    }
    return ActivityResultLauncher(result)
}

actual class ActivityResultLauncher actual constructor(private val input: Any) {
    actual fun launch(intent:Any) {

            (input as? ManagedActivityResultLauncher<Intent, ActivityResult>)?.launch(intent as Intent)

    }
}