package ir.kazemcodes.infinity.core.presentation.layouts

sealed class LayoutType {
    object ListLayout : LayoutType()
    object GridLayout : LayoutType()
    object CompactGrid : LayoutType()
}
