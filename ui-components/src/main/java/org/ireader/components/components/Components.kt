package org.ireader.components.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import org.ireader.components.components.component.ChipPreference
import org.ireader.components.components.component.PreferenceRow
import org.ireader.components.components.component.SliderPreference
import org.ireader.components.components.component.SwitchPreference
import org.ireader.components.text_related.TextSection
import org.ireader.core_ui.ui.PreferenceMutableState

sealed class Components {
    data class Header(val text: String) : Components()
    data class Slider(
        val preferenceAsFloat: PreferenceMutableState<Float>? = null,
        val preferenceAsInt: PreferenceMutableState<Int>? = null,
        val preferenceAsLong: PreferenceMutableState<Long>? = null,
        val mutablePreferences: Float? = null,
        val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val trailing: String? = null,
        val onValueChange: ((Float) -> Unit)? = null,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val onValueChangeFinished: ((Float) -> Unit)? = null,
        val steps: Int = 0
    ) : Components()

    data class Row(
        val title: String,
        val icon: ImageVector? = null,
        val onClick: () -> Unit = {},
        val onLongClick: () -> Unit = {},
        val subtitle: String? = null,
        val action: @Composable (() -> Unit)? = null,
    ) : Components()

    data class Switch(
        val modifier: Modifier = Modifier,
        val preference: PreferenceMutableState<Boolean>,
        val title: String,
        val subtitle: String? = null,
        val painter: Painter? = null,
        val icon: ImageVector? = null,
    ) : Components()

    data class Chip(
        val preference: List<String>,
        val selected: Int,
        val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val onValueChange: ((Int) -> Unit)?
    ) : Components()
}

fun LazyListScope.SetupUiComponent(list: List<Components>) {
    list.forEach { component ->
        item {
            when (component) {
                is Components.Header -> {
                    TextSection(
                        text = component.text
                    )
                }
                is Components.Slider -> {
                    SliderPreference(
                        preferenceAsLong = component.preferenceAsLong,
                        preferenceAsFloat = component.preferenceAsFloat,
                        preferenceAsInt = component.preferenceAsInt,
                        title = component.title,
                        onValueChange = {
                            component.onValueChange?.let { it1 -> it1(it) }
                        },
                        trailing = component.trailing,
                        valueRange = component.valueRange,
                        onValueChangeFinished = {
                            component.onValueChangeFinished?.let { it1 -> it1(it) }
                        },
                    )
                }
                is Components.Row -> {
                    PreferenceRow(
                        title = component.title,
                        action = component.action,
                        subtitle = component.subtitle,
                        onClick = component.onClick,
                        icon = component.icon,
                        onLongClick = component.onLongClick,
                    )
                }
                is Components.Chip -> {
                    ChipPreference(
                        preference = component.preference,
                        selected = component.selected,
                        onValueChange = component.onValueChange,
                        title = component.title
                    )
                }
                is Components.Switch -> {
                    SwitchPreference(
                        preference = component.preference,
                        title = component.title,
                        icon = component.icon,
                        subtitle = component.subtitle,
                    )
                }
            }
        }
    }
}