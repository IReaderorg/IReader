package ireader.core.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of PreferenceStoreFactory using DataStore.
 * 
 * DataStore files are stored in the app's Documents directory.
 */
actual class PreferenceStoreFactory {
    
    private val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val documentsPath: String by lazy {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        )
        (paths.firstOrNull() as? platform.Foundation.NSURL)?.path ?: ""
    }
    
    actual fun create(vararg names: String): PreferenceStore {
        val preferenceName = names.joinToString(separator = "_")
        
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            scope = dataStoreScope,
            produceFile = {
                "$documentsPath/datastore/$preferenceName.preferences_pb".toPath()
            }
        )
        
        return DataStorePreferenceStore(dataStore)
    }
}
