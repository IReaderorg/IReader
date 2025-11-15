package ireader.domain.plugins

/**
 * Complete information about a plugin including runtime status
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */
data class PluginInfo(
    val id: String,
    val manifest: PluginManifest,
    val status: PluginStatus,
    val installDate: Long,
    val lastUpdate: Long?,
    val isPurchased: Boolean,
    val rating: Float?,
    val downloadCount: Int
)
