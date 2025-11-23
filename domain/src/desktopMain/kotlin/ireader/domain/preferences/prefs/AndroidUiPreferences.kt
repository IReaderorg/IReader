package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.models.FontType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DesktopUiPreferences constructor(
    private val preferenceStore: PreferenceStore,
) : PlatformUiPreferences {

    companion object PreferenceKeys {
        const val SAVED_FONT_PREFERENCES = "reader_font_family"
    }

    override fun font(): Preference<FontType> {
        return preferenceStore.getString(
            SAVED_FONT_PREFERENCES, 
            ireader.domain.preferences.models.getDefaultFont().name
        ).asDesktopFont()
    }
}

/**
 * Convert string preference to FontType for desktop
 */
private fun Preference<String>.asDesktopFont(): Preference<FontType> {
    return object : Preference<FontType> {
        override fun key(): String = this@asDesktopFont.key()
        
        override fun get(): FontType {
            val fontName = this@asDesktopFont.get()
            return ireader.domain.preferences.models.FontType(
                name = fontName,
                fontFamily = if (fontName == "Default" || fontName.isEmpty()) {
                    ireader.domain.models.common.FontFamilyModel.Default
                } else {
                    ireader.domain.models.common.FontFamilyModel.Custom(fontName)
                }
            )
        }
        
        override fun set(value: FontType) {
            this@asDesktopFont.set(value.name)
        }
        
        override fun isSet(): Boolean = this@asDesktopFont.isSet()
        
        override fun delete() {
            this@asDesktopFont.delete()
        }
        
        override fun defaultValue(): FontType {
            return ireader.domain.preferences.models.getDefaultFont()
        }
        
        override fun changes(): kotlinx.coroutines.flow.Flow<FontType> {
            return this@asDesktopFont.changes().map { fontName: String ->
                convertToFontType(fontName)
            }
        }
        
        override fun stateIn(scope: CoroutineScope): StateFlow<FontType> {
            return this@asDesktopFont.changes()
                .map { fontName: String -> convertToFontType(fontName) }
                .stateIn(scope, SharingStarted.Eagerly, get())
        }
        
        private fun convertToFontType(fontName: String): FontType {
            return ireader.domain.preferences.models.FontType(
                name = fontName,
                fontFamily = if (fontName == "Default" || fontName.isEmpty()) {
                    ireader.domain.models.common.FontFamilyModel.Default
                } else {
                    ireader.domain.models.common.FontFamilyModel.Custom(fontName)
                }
            )
        }
    }
}