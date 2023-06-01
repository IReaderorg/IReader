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
import java.io.File
import java.net.URLClassLoader

class DesktopCatalogLoader(
    private val httpClients: HttpClients,
    val uiPreferences: UiPreferences,
    preferences: PreferenceStoreFactory
) : CatalogLoader {
    private val catalogPreferences = preferences.create("catalogs_data")
    override suspend fun loadAll(): List<CatalogLocal> {
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

        return (installedCatalogs).distinctBy { it.sourceId }.toSet().toList()
    }

    override fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally? {
        val file = File(ExtensionDir, "${pkgName}/${pkgName}.apk")
        try {

            val pkgInfo = if (file.exists() && file.canRead()) {
                ApkFile(file)
            } else {
                null
            }
            if (pkgInfo == null) {
                Log.warn("The requested catalog {} wasn't found", pkgName)
                return null
            }

            return loadLocalCatalogs(pkgName, pkgInfo, file)
        }catch (e:Exception) {
            file.parentFile.deleteRecursively()
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
        if (!jarFile.exists() || jarFile.length() == 0L) {
            dex2jar(file,jarFile,file.name)
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
            installDir = file.parentFile!!,
            iconUrl = data.icon
        )


    }

    private fun isPackageAnExtension(pkgInfo: ApkMeta): Boolean {
        return pkgInfo.usesFeatures.orEmpty().any { it.name == EXTENSION_FEATURE }
    }

    private fun validateMetadata(pkgName: String, apkFile: ApkFile): ValidatedData? {
        val pkgInfo = apkFile.apkMeta
        if (!isPackageAnExtension(pkgInfo)) {
            Log.warn("Failed to load catalog, package {} isn't a catalog", pkgName)
            return null
        }
        @Suppress("DEPRECATION")
        val versionCode = pkgInfo.versionCode
        val versionName = pkgInfo.versionName

        // Validate lib version
        val majorLibVersion = versionName.substringBefore('.').toInt()
        if (majorLibVersion < LIB_VERSION_MIN || majorLibVersion > LIB_VERSION_MAX) {
            val exception = "Failed to load catalog, the package {} lib version is {}," +
                    "while only versions {} to {} are allowed"
            Log.warn(exception, pkgName, majorLibVersion, LIB_VERSION_MIN, LIB_VERSION_MAX)
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
            Log.warn("Failed to load catalog, the package {} didn't define source class", pkgName)
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
            Log.warn(e, "Failed to load catalog {}", pkgName)
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
            Log.error(e)
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