package org.ireader.app.di

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import ireader.domain.preferences.prefs.AndroidUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.LocalizeHelper
import org.ireader.app.R
import org.ireader.app.initiators.*
import org.kodein.di.*


@OptIn(ExperimentalTextApi::class) val AppModule = DI.Module("appModule") {
    bindSingleton {
        LocalizeHelper(
            instance(),
        )
    }
    bindSingleton {
        ReaderPreferences(
            instance(),
        )
    }
    bindSingleton {
        GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
    }
    bindSingleton { AppInitializers(EmojiCompatInitializer(instance()),
        NotificationsInitializer(instance(),instance()), CrashHandler(instance()), FirebaseInitializer(instance()),
        UpdateServiceInitializer(instance(),instance()), instance())
    }
    bindSingleton {
        AndroidUiPreferences(
            preferenceStore = instance(),
            provider = instance()
        )
    }
    bindProvider { new(::CatalogStoreInitializer) }
    bindProvider { org.ireader.app.initiators.CrashHandler(instance()) }
    bindProvider { org.ireader.app.initiators.EmojiCompatInitializer(instance()) }
    bindProvider { org.ireader.app.initiators.FirebaseInitializer(instance()) }
    bindProvider { org.ireader.app.initiators.NotificationsInitializer(instance(),instance()) }
    bindProvider { org.ireader.app.initiators.SecureActivityDelegateImpl() }
    bindProvider { org.ireader.app.initiators.UpdateServiceInitializer(instance(),instance()) }



}