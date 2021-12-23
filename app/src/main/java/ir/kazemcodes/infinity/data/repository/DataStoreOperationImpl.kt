package ir.kazemcodes.infinity.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import ir.kazemcodes.infinity.domain.repository.DataStoreOperations
import ir.kazemcodes.infinity.presentation.book_detail.PreferenceKeys
import ir.kazemcodes.infinity.presentation.book_detail.PreferenceKeys.SAVED_BRIGHTNESS_PREFERENCES
import ir.kazemcodes.infinity.presentation.book_detail.PreferenceKeys.SAVED_FONT_PREFERENCES
import ir.kazemcodes.infinity.presentation.book_detail.PreferenceKeys.SAVED_FONT_SIZE_PREFERENCES
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PreferenceKeys.TEMP_SAVED_SETTING)

class DataStoreOperationsImpl(context: Context) : DataStoreOperations {

    private object PreferencesKey {
        val fontStateKey = intPreferencesKey(SAVED_FONT_PREFERENCES)
        val fontSizeStateKey = intPreferencesKey(SAVED_FONT_SIZE_PREFERENCES)
        val brightnessStateKey = floatPreferencesKey(SAVED_BRIGHTNESS_PREFERENCES)
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

}