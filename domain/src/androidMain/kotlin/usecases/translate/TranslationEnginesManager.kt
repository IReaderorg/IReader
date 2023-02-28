package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.core.http.HttpClients
import ireader.domain.preferences.prefs.ReaderPreferences



class TranslationEnginesManager(
    private val readerPreferences: ReaderPreferences,
    private val httpClients: HttpClients,
) {

    private val availableEngine = listOf(
        GoogleTranslateML(),
        TranslateDictUseCase(httpClients)
    )

    fun get(): TranslateEngine {
        val engine = readerPreferences.translatorEngine().get()
        return availableEngine.find { it.id  == engine}!!
    }

}