package org.ireader.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.ireader.app.R

/**
 * Library widget provider for home screen integration
 */
class LibraryWidget : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all widgets
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }
    
    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
    
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // Delete widget configurations
        for (appWidgetId in appWidgetIds) {
            LibraryWidgetConfigManager.deleteWidgetConfig(context, appWidgetId)
        }
    }
    
    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val config = LibraryWidgetConfigManager.loadWidgetConfig(context, appWidgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_library)
            
            // Set up the widget based on configuration
            // This is a simplified implementation
            // In a real implementation, you would:
            // 1. Load widget data from repository
            // 2. Populate RemoteViews with data
            // 3. Set up click handlers
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, LibraryWidget::class.java)
            )
            
            val intent = Intent(context, LibraryWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }
}

/**
 * Widget configuration manager
 */
object LibraryWidgetConfigManager {
    
    private const val PREFS_NAME = "library_widget_prefs"
    private const val PREF_PREFIX_KEY = "widget_"
    
    fun saveWidgetConfig(context: Context, appWidgetId: Int, config: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_PREFIX_KEY + appWidgetId, config).apply()
    }
    
    fun loadWidgetConfig(context: Context, appWidgetId: Int): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    }
    
    fun deleteWidgetConfig(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(PREF_PREFIX_KEY + appWidgetId).apply()
    }
}
