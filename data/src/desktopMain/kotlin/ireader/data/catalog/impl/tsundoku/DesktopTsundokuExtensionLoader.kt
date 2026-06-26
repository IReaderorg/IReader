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
import okhttp3.OkHttpClient
import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.TimeUnit

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
    private const val LIB_VERSION_MIN = 1.4
    private const val LIB_VERSION_MAX = 1.6

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
            Log.warn { "TsundokuDesktopLoader: Invalid version format '$versionName' for $pkgName" }
            return null
        }

        if (libVersion < LIB_VERSION_MIN || libVersion > LIB_VERSION_MAX) {
            Log.warn { "TsundokuDesktopLoader: Unsupported lib version $libVersion for $pkgName" }
            return null
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
                val cause = e.cause
                if (cause != null && isAndroidClassError(cause)) {
                    Log.warn { "TsundokuDesktopLoader: ${data.classToLoad} requires Android APIs (not available on Desktop)" }
                } else {
                    Log.error { "TsundokuDesktopLoader: Static init failed for ${data.classToLoad}: ${cause?.message ?: e.message}" }
                }
                return emptyList()
            } catch (e: TypeNotPresentException) {
                Log.warn { "TsundokuDesktopLoader: ${data.classToLoad} requires Android type '${e.typeName()}' (not available on Desktop)" }
                return emptyList()
            } catch (e: NoClassDefFoundError) {
                if (isAndroidClassError(e)) {
                    Log.warn { "TsundokuDesktopLoader: ${data.classToLoad} requires Android APIs (not available on Desktop)" }
                } else {
                    Log.error { "TsundokuDesktopLoader: Missing class for ${data.classToLoad}: ${e.message}" }
                }
                return emptyList()
            } catch (e: Throwable) {
                if (isAndroidClassError(e)) {
                    Log.warn { "TsundokuDesktopLoader: ${data.classToLoad} requires Android APIs (not available on Desktop)" }
                } else {
                    Log.error { "TsundokuDesktopLoader: Failed to instantiate ${data.classToLoad}: ${e::class.simpleName}: ${e.message}" }
                }
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
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .callTimeout(2, TimeUnit.MINUTES)
                .build()

            val networkHelper = eu.kanade.tachiyomi.network.NetworkHelper(okHttpClient)
            uy.kohesive.injekt.Injekt.registerValue(
                eu.kanade.tachiyomi.network.NetworkHelper::class,
                networkHelper
            )

            dependenciesInitialized = true
            Log.info { "TsundokuDesktopLoader: Dependencies initialized (shim mode)" }
        } catch (e: Exception) {
            Log.warn { "TsundokuDesktopLoader: Failed to initialize dependencies: ${e.message}" }
        }
    }

    // ==================== Helpers ====================

    /**
     * Check if an error is caused by missing Android-specific classes.
     * These errors are expected on Desktop and should be handled gracefully.
     */
    private fun isAndroidClassError(error: Throwable): Boolean {
        val message = (error.message ?: "") + (error.cause?.message ?: "")
        return message.contains("android.") ||
            message.contains("androidx.") ||
            message.contains("Type android.") ||
            error is TypeNotPresentException && (error.typeName()?.startsWith("android.") == true ||
                error.typeName()?.startsWith("androidx.") == true)
    }

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
        return TsundokuCatalogSource(tsundokuSource)
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
