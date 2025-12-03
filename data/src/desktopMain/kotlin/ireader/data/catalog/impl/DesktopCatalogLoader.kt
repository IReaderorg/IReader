package ireader.data.catalog.impl


import com.googlecode.d2j.dex.Dex2jar
import com.googlecode.d2j.reader.MultiDexFileReader
import com.googlecode.dex2jar.tools.BaksmaliBaseDexExceptionHandler
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStoreFactory
import ireader.core.prefs.PrefixedPreferenceStore
import ireader.core.source.Source
import ireader.core.storage.ExtensionDir
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.js.engine.JSEnginePool
import ireader.domain.js.loader.JSPluginLoader
import ireader.domain.js.util.JSPluginLogger
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.extensions.withIOContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import okio.Path.Companion.toPath
import java.io.File
import java.net.URLClassLoader

class DesktopCatalogLoader(
    private val httpClients: HttpClients,
    val uiPreferences: UiPreferences,
    preferences: PreferenceStoreFactory
) : CatalogLoader, ireader.domain.catalogs.service.AsyncPluginLoader {
    private val catalogPreferences = preferences.create("catalogs_data")
    
    // JavaScript plugin loader
    private val jsPluginLoader: JSPluginLoader by lazy {
        val jsPluginsDir = File(System.getProperty("user.home"), ".ireader/js-plugins").apply { mkdirs() }
        JSPluginLoader(jsPluginsDir.absolutePath.toPath(), httpClients.default, preferences)
    }
    
    /**
     * Load actual JS plugins in the background, replacing stubs.
     * @param onPluginLoaded Callback when each plugin is loaded
     */
    override suspend fun loadJSPluginsAsync(onPluginLoaded: (ireader.domain.models.entities.JSPluginCatalog) -> Unit) {
        if (!uiPreferences.enableJSPlugins().get()) return
        
        try {
            jsPluginLoader.loadPluginsAsync(onPluginLoaded)
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    /**
     * Get the JS plugin loader for advanced operations.
     */
    fun getJSPluginLoader(): JSPluginLoader = jsPluginLoader
    override suspend fun loadAll(): List<CatalogLocal> {
        val bundled = mutableListOf<ireader.domain.models.entities.CatalogBundled>()
        
        // Add Local Source for reading local novels
        val localSourceCatalog = ireader.domain.models.entities.CatalogBundled(
            source = ireader.core.source.LocalSource(),
            description = "Read novels from local storage",
            name = "Local Source"
        )
        bundled.add(localSourceCatalog)
        
        val localPkgs = ExtensionDir.listFiles()
            .orEmpty()
            .filter { it.isDirectory }
            .map { File(it, it.name + ".apk") }
            .filter { it.exists() }
        // Load each catalog concurrently and wait for completion
        val installedCatalogs = withIOContext {
            val local = if (uiPreferences.showLocalCatalogs().get()) {
                localPkgs.map { file ->
                    async(Dispatchers.Default) {
                        loadLocalCatalog(file.nameWithoutExtension)
                    }
                }
            } else emptyList()
            val deferred = (local)
            deferred.awaitAll()
        }.filterNotNull()

        // Load JavaScript plugins if enabled
        val jsPlugins = if (uiPreferences.enableJSPlugins().get()) {
            try {
                // Load stub plugins instantly for fast startup
                val stubs = jsPluginLoader.loadStubPlugins()
                
                // Note: Actual plugins will be loaded in background by CatalogStore
                // On first run (no stubs), background loading will load all plugins
                // On subsequent runs, background loading will replace stubs with actual plugins
                stubs
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        return (bundled + installedCatalogs + jsPlugins).distinctBy { it.sourceId }.toSet().toList()
    }

    override fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally? {
        val file = File(ExtensionDir, "${pkgName}/${pkgName}.apk")
        try {
            if (!file.exists() || !file.canRead()) {
                return null
            }

            val pkgInfo = ApkFile(file)
            return loadLocalCatalogs(pkgName, pkgInfo, file)
        } catch (e:Exception) {
            return null
        }
    }

    private fun loadLocalCatalogs(
        pkgName: String,
        pkgInfo: ApkFile,
        file: File,
    ): CatalogInstalled.Locally? {
        val data = validateMetadata(pkgName, pkgInfo) ?: return null
        val classLoader = URLClassLoader.getSystemClassLoader()
        val jarFileName = file.name.substringBefore(".apk").plus(".jar")
        val jarFile = File(file.parentFile, jarFileName)
        
        // Check if we need to convert or re-convert the APK to JAR
        val needsConversion = !jarFile.exists() || jarFile.length() == 0L || 
                              jarFile.lastModified() < file.lastModified() // Re-convert if APK is newer
        
        if (needsConversion) {
            // Delete old JAR if it exists to force clean conversion
            if (jarFile.exists()) {
                jarFile.delete()
            }
            
            dex2jar(file,jarFile,file.name)
            
            // Verify JAR was created successfully
            if (!jarFile.exists() || jarFile.length() == 0L) {
                return null
            }
        }
        
        val loader = URLClassLoader(
            arrayOf(jarFile.toURL()),
            classLoader,
        )
        val source = loadSource(pkgName, loader, data) ?: return null

        return CatalogInstalled.Locally(
            name = source.name,
            description = data.description,
            source = source,
            pkgName = pkgName,
            versionName = data.versionName,
            versionCode = data.versionCode,
            nsfw = data.nsfw,
            installDir = file.parentFile!!.absolutePath.toPath(),
            iconUrl = data.icon
        )


    }

    private fun isPackageAnExtension(pkgInfo: ApkMeta): Boolean {
        return pkgInfo.usesFeatures.orEmpty().any { it.name == EXTENSION_FEATURE }
    }

    private fun validateMetadata(pkgName: String, apkFile: ApkFile): ValidatedData? {
        val pkgInfo = apkFile.apkMeta
        if (!isPackageAnExtension(pkgInfo)) {
            return null
        }
        @Suppress("DEPRECATION")
        val versionCode = pkgInfo.versionCode
        val versionName = pkgInfo.versionName

        // Validate lib version
        val majorLibVersion = versionName.substringBefore('.').toInt()
        if (majorLibVersion < LIB_VERSION_MIN || majorLibVersion > LIB_VERSION_MAX) {
            return null
        }

        val appInfo = Jsoup.parse(apkFile.manifestXml, Parser.xmlParser()).select("application").select("meta-data")

        val meta = appInfo.map {
            val element = it.select("meta-data")
            val name = element.attr("android:name")
            val value = element.attr("android:value")
            name to value
        }
        val sourceClassName = meta.find { it.first ==  METADATA_SOURCE_CLASS}?.second
        if (sourceClassName == null) {
            return null
        }


        val description =   meta.find { it.first == METADATA_DESCRIPTION}?.second?.ifBlank { "" } ?: ""
        val icon =  meta.find { it.first == METADATA_ICON}?.second?.ifBlank { "" } ?: ""

        val classToLoad = if (sourceClassName.startsWith(".")) {
            pkgInfo.packageName + sourceClassName
        } else {
            sourceClassName
        }

        val nsfw = meta.find { it.first == METADATA_NSFW}?.second == "1"

        val preferenceSource = PrefixedPreferenceStore(catalogPreferences, pkgName)
        val dependencies = ireader.core.source.Dependencies(httpClients, preferenceSource)

        return ValidatedData(
            versionCode.toInt(),
            versionName,
            description,
            icon,
            nsfw,
            classToLoad,
            dependencies
        )
    }

    private fun loadSource(pkgName: String, loader: ClassLoader, data: ValidatedData): Source? {
        return try {
            val obj = Class.forName(data.classToLoad, false, loader)
                .getConstructor(ireader.core.source.Dependencies::class.java)
                .newInstance(data.dependencies)

            obj as? Source ?: throw Exception("Unknown source class type! ${obj.javaClass}")
        } catch (e: Throwable) {
            return null
        }
    }

    override fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide? {
        return null
    }
    @Suppress("NewApi")
    fun dex2jar(dexFile: File, jarFile: File, fileNameWithoutType: String) {
        // adopted from com.googlecode.dex2jar.tools.Dex2jarCmd.doCommandLine
        // source at: https://github.com/DexPatcher/dex2jar/tree/v2.1-20190905-lanchon/dex-tools/src/main/java/com/googlecode/dex2jar/tools/Dex2jarCmd.java
        try {
            val jarFilePath = jarFile.toPath()
            val reader = MultiDexFileReader.open(dexFile.inputStream())
            val handler = BaksmaliBaseDexExceptionHandler()
            Dex2jar
                    .from(reader)
                    .withExceptionHandler(handler)
                    .reUseReg(false)
                    .topoLogicalSort()
                    .skipDebug(true)
                    .optimizeSynchronized(false)
                    .printIR(false)
                    .noCode(false)
                    .skipExceptions(false)
                    .to(jarFilePath)
        } catch (e: Exception) {
            // Ignore errors
        }

    }

    private data class ValidatedData(
        val versionCode: Int,
        val versionName: String,
        val description: String,
        val icon: String,
        val nsfw: Boolean,
        val classToLoad: String,
        val dependencies: ireader.core.source.Dependencies,
    )

    private companion object {
        const val EXTENSION_FEATURE = "ireader"
        const val METADATA_SOURCE_CLASS = "source.class"
        const val METADATA_DESCRIPTION = "source.description"
        const val METADATA_NSFW = "source.nsfw"
        const val METADATA_ICON = "source.icon"
        const val LIB_VERSION_MIN = 1
        const val LIB_VERSION_MAX = 1
    }
}