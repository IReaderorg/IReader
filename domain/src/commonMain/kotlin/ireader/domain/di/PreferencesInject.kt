package ireader.domain.di


import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.BrowsePreferences
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.preferences.prefs.TranslationPreferences
import ireader.domain.preferences.prefs.UiPreferences
import org.koin.dsl.module


val preferencesInjectModule = module {
    single<AppPreferences> { AppPreferences(get()) }
    single<UiPreferences> { UiPreferences(get()) }
    single<ireader.domain.preferences.prefs.SupabasePreferences> { ireader.domain.preferences.prefs.SupabasePreferences(get()) }
    single<LibraryPreferences> { LibraryPreferences(get()) }
    single<BrowsePreferences> { BrowsePreferences(get()) }
    single<ireader.domain.preferences.prefs.ReadingBuddyPreferences> { ireader.domain.preferences.prefs.ReadingBuddyPreferences(get()) }
    single<TranslationPreferences> { TranslationPreferences(get()) }
}
