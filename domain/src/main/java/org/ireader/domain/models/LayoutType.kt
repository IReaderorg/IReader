package org.ireader.domain.models

sealed class LayoutType {
    object ListLayout : LayoutType()
    object GridLayout : LayoutType()
    object CompactGrid : LayoutType()
}
