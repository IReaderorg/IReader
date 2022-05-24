

package org.ireader.common_models

sealed class DisplayMode(val title: Int, val layout: LayoutType, val layoutIndex: Int) {
    object CompactModel : DisplayMode(R.string.compact_layout, layout = LayoutType.CompactGrid, 0)
    object GridLayout : DisplayMode(R.string.grid_layout, layout = LayoutType.GridLayout, 1)
    object ListLayout : DisplayMode(R.string.list_layout, layout = LayoutType.ListLayout, 2)
}

val layouts = listOf<DisplayMode>(
    DisplayMode.CompactModel,
    DisplayMode.GridLayout,
    DisplayMode.ListLayout,
)
