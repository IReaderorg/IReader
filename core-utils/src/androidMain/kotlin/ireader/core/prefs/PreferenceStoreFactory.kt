package ireader.core.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/**
 * Android implementation of PreferenceStoreFactory that creates DataStore-backed PreferenceStore instances.
 * 
 * This factory creates DataStore instances with automatic migration from SharedPreferences.
 * Each PreferenceStore is backed by a separate DataStore file in the app's private storage.
 * 
 * @param context Android context for accessing app storage and SharedPreferences
 */
actual class PreferenceStoreFactory(private val context: Context) {
    
    private val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Creates a PreferenceStore backed by DataStore with automatic migration from SharedPreferences.
     * 
     * The DataStore file is created in the app's files directory under a 'datastore' subdirectory.
     * If SharedPreferences exist with the same name, they will be automatically migrated on first access.
     * 
     * @param names Variable number of name components that will be joined with underscores
     * @return A DataStore-backed PreferenceStore instance
     */
    actual fun create(vararg names: String): PreferenceStore {
        val preferenceName = names.joinToString(separator = "_")
        
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(
                AndroidPreferencesMigration(
                    context = context,
                    preferenceName = preferenceName
                )
            ),
            scope = dataStoreScope,
            produceFile = {
                File(context.filesDir, "datastore/$preferenceName.preferences_pb")
            }
        )
        
        return DataStorePreferenceStore(dataStore)
    }
}