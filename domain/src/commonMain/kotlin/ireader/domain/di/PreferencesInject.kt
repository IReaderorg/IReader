package ireader.domain.di


import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.preferences.prefs.UiPreferences
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance


val preferencesInjectModule = DI.Module("preferencesModule") {
    bindSingleton<AppPreferences> { AppPreferences(instance()) }
    bindSingleton<UiPreferences> { UiPreferences(instance()) }
    bindSingleton<LibraryPreferences> { LibraryPreferences(instance()) }
}
