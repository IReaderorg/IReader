package org.ireader.core_ui.theme.prefs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.ireader.core_api.prefs.Preference
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.Roboto
import org.ireader.core_ui.theme.fonts

class FontPreferences(
    private val preference: Preference<Int>
) : Preference<FontType> {

    override fun key(): String {
        return preference.key()
    }

    override fun get(): FontType {
        return if (isSet()) {
            fonts.getOrNull(preference.get())?: Roboto
        } else {
            Roboto
        }
    }

    override suspend fun read(): FontType {
        return if (isSet()) {
            fonts.getOrNull(preference.get())?: Roboto
        } else {
            Roboto
        }
    }

    override fun set(value: FontType) {
        if (value != Roboto) {
            val index = fonts.indexOfFirst { it == value }
            preference.set(index)
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
        return   Roboto
    }

    override fun changes(): Flow<FontType> {
        return preference.changes()
            .map { get() }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<FontType> {
        return preference.changes().map { get() }.stateIn(scope, SharingStarted.Eagerly, get())
    }

}

fun Preference<Int>.asFont(): FontPreferences {
    return FontPreferences(this)
}