package ir.kazemcodes.infinity.feature_activity.presentation

import ir.kazemcodes.infinity.feature_activity.domain.models.BottomNavigationScreen

sealed class MainScreenEvent {
    data class NavigateTo(val screen: BottomNavigationScreen) : MainScreenEvent()
}