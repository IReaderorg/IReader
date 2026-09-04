package ireader.domain.utils.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.View
import android.view.View.GONE
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.UiThread
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 *  Makes the activity enter fullscreen mode.
 */
@UiThread
fun Activity.enterFullScreenMode() {
    @Suppress("DEPRECATION")
    window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    @Suppress("DEPRECATION")
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}

/**
 * Makes the Activity exit fullscreen mode.
 */
@UiThread
@Suppress("DEPRECATION")
fun Activity.exitFullScreenMode() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
}

@Suppress("DEPRECATION")
fun Activity.enableFullScreen() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    )
}

val Activity.isImmersiveModeEnabled: Boolean
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insets = window.decorView.rootWindowInsets
            if (insets != null) {
                !insets.isVisible(WindowInsets.Type.statusBars()) &&
                    !insets.isVisible(WindowInsets.Type.navigationBars())
            } else {
                false
            }
        } else {
            @Suppress("DEPRECATION")
            (window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) != 0
        }
    }

/**
 * Sets the screen brightness. Call this before setContentView.
 * 0 is dimmest, 1 is brightest. Default value is 1
 */
fun Activity.brightness(brightness: Float = 1f) {
    val params = window.attributes
    params.screenBrightness = brightness // range from 0 - 1 as per docs
    window.attributes = params
    window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)
}

@SuppressLint("ObsoleteSdkInt")
@Suppress("DEPRECATION")
fun Activity.hideBottomBar() {
    if (Build.VERSION.SDK_INT < 19) { // lower api
        val v = this.window.decorView
        v.systemUiVisibility = GONE
    } else {
        // for new api versions.
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
    }
}

@SuppressLint("ObsoleteSdkInt")
@Suppress("DEPRECATION")
fun Activity.showBottomBar() {
    if (Build.VERSION.SDK_INT < 19) { // lower api
        val v = this.window.decorView
        v.systemUiVisibility = View.VISIBLE
    } else {
        // for new api versions.
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
    }
}

/**
 * Enables immersive mode, setting cutout mode on Android P+ and hiding system bars.
 */
fun Activity.enableImmersiveMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val params = window.attributes
        if (params.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = params
        }
    }
    hideSystemUI()
}

/**
 * Shows the system bars using modern WindowCompat APIs.
 */
fun Activity.showSystemUI() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
    }
}

/**
 * Hides the system bars using modern WindowCompat APIs and configures transient swipe behavior.
 */
fun Activity.hideSystemUI() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val params = window.attributes
        if (params.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = params
        }
    }
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }
}