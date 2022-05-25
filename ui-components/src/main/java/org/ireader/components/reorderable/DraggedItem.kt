package org.ireader.components.reorderable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex

fun Modifier.draggedItem(offset: IntOffset?): Modifier = this.then(
    zIndex(offset?.let { 1f } ?: 0f)
        .graphicsLayer {
            translationX = offset?.x?.toFloat() ?: 0f
            translationY = offset?.y?.toFloat() ?: 0f
            shadowElevation = offset?.let { 8f } ?: 0f
        }
)