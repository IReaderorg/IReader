package ireader.plugin.api

import ireader.plugin.api.source.*
import ireader.plugin.api.tachi.TachiCatalogueSource
import ireader.plugin.api.tachi.TachiSource
import kotlinx.serialization.Serializable

/**
 * Plugin interface for loading Tachiyomi/Mihon extension APKs.
 * 
 * Extends [SourceLoaderPlugin] to integrate with IReader's unified source system.
 * The plugin handles all Tachi-specific logic:
 * - APK parsing and validation
 * - DEX to JAR conversion (desktop) or DexClassLoader (Android)
 * - Source instantiation and lifecycle
 * - Extension repository management
 * - Model conversion to unified source format
 */
interface TachiSourceLoaderPlugin : SourceLoaderPlugin {
    
    override val loaderType: SourceLoaderType
        get() = SourceLoaderType.TACHIYOMI
    
    override val loaderName: String
        get() = "Tachiyomi"
    
    override fun supportsRepositories(): Boolean = true
    
    override val supportedFormats: List<ExtensionFormat>
        get() = listOf(ExtensionFormat.APK)
    
    // ==================== Tachi-Specific Methods ====================
    
    /**
     * Load a Tachiyomi extension from an APK file.
     * @return Extension info with loaded sources
     */
    suspend fun loadTachiExtension(apkPath: String): TachiExtensionInfo
    
    /**
     * Unload a Tachi extension and release resources.
     */
    fun unloadTachiExtension(pkgName: String)
    
    /**
     * Get the raw Tachi source (for advanced usage).
     */
    fun getTachiSource(sourceId: Long): TachiSource?
    
    /**
     * Get raw Tachi catalogue sources.
     */
    fun getTachiCatalogueSources(): List<TachiCatalogueSource>
    
    /**
     * Validate a Tachi APK before loading.
     */
    suspend fun validateTachiExtension(apkPath: String): TachiValidationResult
    
    /**
     * Get loaded Tachi extensions.
     */
    fun getTachiExtensions(): List<TachiExtensionInfo>
}

/**
 * Loaded Tachi extension info.
 */
@Serializable
data class TachiExtensionInfo(
    val pkgName: String,
    val name: String,
    val versionName: String,
    val versionCode: Int,
    val lang: String,
    val isNsfw: Boolean,
    val sourceIds: List<Long>,
    val iconUrl: String? = null,
    val hasUpdate: Boolean = false,
    val libVersion: Double = 1.5
) {
    /** Convert to unified SourceExtensionInfo */
    fun toSourceExtensionInfo(): SourceExtensionInfo = SourceExtensionInfo(
        id = pkgName,
        pkgName = pkgName,
        name = name,
        versionName = versionName,
        versionCode = versionCode,
        lang = lang,
        isNsfw = isNsfw,
        sourceIds = sourceIds,
        iconUrl = iconUrl,
        hasUpdate = hasUpdate,
        loaderType = SourceLoaderType.TACHIYOMI
    )
}

/**
 * Extension validation result.
 */
@Serializable
sealed class TachiValidationResult {
    @Serializable
    data class Valid(val pkgName: String, val name: String, val libVersion: Double) : TachiValidationResult()
    @Serializable
    data class Invalid(val reason: String) : TachiValidationResult()
    @Serializable
    data class UnsupportedVersion(val version: Double) : TachiValidationResult()
}

/**
 * Exception for Tachi extension errors.
 */
class TachiExtensionException(message: String, cause: Throwable? = null) : Exception(message, cause)
