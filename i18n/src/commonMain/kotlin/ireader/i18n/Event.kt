

package ireader.i18n


abstract class Event

sealed class UiEvent : Event() {

    data class ShowSnackbar(val uiText: UiText) : UiEvent()

    data class Navigate(val route: String) : UiEvent()
    object NavigateUp : UiEvent()
    object OnLogin : UiEvent()
}



data class ResourceException(
    val e: Exception,
    val resId: Int,
)