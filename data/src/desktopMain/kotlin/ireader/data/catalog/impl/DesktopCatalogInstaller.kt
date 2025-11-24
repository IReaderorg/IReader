package ireader.data.catalog.impl

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import ireader.core.http.HttpClients
import ireader.core.io.saveTo
import ireader.core.log.Log
import ireader.core.os.InstallStep
import ireader.core.storage.ExtensionDir
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.entities.CatalogRemote
import ireader.i18n.LocalizeHelper
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class DesktopCatalogInstaller(
    private val httpClient: HttpClients,
    private val localizeHelper: LocalizeHelper,
    private val installationChanges: DesktopCatalogInstallationChanges,
) : CatalogInstaller {

    private val client get() = httpClient.default

    override fun install(catalog: CatalogRemote): Flow<InstallStep> =
        flow {
            emit(InstallStep.Downloading)
            
            // Check if this is a JS plugin (LNReader) or traditional APK/JAR extension
            val isJSPlugin = catalog.pkgUrl.endsWith(".js")
            
            if (isJSPlugin) {
                // Handle JS plugin installation
                val jsPluginsDir = File(System.getProperty("user.home"), ".ireader/js-plugins").apply { mkdirs() }
                val jsFile = File(jsPluginsDir, "${catalog.pkgName}.js")
                
                try {
                    val jsResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                        headers.append(HttpHeaders.CacheControl, "no-store")
                    }.body()
                    jsResponse.saveTo(jsFile)
                    
                    // No need to download icon - Coil will handle it via iconUrl with caching
                    
                    emit(InstallStep.Idle)
                    val result = InstallStep.Success
                    installationChanges.notifyAppInstall(catalog.pkgName)
                    emit(result)
                } catch (e: Throwable) {
                    Log.warn(e, "Error installing JS plugin")
                    emit(InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper)))
                }
            } else {
                // Handle traditional APK/JAR installation
                // Improved: Ensure directory exists before creating files
                val fileDir = File(ExtensionDir, "${catalog.pkgName}/")
                if (!fileDir.exists()) {
                    fileDir.mkdirs()
                }
                
                val apkFile = File(fileDir, "${catalog.pkgName}.apk")
                val jarFile = File(fileDir, "${catalog.pkgName}.jar")
                val iconFile = File(fileDir, "${catalog.pkgName}.png")
                
                try {
                    // Improved: Create parent directories if needed
                    apkFile.parentFile?.mkdirs()
                    jarFile.parentFile?.mkdirs()
                    iconFile.parentFile?.mkdirs()
                    
                    // Download APK
                    val apkResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                        headers.append(HttpHeaders.CacheControl, "no-store")
                    }.body()
                    apkResponse.saveTo(apkFile)
                    
                    // Download JAR
                    val jarResponse: ByteReadChannel = client.get(catalog.jarUrl) {
                        headers.append(HttpHeaders.CacheControl, "no-store")
                    }.body()
                    jarResponse.saveTo(jarFile)
                    
                    // No need to download icon - Coil will handle it via iconUrl with caching
                    
                    emit(InstallStep.Idle)
                    val result = InstallStep.Success
                    installationChanges.notifyAppInstall(catalog.pkgName)
                    emit(result)
                } catch (e: Throwable) {
                    Log.warn(e, "Error installing package")
                    emit(InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper)))
                }
            }
        }

    override suspend fun uninstall(pkgName: String): InstallStep {
        return try {
            // Try to uninstall from both locations (APK/JAR and JS plugins)
            var deleted = false
            
            // Try traditional extension directory
            val extensionFile = File(ExtensionDir, pkgName)
            if (extensionFile.exists()) {
                deleted = extensionFile.deleteRecursively()
                extensionFile.deleteOnExit()
            }
            
            // Try JS plugins directory
            val jsPluginsDir = File(System.getProperty("user.home"), ".ireader/js-plugins")
            val jsFile = File(jsPluginsDir, "$pkgName.js")
            if (jsFile.exists()) {
                deleted = jsFile.delete() || deleted
            }
            
            installationChanges.notifyAppUninstall(pkgName)
            if (deleted) InstallStep.Success else InstallStep.Error(localizeHelper.localize(Res.string.failed))
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper))
        }
    }
}