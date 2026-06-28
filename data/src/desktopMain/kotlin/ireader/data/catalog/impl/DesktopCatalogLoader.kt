package ireader.data.catalog.impl


import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStoreFactory
import ireader.core.prefs.PrefixedPreferenceStore
import ireader.core.source.Source
import ireader.core.storage.ExtensionDir
import ireader.data.catalog.impl.tsundoku.DesktopTsundokuExtensionLoader
import ireader.data.catalog.impl.tsundoku.TsundokuCatalogSource
import ireader.data.catalog.impl.tsundoku.TsundokuValidatedData
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.js.loader.JSPluginLoader
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.extensions.withIOContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.File
import java.net.URLClassLoader

class DesktopCatalogLoader(
    private val httpClients: HttpClients,
    val uiPreferences: UiPreferences,
    preferences: PreferenceStoreFactory,
    private val communitySource: ireader.domain.community.CommunitySource,
    private val pluginManager: ireader.core.http.cloudflare.CloudflareBypassPluginManager? = null
) : CatalogLoader, ireader.domain.catalogs.service.AsyncPluginLoader {
    private val catalogPreferences = preferences.create("catalogs_data")
    
    // JavaScript plugin loader - uses lambda to get directory dynamically
    private val jsPluginLoader: JSPluginLoader by lazy {
        JSPluginLoader(
            pluginsDirectoryProvider = {
                File(System.getProperty("user.home"), ".ireader/js-plugins").apply { mkdirs() }.absolutePath.toPath()
            },
            httpClient = httpClients.default,
            preferenceStoreFactory = preferences,
            fileSystem = FileSystem.SYSTEM
        )
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
    
    /**
     * Check if JS engine is missing (plugins installed but can't be loaded).
     */
    override fun isJSEngineMissing(): Boolean = jsPluginLoader.jsEngineMissing
    
    /**
     * Get the number of JS plugins pending due to missing engine.
     */
    override fun getPendingJSPluginsCount(): Int = jsPluginLoader.pendingPluginsCount
    
    /**
     * Load engine plugins before JS plugins.
     * On Desktop, GraalVM is used instead of J2V8, so this is a no-op.
     */
    override suspend fun loadEnginePlugins() {
        // No-op on Desktop - GraalVM is bundled with the app
    }
    
    override suspend fun loadAll(): List<CatalogLocal> {
        val bundled = mutableListOf<CatalogLocal>()
        
        // Add Local Source for reading local novels
        val localSourceCatalog = ireader.domain.models.entities.CatalogBundled(
            source = ireader.core.source.LocalSource(),
            description = "Read novels from local storage",
            name = "Local Source"
        )
        bundled.add(localSourceCatalog)
        
        // Add Community Source for community-translated content
        communitySource?.let { source ->
            val communityCatalog = ireader.domain.models.entities.CommunityCatalog(
                source = source,
                description = "Browse and read community-translated novels"
            )
            bundled.add(communityCatalog)
        }
        
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

        // Load Tsundoku (Tachiyomi/Mihon) extensions natively
        val tsundokuCatalogs = loadTsundokuExtensions()

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

        return (bundled + installedCatalogs + tsundokuCatalogs + jsPlugins).distinctBy { it.sourceId }.toSet().toList()
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
            
            Dex2JarConverter.convert(file, jarFile)
            
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

    /**
     * Load all Tsundoku (Tachiyomi/Mihon) extension APKs from the extensions directory.
     */
    private fun loadTsundokuExtensions(): List<CatalogLocal> {
        val tsundokuPkgs = ExtensionDir.listFiles()
            .orEmpty()
            .filter { it.isDirectory }
            .map { File(it, it.name + ".apk") }
            .filter { it.exists() }
            .filter { file ->
                try {
                    ApkFile(file).use { apkFile ->
                        DesktopTsundokuExtensionLoader.isTsundokuExtension(apkFile.apkMeta)
                    }
                } catch (e: Exception) {
                    false
                }
            }

        if (tsundokuPkgs.isEmpty()) return emptyList()

        Log.info("DesktopCatalogLoader: Found ${tsundokuPkgs.size} tsundoku extensions")

        return tsundokuPkgs.flatMap { file ->
            loadTsundokuCatalog(file)
        }
    }

    /**
     * Load a single Tsundoku extension APK as IReader catalogs.
     * Returns a list because one extension APK can contain multiple sources.
     */
    private fun loadTsundokuCatalog(file: File): List<CatalogInstalled.Locally> {
        val pkgName = file.nameWithoutExtension
        return try {
            val apkFile = ApkFile(file)
            val data = DesktopTsundokuExtensionLoader.validateMetadata(pkgName, apkFile)
            apkFile.close()

            if (data == null) return emptyList()

            val sources = DesktopTsundokuExtensionLoader.loadSources(pkgName, file, data)
            if (sources.isEmpty()) {
                Log.warn { "DesktopCatalogLoader: No sources from tsundoku APK $pkgName" }
                return emptyList()
            }

            Log.info { "DesktopCatalogLoader: Loaded ${sources.size} source(s) from tsundoku APK $pkgName" }

            sources.map { source ->
                CatalogInstalled.Locally(
                    name = source.name,
                    description = if (data.isNovel) "Tsundoku novel extension" else "Tsundoku manga extension",
                    source = source,
                    pkgName = pkgName,
                    versionName = data.versionName,
                    versionCode = data.versionCode,
                    nsfw = data.nsfw,
                    installDir = file.parentFile!!.absolutePath.toPath(),
                    iconUrl = ""
                )
            }
        } catch (e: Exception) {
            Log.error("DesktopCatalogLoader: Failed to load tsundoku catalog $pkgName", e)
            emptyList()
        }
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

        val appInfo = Ksoup.parse(apkFile.manifestXml, Parser.xmlParser()).select("application").select("meta-data")

        val meta = appInfo.map { element ->
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
        const val LIB_VERSION_MIN = 2
        const val LIB_VERSION_MAX = 2
    }
}