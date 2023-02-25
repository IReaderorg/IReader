package org.ireader.app.di

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.AndroidUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import org.ireader.app.R
import org.ireader.app.initiators.AppInitializers
import org.ireader.app.initiators.CatalogStoreInitializer
import org.ireader.app.initiators.CrashHandler
import org.ireader.app.initiators.EmojiCompatInitializer
import org.ireader.app.initiators.FirebaseInitializer
import org.ireader.app.initiators.NotificationsInitializer
import org.ireader.app.initiators.UpdateServiceInitializer

import org.koin.dsl.binds
import org.koin.dsl.module






@OptIn(ExperimentalTextApi::class) val AppModule = module {
    single {
        ReaderPreferences(
            get(),
        )
    }
    single {
        GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
    }
    single { AppInitializers(EmojiCompatInitializer(get()),
        NotificationsInitializer(get()), CrashHandler(get()), FirebaseInitializer(get()),
        UpdateServiceInitializer(get(),get()), CatalogStoreInitializer(get())
    ) }
    single {
        AndroidUiPreferences(
            preferenceStore = get(),
            provider = get()
        )
    }
    factory(qualifier=null) { org.ireader.app.initiators.CatalogStoreInitializer(get()) }
    factory(qualifier=null) { org.ireader.app.initiators.CrashHandler(get()) }
    factory(qualifier=null) { org.ireader.app.initiators.EmojiCompatInitializer(get()) }
    factory(qualifier=null) { org.ireader.app.initiators.FirebaseInitializer(get()) }
    factory(qualifier=null) { org.ireader.app.initiators.NotificationsInitializer(get()) }
    factory(qualifier=null) { org.ireader.app.initiators.SecureActivityDelegateImpl() } binds(arrayOf(org.ireader.app.SecureActivityDelegate::class,androidx.lifecycle.DefaultLifecycleObserver::class))
    factory(qualifier=null) { org.ireader.app.initiators.UpdateServiceInitializer(get(),get()) }


}