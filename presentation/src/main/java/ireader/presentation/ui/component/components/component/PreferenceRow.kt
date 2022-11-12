package ireader.presentation.ui.component.components.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.reusable_composable.CaptionTextComposable
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.ui.PreferenceMutableState
import ireader.presentation.ui.core.utils.horizontalPadding
import ireader.presentation.ui.settings.appearance.ColorPickerInfo

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
        val height = if (subtitle != null) 72.dp else 56.dp

        val titleTextStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        val subtitleTextStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        Row(
                modifier = modifier
                        .fillMaxWidth()
                        .heightIn(min = height)
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
                )
                if (subtitle != null) {
                    Text(
                            text = subtitle,
                            overflow = Ellipsis,
                            maxLines = 1,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
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
    PreferenceRow(
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
        onValueChange: ((Float) -> Unit) = {},
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        onValueChangeFinished: ((Float) -> Unit)? = null,
        steps: Int = 0,
        isEnable: Boolean = true
) {

    val height = if (subtitle != null) 72.dp else 56.dp

    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(height),
            verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                    modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(24.dp),
                    contentDescription = null,
                    imageVector = icon,
                    tint = MaterialTheme.colorScheme.primary,
            )
        }
        Column(
                Modifier
                        .padding(horizontal = 16.dp)
                        .weight(3f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
        ) {
            Text(
                    modifier = Modifier,
                    text = title,
                    overflow = Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Start,
                    softWrap = true,
            )
            if (subtitle != null) {
                Text(
                        modifier = Modifier,
                        text = subtitle,
                        overflow = Ellipsis,
                        maxLines = 1,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        softWrap = true,
                )
            }
        }
        Slider(
                modifier = Modifier
                        .widthIn(max = 200.dp),
                value = preferenceAsFloat?.lazyValue ?: preferenceAsInt?.lazyValue?.toFloat()
                ?: preferenceAsLong?.lazyValue?.toFloat() ?: mutablePreferences?.value ?: 0F,
                onValueChange = {
                    preferenceAsFloat?.lazyValue = it
                    preferenceAsInt?.lazyValue = it.toInt()
                    preferenceAsLong?.lazyValue = it.toLong()
                    mutablePreferences?.value = it
                    onValueChange(it)
                },
                valueRange = valueRange,
                onValueChangeFinished = {
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
                enabled = isEnable
        )
        if (trailing != null) {
            Text(
                    modifier = Modifier
                            .weight(.7f)
                            .padding(horizontal = 0.dp),
                    text = trailing,
                    overflow = Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface),
                    textAlign = TextAlign.Start,
                    softWrap = true,
            )
        }
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
    PreferenceRow(
            title = title,
            subtitle = subtitle,
            icon = icon,
            action = {
                LazyRow {
                    items(count = preference.size) { index ->
                        FilterChip(
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
    PreferenceRow(
            title = title,
            subtitle = subtitle,
            icon = icon,
            action = {
                Button(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 2.dp, top = 4.dp, bottom = 4.dp),
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
                                    text = choices[preference.value] ?: onFailToFindElement,
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
        AlertDialog(
                modifier = Modifier.heightIn(max = 350.dp, min = 200.dp),
                onDismissRequest = { showDialog = false },
                title = { Text(title) },
                text = {
                    LazyColumn {
                        items(choices.toList()) { (value, text) ->
                            Row(
                                    modifier = Modifier
                                            .requiredHeight(48.dp)
                                            .fillMaxWidth()
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
                                        selected = value == preference.value,
                                        onClick = null,
                                )
                                Text(text = text, modifier = Modifier.padding(start = 24.dp))
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
    PreferenceRow(
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
    PreferenceRow(
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
    PreferenceRow(
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

    PreferenceRow(
            title = title,
            subtitle = if (subtitle == null) choices[preference.value] else null,
            onClick = {
                onShowDialog()
                showDialog = true
            },
            enable = enable

    )

    if (showDialog) {
        AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    onDismiss()
                                   },
                title = { Text(title) },
                text = {
                    LazyColumn {
                        items(choices.toList()) { (value, text) ->
                            Row(
                                    modifier = Modifier
                                            .requiredHeight(48.dp)
                                            .fillMaxWidth()
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
                                        selected = value == preference.value,
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
    PreferenceRow(
            title = title,
            subtitle = if (subtitle == null) choices[preference] else null,
            onClick = {
                showDialog = true
                onShowDialog()
            },
            enable = enable

    )

    if (showDialog) {
        AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    onDismiss()
                                   },
                title = { Text(title) },
                text = {
                    LazyColumn {
                        items(choices.toList()) { (value, text) ->
                            Row(
                                    modifier = Modifier
                                            .requiredHeight(48.dp)
                                            .fillMaxWidth()
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
                                        selected = value == preference,
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

    PreferenceRow(
            title = title,
            subtitle = if (subtitle == null) choices[preference.value] else null,
            onClick = {
                showDialog = true
                onShowDialog()
            },
            enable = enable

    )

    if (showDialog) {
        AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(title) },
                text = {
                    LazyColumn {
                        items(choices.toList()) { (value, text) ->
                            Row(
                                    modifier = Modifier
                                            .requiredHeight(48.dp)
                                            .fillMaxWidth()
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
                                        selected = value == preference.value,
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
                    val borderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.54f)
                    Box(
                            modifier = Modifier
                                    .padding(4.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color = initialColor)
                                    .border(BorderStroke(1.dp, borderColor), CircleShape)
                    )
                }
            }
    )
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
