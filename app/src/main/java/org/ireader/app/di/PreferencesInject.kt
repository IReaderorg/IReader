package org.ireader.app.di

import androidx.compose.ui.text.ExperimentalTextApi
import ireader.core.api.prefs.PreferenceStore
import ireader.core.ui.preferences.AppPreferences
import ireader.core.ui.preferences.LibraryPreferences
import ireader.core.ui.preferences.UiPreferences
import org.koin.core.annotation.ComponentScan
import ireader.core.api.di.ISingleton
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
