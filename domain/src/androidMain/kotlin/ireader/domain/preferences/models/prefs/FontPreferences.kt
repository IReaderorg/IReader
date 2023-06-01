package ireader.domain.preferences.models.prefs

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import ireader.core.prefs.Preference
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.models.getDefaultFont
import ireader.i18n.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FontPreferences @OptIn(ExperimentalTextApi::class) constructor(
    private val preference: Preference<String>,
    private val provider: GoogleFont.Provider
) : Preference<FontType> {

    override fun key(): String {
        return preference.key()
    }
    override fun get(): FontType {
        return if (isSet()) {
            getFont()
        } else {
            getDefaultFont()
        }
    }


    private val localFontFamily = listOf<FontType>(
        FontType("Cooper Arabic" ,FontFamily(androidx.compose.ui.text.font.Font(R.font.cooper_arabic, weight = FontWeight.Normal)))
    )

    @OptIn(ExperimentalTextApi::class)
    fun getFont(): FontType {
        val fontName = preference.get()
        localFontFamily.find { it.name == fontName }?.let { font ->
            return font
        }
        return kotlin.runCatching {
            val fontFamily = androidx.compose.ui.text.font.FontFamily(
                Font(
                    googleFont = GoogleFont(fontName),
                    provider
                )
            )
            FontType(
                fontName,
                fontFamily
            )
        }.getOrElse {
            FontType("Roboto", FontFamily.Default)
        }
    }

    override fun set(value: FontType) {
        if (value != getDefaultFont()) {
            preference.set(value.name)
        } else {
            preference.delete()
        }
    }

    override fun isSet(): Boolean {
        return preference.isSet()
    }

    override fun delete() {
        preference.delete()
    }

    override fun defaultValue(): FontType {
        return getDefaultFont()
    }

    override fun changes(): Flow<FontType> {
        return preference.changes()
            .map { get() }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<FontType> {
        return preference.changes().map { get() }.stateIn(scope, SharingStarted.Eagerly, get())
    }
}

@OptIn(ExperimentalTextApi::class)
fun Preference<String>.asFont(provider: GoogleFont.Provider): FontPreferences {
    return FontPreferences(this, provider)
}
