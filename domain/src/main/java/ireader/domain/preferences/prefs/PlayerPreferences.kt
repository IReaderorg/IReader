package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import org.koin.core.annotation.Single

@Single
class PlayerPreferences(private val preferenceStore: PreferenceStore) {

    fun preferredAudioTrackLanguage(): Preference<String> {
        return preferenceStore.getString("preferred_audio_language", "")
    }

    fun preferredQuality(): Preference<Int> {
        return preferenceStore.getInt("preferred_quality", Int.MAX_VALUE)
    }
    fun autoPlay(): Preference<Boolean> {
        return preferenceStore.getBoolean("autoplay_next_key",false)
    }

}