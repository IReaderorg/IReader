package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.core.api.http.HttpClients
import ireader.core.ui.preferences.ReaderPreferences
import org.koin.core.annotation.Factory

@Factory
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