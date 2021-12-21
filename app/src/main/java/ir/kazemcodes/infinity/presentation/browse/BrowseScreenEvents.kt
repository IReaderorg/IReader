package ir.kazemcodes.infinity.presentation.browse

sealed class BrowseScreenEvents {
    data class UpdatePage(val page:Int) : BrowseScreenEvents()
}
