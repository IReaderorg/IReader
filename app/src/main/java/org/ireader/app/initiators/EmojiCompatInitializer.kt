package org.ireader.app.initiators

import android.app.Application
import androidx.core.provider.FontRequest
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import org.ireader.app.R
import org.koin.core.annotation.Factory

@Factory
class EmojiCompatInitializer  constructor(context: Application) {

    init {
        // Note: if play services are not available, emoji fonts won't be downloaded
        val config = DefaultEmojiCompatConfig.create(context) ?: FontRequestEmojiCompatConfig(
            context,
            FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs
            )
        )
        EmojiCompat.init(config.setReplaceAll(true))
    }
}
