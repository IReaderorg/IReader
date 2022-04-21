package org.ireader.presentation.feature_sources.presentation.extension

import androidx.annotation.Keep

sealed class LanguageChoice {
    object All : LanguageChoice()


    data class One(val language: Language) : LanguageChoice()

    data class Others(val languages: List<Language>) : LanguageChoice()
}
