package org.ireader.infinity.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.core.prefs.ThemeSetting
import org.ireader.core.prefs.ThemeSettingPreference
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingModule {

    @Binds
    @Singleton
    abstract fun bindThemeSetting(
        themeSettingPreference: ThemeSettingPreference,
    ): ThemeSetting
}