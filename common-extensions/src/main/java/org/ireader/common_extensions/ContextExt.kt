package org.ireader.common_extensions

import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.net.toUri
import org.ireader.core.R
import java.io.File
import kotlin.math.roundToInt


fun Context.copyToClipboard(label: String, content: String) {
    if (content.isBlank()) return

    try {
        val clipboard = getSystemService<ClipboardManager>()!!
        clipboard.setPrimaryClip(ClipData.newPlainText(label, content))
        toast(R.string.copied_to_clipboard)
    } catch (e: Throwable) {
        toast(R.string.clipboard_copy_error)
    }
}

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

fun Context.toast(
    @StringRes resource: Int,
    duration: Int = Toast.LENGTH_SHORT,
    block: (Toast) -> Unit = {},
): Toast {
    return toast(getString(resource), duration, block)
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

private const val TABLET_UI_MIN_SCREEN_WIDTH_DP = 720


/**
 * Helper method to create a notification builder.
 *
 * @param id the channel id.
 * @param block the function that will execute inside the builder.
 * @return a notification to be displayed or updated.
 */
@RequiresApi(Build.VERSION_CODES.M)
fun Context.notificationBuilder(
    channelId: String,
    block: (NotificationCompat.Builder.() -> Unit)? = null,
): NotificationCompat.Builder {
    val builder = NotificationCompat.Builder(this, channelId)
        .setColor(getColor(R.color.accent_blue))
    if (block != null) {
        builder.block()
    }
    return builder
}

/**
 * Helper method to create a notification.
 *
 * @param id the channel id.
 * @param block the function that will execute inside the builder.
 * @return a notification to be displayed or updated.
 */
@RequiresApi(Build.VERSION_CODES.M)
fun Context.notification(
    channelId: String,
    block: (NotificationCompat.Builder.() -> Unit)?,
): Notification {
    val builder = notificationBuilder(channelId, block)
    return builder.build()
}

/**
 * Checks if the give permission is granted.
 *
 * @param permission the permission to check.
 * @return true if it has permissions.
 */
fun Context.hasPermission(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * Returns the color for the given attribute.
 *
 * @param resource the attribute.
 * @param alphaFactor the alpha number [0,1].
 */
@ColorInt
fun Context.getResourceColor(@AttrRes resource: Int, alphaFactor: Float = 1f): Int {
    val typedArray = obtainStyledAttributes(intArrayOf(resource))
    val color = typedArray.getColor(0, 0)
    typedArray.recycle()

    if (alphaFactor < 1f) {
        val alpha = (color.alpha * alphaFactor).roundToInt()
        return Color.argb(alpha, color.red, color.green, color.blue)
    }

    return color
}

@RequiresApi(Build.VERSION_CODES.M)
@ColorInt
fun Context.getThemeColor(attr: Int): Int {
    val tv = TypedValue()
    return if (this.theme.resolveAttribute(attr, tv, true)) {
        if (tv.resourceId != 0) {
            getColor(tv.resourceId)
        } else {
            tv.data
        }
    } else {
        0
    }
}

/**
 * Converts to dp.
 */
val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

/**
 * Converts to px.
 */
val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * Converts to px and takes into account LTR/RTL layout.
 */
val Float.dpToPxEnd: Float
    get() = (
            this * Resources.getSystem().displayMetrics.density *
                    if (Resources.getSystem().isLTR) 1 else -1
            )

val Resources.isLTR
    get() = configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR

val Context.notificationManager: NotificationManager
    get() = getSystemService()!!

val Context.connectivityManager: ConnectivityManager
    get() = getSystemService()!!

val Context.wifiManager: WifiManager
    get() = getSystemService()!!

val Context.powerManager: PowerManager
    get() = getSystemService()!!

val Context.keyguardManager: KeyguardManager
    get() = getSystemService()!!

val Context.displayCompat: Display?
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display
    } else {
        @Suppress("DEPRECATION")
        getSystemService<WindowManager>()?.defaultDisplay
    }

/** Gets the duration multiplier for general animations on the device
 * @see Settings.Global.ANIMATOR_DURATION_SCALE
 */
val Context.animatorDurationScale: Float
    get() = Settings.Global.getFloat(this.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f)

/**
 * Convenience method to acquire a partial wake lock.
 */
fun Context.acquireWakeLock(tag: String): PowerManager.WakeLock {
    val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$tag:WakeLock")
    wakeLock.acquire()
    return wakeLock
}


/**
 * Returns true if the given service class is running.
 */
fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val className = serviceClass.name
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    @Suppress("DEPRECATION")
    return manager.getRunningServices(Integer.MAX_VALUE)
        .any { className == it.service.className }
}

/**
 * Opens a URL in a custom tab.
 */
fun Context.openInBrowser(url: String, @ColorInt toolbarColor: Int? = null) {
    this.openInBrowser(url.toUri(), toolbarColor)
}

fun Context.openInBrowser(uri: Uri, @ColorInt toolbarColor: Int? = null) {
    try {
        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(toolbarColor ?: getResourceColor(R.attr.colorPrimary))
                    .build()
            )
            .build()
        intent.launchUrl(this, uri)
    } catch (e: Throwable) {
        toast(e.message)
    }
}
