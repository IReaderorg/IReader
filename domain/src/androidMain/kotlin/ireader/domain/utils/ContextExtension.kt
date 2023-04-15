package ireader.domain.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import ireader.core.log.Log
import ireader.i18n.R
import java.io.File

private const val TABLET_UI_MIN_SCREEN_WIDTH_DP = 720

/**
 * Display a toast in this context.
 *
 * @param resource the text resource.
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(
    @StringRes resource: Int,
    duration: Int = Toast.LENGTH_SHORT,
    block: (Toast) -> Unit = {},
): Toast {
    return toast(getString(resource), duration, block)
}

/**
 * Display a toast in this context.
 *
 * @param text the text to display.
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(
    text: String?,
    duration: Int = Toast.LENGTH_SHORT,
    block: (Toast) -> Unit = {},
): Toast {
    return Toast.makeText(this, text.orEmpty(), duration).also {
        block(it)
        it.show()
    }
}

/**
 * Copies a string to clipboard
 *
 * @param label Label to show to the user describing the content
 * @param content the actual text to copy to the board
 */
fun Context.copyToClipboard(label: String, content: String) {
    if (content.isBlank()) return

    try {
        val clipboard = getSystemService<ClipboardManager>()!!
        clipboard.setPrimaryClip(ClipData.newPlainText(label, content))

        toast(getString(R.string.copied_to_clipboard))
    } catch (e: Throwable) {
        Log.error("copyToClipboard $e")
        toast(R.string.clipboard_copy_error)
    }
}


private const val TABLET_UI_REQUIRED_SCREEN_WIDTH_DP = 720

// some tablets have screen width like 711dp = 1600px / 2.25
private const val TABLET_UI_MIN_SCREEN_WIDTH_PORTRAIT_DP = 700

// make sure icons on the nav rail fit
private const val TABLET_UI_MIN_SCREEN_WIDTH_LANDSCAPE_DP = 600
fun Context.isTabletUi(): Boolean {
    return resources.configuration.isTabletUi()
}
fun Configuration.isTabletUi(): Boolean {
    return smallestScreenWidthDp >= TABLET_UI_REQUIRED_SCREEN_WIDTH_DP
}

fun getCacheSize(context: Context): String {
    val size = context.cacheDir.calculateSizeRecursively()
    return when (size) {
        in 0..1024 -> "$size byte"
        in 1024..1048576 -> "${size / 1024} Kb"
        else -> "${size / (1024 * 1024)} Mb"
    }
}
fun File.calculateSizeRecursively(): Long {
    return walkBottomUp().fold(0L) { acc, file -> acc + file.length() }
}

fun Context.findAppCompatAcivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.findAppCompatAcivity()
    else -> null
}

fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
