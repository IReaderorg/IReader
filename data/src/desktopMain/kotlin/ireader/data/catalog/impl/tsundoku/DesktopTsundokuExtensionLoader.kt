package ireader.data.catalog.impl.tsundoku

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import com.googlecode.d2j.dex.Dex2jar
import com.googlecode.d2j.reader.MultiDexFileReader
import com.googlecode.dex2jar.tools.BaksmaliBaseDexExceptionHandler
import ireader.core.log.Log
import ireader.core.source.Source
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import java.io.File
import java.net.URLClassLoader

/**
 * Desktop-specific loader for Tsundoku (Tachiyomi/Mihon) extension APKs.
 *
 * Uses dex2jar to convert APK DEX to JAR, then URLClassLoader to load.
 * Provides shim classes for RxJava, Injekt, and NetworkHelper.
 */
object DesktopTsundokuExtensionLoader {

    // Tsundoku extension feature flags
    private const val EXTENSION_FEATURE = "tachiyomi.extension"
    private const val EXTENSION_FEATURE_NOVEL = "tachiyomi.novelextension"
    private val EXTENSION_FEATURES = setOf(EXTENSION_FEATURE, EXTENSION_FEATURE_NOVEL)

    // Tsundoku metadata keys
    private const val METADATA_SOURCE_CLASS = "tachiyomi.extension.class"
    private const val METADATA_SOURCE_CLASS_NOVEL = "tachiyomi.novelextension.class"
    private const val METADATA_NSFW = "tachiyomi.extension.nsfw"
    private const val METADATA_NOVEL = "tachiyomi.extension.novel"

    // Tsundoku supported lib versions
    private const val LIB_VERSION_MIN = 1.3
    private const val LIB_VERSION_MAX = 2.0

    private var dependenciesInitialized = false

    /**
     * Check if an APK is a Tsundoku extension by its manifest metadata.
     */
    fun isTsundokuExtension(pkgInfo: ApkMeta): Boolean {
        return pkgInfo.usesFeatures.orEmpty().any { it.name in EXTENSION_FEATURES }
    }

    /**
     * Validate Tsundoku extension metadata from an APK file.
     */
    fun validateMetadata(pkgName: String, apkFile: ApkFile): TsundokuValidatedData? {
        val pkgInfo = apkFile.apkMeta
        if (!isTsundokuExtension(pkgInfo)) {
            return null
        }

        val versionName = pkgInfo.versionName ?: run {
            Log.warn { "TsundokuDesktopLoader: Missing versionName for $pkgName" }
            return null
        }

        @Suppress("DEPRECATION")
        val versionCode = pkgInfo.versionCode

        val libVersion = try {
            versionName.substringBeforeLast('.').toDouble()
        } catch (e: NumberFormatException) {
            Log.warn { "TsundokuDesktopLoader: Invalid version format '$versionName' for $pkgName, trying anyway" }
            0.0
        }

        if (libVersion < LIB_VERSION_MIN || libVersion > LIB_VERSION_MAX) {
            Log.warn { "TsundokuDesktopLoader: lib version $libVersion for $pkgName outside range, trying anyway" }
        }

        // Parse metadata from AndroidManifest.xml using Ksoup
        val manifestXml = apkFile.manifestXml
        val metaElements = Ksoup.parse(manifestXml, Parser.xmlParser())
            .select("application")
            .select("meta-data")

        val meta = metaElements.map { element ->
            val name = element.attr("android:name")
            val value = element.attr("android:value")
            name to value
        }

        val isNovel = meta.any { it.first == METADATA_NOVEL && it.second == "1" } ||
            pkgInfo.usesFeatures.orEmpty().any { it.name == EXTENSION_FEATURE_NOVEL }

        val metaNs = if (isNovel) "tachiyomi.novelextension" else "tachiyomi.extension"
        val sourceClassName = meta.find { it.first == "$metaNs.class" }?.second
            ?: meta.find { it.first == METADATA_SOURCE_CLASS }?.second
            ?: meta.find { it.first == METADATA_SOURCE_CLASS_NOVEL }?.second
            ?: run {
                Log.warn { "TsundokuDesktopLoader: Missing source class metadata for $pkgName" }
                return null
            }

        val nsfw = meta.any { (it.first == METADATA_NSFW || it.first == "$metaNs.nsfw") && it.second == "1" }

        val classToLoad = if (sourceClassName.startsWith(".")) {
            pkgInfo.packageName + sourceClassName
        } else {
            sourceClassName
        }

        return TsundokuValidatedData(
            versionCode = versionCode.toInt(),
            versionName = versionName,
            libVersion = libVersion,
            isNovel = isNovel,
            nsfw = nsfw,
            classToLoad = classToLoad,
            factoryClassName = null
        )
    }

    /**
     * Load a Tsundoku source from an APK file using dex2jar + URLClassLoader.
     */
    fun loadSources(pkgName: String, apkFile: File, data: TsundokuValidatedData): List<Source> {
        initializeDependencies()

        return try {
            // Convert APK DEX to JAR using dex2jar
            val jarFile = File(apkFile.parentFile, "${pkgName}_tsundoku.jar")
            convertDex2Jar(apkFile, jarFile)

            if (!jarFile.exists() || jarFile.length() == 0L) {
                Log.error { "TsundokuDesktopLoader: dex2jar conversion failed for $pkgName" }
                return emptyList()
            }

            // Load the JAR with our shim classes on the parent classpath
            val parentClassLoader = this::class.java.classLoader
            val loader = URLClassLoader(
                arrayOf(jarFile.toURL()),
                parentClassLoader
            )

            val clazz = try {
                Class.forName(data.classToLoad, false, loader)
            } catch (e: ClassNotFoundException) {
                // Try fallback namespace
                if (data.classToLoad.startsWith("app.tsundoku.extension.")) {
                    val fallback = data.classToLoad.replace(
                        "app.tsundoku.extension.",
                        "eu.kanade.tachiyomi.extension."
                    )
                    Class.forName(fallback, false, loader)
                } else {
                    throw e
                }
            }

            val instance = try {
                clazz.getDeclaredConstructor().newInstance()
            } catch (e: NoSuchMethodException) {
                Log.error { "TsundokuDesktopLoader: No no-arg constructor for ${data.classToLoad}" }
                return emptyList()
            } catch (e: ExceptionInInitializerError) {
                val causeChain = buildString {
                    append(e.cause?.let { it::class.simpleName } ?: "null")
                    var cause = e.cause?.cause
                    var depth = 0
                    while (cause != null && depth < 3) {
                        append(" → ${cause::class.simpleName}: ${cause.message}")
                        cause = cause.cause
                        depth++
                    }
                }
                Log.error { "TsundokuDesktopLoader: Static init failed for ${data.classToLoad}: $causeChain" }
                return emptyList()
            } catch (e: NoClassDefFoundError) {
                Log.error { "TsundokuDesktopLoader: Missing class for ${data.classToLoad}: ${e.message}" }
                return emptyList()
            } catch (e: Throwable) {
                // Unwrap InvocationTargetException to get the real cause
                val realCause = if (e is java.lang.reflect.InvocationTargetException) e.cause ?: e else e
                val causeChain = buildString {
                    append("${realCause::class.simpleName}: ${realCause.message}")
                    var cause = realCause.cause
                    var depth = 0
                    while (cause != null && depth < 5) {
                        append(" → ${cause::class.simpleName}: ${cause.message}")
                        cause = cause.cause
                        depth++
                    }
                }
                Log.error { "TsundokuDesktopLoader: Failed to instantiate ${data.classToLoad}: $causeChain" }
                return emptyList()
            }

            when {
                isSourceFactory(instance) -> {
                    val sources = invokeCreateSources(instance)
                    sources.map { wrapSource(it) }
                }
                isTsundokuSource(instance) -> {
                    listOf(wrapSource(instance))
                }
                else -> {
                    Log.error { "TsundokuDesktopLoader: Unknown source type: ${instance.javaClass.name}" }
                    emptyList()
                }
            }
        } catch (e: Throwable) {
            Log.error { "TsundokuDesktopLoader: Failed to load $pkgName: ${e::class.simpleName}: ${e.message}" }
            e.printStackTrace()
            emptyList()
        }
    }

    // ==================== DI Setup ====================

    private fun initializeDependencies() {
        if (dependenciesInitialized) return

        try {
            val networkHelper = eu.kanade.tachiyomi.network.NetworkHelper(
                cacheDir = java.io.File(System.getProperty("java.io.tmpdir"), "ireader-network-cache").apply { mkdirs() }
            )
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            }
            val app = android.app.Application()

            uy.kohesive.injekt.Injekt.addSingletonFactory(
                object : uy.kohesive.injekt.api.FullTypeReference<eu.kanade.tachiyomi.network.NetworkHelper>() {}
            ) { networkHelper }

            uy.kohesive.injekt.Injekt.addSingletonFactory(
                object : uy.kohesive.injekt.api.FullTypeReference<kotlinx.serialization.json.Json>() {}
            ) { json }

            uy.kohesive.injekt.Injekt.addSingletonFactory(
                object : uy.kohesive.injekt.api.FullTypeReference<android.app.Application>() {}
            ) { app }

            dependenciesInitialized = true
            Log.info { "TsundokuDesktopLoader: Dependencies initialized" }
        } catch (e: Exception) {
            Log.warn { "TsundokuDesktopLoader: Failed to initialize dependencies: ${e.message}" }
        }
    }

    // ==================== Helpers ====================

    private fun convertDex2Jar(dexFile: File, jarFile: File) {
        try {
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
                .to(jarFile.toPath())
        } catch (e: Exception) {
            Log.error { "TsundokuDesktopLoader: dex2jar error: ${e.message}" }
        }
    }

    private fun wrapSource(tsundokuSource: Any): Source {
        return TsundokuCatalogSource(tsundokuSource as eu.kanade.tachiyomi.source.CatalogueSource)
    }

    private fun isTsundokuSource(obj: Any): Boolean {
        return try {
            Class.forName("eu.kanade.tachiyomi.source.Source").isInstance(obj)
        } catch (e: Exception) {
            false
        }
    }

    private fun isSourceFactory(obj: Any): Boolean {
        return try {
            Class.forName("eu.kanade.tachiyomi.source.SourceFactory").isInstance(obj)
        } catch (e: Exception) {
            false
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun invokeCreateSources(factory: Any): List<Any> {
        val method = factory.javaClass.getMethod("createSources")
        return method.invoke(factory) as? List<Any> ?: emptyList()
    }
}
