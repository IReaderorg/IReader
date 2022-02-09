package org.ireader.domain.models

sealed class DisplayMode(val title: String, val layout: LayoutType, val layoutIndex: Int) {
    object CompactModel : DisplayMode("Compact Layout", layout = LayoutType.CompactGrid, 0)
    object GridLayout : DisplayMode("Grid Layout", layout = LayoutType.GridLayout, 1)
    object ListLayout : DisplayMode("List Layout", layout = LayoutType.ListLayout, 2)
}

val layouts = listOf<DisplayMode>(
    DisplayMode.CompactModel,
    DisplayMode.GridLayout,
    DisplayMode.ListLayout,
)