package ireader.domain.data.repository

import ireader.domain.models.library.LibraryWidgetConfig
import ireader.domain.models.library.LibraryWidgetData
import ireader.domain.models.library.WidgetStatistics
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing library widgets
 */
interface LibraryWidgetRepository {
    
    // Widget configuration
    suspend fun getWidgetConfig(widgetId: Int): LibraryWidgetConfig?
    suspend fun saveWidgetConfig(config: LibraryWidgetConfig): Boolean
    suspend fun deleteWidgetConfig(widgetId: Int): Boolean
    suspend fun getAllWidgetConfigs(): List<LibraryWidgetConfig>
    
    // Widget data
    suspend fun getWidgetData(widgetId: Int): LibraryWidgetData?
    fun getWidgetDataAsFlow(widgetId: Int): Flow<LibraryWidgetData?>
    suspend fun updateWidgetData(widgetId: Int): Boolean
    suspend fun updateAllWidgets(): Boolean
    
    // Widget statistics
    suspend fun getWidgetStatistics(): WidgetStatistics
    fun getWidgetStatisticsAsFlow(): Flow<WidgetStatistics>
    
    // Widget refresh
    suspend fun refreshWidget(widgetId: Int): Boolean
    suspend fun scheduleWidgetRefresh(widgetId: Int, intervalMillis: Long): Boolean
    suspend fun cancelWidgetRefresh(widgetId: Int): Boolean
}
