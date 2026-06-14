package ireader.data.catalog.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Manifest-registered receiver for package install/uninstall events.
 * Dynamic registration is unreliable for implicit broadcasts on some devices.
 */
class PackageInstallReceiver : BroadcastReceiver() {

    companion object {
        var installationChanges: AndroidCatalogInstallationChanges? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val pkgName = intent.data?.encodedSchemeSpecificPart ?: return

        Log.i("PackageInstallReceiver", "RECEIVED: action=${intent.action} pkg=$pkgName")

        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REPLACED -> {
                Log.i("PackageInstallReceiver", "Package INSTALLED/UPDATED: $pkgName")
                installationChanges?.notifyAppInstall(pkgName)
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                Log.i("PackageInstallReceiver", "Package REMOVED: $pkgName")
                installationChanges?.notifyAppUninstall(pkgName)
            }
        }
    }
}
