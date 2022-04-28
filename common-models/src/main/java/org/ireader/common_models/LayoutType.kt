
package org.ireader.common_models

sealed class LayoutType {
    object ListLayout : LayoutType()
    object GridLayout : LayoutType()
    object CompactGrid : LayoutType()
}
