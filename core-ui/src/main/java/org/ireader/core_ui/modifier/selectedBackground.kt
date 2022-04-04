package org.ireader.core_ui.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.selectedBackground(isSelected: Boolean): Modifier = composed {
    if (isSelected) {
        val alpha = if (isSystemInDarkTheme()) 0.19f else 0.22f
        background(MaterialTheme.colors.onBackground.copy(alpha = alpha))
    } else {
        this
    }
}