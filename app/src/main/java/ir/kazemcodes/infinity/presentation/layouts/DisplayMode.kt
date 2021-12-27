package ir.kazemcodes.infinity.presentation.layouts

sealed class DisplayMode(val title: String, val layout: LayoutType) {
    object CompactModel : DisplayMode("Compact Layout", layout = LayoutType.CompactGrid)
    object GridLayout : DisplayMode("Grid Layout",layout = LayoutType.GridLayout)
    object ListLayout : DisplayMode("List Layout",layout = LayoutType.ListLayout)
}