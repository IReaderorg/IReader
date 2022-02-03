package ir.kazemcodes.infinity.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.kazemcodes.infinity.feature_activity.presentation.ThemeSetting
import ir.kazemcodes.infinity.feature_activity.presentation.ThemeSettingPreference
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingModule {

    @Binds
    @Singleton
    abstract fun bindThemeSetting(
        themeSettingPreference: ThemeSettingPreference
    ): ThemeSetting
}