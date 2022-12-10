package ireader.domain.preferences.models.prefs

import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ireader.core.prefs.Preference
import ireader.domain.preferences.models.ReaderColors

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

    override suspend fun read(): ReaderColors {
        return if (isSet()) {
            readerThemes.find { it.id ==  preference.read() } ?: readerThemes.first()
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
        Color(0xff000000),
        Color(0xffffffff),
        true
    ),
    ReaderColors(
        -2, Color(0xffffffff), Color(0xff000000),
        true
    ),
    ReaderColors(
        -3, Color(0xff262626),
        Color(
            0xFFE9E9E9,
        ),
        true
    ),
    ReaderColors(
        -4, Color(0xFF405A61), Color(0xFFFFFFFF),
        true
    ),
    ReaderColors(
        -5, Color(248, 249, 250), Color(51, 51, 51),
        true
    ),
    ReaderColors(
        -6, Color(150, 173, 252), Color(0xff000000),
        true
    ),
    ReaderColors(
        -7, Color(219, 225, 241), Color(0xff000000),
        true
    ),
    ReaderColors(
        -8, Color(237, 221, 110), Color(0xff000000),
        true
    ),
    ReaderColors(
        -9, Color(168, 242, 154), Color(0xff000000),
        true
    ),
    ReaderColors(
        -10, Color(233, 214, 107), Color(0xff000000),
        true
    ),
    ReaderColors(
        -11, Color(237, 209, 176), Color(0xff000000),
        true
    ),
    ReaderColors(
        -12, Color(185, 135, 220), Color(0xff000000),
        true
    ),
    ReaderColors(
        -13, Color(224, 166, 170), Color(0xff000000),
        true
    ),
    ReaderColors(
        -14, Color(248, 253, 137), Color(0xff000000),
        true
    ),
).toMutableStateList()