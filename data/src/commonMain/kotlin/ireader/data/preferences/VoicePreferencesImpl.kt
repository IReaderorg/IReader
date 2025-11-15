package ireader.data.preferences

import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.VoicePreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of VoicePreferences using PreferenceStore
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
class VoicePreferencesImpl(
    private val preferenceStore: PreferenceStore
) : VoicePreferences {
    
    private val piperVoiceKey = "tts_piper_voice_id"
    private val kokoroVoiceKey = "tts_kokoro_voice_id"
    private val mayaLanguageKey = "tts_maya_language"
    private val installedVoicesKey = "tts_installed_voices"
    
    override fun getSelectedVoiceId(engine: String): String? {
        return when (engine.uppercase()) {
            "PIPER" -> preferenceStore.getString(piperVoiceKey).get().takeIf { it.isNotEmpty() }
            "KOKORO" -> preferenceStore.getString(kokoroVoiceKey).get().takeIf { it.isNotEmpty() }
            "MAYA" -> null // Maya uses language, not voice ID
            "SIMULATION" -> null
            else -> null
        }
    }
    
    override fun setSelectedVoiceId(engine: String, voiceId: String) {
        when (engine.uppercase()) {
            "PIPER" -> preferenceStore.getString(piperVoiceKey).set(voiceId)
            "KOKORO" -> preferenceStore.getString(kokoroVoiceKey).set(voiceId)
            else -> {} // No-op for other engines
        }
    }
    
    override fun getSelectedLanguage(engine: String): String? {
        return when (engine.uppercase()) {
            "MAYA" -> preferenceStore.getString(mayaLanguageKey, "en").get()
            else -> null
        }
    }
    
    override fun setSelectedLanguage(engine: String, language: String) {
        when (engine.uppercase()) {
            "MAYA" -> preferenceStore.getString(mayaLanguageKey).set(language)
            else -> {} // No-op for other engines
        }
    }
    
    override fun getSelectedKokoroVoice(): String? {
        return preferenceStore.getString(kokoroVoiceKey).get().takeIf { it.isNotEmpty() }
    }
    
    override fun setSelectedKokoroVoice(voiceId: String) {
        preferenceStore.getString(kokoroVoiceKey).set(voiceId)
    }
    
    override fun getSelectedMayaLanguage(): String? {
        return preferenceStore.getString(mayaLanguageKey, "en").get()
    }
    
    override fun setSelectedMayaLanguage(language: String) {
        preferenceStore.getString(mayaLanguageKey).set(language)
    }
    
    override fun getInstalledVoices(): Set<String> {
        val json = preferenceStore.getString(installedVoicesKey, "[]").get()
        return try {
            Json.decodeFromString<Set<String>>(json)
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    override fun setInstalledVoices(voices: Set<String>) {
        val json = Json.encodeToString(voices)
        preferenceStore.getString(installedVoicesKey).set(json)
    }
}
