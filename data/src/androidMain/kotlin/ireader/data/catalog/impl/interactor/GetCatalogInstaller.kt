package ireader.data.catalog.impl.interactor

import ireader.core.log.Log
import ireader.core.os.InstallStep
import ireader.data.catalog.impl.AndroidCatalogInstaller
import ireader.data.catalog.impl.AndroidLocalInstaller
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach


class InstallCatalogImpl(
    private val androidInstaller: AndroidCatalogInstaller,
    private val androidLocalInstaller: AndroidLocalInstaller,
    private val uiPreferences: UiPreferences
) : InstallCatalog {

    override fun await(catalog: CatalogRemote): Flow<InstallStep> {
        // For LNReader JS plugins, always use local installer (simple file download)
        // regardless of user's installer mode preference
        if (catalog.isLNReaderSource()) {
            return androidLocalInstaller.install(catalog)
        }
        
        // For IReader APK extensions, use the selected installer mode
        return when (uiPreferences.installerMode().get()) {
            PreferenceValues.Installer.LocalInstaller -> {
                androidLocalInstaller.install(catalog)
            }
            PreferenceValues.Installer.AndroidPackageManager -> {
                androidInstaller.install(catalog)
            }
            PreferenceValues.Installer.HybridInstaller -> {
                // Hybrid mode: try local first, fall back to package installer on failure
                installWithFallback(catalog)
            }
        }
    }
    
    /**
     * Hybrid installation: tries local installer first, falls back to package installer on failure.
     * This helps devices that have issues with one installer but work with the other.
     */
    private fun installWithFallback(catalog: CatalogRemote): Flow<InstallStep> = channelFlow {
        var localFailed = false
        var lastError: String? = null
        
        Log.info("HybridInstaller: Starting installation of ${catalog.name}, trying local installer first")
        
        // Try local installer first
        try {
            androidLocalInstaller.install(catalog).onEach { step ->
                when (step) {
                    is InstallStep.Error -> {
                        localFailed = true
                        lastError = step.error
                        Log.warn("HybridInstaller: Local installer failed for ${catalog.name}: ${step.error}")
                    }
                    is InstallStep.Success -> {
                        Log.info("HybridInstaller: Local installer succeeded for ${catalog.name}")
                        send(step)
                    }
                    else -> {
                        // Forward other steps (Downloading, Installing, etc.)
                        if (!localFailed) {
                            send(step)
                        }
                    }
                }
            }.collect()
        } catch (e: Exception) {
            localFailed = true
            lastError = e.message
            Log.warn("HybridInstaller: Local installer threw exception for ${catalog.name}: ${e.message}")
        }
        
        // If local installer failed, try package installer
        if (localFailed) {
            Log.info("HybridInstaller: Falling back to package installer for ${catalog.name}")
            send(InstallStep.Downloading) // Reset state for UI
            
            try {
                androidInstaller.install(catalog).onEach { step ->
                    when (step) {
                        is InstallStep.Error -> {
                            // Both installers failed
                            Log.error("HybridInstaller: Package installer also failed for ${catalog.name}: ${step.error}")
                            send(InstallStep.Error("Local: $lastError\nPackage: ${step.error}"))
                        }
                        else -> send(step)
                    }
                }.collect()
            } catch (e: Exception) {
                Log.error("HybridInstaller: Package installer threw exception for ${catalog.name}: ${e.message}")
                send(InstallStep.Error("Local: $lastError\nPackage: ${e.message}"))
                send(InstallStep.Idle)
            }
        }
    }

    override fun await(type: PreferenceValues.Installer): CatalogInstaller {
        return when (type) {
            PreferenceValues.Installer.LocalInstaller -> androidLocalInstaller
            PreferenceValues.Installer.AndroidPackageManager -> androidInstaller
            PreferenceValues.Installer.HybridInstaller -> androidLocalInstaller // Default to local for direct access
        }
    }
}
