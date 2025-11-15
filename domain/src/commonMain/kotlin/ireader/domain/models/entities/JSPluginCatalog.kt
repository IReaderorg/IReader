package ireader.domain.models.entities

import ireader.domain.js.bridge.JSPluginSource
import ireader.domain.js.models.PluginMetadata
import java.io.File

/**
 * Catalog implementation for JavaScript plugins.
 * Wraps a JSPluginSource as a CatalogInstalled for integration with IReader's catalog system.
 */
data class JSPluginCatalog(
    override val source: JSPluginSource,
    val metadata: PluginMetadata,
    val pluginFile: File,
    override val name: String = metadata.name,
    override val description: String = metadata.site,
    override val pkgName: String = metadata.id,
    override val versionName: String = metadata.version,
    override val versionCode: Int = metadata.version.replace(".", "").toIntOrNull() ?: 100,
    // Convert relative icon paths to LNReader GitHub URLs
    override val iconUrl: String = when {
        metadata.icon.startsWith("http") -> metadata.icon
        metadata.icon.isNotBlank() -> {
            // LNReader plugins use GitHub raw URLs
            // Format: https://raw.githubusercontent.com/LNReader/lnreader-plugins/plugins/v3.0.0/public/static/{icon_path}
            val iconPath = metadata.icon.removePrefix("src/").removePrefix("/")
            "https://raw.githubusercontent.com/LNReader/lnreader-plugins/plugins/v3.0.0/public/static/$iconPath"
        }
        else -> "https://via.placeholder.com/300x300?text=${metadata.id}"
    },
    override val installDir: File = pluginFile.parentFile,
    override val nsfw: Boolean = false,
    override val isPinned: Boolean = false
) : CatalogInstalled() {
    
    /**
     * Plugin site URL.
     */
    val site: String get() = metadata.site
    
    /**
     * Equality based on source ID (plugin ID hash).
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JSPluginCatalog) return false
        return sourceId == other.sourceId
    }
    
    /**
     * Hash code based on source ID.
     */
    override fun hashCode(): Int {
        return sourceId.hashCode()
    }
}
