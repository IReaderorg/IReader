package ireader.domain.usecases.library

import ireader.domain.data.repository.LibraryWidgetRepository
import ireader.domain.models.library.LibraryWidgetConfig
import ireader.domain.models.library.LibraryWidgetData
import ireader.domain.models.library.WidgetStatistics
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing library widgets
 */
class LibraryWidgetUseCase(
    private val widgetRepository: LibraryWidgetRepository
) {
    
    /**
     * Get widget configuration
     */
    suspend fun getWidgetConfig(widgetId: Int): LibraryWidgetConfig? {
        return widgetRepository.getWidgetConfig(widgetId)
    }
    
    /**
     * Save widget configuration
     */
    suspend fun saveWidgetConfig(config: LibraryWidgetConfig): Boolean {
        return widgetRepository.saveWidgetConfig(config)
    }
    
    /**
     * Delete widget configuration
     */
    suspend fun deleteWidgetConfig(widgetId: Int): Boolean {
        return widgetRepository.deleteWidgetConfig(widgetId)
    }
    
    /**
     * Get all widget configurations
     */
    suspend fun getAllWidgetConfigs(): List<LibraryWidgetConfig> {
        return widgetRepository.getAllWidgetConfigs()
    }
    
    /**
     * Get widget data
     */
    suspend fun getWidgetData(widgetId: Int): LibraryWidgetData? {
        return widgetRepository.getWidgetData(widgetId)
    }
    
    /**
     * Get widget data as Flow
     */
    fun getWidgetDataAsFlow(widgetId: Int): Flow<LibraryWidgetData?> {
        return widgetRepository.getWidgetDataAsFlow(widgetId)
    }
    
    /**
     * Update widget data
     */
    suspend fun updateWidgetData(widgetId: Int): Boolean {
        return widgetRepository.updateWidgetData(widgetId)
    }
    
    /**
     * Update all widgets
     */
    suspend fun updateAllWidgets(): Boolean {
        return widgetRepository.updateAllWidgets()
    }
    
    /**
     * Get widget statistics
     */
    suspend fun getWidgetStatistics(): WidgetStatistics {
        return widgetRepository.getWidgetStatistics()
    }
    
    /**
     * Get widget statistics as Flow
     */
    fun getWidgetStatisticsAsFlow(): Flow<WidgetStatistics> {
        return widgetRepository.getWidgetStatisticsAsFlow()
    }
    
    /**
     * Refresh widget
     */
    suspend fun refreshWidget(widgetId: Int): Boolean {
        return widgetRepository.refreshWidget(widgetId)
    }
    
    /**
     * Schedule widget refresh
     */
    suspend fun scheduleWidgetRefresh(widgetId: Int, intervalMillis: Long): Boolean {
        return widgetRepository.scheduleWidgetRefresh(widgetId, intervalMillis)
    }
    
    /**
     * Cancel widget refresh
     */
    suspend fun cancelWidgetRefresh(widgetId: Int): Boolean {
        return widgetRepository.cancelWidgetRefresh(widgetId)
    }
}
