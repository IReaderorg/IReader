package ireader.presentation.ui.settings.statistics

import ireader.presentation.core.viewmodel.IReaderStateScreenModel
// screenModelScope is provided by IReaderStateScreenModel
import ireader.core.log.Log
import ireader.domain.models.entities.*
import ireader.domain.usecases.statistics.*
import ireader.domain.usecases.remote.GlobalSearchUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Comprehensive statistics screen model following Mihon's StateScreenModel pattern
 */
class StatsScreenModel(
    private val getLibraryInsights: GetLibraryInsightsUseCase,
    private val getReadingAnalytics: GetReadingAnalyticsUseCase,
    private val getReadingStatistics: GetReadingStatisticsUseCase,
    private val getUpcomingReleases: GetUpcomingReleasesUseCase,
    private val getRecommendations: GetRecommendationsUseCase,
    private val exportStatistics: ExportStatisticsUseCase,
    private val globalSearch: GlobalSearchUseCase,
    private val applyAdvancedFilters: ApplyAdvancedFiltersUseCase
) : IReaderStateScreenModel<StatsScreenModel.State>(State()) {

    data class State(
        val isLoading: Boolean = true,
        val error: String? = null,
        val libraryInsights: LibraryInsights = LibraryInsights(),
        val readingStatisticsType1: ReadingStatisticsType1 = ReadingStatisticsType1(),
        val readingAnalytics: ReadingAnalytics = ReadingAnalytics(),
        val upcomingReleases: List<UpcomingRelease> = emptyList(),
        val recommendations: List<BookRecommendation> = emptyList(),
        val selectedTab: StatsTab = StatsTab.OVERVIEW,
        val searchQuery: String = "",
        val searchResults: GlobalSearchResult? = null,
        val isSearching: Boolean = false,
        val filterState: AdvancedFilterState = AdvancedFilterState(),
        val filteredBooks: List<BookItem> = emptyList(),
        val availableGenres: List<String> = emptyList(),
        val availableAuthors: List<String> = emptyList(),
        val exportedData: String? = null
    )

    enum class StatsTab {
        OVERVIEW,
        ANALYTICS,
        UPCOMING,
        RECOMMENDATIONS,
        SEARCH,
        FILTERS
    }

    init {
        loadAllData()
    }

    private fun loadAllData() {
        screenModelScope.launch {
            try {
                updateState { it.copy(isLoading = true, error = null) }

                // Load library insights
                getLibraryInsights.asFlow().collect { insights ->
                    updateState { it.copy(libraryInsights = insights) }
                }

                // Load reading statistics
                getReadingStatistics().collect { stats ->
                    updateState { it.copy(readingStatisticsType1 = stats) }
                }

                // Load reading analytics
                getReadingAnalytics.asFlow().collect { analytics ->
                    updateState { it.copy(readingAnalytics = analytics) }
                }

                // Load upcoming releases
                getUpcomingReleases.asFlow().collect { releases ->
                    updateState { it.copy(upcomingReleases = releases) }
                }

                // Load recommendations
                val recommendations = getRecommendations(20)
                updateState { it.copy(recommendations = recommendations) }

                // Load available genres and authors for filtering
                val genres = applyAdvancedFilters.getAvailableGenres()
                val authors = applyAdvancedFilters.getAvailableAuthors()
                updateState { 
                    it.copy(
                        availableGenres = genres,
                        availableAuthors = authors,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.error { "Failed to load statistics: ${e.message}" }
                updateState { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun selectTab(tab: StatsTab) {
        updateState { it.copy(selectedTab = tab) }
    }

    fun performGlobalSearch(query: String, sources: List<Long> = emptyList()) {
        screenModelScope.launch {
            try {
                updateState { 
                    it.copy(
                        isSearching = true,
                        searchQuery = query,
                        error = null
                    )
                }

                globalSearch.asFlow(query, sources).collect { result ->
                    updateState { 
                        it.copy(
                            searchResults = result,
                            isSearching = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.error { "Failed to perform global search: ${e.message}" }
                updateState { 
                    it.copy(
                        isSearching = false,
                        error = e.message ?: "Search failed"
                    )
                }
            }
        }
    }

    fun updateFilterState(filterState: AdvancedFilterState) {
        screenModelScope.launch {
            try {
                updateState { it.copy(filterState = filterState) }

                applyAdvancedFilters.asFlow(filterState).collect { books ->
                    updateState { it.copy(filteredBooks = books) }
                }
            } catch (e: Exception) {
                Log.error { "Failed to apply filters: ${e.message}" }
                updateState { 
                    it.copy(error = e.message ?: "Filter failed")
                }
            }
        }
    }

    fun saveFilterPreset(name: String) {
        screenModelScope.launch {
            try {
                applyAdvancedFilters.savePreset(name, state.value.filterState)
                Log.info { "Filter preset saved: $name" }
            } catch (e: Exception) {
                Log.error { "Failed to save filter preset: ${e.message}" }
                updateState { 
                    it.copy(error = e.message ?: "Failed to save preset")
                }
            }
        }
    }

    fun exportStatisticsToJson() {
        screenModelScope.launch {
            try {
                val json = exportStatistics.toJson()
                updateState { it.copy(exportedData = json) }
                Log.info { "Statistics exported successfully" }
            } catch (e: Exception) {
                Log.error { "Failed to export statistics: ${e.message}" }
                updateState { 
                    it.copy(error = e.message ?: "Export failed")
                }
            }
        }
    }

    fun refresh() {
        loadAllData()
    }

    fun clearError() {
        updateState { it.copy(error = null) }
    }
}
