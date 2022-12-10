package ireader.domain.usecases.preferences.reader_preferences

import ireader.domain.models.library.LibrarySort
import ireader.domain.models.library.deserialize
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.models.prefs.IReaderVoice
import org.koin.core.annotation.Factory

class TextAlignmentUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(textAlign: PreferenceValues.PreferenceTextAlignment) {
        prefs.textAlign().set(textAlign)
    }

    suspend fun read(): PreferenceValues.PreferenceTextAlignment {
        return prefs.textAlign().get()
    }
}

class SortersUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(value: String) {
        appPreferences.sortLibraryScreen().set(value)
    }

    suspend fun read(): LibrarySort {
        return LibrarySort.deserialize(appPreferences.sortLibraryScreen().read())
    }
}

class SortersDescUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(value: Boolean) {
        appPreferences.sortDescLibraryScreen().set(value)
    }

    suspend fun read(): Boolean {
        return appPreferences.sortDescLibraryScreen().read()
    }
}
@Factory
class TextReaderPrefUseCase(
    private val prefs: ReaderPreferences,
) {
    fun savePitch(value: Float) {
        prefs.speechPitch().set(value)
    }

    suspend fun readPitch(): Float {
        return prefs.speechPitch().get()
    }

    fun saveRate(value: Float) {
        prefs.speechRate().set(value)
    }

    suspend fun readRate(): Float {
        return prefs.speechRate().get()
    }

    fun saveLanguage(value: String) {
        prefs.speechLanguage().set(value)
    }

    suspend fun readLanguage(): String {
        return prefs.speechLanguage().get()
    }

    fun saveVoice(value: IReaderVoice) {
        kotlin.runCatching {
            prefs.speechVoice().set(value)
        }
    }

    fun readVoice(): IReaderVoice? {
        /**
         * because the default value is "" which means the first value should return null
         */
        return kotlin.runCatching {
            return prefs.speechVoice().get()
        }.getOrNull()
    }

    fun saveAutoNext(value: Boolean) {
        prefs.readerAutoNext().set(value)
    }

    suspend fun readAutoNext(): Boolean {
        return prefs.readerAutoNext().get()
    }
}
