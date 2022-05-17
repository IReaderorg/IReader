package org.ireader.components.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import org.ireader.core_ui.ui.PreferenceMutableState

sealed class Components {
    data class Header(val text: String) : Components()
    data class Slider(
        val preferenceAsFloat: PreferenceMutableState<Float>?=null,
        val preferenceAsInt: PreferenceMutableState<Int>?=null,
        val preferenceAsLong: PreferenceMutableState<Long>?=null,
        val mutablePreferences: Float?=null,
        val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val trailing: String? = null,
        val onValueChange: ( (Float) -> Unit) ? = null,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val onValueChangeFinished: ((Float) -> Unit)? = null,
        val steps: Int = 0
    ) : Components()

    data class PreferencesRow(
        val title: String,
        val icon: ImageVector? = null,
        val onClick: () -> Unit = {},
        val  onLongClick: () -> Unit = {},
        val subtitle: String? = null,
        val action: @Composable (() -> Unit)? = null,
    )
    data class SwitchPreference(
        val modifier: Modifier = Modifier,
        val preference: PreferenceMutableState<Boolean>,
        val title: String,
        val subtitle: String? = null,
        val painter: Painter? = null,
        val icon: ImageVector? = null,
    )
}
