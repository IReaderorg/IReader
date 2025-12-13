package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Fetches and parses plugin repository index.json files
 */
interface PluginRepositoryIndexFetcher {
    /**
     * Fetch repository index from URL
     */
    suspend fun fetchIndex(url: String): Result<PluginRepositoryIndex>
}

/**
 * Repository index structure (matches IReader-plugins repo/index.json)
 */
@Serializable
data class PluginRepositoryIndex(
    val version: Int = 1,
    val plugins: List<PluginIndexEntry> = emptyList()
)

/**
 * Plugin entry in repository index
 */
@Serializable
data class PluginIndexEntry(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val description: String,
    val author: PluginAuthorInfo,
    val type: String,
    val permissions: List<String> = emptyList(),
    val minIReaderVersion: String = "1.0.0",
    val platforms: List<String> = emptyList(),
    val iconUrl: String? = null,
    val monetization: PluginMonetizationInfo? = null,
    val downloadUrl: String,
    val fileSize: Long = 0,
    val checksum: String? = null
)

@Serializable
data class PluginAuthorInfo(
    val name: String,
    val email: String? = null,
    val website: String? = null
)

@Serializable
data class PluginMonetizationInfo(
    val type: String = "FREE",
    val price: Double? = null,
    val currency: String? = "USD",
    val trialDays: Int? = null
)
