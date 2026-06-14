

package ireader.data.catalog.impl

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log as AndroidLog
import kotlinx.coroutines.flow.MutableSharedFlow
import ireader.domain.catalogs.service.CatalogInstallationChange
import ireader.domain.catalogs.service.CatalogInstallationChanges

class AndroidCatalogInstallationChanges(
    context: Application,
) : CatalogInstallationChanges {

    override val flow = MutableSharedFlow<CatalogInstallationChange>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )

    init {
        PackageInstallReceiver.installationChanges = this
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }
        context.registerReceiver(Receiver(), filter)
    }

    fun notifyAppInstall(pkgName: String) {
        flow.tryEmit(CatalogInstallationChange.LocalInstall(pkgName))
    }

    fun notifyAppUninstall(pkgName: String) {
        flow.tryEmit(CatalogInstallationChange.LocalUninstall(pkgName))
    }

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            val pkgName = intent.data?.encodedSchemeSpecificPart ?: return

            AndroidLog.i("ExtensionInstallReceiver", "RECEIVED: action=${intent.action} pkg=$pkgName")

            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REPLACED -> {
                    AndroidLog.i("ExtensionInstallReceiver", "Package INSTALLED/UPDATED: $pkgName")
                    val emitted = flow.tryEmit(CatalogInstallationChange.SystemInstall(pkgName))
                    AndroidLog.i("ExtensionInstallReceiver", "Flow emit result: $emitted")
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    AndroidLog.i("ExtensionInstallReceiver", "Package REMOVED: $pkgName")
                    val emitted = flow.tryEmit(CatalogInstallationChange.SystemUninstall(pkgName))
                    AndroidLog.i("ExtensionInstallReceiver", "Flow emit result: $emitted")
                }
            }
        }
    }
}
