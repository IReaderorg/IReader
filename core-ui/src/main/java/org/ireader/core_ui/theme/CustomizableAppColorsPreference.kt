package org.ireader.core_ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import org.ireader.core_api.prefs.Preference
import org.ireader.core_ui.preferences.UiPreferences
import org.ireader.core_ui.theme.prefs.asThemeColor
import org.ireader.core_ui.ui.PreferenceMutableState
import org.ireader.core_ui.ui.asStateIn

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
    var primary by mutableStateOf(primaryState, structuralEqualityPolicy())
    var secondary by mutableStateOf(secondaryState, structuralEqualityPolicy())
    var bars by mutableStateOf(barsState, structuralEqualityPolicy())
}

fun UiPreferences.getLightColors(): CustomizableAppColorsPreference {
    return CustomizableAppColorsPreference(
        colorPrimaryLight().asThemeColor(),
        colorSecondaryLight().asThemeColor(),
        colorBarsLight().asThemeColor()
    )
}

fun UiPreferences.getDarkColors(): CustomizableAppColorsPreference {
    return CustomizableAppColorsPreference(
        colorPrimaryDark().asThemeColor(),
        colorSecondaryDark().asThemeColor(),
        colorBarsDark().asThemeColor()
    )
}

fun CustomizableAppColorsPreference.asState(scope: CoroutineScope): CustomizableAppColorsPreferenceState {
    return CustomizableAppColorsPreferenceState(
        primary.asStateIn(scope),
        secondary.asStateIn(scope),
        bars.asStateIn(scope)
    )
}
