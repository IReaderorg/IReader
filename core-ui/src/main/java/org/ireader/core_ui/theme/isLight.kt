package org.ireader.core_ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

@Composable
fun ColorScheme.isLight() = this.background.luminance() > 0.5