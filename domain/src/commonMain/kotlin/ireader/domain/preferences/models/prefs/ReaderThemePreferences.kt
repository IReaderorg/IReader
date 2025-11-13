package ireader.domain.preferences.models.prefs

import ireader.core.prefs.Preference
import ireader.domain.models.common.ColorModel
import ireader.domain.preferences.models.ReaderColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ReaderThemePreferences(
    private val preference: Preference<Long>
) : Preference<ReaderColors> {

    override fun key(): String {
        return preference.key()
    }

    override fun get(): ReaderColors {
        return if (isSet()) {
            readerThemes.find { it.id ==  preference.get() } ?: readerThemes.first()
        } else {
            readerThemes.first()
        }
    }

    override fun set(value: ReaderColors) {
        preference.set(value.id)
    }

    override fun isSet(): Boolean {
        return preference.isSet()
    }

    override fun delete() {
        preference.delete()
    }

    override fun defaultValue(): ReaderColors {
        return readerThemes.first()
    }

    override fun changes(): Flow<ReaderColors> {
        return preference.changes()
            .map { get() }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<ReaderColors> {
        return preference.changes().map { get() }.stateIn(scope, SharingStarted.Eagerly, get())
    }
}

fun Preference<Long>.asReaderTheme(): ReaderThemePreferences {
    return ReaderThemePreferences(this)
}
val readerThemes = listOf<ReaderColors>(
    ReaderColors(
        id = -1,
        ColorModel.fromArgb(0xff000000.toInt()),
        ColorModel.fromArgb(0xffffffff.toInt()),
        true
    ),
    ReaderColors(
        -2, ColorModel.fromArgb(0xffffffff.toInt()), ColorModel.fromArgb(0xff000000.toInt()),
        true
    ),
    ReaderColors(
        -3, ColorModel.fromArgb(0xff262626.toInt()),
        ColorModel.fromArgb(0xFFE9E9E9.toInt()),
        true
    ),
    ReaderColors(
        -4, ColorModel.fromArgb(0xFF405A61.toInt()), ColorModel.fromArgb(0xFFFFFFFF.toInt()),
        true
    ),
    ReaderColors(
        -5, ColorModel.fromRgb(248, 249, 250), ColorModel.fromRgb(51, 51, 51),
        true
    ),
    ReaderColors(
        -6, ColorModel.fromRgb(150, 173, 252), ColorModel.fromArgb(0xff000000.toInt()),
        true
    ),
    ReaderColors(
        -7, ColorModel.fromRgb(219, 225, 241), ColorModel.fromArgb(0xff000000.toInt()),
        true
    ),
    ReaderColors(
        -8, ColorModel.fromRgb(237, 221, 110), ColorModel.fromArgb(0xff000000.toInt()),
        true
    ),
    ReaderColors(
        -9, ColorModel.fromRgb(168, 242, 154), ColorModel.fromArgb(0xff000000.toInt()),
        true
    ),
    ReaderColors(
        -10, ColorModel.fromRgb(233, 214, 107), ColorModel.fromArgb(0xff000000.toInt()),
        true
    ),
    ReaderColors(
        -11, ColorModel.fromRgb(237, 209, 176), ColorModel.fromArgb(0xff000000.toInt()),
        true
    ),
    ReaderColors(
        -12, ColorModel.fromRgb(185, 135, 220), ColorModel.fromArgb(0xff000000.toInt()),
        true
    ),
    ReaderColors(
        -13, ColorModel.fromRgb(224, 166, 170), ColorModel.fromArgb(0xff000000.toInt()),
        true
    ),
    ReaderColors(
        -14, ColorModel.fromRgb(248, 253, 137), ColorModel.fromArgb(0xff000000.toInt()),
        true
    ),
)