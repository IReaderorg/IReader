package ireader.data.catalog.impl

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import ireader.core.http.HttpClients
import ireader.core.io.saveTo
import ireader.core.os.InstallStep
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.entities.CatalogRemote
import ireader.i18n.LocalizeHelper
import ireader.i18n.UiText
import ireader.i18n.asString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * iOS implementation of CatalogInstaller
 * 
 * On iOS, we only support JS plugin installation since:
 * - APK/DEX loading is not possible (Android-specific)
 * - JAR loading is not possible (JVM-specific)
 * - Dynamic native code loading is prohibited by App Store
 * 
 * JS plugins are downloaded and executed via JavaScriptCore
 */
class IosCatalogInstaller(
    private val httpClient: HttpClients,
    private val localizeHelper: LocalizeHelper,
    private val installationChanges: IosCatalogInstallationChanges,
) : CatalogInstaller {

    private val client get() = httpClient.default
    
    // JS plugins directory - stored in app's Documents directory
    private val jsPluginsDir: String
        get() {
            // TODO: Use proper iOS Documents directory path
            // platform.Foundation.NSSearchPathForDirectoriesInDomains(
            //     NSDocumentDirectory, NSUserDomainMask, true
            // ).firstOrNull() + "/js-plugins"
            return "js-plugins"
        }

    override fun install(catalog: CatalogRemote): Flow<InstallStep> = flow {
        emit(InstallStep.Downloading)
        
        // On iOS, we only support JS plugins
        val isJSPlugin = catalog.pkgUrl.endsWith(".js")
        
        if (!isJSPlugin) {
            emit(InstallStep.Error("Only JavaScript plugins are supported on iOS. APK/JAR extensions cannot be installed."))
            return@flow
        }
        
        try {
            val jsFilePath = "$jsPluginsDir/${catalog.pkgName}.js".toPath()
            val metadataFilePath = "$jsPluginsDir/${catalog.pkgName}.meta.json".toPath()
            
            // Ensure directory exists
            val fileSystem = FileSystem.SYSTEM
            val dirPath = jsPluginsDir.toPath()
            if (!fileSystem.exists(dirPath)) {
                fileSystem.createDirectories(dirPath)
            }
            
            // Download JS plugin
            val jsResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            jsResponse.saveTo(jsFilePath, fileSystem)
            
            // Save metadata
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
            
            fileSystem.write(metadataFilePath) {
                writeUtf8(metadata)
            }
            
            installationChanges.notifyAppInstall(catalog.pkgName)
            emit(InstallStep.Idle)
            emit(InstallStep.Success)
        } catch (e: Throwable) {
            emit(InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper)))
        }
    }

    override suspend fun uninstall(pkgName: String): InstallStep {
        return try {
            // Notify uninstall first
            installationChanges.notifyAppUninstall(pkgName)
            
            val fileSystem = FileSystem.SYSTEM
            val jsFilePath = "$jsPluginsDir/$pkgName.js".toPath()
            val metadataFilePath = "$jsPluginsDir/$pkgName.meta.json".toPath()
            
            // Delete JS file
            if (fileSystem.exists(jsFilePath)) {
                fileSystem.delete(jsFilePath)
            }
            
            // Delete metadata file
            if (fileSystem.exists(metadataFilePath)) {
                fileSystem.delete(metadataFilePath)
            }
            
            InstallStep.Success
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper))
        }
    }
}
