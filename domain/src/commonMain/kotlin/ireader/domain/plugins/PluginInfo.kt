package ireader.domain.plugins

/**
 * Complete information about a plugin including runtime status
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */
data class PluginInfo(
    val id: String,
    val manifest: PluginManifest,
    val status: PluginStatus,
    val installDate: Long? = null,
    val lastUpdate: Long? = null,
    val isPurchased: Boolean = false,
    val rating: Float? = null,
    val downloadCount: Int = 0,
    /** URL of the repository this plugin came from */
    val repositoryUrl: String? = null,
    /** Direct download URL for the plugin */
    val downloadUrl: String? = null,
    /** File size in bytes */
    val fileSize: Long = 0,
    /** Checksum for verification */
    val checksum: String? = null
)
