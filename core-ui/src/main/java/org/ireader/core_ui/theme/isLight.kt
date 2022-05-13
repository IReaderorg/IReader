package org.ireader.core_ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.luminance


fun ColorScheme.isLight() = this.background.luminance() > 0.5

//val ColorScheme.isLight: Boolean
//    get() = this.background.luminance() > 0.5