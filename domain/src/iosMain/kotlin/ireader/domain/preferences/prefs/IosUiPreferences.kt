package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.common.FontFamilyModel
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.models.getDefaultFont
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class IosUiPreferences(
    private val preferenceStore: PreferenceStore,
) : PlatformUiPreferences {

    override fun font(): Preference<FontType> {
        return preferenceStore.getString(
            SAVED_FONT_PREFERENCES,
            getDefaultFont().name
        ).asFont()
    }

    private companion object {
        const val SAVED_FONT_PREFERENCES = "reader_font_family"
    }
}

private fun Preference<String>.asFont(): Preference<FontType> {
    return object : Preference<FontType> {
        override fun key(): String = this@asFont.key()

        override fun get(): FontType = toFont(this@asFont.get())

        override fun set(value: FontType) {
            this@asFont.set(value.name)
        }

        override fun isSet(): Boolean = this@asFont.isSet()

        override fun delete() {
            this@asFont.delete()
        }

        override fun defaultValue(): FontType = getDefaultFont()

        override fun changes() = this@asFont.changes().map(::toFont)

        override fun stateIn(scope: CoroutineScope): StateFlow<FontType> {
            return changes().stateIn(scope, SharingStarted.Eagerly, get())
        }
    }
}

private fun toFont(fontName: String): FontType {
    return FontType(
        name = fontName,
        fontFamily = if (fontName == "Default" || fontName.isEmpty()) {
            FontFamilyModel.Default
        } else {
            FontFamilyModel.Custom(fontName)
        }
    )
}
