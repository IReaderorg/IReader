

package ireader.data.catalog

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.flow.MutableSharedFlow
import ireader.domain.catalogs.service.CatalogInstallationChange
import ireader.domain.catalogs.service.CatalogInstallationChanges

class AndroidCatalogInstallationChanges(
    context: Application,
) : CatalogInstallationChanges {

    override val flow = MutableSharedFlow<CatalogInstallationChange>(
        extraBufferCapacity = Int.MAX_VALUE
    )

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
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

            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    flow.tryEmit(CatalogInstallationChange.SystemInstall(pkgName))
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    flow.tryEmit(CatalogInstallationChange.SystemUninstall(pkgName))
                }
            }
        }
    }
}
