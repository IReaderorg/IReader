package ireader.presentation.ui.core.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import ireader.core.prefs.Preference
import ireader.domain.models.common.DomainColor
import ireader.domain.preferences.models.prefs.asThemeDomainColor
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.ui.PreferenceMutableState
import ireader.presentation.ui.core.ui.asStateIn
import kotlinx.coroutines.CoroutineScope

data class CustomizableAppColorsPreference(
    val primary: Preference<DomainColor>,
    val secondary: Preference<DomainColor>,
    val bars: Preference<DomainColor>,
)

class CustomizableAppColorsPreferenceState(
    val primaryState: PreferenceMutableState<DomainColor>,
    val secondaryState: PreferenceMutableState<DomainColor>,
    val barsState: PreferenceMutableState<DomainColor>,
) {
    var primary by mutableStateOf(primaryState, structuralEqualityPolicy())
    var secondary by mutableStateOf(secondaryState, structuralEqualityPolicy())
    var bars by mutableStateOf(barsState, structuralEqualityPolicy())
}

fun UiPreferences.getLightColors(): CustomizableAppColorsPreference {
    return CustomizableAppColorsPreference(
        colorPrimaryLight().asThemeDomainColor(),
        colorSecondaryLight().asThemeDomainColor(),
        colorBarsLight().asThemeDomainColor()
    )
}

fun UiPreferences.getDarkColors(): CustomizableAppColorsPreference {
    return CustomizableAppColorsPreference(
        colorPrimaryDark().asThemeDomainColor(),
        colorSecondaryDark().asThemeDomainColor(),
        colorBarsDark().asThemeDomainColor()
    )
}

fun CustomizableAppColorsPreference.asState(scope: CoroutineScope): CustomizableAppColorsPreferenceState {
    return CustomizableAppColorsPreferenceState(
        primary.asStateIn(scope),
        secondary.asStateIn(scope),
        bars.asStateIn(scope)
    )
}
