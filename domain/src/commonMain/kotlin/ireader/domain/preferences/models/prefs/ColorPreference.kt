package ireader.domain.preferences.models.prefs

import ireader.core.prefs.Preference
import ireader.domain.models.common.DomainColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Preference wrapper for DomainColor that stores colors as ARGB integers
 * 
 * This replaces the Compose Color-based preference to maintain clean architecture.
 */
class ThemeColorPreference(
    private val preference: Preference<Int>
) : Preference<DomainColor> {

    override fun key(): String {
        return preference.key()
    }

    override fun get(): DomainColor {
        return if (isSet()) {
            DomainColor.fromArgb(preference.get())
        } else {
            DomainColor.Unspecified
        }
    }

    override fun set(value: DomainColor) {
        if (value != DomainColor.Unspecified) {
            preference.set(value.toArgb())
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

    override fun defaultValue(): DomainColor {
        return DomainColor.Unspecified
    }

    override fun changes(): Flow<DomainColor> {
        return preference.changes()
            .map { get() }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<DomainColor> {
        return preference.changes().map { get() }.stateIn(scope, SharingStarted.Eagerly, get())
    }
}

fun Preference<Int>.asDomainColor(): ColorPreference {
    return ColorPreference(this)
}

class ColorPreference(
    private val preference: Preference<Int>
) : Preference<DomainColor> {

    override fun key(): String {
        return preference.key()
    }

    override fun get(): DomainColor {
        return if (isSet()) {
            DomainColor.fromArgb(preference.get())
        } else {
            defaultValue()
        }
    }

    override fun set(value: DomainColor) {
        if (value != DomainColor.Unspecified) {
            preference.set(value.toArgb())
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

    override fun defaultValue(): DomainColor {
        return kotlin.runCatching {
            DomainColor.fromArgb(preference.defaultValue())
        }.getOrElse {
            DomainColor.Unspecified
        }
    }

    override fun changes(): Flow<DomainColor> {
        return preference.changes()
            .map { get() }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<DomainColor> {
        return preference.changes().map { get() }.stateIn(scope, SharingStarted.Eagerly, get())
    }
}

fun Preference<Int>.asThemeDomainColor(): ThemeColorPreference {
    return ThemeColorPreference(this)
}


