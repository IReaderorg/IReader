package ireader.data.catalog.impl.interactor

import ireader.common.models.entities.CatalogRemote
import ireader.core.os.InstallStep
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.data.catalog.impl.AndroidCatalogInstaller
import ireader.data.catalog.impl.AndroidLocalInstaller
import ireader.domain.catalogs.interactor.InstallCatalog
import kotlinx.coroutines.flow.Flow


class InstallCatalogImpl(
    private val androidInstaller: AndroidCatalogInstaller,
    private val androidInAppInstaller: AndroidLocalInstaller,
    private val uiPreferences: UiPreferences
): InstallCatalog {

    override fun await(catalog: CatalogRemote): Flow<InstallStep> {
        return when (uiPreferences.installerMode().get()) {
            PreferenceValues.Installer.LocalInstaller -> {
                androidInAppInstaller.install(catalog)
            }
            else -> {
                androidInstaller.install(catalog)
            }
        }
    }
}
