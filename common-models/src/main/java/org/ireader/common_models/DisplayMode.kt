

package org.ireader.common_models

import org.ireader.common_resources.UiText

sealed class DisplayMode(val title: UiText, val layout: LayoutType, val layoutIndex: Int) {
    object CompactModel : DisplayMode(UiText.StringResource(R.string.compact_layout), layout = LayoutType.CompactGrid, 0)
    object GridLayout : DisplayMode(UiText.StringResource(R.string.grid_layout), layout = LayoutType.GridLayout, 1)
    object ListLayout : DisplayMode(UiText.StringResource(R.string.list_layout), layout = LayoutType.ListLayout, 2)
}

val layouts = listOf<DisplayMode>(
    DisplayMode.CompactModel,
    DisplayMode.GridLayout,
    DisplayMode.ListLayout,
)
