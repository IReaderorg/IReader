package org.ireader.app.di

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import ireader.core.ui.preferences.ReaderPreferences
import org.ireader.app.R
import ireader.core.api.prefs.PreferenceStore
import org.ireader.app.initiators.AppInitializers
import org.ireader.app.initiators.CatalogStoreInitializer
import org.ireader.app.initiators.CrashHandler
import org.ireader.app.initiators.EmojiCompatInitializer
import org.ireader.app.initiators.FirebaseInitializer
import org.ireader.app.initiators.NotificationsInitializer
import org.ireader.app.initiators.UpdateServiceInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

@OptIn(ExperimentalTextApi::class) val AppModule = module {
    single {
        ReaderPreferences(
            get(),
            get()
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
}