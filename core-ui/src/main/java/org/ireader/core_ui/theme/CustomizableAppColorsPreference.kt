package org.ireader.core_ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import org.ireader.core.prefs.Preference
import org.ireader.core.prefs.asColor
import org.ireader.core_ui.ui.PreferenceMutableState
import org.ireader.core_ui.ui.asStateIn
import org.ireader.domain.ui.UiPreferences

data class CustomizableAppColorsPreference(
    val primary: Preference<Color>,
    val secondary: Preference<Color>,
    val bars: Preference<Color>,
)

class CustomizableAppColorsPreferenceState(
    val primaryState: PreferenceMutableState<Color>,
    val secondaryState: PreferenceMutableState<Color>,
    val barsState: PreferenceMutableState<Color>,
) {
    val primary by primaryState
    val secondary by secondaryState
    val bars by barsState
}

fun UiPreferences.getLightColors(): CustomizableAppColorsPreference {
    return CustomizableAppColorsPreference(
        colorPrimaryLight().asColor(),
        colorSecondaryLight().asColor(),
        colorBarsLight().asColor()
    )
}

fun UiPreferences.getDarkColors(): CustomizableAppColorsPreference {
    return CustomizableAppColorsPreference(
        colorPrimaryDark().asColor(),
        colorSecondaryDark().asColor(),
        colorBarsDark().asColor()
    )
}

fun CustomizableAppColorsPreference.asState(scope: CoroutineScope): CustomizableAppColorsPreferenceState {
    return CustomizableAppColorsPreferenceState(
        primary.asStateIn(scope),
        secondary.asStateIn(scope),
        bars.asStateIn(scope)
    )
}
