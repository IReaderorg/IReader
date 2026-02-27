package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.TranslationPreferences

/**
 * Use case to check if Google ML translation engine requires initialization
 * for auto-translate features (novel names/descriptions).
 * 
 * Auto-translate for names/descriptions ALWAYS uses Google ML (offline, free)
 * regardless of the user's selected engine for content translation.
 */
class CheckTranslationEngineInitializationUseCase(
    private val translationEnginesManager: TranslationEnginesManager,
    private val translationPreferences: TranslationPreferences,
    private val readerPreferences: ReaderPreferences
) {
    
    /**
     * Get Google ML engine specifically for metadata translation
     * Auto-translate always uses Google ML (id=0)
     */
    private fun getGoogleMLEngine(): TranslateEngine {
        return translationEnginesManager.builtInEngines.first { it.id == 0L }
    }
    
    /**
     * Check if Google ML requires initialization
     * This is always checked for auto-translate features
     */
    fun requiresInitialization(): Boolean {
        val googleML = getGoogleMLEngine()
        return googleML.requiresInitialization
    }
    
    /**
     * Check if auto-translate is enabled for names or descriptions
     */
    fun isAutoTranslateEnabled(): Boolean {
        return translationPreferences.autoTranslateNovelNames().get() ||
               translationPreferences.autoTranslateDescriptions().get()
    }
    
    /**
     * Get the engine name (always Google ML for auto-translate)
     */
    fun getCurrentEngineName(): String {
        return getGoogleMLEngine().engineName
    }
    
    /**
     * Get source and target languages for initialization
     */
    fun getLanguages(): Pair<String, String> {
        val source = readerPreferences.translatorOriginLanguage().get()
        val target = readerPreferences.translatorTargetLanguage().get()
        return source to target
    }
    
    /**
     * Check if we should prompt for initialization
     * Returns true if:
     * 1. Auto-translate is enabled
     * 2. Google ML requires initialization
     */
    fun shouldPromptInitialization(): Boolean {
        return isAutoTranslateEnabled() && requiresInitialization()
    }
}
