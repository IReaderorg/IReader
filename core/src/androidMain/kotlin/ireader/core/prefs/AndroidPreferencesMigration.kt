package ireader.core.prefs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * Migrates SharedPreferences data to DataStore on Android.
 * 
 * This migration runs once when the app first uses DataStore. It reads all existing
 * SharedPreferences and copies them to DataStore, preserving all preference types.
 * 
 * @param context Android context for accessing SharedPreferences
 * @param preferenceName Name of the SharedPreferences file to migrate from
 */
class AndroidPreferencesMigration(
    private val context: Context,
    private val preferenceName: String
) : DataMigration<Preferences> {

    companion object {
        private const val TAG = "AndroidPrefsMigration"
        private const val MIGRATION_COMPLETED_KEY = "migration_completed"
    }

    /**
     * Checks if migration should run by looking for the migration_completed flag.
     * 
     * @param currentData Current DataStore preferences
     * @return true if migration has not been completed yet
     */
    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        val shouldMigrate = !currentData.contains(stringPreferencesKey(MIGRATION_COMPLETED_KEY))
        if (shouldMigrate) {
            Log.i(TAG, "Migration needed for preferences: $preferenceName")
        } else {
            Log.d(TAG, "Migration already completed for preferences: $preferenceName")
        }
        return shouldMigrate
    }

    /**
     * Migrates all SharedPreferences data to DataStore.
     * 
     * Reads all key-value pairs from SharedPreferences and copies them to DataStore,
     * handling all supported preference types. Errors for individual keys are logged
     * but don't stop the migration process.
     * 
     * @param currentData Current DataStore preferences (should be empty on first run)
     * @return Updated preferences with migrated data and migration_completed flag
     */
    override suspend fun migrate(currentData: Preferences): Preferences {
        Log.i(TAG, "Starting migration for preferences: $preferenceName")
        
        val sharedPrefs: SharedPreferences = try {
            context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to access SharedPreferences: $preferenceName", e)
            // Mark migration as complete even if we can't access old prefs
            return currentData.toMutablePreferences().apply {
                this[stringPreferencesKey(MIGRATION_COMPLETED_KEY)] = "true"
            }.toPreferences()
        }

        val allPrefs = try {
            sharedPrefs.all
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read SharedPreferences data", e)
            return currentData.toMutablePreferences().apply {
                this[stringPreferencesKey(MIGRATION_COMPLETED_KEY)] = "true"
            }.toPreferences()
        }

        if (allPrefs.isEmpty()) {
            Log.i(TAG, "No preferences to migrate from: $preferenceName")
        } else {
            Log.i(TAG, "Migrating ${allPrefs.size} preferences from: $preferenceName")
        }

        val mutablePrefs = currentData.toMutablePreferences()
        var successCount = 0
        var errorCount = 0

        allPrefs.forEach { (key, value) ->
            try {
                when (value) {
                    is String -> {
                        mutablePrefs[stringPreferencesKey(key)] = value
                        Log.d(TAG, "Migrated String: $key")
                    }
                    is Int -> {
                        mutablePrefs[intPreferencesKey(key)] = value
                        Log.d(TAG, "Migrated Int: $key")
                    }
                    is Long -> {
                        mutablePrefs[longPreferencesKey(key)] = value
                        Log.d(TAG, "Migrated Long: $key")
                    }
                    is Float -> {
                        mutablePrefs[floatPreferencesKey(key)] = value
                        Log.d(TAG, "Migrated Float: $key")
                    }
                    is Boolean -> {
                        mutablePrefs[booleanPreferencesKey(key)] = value
                        Log.d(TAG, "Migrated Boolean: $key")
                    }
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val stringSet = value as? Set<String>
                        if (stringSet != null) {
                            mutablePrefs[stringSetPreferencesKey(key)] = stringSet
                            Log.d(TAG, "Migrated Set<String>: $key")
                        } else {
                            Log.w(TAG, "Skipping non-String Set: $key")
                            errorCount++
                        }
                    }
                    else -> {
                        Log.w(TAG, "Skipping unsupported type for key: $key (type: ${value?.javaClass?.simpleName})")
                        errorCount++
                    }
                }
                successCount++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to migrate preference: $key", e)
                errorCount++
            }
        }

        // Mark migration as complete
        mutablePrefs[stringPreferencesKey(MIGRATION_COMPLETED_KEY)] = "true"
        
        Log.i(TAG, "Migration completed for $preferenceName: $successCount successful, $errorCount errors")
        
        return mutablePrefs.toPreferences()
    }

    /**
     * Cleanup after migration. Currently does nothing to preserve old SharedPreferences
     * as a backup in case of issues.
     */
    override suspend fun cleanUp() {
        Log.d(TAG, "Migration cleanup completed for: $preferenceName")
        // Intentionally not deleting old SharedPreferences to keep as backup
        // Users can manually clear app data if they want to remove old files
    }
}
