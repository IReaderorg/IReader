package ireader.ui.component.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ireader.ui.component.components.component.ChipPreference
import ireader.ui.component.components.component.PreferenceRow
import ireader.ui.component.components.component.SliderPreference
import ireader.ui.component.components.component.SwitchPreference
import ireader.ui.component.text_related.TextSection
import ireader.core.ui.ui.PreferenceMutableState

sealed class Components {
    data class Header(
        val text: String,
        val toUpper: Boolean = false,
        val padding: PaddingValues = PaddingValues(16.dp),
        val visible: Boolean = true,

        ) : Components()

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
        val steps: Int = 0,
        val visible: Boolean = true
    ) : Components()

    data class Row(
        val title: String,
        val icon: ImageVector? = null,
        val onClick: () -> Unit = {},
        val onLongClick: () -> Unit = {},
        val subtitle: String? = null,
        val action: @Composable (() -> Unit)? = null,
        val visible: Boolean = true
    ) : Components()

    data class Switch(
        val modifier: Modifier = Modifier,
        val preference: PreferenceMutableState<Boolean>,
        val title: String,
        val subtitle: String? = null,
        val painter: Painter? = null,
        val icon: ImageVector? = null,
        val onValue: ((Boolean) -> Unit)? = null,
        val visible: Boolean = true
    ) : Components()

    data class Dynamic(
        val component: @Composable () -> Unit,
    ) : Components()

    data class Chip(
        val preference: List<String>,
        val selected: Int,
        val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val onValueChange: ((Int) -> Unit)?,
        val visible: Boolean = true
    ) : Components()

    object Space : Components()
}

@Composable
fun SetupSettingComponents(
    scaffoldPadding: PaddingValues,
    items: List<Components>,
) {
    LazyColumn(
        modifier = androidx.compose.ui.Modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
    ) {
        setupUiComponent(items)
    }
}

@Composable
fun Components.Build() {
    when (this) {
        is Components.Header -> {
            if (this.visible) {
                if (this.visible) {
                    TextSection(
                        text = this.text,
                        padding = this.padding,
                        toUpper = this.toUpper
                    )
                }
            }
        }
        is Components.Slider -> {
            if (this.visible) {
                SliderPreference(
                    preferenceAsLong = this.preferenceAsLong,
                    preferenceAsFloat = this.preferenceAsFloat,
                    preferenceAsInt = this.preferenceAsInt,
                    title = this.title,
                    onValueChange = {
                        this.onValueChange?.let { it1 -> it1(it) }
                    },
                    trailing = this.trailing,
                    valueRange = this.valueRange,
                    onValueChangeFinished = {
                        this.onValueChangeFinished?.let { it1 -> it1(it) }
                    },
                    steps = this.steps
                )
            }
        }
        is Components.Row -> {
            if (this.visible) {
                PreferenceRow(
                    title = this.title,
                    action = this.action,
                    subtitle = this.subtitle,
                    onClick = this.onClick,
                    icon = this.icon,
                    onLongClick = this.onLongClick,
                )
            }
        }
        is Components.Chip -> {
            if (this.visible) {
                ChipPreference(
                    preference = this.preference,
                    selected = this.selected,
                    onValueChange = this.onValueChange,
                    title = this.title,
                    subtitle = this.subtitle,
                    icon = this.icon
                )
            }
        }
        is Components.Switch -> {
            if (this.visible) {
                SwitchPreference(
                    preference = this.preference,
                    title = this.title,
                    icon = this.icon,
                    subtitle = this.subtitle,
                    onValue = this.onValue
                )
            }
        }
        is Components.Dynamic -> {
            this.component()
        }
        is Components.Space -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
    }
}

fun LazyListScope.setupUiComponent(
    list: List<Components>,
) {
    list.forEach { component ->
        item {
            when (component) {
                is Components.Header -> {
                    if (component.visible) {
                        if (component.visible) {
                            TextSection(
                                text = component.text,
                                padding = component.padding,
                                toUpper = component.toUpper
                            )
                        }
                    }
                }
                is Components.Slider -> {
                    if (component.visible) {
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
                            steps = component.steps
                        )
                    }
                }
                is Components.Row -> {
                    if (component.visible) {
                        PreferenceRow(
                            title = component.title,
                            action = component.action,
                            subtitle = component.subtitle,
                            onClick = component.onClick,
                            icon = component.icon,
                            onLongClick = component.onLongClick,
                        )
                    }
                }
                is Components.Chip -> {
                    if (component.visible) {
                        ChipPreference(
                            preference = component.preference,
                            selected = component.selected,
                            onValueChange = component.onValueChange,
                            title = component.title,
                            subtitle = component.subtitle,
                            icon = component.icon
                        )
                    }
                }
                is Components.Switch -> {
                    if (component.visible) {
                        SwitchPreference(
                            preference = component.preference,
                            title = component.title,
                            icon = component.icon,
                            subtitle = component.subtitle,
                            onValue = component.onValue
                        )
                    }
                }
                is Components.Dynamic -> {
                    component.component()
                }
                is Components.Space -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )
                }
            }
        }
    }
}
