package ir.kazemcodes.infinity.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import ir.kazemcodes.infinity.data.network.models.Dns
import ir.kazemcodes.infinity.domain.repository.DataStoreHelper
import ir.kazemcodes.infinity.presentation.layouts.layouts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DataStoreHelperImpl.PreferenceKeys.TEMP_SAVED_SETTING)

class DataStoreHelperImpl(context: Context) : DataStoreHelper {
    companion object PreferenceKeys {
        const val TEMP_SAVED_SETTING = "TEMP_SAVED_SETTING"
        const val SAVED_FONT_SIZE_PREFERENCES = "SAVED_FONT_SIZE_PREFERENCES"
        const val SAVED_FONT_PREFERENCES = "SAVED_FONT_PREFERENCES"
        const val SAVED_BRIGHTNESS_PREFERENCES = "SAVED_BRIGHTNESS_PREFERENCES"
        const val SAVED_LATEST_CHAPTER_KEY = "SAVED_LATEST_CHAPTER_KEY"

        const val SAVED_LIBRARY_LAYOUT_KEY = "SAVED_LIBRARY_LAYOUT_KEY"
        const val SAVED_BROWSE_LAYOUT_KEY = "SAVED_BROWSE_LAYOUT_KEY"

        /** Setting Pref**/
        const val SAVED_DOH_KEY = "SAVED_DOH_KEY"


    }

    private object PreferencesKey {
        val fontStateKey = intPreferencesKey(SAVED_FONT_PREFERENCES)
        val fontSizeStateKey = intPreferencesKey(SAVED_FONT_SIZE_PREFERENCES)
        val brightnessStateKey = floatPreferencesKey(SAVED_BRIGHTNESS_PREFERENCES)
        val latestChapterStateKey = stringPreferencesKey(SAVED_LATEST_CHAPTER_KEY)
        val libraryLayoutTypeStateKey = intPreferencesKey(SAVED_LATEST_CHAPTER_KEY)
        val browseLayoutTypeStateKey = intPreferencesKey(SAVED_BROWSE_LAYOUT_KEY)
        val dohStateKey = intPreferencesKey(SAVED_DOH_KEY)
    }

    private val dataStore = context.dataStore

    /**
     * save the index of font according to position of font in fonts list.
     */
    override suspend fun saveSelectedFontState(fontIndex: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.fontStateKey] = fontIndex
        }
    }

    /**
     * return a index of font that is in fonts list in type package.
     */
    override fun readSelectedFontState(): Flow<Int> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKey.fontStateKey] ?: 0
            }
    }

    override suspend fun saveFontSizeState(fontSize: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.fontSizeStateKey] = fontSize
        }
    }

    override fun readFontSizeState(): Flow<Int> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKey.fontSizeStateKey] ?: 18
            }
    }

    override suspend fun saveBrightnessState(brightness: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.brightnessStateKey] = brightness
        }
    }

    override fun readBrightnessState(): Flow<Float> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKey.brightnessStateKey] ?: .8f
            }
    }

    override suspend fun saveLatestChapterUseCase(listOfLatestReadBook: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.latestChapterStateKey] = listOfLatestReadBook
        }
    }

    override fun readLatestChapterUseCase(): Flow<String> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKey.latestChapterStateKey] ?: "[]"
            }
    }

    override fun readLibraryLayoutTypeStateUseCase(): Flow<Int> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKey.libraryLayoutTypeStateKey] ?: layouts.first().layoutIndex
            }
    }

    override suspend fun saveLibraryLayoutTypeStateUseCase(layoutIndex: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.libraryLayoutTypeStateKey] = layoutIndex
        }
    }

    override fun readBrowseLayoutTypeStateUseCase(): Flow<Int> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKey.browseLayoutTypeStateKey] ?: layouts.first().layoutIndex
            }
    }

    override suspend fun saveBrowseLayoutTypeStateUseCase(layoutIndex: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.browseLayoutTypeStateKey] = layoutIndex
        }
    }



    override fun readDohPrefUseCase() : Flow<Int> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKey.dohStateKey] ?: Dns.Disable.prefCode
            }
    }

    override suspend fun saveDohPrefUseCase(dohPref : Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.dohStateKey] = dohPref
        }
    }

}