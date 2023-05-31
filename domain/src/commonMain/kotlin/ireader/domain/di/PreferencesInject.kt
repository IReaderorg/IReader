package ireader.domain.di


import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.preferences.prefs.UiPreferences
import org.koin.dsl.module


val preferencesInjectModule = module {
    single<AppPreferences> { AppPreferences(get()) }
    single<UiPreferences> { UiPreferences(get()) }
    single<LibraryPreferences> { LibraryPreferences(get()) }
}
