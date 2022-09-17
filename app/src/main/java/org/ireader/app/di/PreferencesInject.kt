package org.ireader.app.di

import androidx.compose.ui.text.ExperimentalTextApi
import ireader.core.api.prefs.PreferenceStore
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.preferences.prefs.UiPreferences
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Single

@org.koin.core.annotation.Module
@ComponentScan("org.ireader.app.di.PreferencesInject")
class PreferencesInject {

    @Single
    fun provideAppPreferences(
        preferenceStore: PreferenceStore,
    ): AppPreferences {
        return AppPreferences(
            preferenceStore
        )
    }

    @Single
    fun provideUiPreferences(
        preferenceStore: PreferenceStore,
    ): UiPreferences {
        return UiPreferences(
            preferenceStore
        )
    }

    @OptIn(ExperimentalTextApi::class)

//    @Singleton
//    fun provideReaderPreferences(
//        preferenceStore: PreferenceStore,
//        provider: GoogleFont.Provider
//    ): ReaderPreferences {
//        return ReaderPreferences(
//            preferenceStore,
//            provider
//        )
//    }

    @Single
    fun provideLibraryPreferences(
        preferenceStore: PreferenceStore,
    ): LibraryPreferences {
        return LibraryPreferences(preferenceStore)
    }
}
