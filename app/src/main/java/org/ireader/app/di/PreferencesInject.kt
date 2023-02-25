package org.ireader.app.di

import androidx.compose.ui.text.ExperimentalTextApi
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.preferences.prefs.UiPreferences


import org.koin.dsl.module


val preferencesInjectModule = module {
    single<AppPreferences>(qualifier=null) { AppPreferences(get()) }
    single<UiPreferences>(qualifier=null) { UiPreferences(get()) }
    single<LibraryPreferences>(qualifier=null) { LibraryPreferences(get()) }
}
