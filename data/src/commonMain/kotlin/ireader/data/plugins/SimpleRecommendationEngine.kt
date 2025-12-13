package ireader.data.plugins

import ireader.domain.plugins.marketplace.PluginRecommendation
import ireader.domain.plugins.marketplace.RecommendationEngine
import ireader.domain.plugins.marketplace.RecommendationReason

/**
 * Simple recommendation engine implementation.
 * In production, this would use more sophisticated algorithms.
 */
class SimpleRecommendationEngine(
    private val pluginRepository: PluginRecommendationDataSource
) : RecommendationEngine {
    
    override suspend fun getRecommendations(
        installedPluginIds: List<String>
    ): List<PluginRecommendation> {
        val recommendations = mutableListOf<PluginRecommendation>()
        
        // Get similar plugins based on installed ones
        for (pluginId in installedPluginIds.take(5)) {
            val similar = getSimilarPlugins(pluginId)
                .filter { it.pluginId !in installedPluginIds }
            recommendations.addAll(similar)
        }
        
        // Add trending plugins
        val trending = pluginRepository.getTrendingPlugins(10)
            .filter { it.pluginId !in installedPluginIds }
            .map { plugin ->
                PluginRecommendation(
                    pluginId = plugin.pluginId,
                    pluginName = plugin.pluginName,
                    pluginIconUrl = plugin.pluginIconUrl,
                    reason = RecommendationReason.TRENDING,
                    score = plugin.trendScore,
                    basedOnPluginIds = emptyList()
                )
            }
        recommendations.addAll(trending)
        
        // Add highly rated plugins
        val highlyRated = pluginRepository.getHighlyRatedPlugins(10)
            .filter { it.pluginId !in installedPluginIds }
            .map { plugin ->
                PluginRecommendation(
                    pluginId = plugin.pluginId,
                    pluginName = plugin.pluginName,
                    pluginIconUrl = plugin.pluginIconUrl,
                    reason = RecommendationReason.HIGHLY_RATED,
                    score = plugin.rating,
                    basedOnPluginIds = emptyList()
                )
            }
        recommendations.addAll(highlyRated)
        
        // Deduplicate and sort by score
        return recommendations
            .distinctBy { it.pluginId }
            .sortedByDescending { it.score }
            .take(20)
    }
    
    override suspend fun getSimilarPlugins(pluginId: String): List<PluginRecommendation> {
        val plugin = pluginRepository.getPluginInfo(pluginId) ?: return emptyList()
        
        // Find plugins with similar tags/category
        return pluginRepository.getPluginsByCategory(plugin.category)
            .filter { it.pluginId != pluginId }
            .map { similar ->
                PluginRecommendation(
                    pluginId = similar.pluginId,
                    pluginName = similar.pluginName,
                    pluginIconUrl = similar.pluginIconUrl,
                    reason = RecommendationReason.SIMILAR_TO_INSTALLED,
                    score = calculateSimilarityScore(plugin, similar),
                    basedOnPluginIds = listOf(pluginId)
                )
            }
            .sortedByDescending { it.score }
            .take(5)
    }
    
    override suspend fun getFrequentlyUsedTogether(
        pluginId: String
    ): List<PluginRecommendation> {
        return pluginRepository.getFrequentlyUsedTogether(pluginId)
            .map { plugin ->
                PluginRecommendation(
                    pluginId = plugin.pluginId,
                    pluginName = plugin.pluginName,
                    pluginIconUrl = plugin.pluginIconUrl,
                    reason = RecommendationReason.FREQUENTLY_USED_TOGETHER,
                    score = plugin.coUsageScore,
                    basedOnPluginIds = listOf(pluginId)
                )
            }
    }
    
    private fun calculateSimilarityScore(
        plugin1: PluginBasicInfo,
        plugin2: PluginBasicInfo
    ): Float {
        var score = 0f
        
        // Same category
        if (plugin1.category == plugin2.category) score += 0.3f
        
        // Shared tags
        val sharedTags = plugin1.tags.intersect(plugin2.tags.toSet())
        score += (sharedTags.size.toFloat() / maxOf(plugin1.tags.size, plugin2.tags.size, 1)) * 0.4f
        
        // Similar rating
        val ratingDiff = kotlin.math.abs(plugin1.rating - plugin2.rating)
        score += (1f - ratingDiff / 5f) * 0.2f
        
        // Popularity factor
        score += (plugin2.downloadCount.toFloat() / 1_000_000f).coerceAtMost(0.1f)
        
        return score.coerceIn(0f, 1f)
    }
}

/**
 * Data source interface for recommendation data.
 */
interface PluginRecommendationDataSource {
    suspend fun getPluginInfo(pluginId: String): PluginBasicInfo?
    suspend fun getPluginsByCategory(category: String): List<PluginBasicInfo>
    suspend fun getTrendingPlugins(limit: Int): List<TrendingPluginInfo>
    suspend fun getHighlyRatedPlugins(limit: Int): List<PluginBasicInfo>
    suspend fun getFrequentlyUsedTogether(pluginId: String): List<CoUsagePluginInfo>
}

data class PluginBasicInfo(
    val pluginId: String,
    val pluginName: String,
    val pluginIconUrl: String?,
    val category: String,
    val tags: List<String>,
    val rating: Float,
    val downloadCount: Long
)

data class TrendingPluginInfo(
    val pluginId: String,
    val pluginName: String,
    val pluginIconUrl: String?,
    val trendScore: Float
)

data class CoUsagePluginInfo(
    val pluginId: String,
    val pluginName: String,
    val pluginIconUrl: String?,
    val coUsageScore: Float
)
