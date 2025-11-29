package ireader.presentation.ui.component.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.reusable_composable.CaptionTextComposable
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.ContentAlpha
import ireader.presentation.ui.core.ui.PreferenceMutableState
import ireader.presentation.ui.core.utils.horizontalPadding

@Composable
fun Divider(
        modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Divider(
            modifier = modifier,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    )
}
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun PreferenceRow(
//    modifier: Modifier = Modifier,
//    title: String,
//    icon: ImageVector? = null,
//    onClick: () -> Unit = {},
//    onLongClick: () -> Unit = {},
//    subtitle: String? = null,
//    action: @Composable (() -> Unit)? = null,
//    actionAlignment: Alignment = Alignment.CenterEnd,
//    enableNoIndicator: Boolean = false,
//) {
//    val height = if (subtitle != null) 72.dp else 56.dp
//
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .requiredHeight(height)
//            .combinedClickable(
//                onLongClick = onLongClick,
//                onClick = onClick
//            ),
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        if (icon != null) {
//            Icon(
//                modifier = Modifier
//                    .padding(horizontal = 16.dp)
//                    .size(24.dp),
//                contentDescription = null,
//                imageVector = icon,
//                tint = MaterialTheme.colorScheme.primary,
//
//                )
//        }
//        Column(
//            Modifier
//                .padding(horizontal = 16.dp)
//                .weight(1f)
//        ) {
//            Text(
//                text = title,
//                overflow = Ellipsis,
//                maxLines = 1,
//                style = MaterialTheme.typography.labelMedium,
//            )
//            if (subtitle != null) {
//                Text(
//                    text = subtitle,
//                    overflow = Ellipsis,
//                    maxLines = 1,
//                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
//                    style = MaterialTheme.typography.labelMedium
//                )
//            }
//        }
//        if (action != null) {
//            Box(Modifier.widthIn(min = 56.dp, max = 250.dp), contentAlignment = actionAlignment) {
//                action()
//            }
//        }
//    }
//}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferenceRow(
        modifier: Modifier = Modifier,
        title: String,
        painter: Painter? = null,
        icon: ImageVector? = null,
        onClick: () -> Unit = {},
        onLongClick: () -> Unit = {},
        subtitle: String? = null,
        clickable: Boolean = true,
        actionAlignment: Alignment = Alignment.CenterEnd,
        enable: Boolean = true,
        action: @Composable (() -> Unit)? = null,
) {
    if (enable) {
        // Ensure minimum touch target height of 48dp for accessibility
        val height = if (subtitle != null) 72.dp else 56.dp

        val titleTextStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        val subtitleTextStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        // Build content description for accessibility
        val contentDesc = buildString {
            append(title)
            if (subtitle != null) {
                append(". ")
                append(subtitle)
            }
        }

        Row(
                modifier = modifier
                        .fillMaxWidth()
                        .heightIn(min = height)
                        .semantics {
                            contentDescription = contentDesc
                            role = androidx.compose.ui.semantics.Role.Button
                        }
                        .combinedClickable(
                                onLongClick = onLongClick,
                                onClick = onClick,
                        ),
                verticalAlignment = Alignment.CenterVertically
        ) {
            if (painter != null) {
                Icon(
                        painter = painter,
                        modifier = Modifier
                                .padding(horizontal = horizontalPadding)
                                .size(24.dp),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                )
            }
            if (icon != null) {
                Icon(
                        imageVector = icon,
                        modifier = Modifier
                                .padding(horizontal = horizontalPadding)
                                .size(24.dp),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                )
            }
            Column(
                    Modifier
                            .padding(horizontal = horizontalPadding)
                            .weight(1f)
            ) {
                Text(
                        text = title,
                        overflow = Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (subtitle != null) {
                    Text(
                            text = subtitle,
                            overflow = Ellipsis,
                            maxLines = 1,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium()),
                            style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            if (action != null) {
                Box(
                        Modifier.widthIn(min = 56.dp, max = 250.dp),
                        contentAlignment = actionAlignment
                ) {
                    action()
                }
            }
        }
    }
}

@Composable
fun SwitchPreference(
        modifier: Modifier = Modifier,
        preference: PreferenceMutableState<Boolean>,
        title: String,
        subtitle: String? = null,
        painter: Painter? = null,
        icon: ImageVector? = null,
) {
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        }
        append(". ")
        append(if (preference.value) "Enabled" else "Disabled")
    }
    
    PreferenceRow(
            modifier = modifier.semantics(mergeDescendants = true) {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.Switch
            },
            title = title,
            subtitle = subtitle,
            painter = painter,
            icon = icon,
            action = {
                Switch(
                        checked = preference.value,
                        onCheckedChange = { preference.value = !preference.value },
                )
            },
            onClick = { preference.value = !preference.value },
            clickable = true,
    )
}

// @Composable
// fun SliderPreference(
//    preference: MutableState<Float>,
//    title: String,
//    subtitle: String? = null,
//    icon: ImageVector? = null,
//    trailing: String? = null,
//    onValueChange: ((Float) -> Unit) = {},
//    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
//    onValueChangeFinished: ((Float) -> Unit)? = null,
//    steps : Int = 0
// ) {
//
//    val height = if (subtitle != null) 72.dp else 56.dp
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .requiredHeight(height),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        if (icon != null) {
//            Icon(
//                modifier = Modifier
//                    .padding(horizontal = 16.dp)
//                    .size(24.dp),
//                contentDescription = null,
//                imageVector = icon,
//                tint = MaterialTheme.colorScheme.primary,
//            )
//        }
//        Column(
//            Modifier
//                .padding(horizontal = 16.dp)
//                .weight(3f),
//            horizontalAlignment = Alignment.Start,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(
//                modifier = Modifier,
//                text = title,
//                overflow = Ellipsis,
//                maxLines = 1,
//                style = MaterialTheme.typography.labelSmall,
//                textAlign = TextAlign.Start,
//                softWrap  = true,
//            )
//            if (subtitle != null) {
//                Text(
//                    modifier = Modifier,
//                    text = subtitle,
//                    overflow = Ellipsis,
//                    maxLines = 1,
//                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
//                    style = MaterialTheme.typography.labelMedium,
//                    softWrap  = true,
//                )
//            }
//        }
//        Slider(
//            modifier = Modifier
//                .weight(7f),
//            value = preference.value,
//            onValueChange = {
//                preference.value = it
//                onValueChange(it)
//            },
//            valueRange = valueRange,
//            onValueChangeFinished = {
//                if (onValueChangeFinished != null) {
//                    onValueChangeFinished(preference.value)
//                }
//            },
//            steps = steps
//        )
//        if (trailing != null) {
//            Text(
//                modifier = Modifier
//                    .weight(.5f)
//                    .padding(horizontal = 8.dp),
//                text = trailing,
//                overflow = Ellipsis,
//                maxLines = 1,
//                style = MaterialTheme.typography.labelSmall,
//                textAlign = TextAlign.Start,
//                softWrap  = true,
//            )
//        }
//    }
// }

@Composable
fun SliderPreference(
        preferenceAsFloat: PreferenceMutableState<Float>? = null,
        preferenceAsInt: PreferenceMutableState<Int>? = null,
        preferenceAsLong: PreferenceMutableState<Long>? = null,
        mutablePreferences: MutableState<Float>? = null,
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        trailing: String? = null,
        trailingFormatter: ((Float) -> String)? = null,
        onValueChange: ((Float) -> Unit) = {},
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        onValueChangeFinished: ((Float) -> Unit)? = null,
        steps: Int = 0,
        style: TextStyle = MaterialTheme.typography.labelMedium,
        color: Color = MaterialTheme.colorScheme.primary,
        isEnable: Boolean = true
) {
    // Calculate minimum height based on subtitle presence (ensure 48dp minimum for accessibility)
    val minHeight = if (subtitle != null) 96.dp else 80.dp
    
    // Track if slider is being actively dragged for enhanced visual feedback
    var isInteracting by remember { mutableStateOf(false) }
    
    // Get current value for real-time display
    val currentValue = preferenceAsFloat?.lazyValue ?: preferenceAsInt?.lazyValue?.toFloat()
        ?: preferenceAsLong?.lazyValue?.toFloat() ?: mutablePreferences?.value ?: 0F
    
    // Calculate trailing text with real-time updates
    val displayTrailing = remember(currentValue, trailing, trailingFormatter) {
        when {
            trailingFormatter != null -> trailingFormatter(currentValue)
            trailing != null -> trailing
            else -> currentValue.toInt().toString()
        }
    }
    
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        }
        append(". Current value: ")
        append(displayTrailing)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .padding(vertical = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = contentDesc
            }
    ) {
        // Header row with icon, title, subtitle, and value
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (icon != null) {
                Icon(
                    modifier = Modifier
                        .size(24.dp),
                    contentDescription = null,
                    imageVector = icon,
                    tint = if (isEnable) {
                        if (isInteracting) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            // Title and subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    overflow = Ellipsis,
                    maxLines = 1,
                    style = style,
                    color = if (isEnable) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        overflow = Ellipsis,
                        maxLines = 2,
                        style = style,
                        color = if (isEnable) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            // Value display with enhanced styling and real-time updates
            Surface(
                modifier = Modifier.padding(start = 12.dp),
                color = if (isInteracting && isEnable) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    text = displayTrailing,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isEnable) {
                        if (isInteracting) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Slider with enhanced visual feedback
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (icon != null) 56.dp else 16.dp),
            value = preferenceAsFloat?.lazyValue ?: preferenceAsInt?.lazyValue?.toFloat()
                ?: preferenceAsLong?.lazyValue?.toFloat() ?: mutablePreferences?.value ?: 0F,
            onValueChange = {
                isInteracting = true
                preferenceAsFloat?.lazyValue = it
                preferenceAsInt?.lazyValue = it.toInt()
                preferenceAsLong?.lazyValue = it.toLong()
                mutablePreferences?.value = it
                onValueChange(it)
            },
            valueRange = valueRange,
            onValueChangeFinished = {
                isInteracting = false
                if (onValueChangeFinished != null) {
                    onValueChangeFinished(
                        preferenceAsFloat?.value?.toFloat()
                            ?: preferenceAsInt?.value?.toFloat()
                            ?: preferenceAsLong?.value?.toFloat()
                            ?: mutablePreferences?.value ?: 0F
                    )
                }
            },
            steps = steps,
            enabled = isEnable,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledActiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
                disabledInactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipPreference(
        preference: List<String>,
        selected: Int,
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        containerColor: Color = MaterialTheme.colorScheme.surface,
        labelColor: Color = MaterialTheme.colorScheme.onPrimary,
        selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
        onValueChange: ((Int) -> Unit)?
) {
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        }
        append(". Selected: ")
        append(preference.getOrNull(selected) ?: "None")
    }
    
    PreferenceRow(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = contentDesc
            },
            title = title,
            subtitle = subtitle,
            icon = icon,
            action = {
                LazyRow {
                    items(count = preference.size) { index ->
                        FilterChip(
                                modifier = Modifier.semantics {
                                    contentDescription = "${preference[index]}. ${if (index == selected) "Selected" else "Not selected"}"
                                    role = androidx.compose.ui.semantics.Role.RadioButton
                                },
                                selected = index == selected,
                                onClick = {
                                    if (onValueChange != null) {
                                        onValueChange(index)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                        containerColor = containerColor,
                                        labelColor = labelColor,
                                        selectedContainerColor = selectedContainerColor,
                                ),
                                label = {
                                    CaptionTextComposable(
                                            text = preference[index],
                                            maxLine = 1,
                                            align = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                            color = if (selected == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Key> ChipChoicePreference(
        preference: PreferenceMutableState<Key>,
        choices: Map<Key, String>,
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        onValue: ((Key) -> Unit)? = null,
        onFailToFindElement: String = ""
) {
    var showDialog by remember { mutableStateOf(false) }
    val selectedValue = choices[preference.value] ?: onFailToFindElement
    
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        }
        append(". Current selection: ")
        append(selectedValue)
    }
    
    PreferenceRow(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.DropdownList
            },
            title = title,
            subtitle = subtitle,
            icon = icon,
            action = {
                Button(
                        modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .padding(end = 2.dp, top = 4.dp, bottom = 4.dp)
                                .semantics {
                                    contentDescription = "Select $title. Current: $selectedValue"
                                },
                        onClick = {
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = RoundedCornerShape(4.dp),
                        content = {
                            CaptionTextComposable(
                                    text = selectedValue,
                                    maxLine = 1,
                                    align = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                )
            },
    )
    if (showDialog) {
        IAlertDialog(
                modifier = Modifier.heightIn(max = 350.dp, min = 200.dp),
                onDismissRequest = { showDialog = false },
                title = { Text(title ,color = MaterialTheme.colorScheme.onBackground) },
                text = {
                    LazyColumn {
                        items(choices.toList()) { (value, text) ->
                            val isSelected = value == preference.value
                            Row(
                                    modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .fillMaxWidth()
                                            .semantics(mergeDescendants = true) {
                                                contentDescription = "$text. ${if (isSelected) "Selected" else "Not selected"}"
                                                role = androidx.compose.ui.semantics.Role.RadioButton
                                            }
                                            .clickable(onClick = {
                                                if (onValue != null) {
                                                    onValue(value)
                                                } else {
                                                    preference.value = value
                                                }

                                                showDialog = false
                                            }),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                        selected = isSelected,
                                        onClick = null,
                                )
                                Text(text = text, modifier = Modifier.padding(start = 24.dp), color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                },
                confirmButton = {}
        )
    }
}

@Composable
fun SwitchPreference(
        preference: Boolean,
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        onValueChange: ((Boolean) -> Unit)?
) {
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        }
        append(". ")
        append(if (preference) "Enabled" else "Disabled")
    }
    
    PreferenceRow(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.Switch
            },
            title = title,
            subtitle = subtitle,
            icon = icon,
            action = { Switch(checked = preference, onCheckedChange = null) },
            onClick = {
                if (onValueChange != null) {
                    onValueChange(!preference)
                }
            }
    )
}

@Composable
fun SwitchPreference(
        preference: PreferenceMutableState<Boolean>,
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        onValue: ((Boolean) -> Unit)? = null
) {
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        }
        append(". ")
        append(if (preference.value) "Enabled" else "Disabled")
    }
    
    PreferenceRow(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.Switch
            },
            title = title,
            subtitle = subtitle,
            icon = icon,
            action = { Switch(checked = preference.value, onCheckedChange = null) },
            onClick = {
                if (onValue != null) {
                    onValue(preference.value)
                } else {
                    preference.value = !preference.value
                }
            }
    )
}

@Composable
fun SwitchPreference(
        preference: MutableState<Boolean>,
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
) {
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        }
        append(". ")
        append(if (preference.value) "Enabled" else "Disabled")
    }
    
    PreferenceRow(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.Switch
            },
            title = title,
            subtitle = subtitle,
            icon = icon,
            action = { Switch(checked = preference.value, onCheckedChange = null) },
            onClick = { preference.value = !preference.value }
    )
}

@Composable
fun <Key> ChoicePreference(
        preference: MutableState<Key>,
        choices: Map<Key, String>,
        title: String,
        subtitle: String? = null,
        onValue: ((Key) -> Unit)? = null,
        confirmText: String = "",
        onConfirm: (() -> Unit)? = null,
        onItemSelected: (() -> Unit)? = null,
        enable: Boolean = true,
        onShowDialog: () -> Unit = {},
        onDismiss: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    val selectedValue = choices[preference.value]
    
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        } else if (selectedValue != null) {
            append(". Current selection: ")
            append(selectedValue)
        }
    }

    PreferenceRow(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.DropdownList
            },
            title = title,
            subtitle = if (subtitle == null) selectedValue else null,
            onClick = {
                onShowDialog()
                showDialog = true
            },
            enable = enable

    )

    if (showDialog) {
        IAlertDialog(
                onDismissRequest = {
                    showDialog = false
                    onDismiss()
                                   },
                title = { Text(title,    color = MaterialTheme.colorScheme.onBackground,) },
                text = {
                    LazyColumn {
                        items(choices.toList()) { (value, text) ->
                            val isSelected = value == preference.value
                            Row(
                                    modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .fillMaxWidth()
                                            .semantics(mergeDescendants = true) {
                                                contentDescription = "$text. ${if (isSelected) "Selected" else "Not selected"}"
                                                role = androidx.compose.ui.semantics.Role.RadioButton
                                            }
                                            .clickable(onClick = {
                                                if (onValue != null) {
                                                    onValue(value)
                                                } else {
                                                    preference.value = value
                                                }
                                                if (onItemSelected != null) {
                                                    onItemSelected()
                                                }

                                                showDialog = false
                                            }),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                        selected = isSelected,
                                        onClick = null,
                                )
                                Text(text = text, modifier = Modifier.padding(start = 24.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    if (onConfirm != null) {
                        TextButton(onClick = onConfirm) {
                            MidSizeTextComposable(text = confirmText)
                        }
                    }
                }
        )
    }
}

@Composable
fun <Key> ChoicePreference(
        preference: Key,
        choices: Map<Key, String>,
        title: String,
        subtitle: String? = null,
        onValue: ((Key) -> Unit)? = null,
        confirmText: String = "",
        onConfirm: (() -> Unit)? = null,
        onItemSelected: (() -> Unit)? = null,
        enable: Boolean = true,
        onShowDialog: () -> Unit = {},
        onDismiss: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    val selectedValue = choices[preference]
    
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        } else if (selectedValue != null) {
            append(". Current selection: ")
            append(selectedValue)
        }
    }
    
    PreferenceRow(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.DropdownList
            },
            title = title,
            subtitle = if (subtitle == null) selectedValue else null,
            onClick = {
                showDialog = true
                onShowDialog()
            },
            enable = enable

    )

    if (showDialog) {
        IAlertDialog(
                onDismissRequest = {
                    showDialog = false
                    onDismiss()
                                   },
                title = { Text(title) },
                text = {
                    LazyColumn {
                        items(choices.toList()) { (value, text) ->
                            val isSelected = value == preference
                            Row(
                                    modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .fillMaxWidth()
                                            .semantics(mergeDescendants = true) {
                                                contentDescription = "$text. ${if (isSelected) "Selected" else "Not selected"}"
                                                role = androidx.compose.ui.semantics.Role.RadioButton
                                            }
                                            .clickable(onClick = {
                                                if (onValue != null) {
                                                    onValue(value)
                                                }
                                                if (onItemSelected != null) {
                                                    onItemSelected()
                                                }

                                                showDialog = false
                                            }),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                        selected = isSelected,
                                        onClick = null,
                                )
                                Text(text = text, modifier = Modifier.padding(start = 24.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    if (onConfirm != null) {
                        TextButton(onClick = onConfirm) {
                            MidSizeTextComposable(text = confirmText)
                        }
                    }
                }
        )
    }
}

@Composable
fun <Key> ChoicePreference(
        preference: PreferenceMutableState<Key>,
        choices: Map<Key, String>,
        title: String,
        subtitle: String? = null,
        onValue: ((Key) -> Unit)? = null,
        confirmText: String = "",
        onConfirm: (() -> Unit)? = null,
        onItemSelected: (() -> Unit)? = null,
        enable: Boolean = true,
        onShowDialog: () -> Unit = {},
        onDismiss: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    val selectedValue = choices[preference.value]
    
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        } else if (selectedValue != null) {
            append(". Current selection: ")
            append(selectedValue)
        }
    }

    PreferenceRow(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.DropdownList
            },
            title = title,
            subtitle = if (subtitle == null) selectedValue else null,
            onClick = {
                showDialog = true
                onShowDialog()
            },
            enable = enable,

    )

    if (showDialog) {
        IAlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(title ,color = MaterialTheme.colorScheme.onBackground) },
                text = {
                    LazyColumn {
                        items(choices.toList()) { (value, text) ->
                            val isSelected = value == preference.value
                            Row(
                                    modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .fillMaxWidth()
                                            .semantics(mergeDescendants = true) {
                                                contentDescription = "$text. ${if (isSelected) "Selected" else "Not selected"}"
                                                role = androidx.compose.ui.semantics.Role.RadioButton
                                            }
                                            .clickable(onClick = {
                                                if (onValue != null) {
                                                    onValue(value)
                                                } else {
                                                    preference.value = value
                                                }
                                                if (onItemSelected != null) {
                                                    onItemSelected()
                                                }

                                                showDialog = false
                                            }),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                        selected = isSelected,
                                        onClick = null,
                                )
                                Text(text = text, modifier = Modifier.padding(start = 24.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    if (onConfirm != null) {
                        TextButton(onClick = onConfirm) {
                            MidSizeTextComposable(text = confirmText)
                        }
                    }
                }
        )
    }
}

@Composable
fun ColorPreference(
        preference: PreferenceMutableState<Color>,
        title: String,
        subtitle: String? = null,
        unsetColor: Color = Color.Unspecified,
        onChangeColor: () -> Unit = {},
        onRestToDefault: () -> Unit = {},
        showColorDialog: MutableState<Boolean>? = null,
        onShow: (ColorPickerInfo) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    val initialColor = preference.value.takeOrElse { unsetColor }
    val hexColor = "#${initialColor.value.toString(16).substring(2, 8).uppercase()}"
    
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        }
        if (initialColor != Color.Unspecified) {
            append(". Current color: ")
            append(hexColor)
        }
        append(". Long press to reset to default")
    }
    
    // Enhanced layout with card-like appearance
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.Button
            },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        PreferenceRow(
                title = title,
                subtitle = subtitle,
                onClick = {
                    if (showColorDialog == null) {
                        showDialog = true
                    } else {
                        showColorDialog.value = true
                        onShow(ColorPickerInfo(preference, title, onChangeColor, initialColor))
                    }
                },
                onLongClick = {
                    preference.value = Color.Unspecified
                    onRestToDefault()
                },
                action = {
                    if (preference.value != Color.Unspecified || unsetColor != Color.Unspecified) {
                        // Enhanced color preview with better visual design
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            // Color preview circle with enhanced styling
                            val borderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            Box(
                                    modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(color = initialColor)
                                            .border(BorderStroke(2.dp, borderColor), CircleShape)
                            )
                            
                            // Color hex value display for real-time preview
                            Text(
                                text = "#${initialColor.value.toString(16).substring(2, 8).uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
        )
    }
    
    if (showDialog) {
        ColorPickerDialog(
                title = { Text(title) },
                onDismissRequest = { showDialog = false },
                onSelected = {
                    preference.value = it
                    showDialog = false
                    onChangeColor()
                },
                initialColor = initialColor
        )
    }
}
