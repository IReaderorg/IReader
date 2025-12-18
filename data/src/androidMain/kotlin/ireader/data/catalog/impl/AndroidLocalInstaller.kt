package ireader.data.catalog.impl

import android.app.Application
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import ireader.core.http.HttpClients
import ireader.core.io.saveTo
import ireader.core.log.Log
import ireader.core.os.InstallStep
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.i18n.LocalizeHelper
import ireader.i18n.UiText
import ireader.i18n.asString
import kotlinx.coroutines.flow.channelFlow
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.File


/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */

class AndroidLocalInstaller(
    private val context: Application,
    private val httpClients: HttpClients,
    private val installationChanges: AndroidCatalogInstallationChanges,
    private val getSimpleStorage: GetSimpleStorage,
    private val uiPreferences: UiPreferences,
    private val localizeHelper: LocalizeHelper
) : CatalogInstaller {


    private fun savedCatalogLocation(catalog: CatalogRemote): File {
        val savedFromCache= uiPreferences.savedLocalCatalogLocation().get()
        val cacheLocation = File(getSimpleStorage.cacheExtensionDir().toFile(), catalog.pkgName).apply { mkdirs() }
        val primaryLocation = File(getSimpleStorage.extensionDirectory().toFile(), catalog.pkgName).apply { mkdirs() }

        return if (savedFromCache) cacheLocation else primaryLocation
    }

    /**
     * The client used for http requests.
     */
    private val client get() = httpClients.default
    /**
     * Adds the given extension to the downloads queue and returns an observable containing its
     * step in the installation process.
     *
     * @param catalog The catalog to install.
     */
    override fun install(catalog: CatalogRemote) = channelFlow  {
        send(InstallStep.Downloading)
        
        // Check if this is an LNReader plugin (JavaScript-based)
        if (catalog.isLNReaderSource()) {
            installJSPlugin(catalog)
            return@channelFlow
        }
        
        // Traditional APK installation
        val tmpApkFile = File(context.codeCacheDir, "${catalog.pkgName}.apk")
        try {
            val apkResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            apkResponse.saveTo(tmpApkFile.absolutePath.toPath(), FileSystem.SYSTEM)

            send(InstallStep.Downloading)
            val extDir = savedCatalogLocation(catalog)

            val apkFile = File(extDir, tmpApkFile.name).apply { mkdirs() }

            val apkSuccess = tmpApkFile.copyRecursively(apkFile, true)
            if (apkSuccess) {
                installationChanges.notifyAppInstall(catalog.pkgName)
                send(InstallStep.Success)
            }

            send(InstallStep.Success)
            send(InstallStep.Idle)
        } catch (e: Exception) {
            Log.warn(e, "Error installing package")
            send(InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper)))
            send(InstallStep.Idle)
        } finally {
            tmpApkFile.delete()
            send(InstallStep.Idle)
        }
    }
    
    /**
     * Install a JavaScript plugin (LNReader format)
     * This is a suspend function that will be called from the channelFlow
     * Uses SAF (Storage Access Framework) when available for proper permission handling.
     */
    private suspend fun kotlinx.coroutines.channels.ProducerScope<InstallStep>.installJSPlugin(catalog: CatalogRemote) {
        try {
            send(InstallStep.Downloading)
            
            Log.info("AndroidLocalInstaller: Installing JS plugin ${catalog.name}")
            
            // Download the JS plugin content first
            val jsResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            
            // Read content into memory
            val tempFile = ireader.domain.storage.SecureStorageHelper.createTempFile(context, "js_plugin_", ".js")
            try {
                jsResponse.saveTo(tempFile.absolutePath.toPath(), FileSystem.SYSTEM)
                val jsContent = tempFile.readBytes()
                
                // Prepare metadata
                val metadata = """
                    {
                      "id": "${catalog.pkgName}",
                      "name": "${catalog.name.replace("\"", "\\\"")}",
                      "lang": "${catalog.lang}",
                      "version": "${catalog.versionName}",
                      "site": "${catalog.description.replace("\"", "\\\"")}",
                      "icon": "${catalog.iconUrl.replace("\"", "\\\"")}"
                    }
                """.trimIndent()
                
                // Write using SecureStorageHelper (handles SAF vs fallback automatically)
                val jsFileName = "${catalog.pkgName}.js"
                val metaFileName = "${catalog.pkgName}.meta.json"
                
                val jsWritten = ireader.domain.storage.SecureStorageHelper.writeJsPluginBytes(context, jsFileName, jsContent)
                val metaWritten = ireader.domain.storage.SecureStorageHelper.writeJsPluginMetadata(context, metaFileName, metadata)
                
                if (jsWritten && metaWritten) {
                    Log.info("AndroidLocalInstaller: Successfully installed JS plugin ${catalog.name}")
                    
                    // Sync from SAF to fallback so JSPluginLoader can find the plugin
                    // JSPluginLoader uses FileSystem.SYSTEM which requires regular file paths
                    try {
                        val synced = ireader.domain.storage.SecureStorageHelper.syncJsPluginsFromSaf(context)
                        if (synced > 0) {
                            Log.info("AndroidLocalInstaller: Synced $synced JS plugins from SAF to fallback")
                        }
                    } catch (e: Exception) {
                        Log.warn("AndroidLocalInstaller: Failed to sync JS plugins from SAF: ${e.message}")
                    }
                    
                    installationChanges.notifyAppInstall(catalog.pkgName)
                    send(InstallStep.Success)
                } else {
                    Log.error("AndroidLocalInstaller: Failed to write JS plugin files: js=$jsWritten, meta=$metaWritten")
                    send(InstallStep.Error("Failed to write plugin files"))
                }
            } finally {
                tempFile.delete()
            }
            
            send(InstallStep.Idle)
            
        } catch (e: Exception) {
            Log.error("AndroidLocalInstaller: Failed to install JS plugin: ${catalog.name}", e)
            send(InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper)))
            send(InstallStep.Idle)
        }
    }

    /**
     * Starts an intent to uninstall the extension by the given package name.
     * Uses SAF (Storage Access Framework) when available.
     *
     * @param pkgName The package name of the extension to uninstall
     */
    override suspend fun uninstall(pkgName: String): InstallStep {
        return try {
            // Try to uninstall as traditional APK extension
            val file = File(getSimpleStorage.extensionDirectory().toFile(), pkgName)
            val cacheFile = File(getSimpleStorage.cacheExtensionDir().toFile(), pkgName)
            file.deleteRecursively()
            cacheFile.deleteRecursively()
            
            // Uninstall JS plugin using SecureStorageHelper (handles SAF + fallback)
            val jsFileName = "$pkgName.js"
            val metaFileName = "$pkgName.meta.json"
            
            ireader.domain.storage.SecureStorageHelper.deleteJsPlugin(context, jsFileName)
            ireader.domain.storage.SecureStorageHelper.deleteJsPlugin(context, metaFileName)
            
            installationChanges.notifyAppUninstall(pkgName)
            InstallStep.Success
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper))
        }
    }

}