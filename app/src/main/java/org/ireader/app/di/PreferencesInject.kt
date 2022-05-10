package org.ireader.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.core_api.prefs.PreferenceStore
import org.ireader.core_ui.preferences.AppPreferences
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.preferences.UiPreferences
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class PreferencesInject {


    @Provides
    @Singleton
    fun provideAppPreferences(
        preferenceStore: PreferenceStore,
    ): AppPreferences {
        return AppPreferences(
            preferenceStore
        )
    }

    @Provides
    @Singleton
    fun provideUiPreferences(
        preferenceStore: PreferenceStore,
    ): UiPreferences {
        return UiPreferences(
            preferenceStore
        )
    }
    @Provides
    @Singleton
    fun provideReaderPreferences(
        preferenceStore: PreferenceStore,
    ): ReaderPreferences {
        return ReaderPreferences(
            preferenceStore
        )
    }

}