package org.ireader.domain.use_cases.translate

import org.ireader.common_data.TranslateEngine
import org.ireader.core_api.http.HttpClients
import org.ireader.core_ui.preferences.ReaderPreferences
import javax.inject.Inject


class TranslationEnginesManager @Inject constructor(
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