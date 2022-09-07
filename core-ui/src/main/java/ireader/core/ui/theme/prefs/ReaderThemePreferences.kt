package ireader.core.ui.theme.prefs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ireader.core.api.prefs.Preference
import ireader.core.ui.theme.ReaderColors
import ireader.core.ui.theme.readerThemes

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