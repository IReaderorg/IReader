package ireader.data.catalog.impl.interactor

import ireader.domain.models.entities.CatalogRemote
import ireader.core.os.InstallStep
import ireader.data.catalog.impl.AndroidCatalogInstaller
import ireader.data.catalog.impl.AndroidLocalInstaller
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.Flow


class InstallCatalogImpl(
    private val androidInstaller: AndroidCatalogInstaller,
    private val androidLocalInstaller: AndroidLocalInstaller,
    private val uiPreferences: UiPreferences
): InstallCatalog {

    override fun await(catalog: CatalogRemote): Flow<InstallStep> {
        // For LNReader JS plugins, always use local installer (simple file download)
        // regardless of user's installer mode preference
        if (catalog.isLNReaderSource()) {
            return androidLocalInstaller.install(catalog)
        }
        
        // For IReader APK extensions, respect user's installer mode preference
        return when (uiPreferences.installerMode().get()) {
            PreferenceValues.Installer.LocalInstaller -> {
                androidLocalInstaller.install(catalog)
            }
            else -> {
                androidInstaller.install(catalog)
            }
        }
    }

    override fun await(type: PreferenceValues.Installer): CatalogInstaller {
        return when (type) {
            PreferenceValues.Installer.LocalInstaller -> {
                androidLocalInstaller
            }
            else -> {
                androidInstaller
            }
        }
    }

}
