package ir.kazemcodes.infinity.presentation.home

sealed class MainScreenEvent {
    data class ChangeScreenIndex(val index: Int) : MainScreenEvent()
}