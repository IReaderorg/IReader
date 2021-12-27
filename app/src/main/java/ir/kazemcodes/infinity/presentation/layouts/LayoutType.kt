package ir.kazemcodes.infinity.presentation.layouts

sealed class LayoutType {
    object ListLayout : LayoutType()
    object GridLayout : LayoutType()
    object CompactGrid : LayoutType()
}
