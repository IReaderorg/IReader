package ireader.domain.usecases.preferences

import ireader.domain.preferences.models.prefs.IReaderVoice
import ireader.domain.preferences.prefs.AndroidUiPreferences
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences

class TextReaderPrefUseCase(
        private val prefs: ReaderPreferences,
        private val AndroidPrefs: AppPreferences,
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
            AndroidPrefs.speechVoice().set(value)
        }
    }

    fun readVoice(): IReaderVoice? {
        /**
         * because the default value is "" which means the first value should return null
         */
        return kotlin.runCatching {
            return AndroidPrefs.speechVoice().get()
        }.getOrNull()
    }

    fun saveAutoNext(value: Boolean) {
        prefs.readerAutoNext().set(value)
    }

    suspend fun readAutoNext(): Boolean {
        return prefs.readerAutoNext().get()
    }
}
