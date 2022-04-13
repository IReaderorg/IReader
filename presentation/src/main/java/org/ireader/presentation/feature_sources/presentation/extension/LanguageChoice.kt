package org.ireader.presentation.feature_sources.presentation.extension

import androidx.annotation.Keep

sealed class LanguageChoice {
    object All : LanguageChoice()

    @Keep
    data class One(val language: Language) : LanguageChoice()
    @Keep
    data class Others(val languages: List<Language>) : LanguageChoice()
}
