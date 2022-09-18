package ireader.ui.component.components

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ireader.i18n.R

@Composable
fun ConfirmExitBackHandler(confirmExit: Boolean) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val message = stringResource(R.string.confirm_exit_message)

    var isConfirmingExit by remember { mutableStateOf(false) }

    // Always install the back handler even if the preference is not active because the order of
    // installation matters and when the setting is enabled it'd be placed at the top, overriding
    // the navigation back handler.
    BackHandler(enabled = confirmExit && !isConfirmingExit) {
        isConfirmingExit = true
        context.toast(message, Toast.LENGTH_LONG)
        scope.launch {
            delay(2000)
            isConfirmingExit = false
        }
    }
}

/**
 * Display a toast in this context.
 *
 * @param text the text to display.
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text.orEmpty(), duration).show()
}

/**
 * Display a toast in this context.
 *
 * @param textRes the text resource to display.
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(@StringRes textRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, textRes, duration).show()
}
