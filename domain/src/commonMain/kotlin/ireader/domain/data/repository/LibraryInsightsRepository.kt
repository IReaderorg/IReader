package ireader.domain.data.repository

import ireader.domain.models.entities.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for library insights and analytics
 */
interface LibraryInsightsRepository {
    /**
     * Get comprehensive library insights
     */
    suspend fun getLibraryInsights(): LibraryInsights
    
    /**
     * Get library insights as a flow for reactive updates
     */
    fun getLibraryInsightsFlow(): Flow<LibraryInsights>
    
    /**
     * Get reading analytics
     */
    suspend fun getReadingAnalytics(): ReadingAnalytics
    
    /**
     * Get reading analytics as a flow
     */
    fun getReadingAnalyticsFlow(): Flow<ReadingAnalytics>
    
    /**
     * Track a reading session
     */
    suspend fun trackReadingSession(session: ReadingSession)
    
    /**
     * Get upcoming releases based on release patterns
     */
    suspend fun getUpcomingReleases(): List<UpcomingRelease>
    
    /**
     * Get upcoming releases as a flow
     */
    fun getUpcomingReleasesFlow(): Flow<List<UpcomingRelease>>
    
    /**
     * Get book recommendations based on reading history
     */
    suspend fun getRecommendations(limit: Int = 20): List<BookRecommendation>
    
    /**
     * Export statistics to JSON
     */
    suspend fun exportStatistics(): StatisticsExport
}

/**
 * Repository for advanced search functionality
 */
interface GlobalSearchRepository {
    /**
     * Perform global search across all sources
     */
    suspend fun searchGlobal(
        query: String,
        sources: List<Long> = emptyList()
    ): GlobalSearchResult
    
    /**
     * Perform global search as a flow for progressive results
     */
    fun searchGlobalFlow(
        query: String,
        sources: List<Long> = emptyList()
    ): Flow<GlobalSearchResult>
    
    /**
     * Save search history
     */
    suspend fun saveSearchHistory(query: String)
    
    /**
     * Get search history
     */
    suspend fun getSearchHistory(limit: Int = 20): List<String>
    
    /**
     * Clear search history
     */
    suspend fun clearSearchHistory()
}

/**
 * Repository for advanced filtering
 */
interface AdvancedFilterRepository {
    /**
     * Apply advanced filters to library
     */
    suspend fun applyFilters(filterState: AdvancedFilterState): List<BookItem>
    
    /**
     * Apply filters as a flow
     */
    fun applyFiltersFlow(filterState: AdvancedFilterState): Flow<List<BookItem>>
    
    /**
     * Save filter preset
     */
    suspend fun saveFilterPreset(name: String, filterState: AdvancedFilterState)
    
    /**
     * Get saved filter presets
     */
    suspend fun getFilterPresets(): List<Pair<String, AdvancedFilterState>>
    
    /**
     * Delete filter preset
     */
    suspend fun deleteFilterPreset(name: String)
    
    /**
     * Get available genres from library
     */
    suspend fun getAvailableGenres(): List<String>
    
    /**
     * Get available authors from library
     */
    suspend fun getAvailableAuthors(): List<String>
}
