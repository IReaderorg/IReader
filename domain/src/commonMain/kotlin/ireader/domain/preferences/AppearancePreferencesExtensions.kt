package ireader.domain.preferences

import ireader.core.prefs.Preference
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Extension properties for appearance-related preferences.
 * These provide convenient access to novel info display settings.
 */

/**
 * Flow that emits whether backdrop images should be hidden on novel detail screens.
 */
val UiPreferences.hideNovelBackdropFlow: Flow<Boolean>
    get() = hideNovelBackdrop().changes()

/**
 * Flow that emits whether FAB should be used instead of standard buttons on novel detail screens.
 */
val UiPreferences.useFabInNovelInfoFlow: Flow<Boolean>
    get() = useFabInNovelInfo().changes()

/**
 * Get the current value of hideNovelBackdrop preference.
 */
fun UiPreferences.isNovelBackdropHidden(): Boolean {
    return hideNovelBackdrop().get()
}

/**
 * Get the current value of useFabInNovelInfo preference.
 */
fun UiPreferences.isFabUsedInNovelInfo(): Boolean {
    return useFabInNovelInfo().get()
}

/**
 * Set whether to hide backdrop images on novel detail screens.
 */
fun UiPreferences.setHideNovelBackdrop(hide: Boolean) {
    hideNovelBackdrop().set(hide)
}

/**
 * Set whether to use FAB instead of standard buttons on novel detail screens.
 */
fun UiPreferences.setUseFabInNovelInfo(useFab: Boolean) {
    useFabInNovelInfo().set(useFab)
}
