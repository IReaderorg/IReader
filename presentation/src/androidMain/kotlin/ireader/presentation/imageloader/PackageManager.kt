package ireader.presentation.imageloader

import android.content.Context

actual class PackageManager(
        private val context: Context
) {
    actual fun getApplicationIcon(pkg:String): Any {
        return context.packageManager.getApplicationIcon(pkg)
    }
}
