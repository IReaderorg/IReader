package ir.kazemcodes.infinity.feature_activity.presentation

sealed class MainScreenEvent {
    data class ChangeScreenIndex(val index: Int) : MainScreenEvent()
}