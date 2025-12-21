package ireader.plugin.api.source

import ireader.plugin.api.PluginContext
import kotlinx.serialization.Serializable

/**
 * General interface for loading source extensions from various formats.
 * 
 * Supports:
 * - APK files (Tachiyomi/Mihon extensions)
 * - JAR files (JVM-based extensions)
 * - JSON files (declarative source definitions)
 * - JS bundles (LNReader-style JavaScript sources)
 * - Native plugins (.iplugin format)
 * 
 * Each source loader plugin implements this interface to handle
 * its specific extension format.
 */
interface ExtensionLoader {
    
    /**
     * Supported extension file formats.
     */
    val supportedFormats: List<ExtensionFormat>
    
    /**
     * Load an extension from a file path.
     * @param path Path to the extension file
     * @return Loaded extension with sources
     */
    suspend fun loadExtension(path: String): ExtensionLoadResult
    
    /**
     * Load an extension from raw bytes.
     * Useful for loading from network or embedded resources.
     * @param bytes Raw extension data
     * @param format The format of the extension
     * @return Loaded extension with sources
     */
    suspend fun loadExtensionFromBytes(bytes: ByteArray, format: ExtensionFormat): ExtensionLoadResult
    
    /**
     * Unload an extension and release resources.
     * @param extensionId The extension identifier
     */
    fun unloadExtension(extensionId: String)
    
    /**
     * Validate an extension file before loading.
     * @param path Path to the extension file
     * @return Validation result
     */
    suspend fun validateExtension(path: String): ExtensionValidationResult
    
    /**
     * Validate extension from bytes.
     * @param bytes Raw extension data
     * @param format The format of the extension
     * @return Validation result
     */
    suspend fun validateExtensionFromBytes(bytes: ByteArray, format: ExtensionFormat): ExtensionValidationResult
    
    /**
     * Get metadata from an extension without fully loading it.
     * Useful for displaying extension info before installation.
     * @param path Path to the extension file
     * @return Extension metadata
     */
    suspend fun getExtensionMetadata(path: String): ExtensionMetadataResult
    
    /**
     * Check if this loader can handle the given file.
     * @param path Path to the file
     * @return true if this loader can handle the file
     */
    fun canHandle(path: String): Boolean {
        val extension = path.substringAfterLast('.', "").lowercase()
        return supportedFormats.any { it.fileExtensions.contains(extension) }
    }
    
    /**
     * Check if this loader can handle the given format.
     */
    fun canHandle(format: ExtensionFormat): Boolean {
        return supportedFormats.contains(format)
    }
}

/**
 * Extension file formats supported by loaders.
 */
@Serializable
enum class ExtensionFormat(val fileExtensions: List<String>) {
    /** Android APK (Tachiyomi extensions) */
    APK(listOf("apk")),
    
    /** Java JAR file */
    JAR(listOf("jar")),
    
    /** IReader plugin format (ZIP with manifest) */
    IPLUGIN(listOf("iplugin")),
    
    /** JSON source definition */
    JSON(listOf("json")),
    
    /** JavaScript bundle (LNReader sources) */
    JS_BUNDLE(listOf("js", "mjs")),
    
    /** TypeScript source (compiled to JS) */
    TS_BUNDLE(listOf("ts")),
    
    /** ZIP archive containing sources */
    ZIP(listOf("zip")),
    
    /** DEX file (Android bytecode) */
    DEX(listOf("dex"))
}

/**
 * Result of loading an extension.
 */
sealed class ExtensionLoadResult {
    /**
     * Extension loaded successfully.
     */
    data class Success(
        val extension: LoadedExtension
    ) : ExtensionLoadResult()
    
    /**
     * Extension failed to load.
     */
    data class Failure(
        val error: ExtensionLoadError
    ) : ExtensionLoadResult()
}

/**
 * A successfully loaded extension.
 */
data class LoadedExtension(
    /** Unique extension identifier (usually package name) */
    val id: String,
    /** Display name */
    val name: String,
    /** Version string */
    val versionName: String,
    /** Version code for comparison */
    val versionCode: Int,
    /** Primary language code */
    val lang: String,
    /** Whether extension contains NSFW content */
    val isNsfw: Boolean,
    /** Sources provided by this extension */
    val sources: List<UnifiedSource>,
    /** Icon URL or data URI */
    val iconUrl: String? = null,
    /** Extension format */
    val format: ExtensionFormat,
    /** Loader type that loaded this extension */
    val loaderType: SourceLoaderType,
    /** Additional metadata */
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Extension load errors.
 */
@Serializable
sealed class ExtensionLoadError {
    /** File not found */
    @Serializable
    data class FileNotFound(val path: String) : ExtensionLoadError()
    
    /** Invalid or corrupted file */
    @Serializable
    data class InvalidFormat(val reason: String) : ExtensionLoadError()
    
    /** Unsupported extension version */
    @Serializable
    data class UnsupportedVersion(val version: String, val minSupported: String, val maxSupported: String) : ExtensionLoadError()
    
    /** Missing required dependency */
    @Serializable
    data class MissingDependency(val dependency: String) : ExtensionLoadError()
    
    /** Class loading failed */
    @Serializable
    data class ClassLoadError(val className: String, val reason: String) : ExtensionLoadError()
    
    /** Source instantiation failed */
    @Serializable
    data class InstantiationError(val className: String, val reason: String) : ExtensionLoadError()
    
    /** Security validation failed */
    @Serializable
    data class SecurityError(val reason: String) : ExtensionLoadError()
    
    /** Generic error */
    @Serializable
    data class Unknown(val message: String) : ExtensionLoadError()
}

/**
 * Result of validating an extension.
 */
@Serializable
sealed class ExtensionValidationResult {
    /** Extension is valid and can be loaded */
    @Serializable
    data class Valid(
        val id: String,
        val name: String,
        val version: String,
        val format: ExtensionFormat
    ) : ExtensionValidationResult()
    
    /** Extension is invalid */
    @Serializable
    data class Invalid(
        val reason: String,
        val error: ExtensionLoadError? = null
    ) : ExtensionValidationResult()
    
    /** Extension version is not supported */
    @Serializable
    data class UnsupportedVersion(
        val version: String,
        val minSupported: String,
        val maxSupported: String
    ) : ExtensionValidationResult()
}

/**
 * Result of getting extension metadata.
 */
sealed class ExtensionMetadataResult {
    /** Metadata retrieved successfully */
    data class Success(
        val metadata: ExtensionMetadata
    ) : ExtensionMetadataResult()
    
    /** Failed to get metadata */
    data class Failure(
        val error: ExtensionLoadError
    ) : ExtensionMetadataResult()
}

/**
 * Extension metadata without loading sources.
 */
@Serializable
data class ExtensionMetadata(
    /** Unique identifier */
    val id: String,
    /** Display name */
    val name: String,
    /** Version string */
    val versionName: String,
    /** Version code */
    val versionCode: Int,
    /** Primary language */
    val lang: String,
    /** Whether NSFW */
    val isNsfw: Boolean,
    /** Extension format */
    val format: ExtensionFormat,
    /** Number of sources in extension */
    val sourceCount: Int = 0,
    /** Main class name (for APK/JAR) */
    val mainClass: String? = null,
    /** Library/API version */
    val libVersion: String? = null,
    /** Description */
    val description: String? = null,
    /** Author */
    val author: String? = null,
    /** Icon URL */
    val iconUrl: String? = null,
    /** File size in bytes */
    val fileSize: Long? = null,
    /** Additional metadata */
    val extra: Map<String, String> = emptyMap()
)

/**
 * Platform-specific extension installer.
 * Handles the actual installation of extensions to the device.
 */
interface ExtensionInstaller {
    
    /**
     * Install an extension from a file.
     * @param path Path to the extension file
     * @param targetDir Target directory for installation
     * @return Installation result
     */
    suspend fun install(path: String, targetDir: String): ExtensionInstallResult
    
    /**
     * Install an extension from bytes.
     * @param bytes Raw extension data
     * @param fileName File name for the extension
     * @param targetDir Target directory for installation
     * @return Installation result
     */
    suspend fun installFromBytes(bytes: ByteArray, fileName: String, targetDir: String): ExtensionInstallResult
    
    /**
     * Uninstall an extension.
     * @param extensionId Extension identifier
     * @param installDir Installation directory
     * @return true if uninstalled successfully
     */
    suspend fun uninstall(extensionId: String, installDir: String): Boolean
    
    /**
     * Get the installation path for an extension.
     * @param extensionId Extension identifier
     * @param installDir Base installation directory
     * @return Path to the installed extension, or null if not installed
     */
    fun getInstalledPath(extensionId: String, installDir: String): String?
    
    /**
     * List all installed extensions in a directory.
     * @param installDir Installation directory
     * @return List of installed extension IDs
     */
    fun listInstalled(installDir: String): List<String>
}

/**
 * Result of installing an extension.
 */
sealed class ExtensionInstallResult {
    /** Installation successful */
    data class Success(
        val extensionId: String,
        val installedPath: String
    ) : ExtensionInstallResult()
    
    /** Installation failed */
    data class Failure(
        val error: ExtensionLoadError
    ) : ExtensionInstallResult()
}

/**
 * Extension downloader for fetching extensions from repositories.
 */
interface ExtensionDownloader {
    
    /**
     * Download an extension from URL.
     * @param url Download URL
     * @param targetPath Target file path
     * @param onProgress Progress callback (0.0 to 1.0)
     * @return Download result
     */
    suspend fun download(
        url: String,
        targetPath: String,
        onProgress: (Float) -> Unit = {}
    ): ExtensionDownloadResult
    
    /**
     * Download an extension to memory.
     * @param url Download URL
     * @param onProgress Progress callback
     * @return Downloaded bytes or error
     */
    suspend fun downloadToMemory(
        url: String,
        onProgress: (Float) -> Unit = {}
    ): ExtensionDownloadBytesResult
}

/**
 * Result of downloading an extension.
 */
sealed class ExtensionDownloadResult {
    data class Success(val path: String, val size: Long) : ExtensionDownloadResult()
    data class Failure(val error: String) : ExtensionDownloadResult()
}

/**
 * Result of downloading extension to memory.
 */
sealed class ExtensionDownloadBytesResult {
    data class Success(val bytes: ByteArray, val size: Long) : ExtensionDownloadBytesResult()
    data class Failure(val error: String) : ExtensionDownloadBytesResult()
}
